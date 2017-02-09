package bm.wordclock.Helper;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import bm.wordclock.model.Plugin;

/**
 * Created by mrks on 04.02.17.
 */

public class WCProtocol extends WCCommunication {

    protected WCCallbacks mCallbacks;

    /* must match wordclock */
    public static final int EVENT_LEFT = 0;
    public static final int EVENT_RIGHT = 1;
    public static final int EVENT_RETURN = 2;

    protected static final byte [] rawPacketLeft;
    protected static final byte [] rawPacketRight;
    protected static final byte [] rawPacketReturn;

    protected static byte [] makeSimpleRawPkg(String cmd, int param) {
        try {
            JSONObject obj = createMessage();
            obj.put(cmd, param);
            return getRawBuffer(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static {
        rawPacketLeft = makeSimpleRawPkg("SEND_EVENT", EVENT_LEFT);
        rawPacketRight = makeSimpleRawPkg("SEND_EVENT", EVENT_RIGHT);
        rawPacketReturn = makeSimpleRawPkg("SEND_EVENT", EVENT_RETURN);
    }

    public WCProtocol(@NonNull String hostName) {
        super(hostName);
        mCallbacks = null;
    }



    public void getConfiguration() throws IOException {
        try {
            JSONObject reqConfig = createMessage();
            reqConfig.put("GET_CONFIG", 0);
            writeObject(reqConfig);
        } catch (JSONException e) {
            /* should never happen */
        }
    }

    public void setActivePlugin(int index) throws IOException {
        try {
            JSONObject msg = createMessage();
            msg.put("SET_ACTIVE_PLUGIN", index);
            writeObject(msg);
        } catch (JSONException e) {
            /* should never happen */
        }
    }

    public void btn_left() throws IOException {
        writeRaw(rawPacketLeft);
    }

    public void btn_right() throws IOException {
        writeRaw(rawPacketRight);
    }

    public void btn_return() throws IOException {
        writeRaw(rawPacketReturn);
    }

    public void readAndProcess() throws IOException, ProtocolException {
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
            mCallbacks.onPluginsChanged(list);
        }

        int activePlugin = json.optInt("ACTIVE_PLUGIN", -1);
        if (activePlugin != -1) {
            mCallbacks.onActivePluginChanged(activePlugin);
        }
    }

}
