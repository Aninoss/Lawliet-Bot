package events.sync;

import java.util.function.Function;
import org.json.JSONObject;

public interface SyncServerFunction extends Function<JSONObject, JSONObject> {

}