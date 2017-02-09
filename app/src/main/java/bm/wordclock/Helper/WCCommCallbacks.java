package bm.wordclock.Helper;

/**
 * Created by mrks on 05.02.17.
 */

public interface WCCommCallbacks extends WCCallbacks {
    public enum STATE {
        CONNECTED,
        DISCONNECTED,
        COULD_NOT_CONNECT,
        HOST_UNKNOWN
    }

    void onStateChanged(STATE state);
}
