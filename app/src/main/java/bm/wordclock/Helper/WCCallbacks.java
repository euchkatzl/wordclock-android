package bm.wordclock.Helper;

import java.util.Collection;

import bm.wordclock.model.Plugin;

/**
 * Created by mrks on 04.02.17.
 */

public interface WCCallbacks {
    void onPluginsChanged(Collection<Plugin> plugins);
    void onActivePluginChanged(int index);

}
