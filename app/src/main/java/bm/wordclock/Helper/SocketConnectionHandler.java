package bm.wordclock.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;


import bm.wordclock.model.Plugin;
import bm.wordclock.android.SettingsActivity;

/**
 * Created by phenze on 08.02.17.
 */

public class SocketConnectionHandler extends WCProtocol {


    public interface SocketConnectionListener {
        void onStateChanged(ConnectionState state);
        void onPluginListChanged();
        void onActivePluginChanged();
    }


    public enum ConnectionState {
        CONNECTED,
        DISCONNECTED,
        COULD_NOT_CONNECT,
        HOST_UNKNOWN
    }

    private ConnectionState mConnectionState;

    private Thread mInThread;
    /** Output thread (most of the time: waiting for some packet to send) */
    private Thread mOutThread;
    /** Flag whether threads are supposed to run */
    private boolean mRunning;

    private List<SocketConnectionListener> mListener;
    private Handler mUIHandler;
    private WCProtocolProxy mSendingProxy;
    private Context mContext;




    private static SocketConnectionHandler sInstance;
    public static void createInstance(Context context)
    {
        sInstance = new SocketConnectionHandler(context);
    }

    public static SocketConnectionHandler getSocketConnectionHandler()
    {
        return sInstance;
    }

    private SocketConnectionHandler(Context context)
    {
        super();


        mContext = context;
        mConnectionState = ConnectionState.DISCONNECTED;
        mListener = new ArrayList<>();


        mUIHandler = new Handler(Looper.getMainLooper());
    }


    public void addSocketConnectionListener(SocketConnectionListener listener) {
        if(!mListener.contains(listener)) {
            mListener.add(listener);
        }
    }

    public void removeSocketConnectionListener(SocketConnectionListener listener) {
        mListener.remove(listener);
    }

    public void setConnectionState(final ConnectionState connectionState) {
        this.mConnectionState = connectionState;
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                for(SocketConnectionListener listener : mListener) {
                    listener.onStateChanged(connectionState);
                }
            }
        });

    }

    @Override
    protected void readAndProcess() throws IOException, WCCommunication.ProtocolException {
        List<Plugin> oldList = mPlugins;
        int oldActive = mActivePlugin;
        super.readAndProcess();

        if(oldList.size() != mPlugins.size()) {
            mUIHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    for(SocketConnectionHandler.SocketConnectionListener listener : mListener) {
                        listener.onPluginListChanged();
                    }
                }
            });
        }

        if(oldActive != mActivePlugin) {
            mUIHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    for(SocketConnectionHandler.SocketConnectionListener listener : mListener) {
                        listener.onActivePluginChanged();
                    }
                }
            });
        }
    }

    public void start() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String hostName = sharedPref.getString(SettingsActivity.GeneralPreferenceFragment.KEY_HOST_NAME, "");
        mHostName = hostName;
        mRunning = true;
        mInThread = new Thread(new ReceiveLoop());
        mSendingProxy = new WCProtocolProxy();
        mOutThread = new Thread(mSendingProxy);
        mInThread.start();
        mOutThread.start();
    }

    public void stop() {
        mRunning = false;
        mInThread.interrupt();
        mOutThread.interrupt();
        mSendingProxy = null;
        disconnect();
    }

    @Override
    public synchronized void disconnect() {
        super.disconnect();
        setConnectionState(ConnectionState.DISCONNECTED);
    }



    public void getConfiguration() throws IOException {
        if(!isConnected())
            return;
        mSendingProxy.post(makeSimpleRawPkg("GET_CONFIG",0));

    }



    public void sendEvent(int event) {
        if(!isConnected())
            return;
        mSendingProxy.post(makeSimpleRawPkg("SEND_EVENT", event));
    }

    public void setActivePlugin(int index)  {
        if(!isConnected())
            return;
        mSendingProxy.post(makeSimpleRawPkg("SET_ACTIVE_PLUGIN", index));
    }


    private class WCProtocolProxy implements Runnable {
        /** Output Queue of messages */
        private final LinkedBlockingDeque<byte[]> outQueue = new LinkedBlockingDeque<>();

        private WCProtocolProxy() {
            //super(hostName, callbacks);

        }

        public void post(byte [] pkt) {
            outQueue.addLast(pkt);
        }

        @Override
        public void run() {
            while (mRunning) {
                byte [] l;
                try {
                    /* get a message from the queue */
                    l = outQueue.takeFirst();
                } catch (InterruptedException e) {
                    continue;
                }
                try {
                    writeRaw(l);
                } catch (Exception ignored) {
                    /* input thread will handle that */
                    outQueue.clear();
                }
            }
        }



    }

    private class ReceiveLoop implements Runnable {

        private void tryConnect() {
            while (mRunning) {
                try{
                    connect();
                    setConnectionState(ConnectionState.CONNECTED);
                }catch (UnknownHostException e) {
                    setConnectionState(ConnectionState.HOST_UNKNOWN);
                    e.printStackTrace();
                }
                catch (IOException e) {
                    setConnectionState(ConnectionState.COULD_NOT_CONNECT);
                    e.printStackTrace();
                }
                if(isConnected()) {
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }

        @Override
        public void run() {
            while (mRunning) {
                /* Try to connect */
                tryConnect();

                if (!mRunning)
                    break;

                try {
                    /* Get configuration */
                    getConfiguration();
                    /* read and process packets */
                    while (mRunning)
                        readAndProcess();
                } catch (ProtocolException | IOException e) {
                    disconnect();
                    /* TODO: give more information to the user, especially on ProtocolException */
                    setConnectionState(ConnectionState.DISCONNECTED);
                    //setStateOnActivity(WCCommCallbacks.STATE.DISCONNECTED);
                }
            }
            disconnect();

        }
    }


}
