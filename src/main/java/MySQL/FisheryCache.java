package MySQL;

import Constants.FishingCategoryInterface;
import Constants.FisheryStatus;
import General.DiscordApiCollection;
import General.Fishing.FishingProfile;
import MySQL.Server.DBServer;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FisheryCache {

    private static HashMap<Integer, FisheryCache> ourInstances = new HashMap<>();
    private final int MINUTES_INTERVAL = 60;

    private Map<Long, Map<Long, ActivityUserData>> activities = new HashMap<>(); //serverId, userId, activity
    private int messagePhase;

    private Instant nextMessageCheck = Instant.now(), nextVCCheck = Instant.now();

    private boolean active = true;
    private int shardId;

    public static FisheryCache getInstance(int shardId) {
        return ourInstances.computeIfAbsent(shardId, k -> new FisheryCache(shardId));
    }

    private FisheryCache(int shardId) {
        this.shardId = shardId;
        this.messagePhase = shardId * MINUTES_INTERVAL * 3 / DiscordApiCollection.getInstance().size();
        Thread t = new Thread(this::messageCollector);
        t.setName("message_collector");
        t.start();
    }

    public boolean addActivity(User user, ServerTextChannel channel) {
        try {
            Server server = channel.getServer();
            FisheryStatus fisheryStatus = DBServer.getInstance().getBean(server.getId()).getFisheryStatus();
            ArrayList<Long> powerPlantIgnoredChannelIds = DBServerOld.getPowerPlantIgnoredChannelIdsFromServer(server);

            boolean whiteListed = DBServerOld.isChannelWhitelisted(channel);
            if (fisheryStatus == FisheryStatus.ACTIVE && !powerPlantIgnoredChannelIds.contains(channel.getId())) {
                ActivityUserData activityUserData = getActivities(server, user);
                boolean registered = activityUserData.registerMessage(messagePhase, whiteListed ? channel : null);
                setActivities(server, user, activityUserData);
                return registered;
            }
        } catch (SQLException | ExecutionException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void messageCollector() {
        while(active) {
            try {
                Duration duration = Duration.between(Instant.now(), nextMessageCheck);
                Thread.sleep(Math.max(1, duration.getSeconds() * 1000 + duration.getNano() / 1000000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nextMessageCheck = Instant.now().plusSeconds(20);

            messagePhase++;
            if (messagePhase >= 3 * MINUTES_INTERVAL) {
                messagePhase = 0;
                saveData();
            }
        }
    }

    public void saveData() {
        Map<Long, Map<Long, ActivityUserData>> finalActivites = activities;
        activities = new HashMap<>();

        synchronized (this) {
            if (finalActivites.size() > 0) {
                Thread t = new Thread(() -> {
                    System.out.println("Collector START");

                    for(long serverId: finalActivites.keySet()) {
                        for(long userId: finalActivites.get(serverId).keySet()) {
                            try {
                                synchronized (FisheryCache.class) {
                                    ActivityUserData activityUserData = finalActivites.get(serverId).get(userId);
                                    if (activityUserData.getAmountVC() + activityUserData.getAmountMessage() > 0) {
                                        DBUser.addMessageSingle(serverId, userId, activityUserData);
                                        Thread.sleep(1000);
                                    }
                                }
                            } catch (SQLException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    System.out.println("Collector END");
                });

                t.setName("message_collector_db");
                t.start();
            }
        }
    }

    public long flush(Server server, User user, boolean clear) throws SQLException {
        return flush(server, user, clear, null);
    }

    public long flush(Server server, User user, boolean clear, FishingProfile fishingProfile) throws SQLException {
        if (activities.containsKey(server.getId()) && activities.get(server.getId()).containsKey(user.getId())) {
            ActivityUserData activityUserData = activities.get(server.getId()).get(user.getId());
            if (activityUserData.getAmountVC() + activityUserData.getAmountMessage() > 0) {
                if (fishingProfile == null) fishingProfile = DBUser.getFishingProfile(server, user, false);

                long fishMessage = activityUserData.getAmountMessage() * fishingProfile.getEffect(FishingCategoryInterface.PER_MESSAGE);
                long fishVC = activityUserData.getAmountVC() * fishingProfile.getEffect(FishingCategoryInterface.PER_VC);

                if (clear) activityUserData.reset();
                return fishMessage + fishVC;
            }
        }

        return 0L;
    }

    private void VCCollector(DiscordApi api) {
        while(active) {
            try {
                Duration duration = Duration.between(Instant.now(), nextVCCheck);
                Thread.sleep(Math.max(1, duration.getSeconds() * 1000 + duration.getNano() / 1000000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            nextVCCheck = Instant.now().plusSeconds(5 * 60);

            for (Server server : api.getServers()) {
                try {
                    if (DBServer.getInstance().getBean(server.getId()).getFisheryStatus() == FisheryStatus.ACTIVE) {
                        for (ServerVoiceChannel channel : server.getVoiceChannels()) {
                            ArrayList<User> validUsers = new ArrayList<>();
                            for (User user : channel.getConnectedUsers()) {
                                if (!user.isBot() &&
                                        !user.isMuted(server) &&
                                        !user.isDeafened(server) &&
                                        !user.isSelfDeafened(server) &&
                                        !user.isSelfMuted(server)
                                ) {
                                    validUsers.add(user);
                                }
                            }

                            if (validUsers.size() > 1 &&
                                    (!server.getAfkChannel().isPresent() || channel.getId() != server.getAfkChannel().get().getId())
                            ) {
                                for (User user : validUsers) {
                                    ActivityUserData activityUserData = getActivities(server, user);
                                    activityUserData.registerVC(5);
                                    setActivities(server, user, activityUserData);
                                }
                            }
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void stopServer(Server server) {
        activities.remove(server.getId());
    }

    public void startVCCollector(DiscordApi api) {
        Thread t = new Thread(() -> VCCollector(api));
        t.setPriority(1);
        t.setName("vc_collector_" + api.getCurrentShard());
        t.start();
    }

    private ActivityUserData getActivities(Server server, User user) {
        return getActivities(server.getId(), user.getId());
    }

    public ActivityUserData getActivities(long serverId, long userId) {
        Map<Long, ActivityUserData> serverMap = activities.computeIfAbsent(serverId, k -> new HashMap<>());
        return serverMap.computeIfAbsent(userId, k -> new ActivityUserData());
    }

    private void setActivities(Server server, User user, ActivityUserData activityUserData) {
        Map<Long, ActivityUserData> serverMap = activities.computeIfAbsent(server.getId(), k -> new HashMap<>());
        serverMap.putIfAbsent(user.getId(), activityUserData);
    }

    public void turnOff() {
        ourInstances.remove(shardId);
        active = false;
    }

}
