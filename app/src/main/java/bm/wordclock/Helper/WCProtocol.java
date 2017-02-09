package bm.wordclock.Helper;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import bm.wordclock.model.Plugin;

/**
 * Created by mrks on 04.02.17.
 */

public class WCProtocol extends WCCommunication {


    /* must match wordclock */
    public static final int EVENT_LEFT = 0;
    public static final int EVENT_RIGHT = 1;
    public static final int EVENT_RETURN = 2;

    protected List<Plugin> mPlugins;
    protected int mActivePlugin;

    public WCProtocol() {
        super();
        mPlugins = new ArrayList<>();
        mActivePlugin = -1;
    }

    public List<Plugin> getPlugins() {
        return mPlugins;
    }

    public int getActivePlugin() {
        return mActivePlugin;
    }


    protected void readAndProcess() throws IOException, ProtocolException {
        JSONObject json = readObject();

        JSONArray plugins = json.optJSONArray("PLUGINS");
        if (plugins != null) {
            List<Plugin> list = new LinkedList<>();
            try {
                for (int i = 0; i < plugins.length(); ++i) {
                    String name = plugins.getString(i);
                    list.add(new Plugin(name));
                }
            } catch (JSONException e) {
                throw new ProtocolException("Malformed plugin list");
            }
            if(list.size() != mPlugins.size()) {
                mPlugins.clear();
                mPlugins.addAll(list);


            }
        }

        int activePlugin = json.optInt("ACTIVE_PLUGIN", -1);
        if (activePlugin != -1 && mActivePlugin != activePlugin) {
            mActivePlugin = activePlugin;



        }
    }

}
