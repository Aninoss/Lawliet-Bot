package events.sync.events;

import constants.Language;
import core.Program;
import core.ShardManager;
import core.cache.PatreonCache;
import dashboard.DashboardCategory;
import dashboard.DashboardManager;
import events.sync.SyncServerEvent;
import events.sync.SyncServerFunction;
import mysql.hibernate.HibernateManager;
import mysql.hibernate.entity.guild.GuildEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

@SyncServerEvent(event = "DASH_INIT")
public class OnDashboardInit implements SyncServerFunction {

    @Override
    public JSONObject apply(JSONObject jsonObject) {
        JSONObject resultJson = new JSONObject();
        long guildId = jsonObject.getLong("guild_id");
        boolean ok = ShardManager.getLocalGuildById(guildId).isPresent();
        resultJson.put("ok", ok);
        if (ok) {
            resultJson.put("premium", Program.productionMode() && PatreonCache.getInstance().isUnlocked(guildId));
            long userId = jsonObject.getLong("user_id");
            String localeString = jsonObject.getString("locale");
            Locale locale = Language.from(localeString).getLocale();
            addTitles(guildId, userId, locale, resultJson);
        }
        return resultJson;
    }

    private void addTitles(long guildId, long userId, Locale locale, JSONObject resultJson) {
        JSONArray titlesJson = new JSONArray();
        try (GuildEntity guildEntity = HibernateManager.findGuildEntity(guildId, OnDashboardInit.class)) {
            for (DashboardCategory retrieveCategory : DashboardManager.retrieveCategories(guildId, userId, locale, guildEntity)) {
                if (retrieveCategory.anyCommandRequirementsAreAccessible()) {
                    JSONObject data = new JSONObject();
                    data.put("id", retrieveCategory.getProperties().id());
                    data.put("title", retrieveCategory.retrievePageTitle());
                    titlesJson.put(data);
                }
            }
        }
        resultJson.put("titles", titlesJson);
    }

}
