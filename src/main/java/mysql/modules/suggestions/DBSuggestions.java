package mysql.modules.suggestions;

import java.sql.Types;
import java.util.HashMap;
import java.util.Optional;
import modules.suggestions.SuggestionMessage;
import mysql.DBDataLoad;
import mysql.MySQLManager;
import mysql.DBObserverMapCache;

public class DBSuggestions extends DBObserverMapCache<Long, SuggestionsData> {

    private static final DBSuggestions ourInstance = new DBSuggestions();

    public static DBSuggestions getInstance() {
        return ourInstance;
    }

    private DBSuggestions() {
    }

    @Override
    protected SuggestionsData load(Long serverId) throws Exception {
        SuggestionsData suggestionsBean = MySQLManager.get(
                "SELECT active, channelId FROM SuggestionConfig WHERE serverId = ?;",
                preparedStatement -> preparedStatement.setLong(1, serverId),
                resultSet -> {
                    if (resultSet.next()) {
                        return new SuggestionsData(
                                serverId,
                                resultSet.getBoolean(1),
                                resultSet.getLong(2),
                                getSuggestionMessages(serverId)
                        );
                    } else {
                        return new SuggestionsData(
                                serverId,
                                false,
                                null,
                                new HashMap<>()
                        );
                    }
                }
        );

        suggestionsBean.getSuggestionMessages()
                .addMapAddListener(this::addSuggestionMessage)
                .addMapRemoveListener(this::addSuggestionMessage);

        return suggestionsBean;
    }

    @Override
    protected void save(SuggestionsData suggestionsBean) {
        MySQLManager.asyncUpdate("REPLACE INTO SuggestionConfig (serverId, active, channelId) VALUES (?, ?, ?);", preparedStatement -> {
            preparedStatement.setLong(1, suggestionsBean.getGuildId());
            preparedStatement.setBoolean(2, suggestionsBean.isActive());

            Optional<Long> channelIdOpt = suggestionsBean.getTextChannelId();
            if (channelIdOpt.isPresent()) {
                preparedStatement.setLong(3, channelIdOpt.get());
            } else {
                preparedStatement.setNull(3, Types.BIGINT);
            }
        });
    }

    private HashMap<Long, SuggestionMessage> getSuggestionMessages(long serverId) {
        return new DBDataLoad<SuggestionMessage>("SuggestionMessages", "messageId, content, author", "serverId = ?",
                preparedStatement -> preparedStatement.setLong(1, serverId)
        ).getHashMap(
                SuggestionMessage::getMessageId,
                resultSet -> new SuggestionMessage(
                        serverId,
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3)
                )
        );
    }

    private void addSuggestionMessage(SuggestionMessage suggestionMessage) {
        MySQLManager.asyncUpdate("INSERT IGNORE INTO SuggestionMessages (serverId, messageId, content, author) VALUES (?, ?, ?, ?);", preparedStatement -> {
            preparedStatement.setLong(1, suggestionMessage.getGuildId());
            preparedStatement.setLong(2, suggestionMessage.getMessageId());
            preparedStatement.setString(3, suggestionMessage.getContent());
            preparedStatement.setString(4, suggestionMessage.getAuthor());
        });
    }

    private void removeSuggestionMessage(SuggestionMessage suggestionMessage) {
        MySQLManager.asyncUpdate("DELETE FROM SuggestionMessages WHERE serverId = ? AND messageId = ?;", preparedStatement -> {
            preparedStatement.setLong(1, suggestionMessage.getGuildId());
            preparedStatement.setLong(2, suggestionMessage.getMessageId());
        });
    }

}
