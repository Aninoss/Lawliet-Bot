package commands.runnables.externalcategory;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import commands.Command;
import commands.CommandEvent;
import commands.listeners.CommandProperties;
import core.*;
import core.internet.HttpRequest;
import core.internet.HttpResponse;
import core.utils.EmojiUtil;
import core.utils.JDAUtil;
import core.utils.StringUtil;
import net.dv8tion.jda.api.entities.Member;
import org.json.JSONObject;

@CommandProperties(
        trigger = "ytmp3",
        emoji = "\uD83D\uDCE5",
        executableWithoutArgs = false,
        patreonRequired = true,
        maxCalculationTimeSec = 60,
        aliases = { "youtube", "yt", "youtubemp3" }
)
public class YouTubeMP3Command extends Command {

    private final static int MINUTES_CAP = 30;
    private static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    static {
        playerManager.registerSourceManager(new YoutubeAudioSourceManager(false));
    }

    public YouTubeMP3Command(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onTrigger(CommandEvent event, String args) throws ExecutionException, InterruptedException {
        args = args.replace("<", "").replace(">", "");

        if (args.isEmpty()) {
            drawMessageNew(EmbedFactory.getEmbedError(this, TextManager.getString(getLocale(), TextManager.GENERAL, "no_args")))
                    .exceptionally(ExceptionLogger.get());
            return false;
        }

        if (args.contains("&")) {
            args = args.split("&")[0];
        }

        AudioTrack audioTrack = AudioManager.fetchAudioTrack(args, playerManager).get();
        if (audioTrack == null || audioTrack.getInfo().isStream) {
            drawMessageNew(EmbedFactory.getEmbedError(this, TextManager.getNoResultsString(getLocale(), args)))
                    .exceptionally(ExceptionLogger.get());
            return false;
        }

        AudioTrackInfo meta = audioTrack.getInfo();
        if (meta.length >= MINUTES_CAP * 60_000) {
            drawMessageNew(EmbedFactory.getEmbedError(this, getString("toolong", String.valueOf(MINUTES_CAP))))
                    .exceptionally(ExceptionLogger.get());
            return false;
        }

        drawMessage(
                EmbedFactory.getEmbedDefault(
                        this,
                        getString(
                                "loading",
                                StringUtil.escapeMarkdownInField(meta.title),
                                EmojiUtil.getLoadingEmojiMention(event.getChannel())
                        )
                )
        ).exceptionally(ExceptionLogger.get());

        if (sendApiRequest("https://www.youtube.com/watch?v=" + meta.identifier)) {
            Pattern filePattern = Pattern.compile(String.format(".*\\[%s\\]\\.[A-Za-z0-9]*$", Pattern.quote(meta.identifier)));
            for (int i = 0; i < 1000; i++) {
                Thread.sleep(100);
                List<File> validFiles = getValidFiles(new LocalFile("data/youtube-dl"), filePattern);

                if (validFiles.size() == 1 && validFiles.get(0).getAbsolutePath().endsWith(".mp3")) {
                    handleFile(event.getMember(), meta, validFiles.get(0));
                    return true;
                }
            }
        }

        drawMessage(EmbedFactory.getEmbedError(
                this,
                getString("error"),
                TextManager.getString(getLocale(), TextManager.GENERAL, "error")
        )).exceptionally(ExceptionLogger.get());
        return false;
    }

    private List<File> getValidFiles(LocalFile root, Pattern filePattern) {
        return Arrays.stream(Objects.requireNonNull(root.listFiles()))
                .filter(file -> filePattern.matcher(file.getAbsolutePath()).matches())
                .collect(Collectors.toUnmodifiableList());
    }

    private boolean sendApiRequest(String url) throws ExecutionException, InterruptedException {
        String body = String.format("url=%s&format=mp3", URLEncoder.encode(url, StandardCharsets.UTF_8));
        HttpResponse response = HttpRequest.post("http://youtube-dl:8080/youtube-dl/q", "application/x-www-form-urlencoded", body).get();
        return Optional.ofNullable(response.getBody())
                .map(data -> new JSONObject(data).getBoolean("success"))
                .orElse(false);
    }

    private void handleFile(Member member, AudioTrackInfo meta, File mp3File) {
        JDAUtil.sendPrivateMessage(member.getIdLong(), privateChannel -> {
            return privateChannel.sendMessage(getString("success_dm", StringUtil.escapeMarkdownInField(meta.title), StringUtil.escapeMarkdownInField(meta.author)))
                    .addFile(mp3File);
        }).queue(m -> {
            mp3File.delete();
            drawMessage(EmbedFactory.getEmbedDefault(this, getString("success")))
                    .exceptionally(ExceptionLogger.get());
        }, e -> {
            MainLogger.get().error("Ytmp3 Error", e);
            mp3File.delete();
            drawMessage(EmbedFactory.getEmbedError(this, TextManager.getString(getLocale(), TextManager.GENERAL, "no_dms"), TextManager.getString(getLocale(), TextManager.GENERAL, "error")))
                    .exceptionally(ExceptionLogger.get());
        });
    }

}