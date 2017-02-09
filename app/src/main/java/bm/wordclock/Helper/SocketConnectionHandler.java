package bm.wordclock.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;


import bm.wordclock.model.Plugin;
import bm.wordclock.android.SettingsActivity;

/**
 * Created by phenze on 08.02.17.
 */

public class SocketConnectionHandler extends WCProtocol implements WCCallbacks {


    private Thread mInThread;
    /** Output thread (most of the time: waiting for some packet to send) */
    private Thread mOutThread;
    /** Flag whether threads are supposed to run */
    private boolean mRunning;

    private Handler mUIHandler;
    private WCProtocolProxy mSendingProxy;
    private Context mContext;
    private List<WCCommCallbacks> mListener;
    private List<Plugin> mPlugins;
    private int mActivePlugin;




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
        super("");
        mCallbacks = this;
        mContext = context;
        mUIHandler = new Handler(Looper.getMainLooper());
        mListener = new ArrayList<>();
        mPlugins = new ArrayList<>();
        mActivePlugin = -1;
    }


    public void addSocketConnectionListener(WCCommCallbacks listener) {
        if(!mListener.contains(listener)) {
            mListener.add(listener);
        }
    }

    public void removeSocketConnectionListener(WCCommCallbacks listener) {
        mListener.remove(listener);
    }


    public List<Plugin> getPlugins() {
        return mPlugins;
    }

    public int getActivePlugin() {
        return mActivePlugin;
    }

    public void setConnectionState(final WCCommCallbacks.STATE connectionState) {
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                for(WCCommCallbacks listener : mListener) {
                    listener.onStateChanged(connectionState);
                }
            }
        });

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
        setConnectionState(WCCommCallbacks.STATE.DISCONNECTED);
    }




    public void sendEvent(int event) {
        if(!isConnected())
            return;
        mSendingProxy.post(makeSimpleRawPkg("SEND_EVENT", event));
    }

    public void sendButtonEvent(int event) {
        if(event == WCProtocol.EVENT_LEFT) {
            mSendingProxy.post(rawPacketLeft);
        }else if(event == WCProtocol.EVENT_RIGHT) {
            mSendingProxy.post(rawPacketRight);
        }else if(event == WCProtocol.EVENT_RETURN) {
            mSendingProxy.post(rawPacketReturn);
        }
    }



    public void setActivePlugin(int index)  {
        if(!isConnected())
            return;
        mSendingProxy.post(makeSimpleRawPkg("SET_ACTIVE_PLUGIN", index));
    }



    @Override
    public void onPluginsChanged(final Collection<Plugin> plugins) {
        mPlugins.clear();
        mPlugins.addAll(plugins);
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                for(WCCommCallbacks listener : mListener) {
                    listener.onPluginsChanged(plugins);
                }
            }
        });
    }

    @Override
    public void onActivePluginChanged(final int index) {
        mActivePlugin = index;
        mUIHandler.post(new Runnable() {
            @Override
            public void run() {
                for(WCCommCallbacks listener : mListener) {
                    listener.onActivePluginChanged(index);
                }
            }
        });
    }


    private class WCProtocolProxy implements Runnable {
        /** Output Queue of messages */
        private final LinkedBlockingDeque<byte[]> outQueue = new LinkedBlockingDeque<>();

        private WCProtocolProxy() {
            super();
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
                    Log.d("send","sending start");
                    writeRaw(l);
                    Log.d("send","sending end");
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
                    setConnectionState(WCCommCallbacks.STATE.CONNECTED);
                }catch (UnknownHostException e) {
                    setConnectionState(WCCommCallbacks.STATE.HOST_UNKNOWN);
                    e.printStackTrace();
                }
                catch (IOException e) {
                    setConnectionState(WCCommCallbacks.STATE.COULD_NOT_CONNECT);
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
                    setConnectionState(WCCommCallbacks.STATE.DISCONNECTED);
                    //setStateOnActivity(WCCommCallbacks.STATE.DISCONNECTED);
                }
            }
            disconnect();

        }
    }


}
