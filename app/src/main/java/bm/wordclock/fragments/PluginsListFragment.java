package bm.wordclock.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import bm.wordclock.Helper.SocketConnectionHandler;
import bm.wordclock.Helper.WCCallbacks;
import bm.wordclock.Helper.WCCommCallbacks;
import bm.wordclock.android.R;
import bm.wordclock.model.Plugin;

public class PluginsListFragment extends BaseFragment implements WCCommCallbacks {

    //private OnFragmentInteractionListener mListener;
    private PluginListAdapter mPluginsAdapter;

    //private int mCurrentPlugin;
    //private boolean mIsAttached;

    private ListView mListView;
    public PluginsListFragment() {
        // Required empty public constructor




    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_plugins, container, false);
        mListView = (ListView) v.findViewById(R.id.plugin_list_view);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        mListView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);

        mPluginsAdapter = new PluginListAdapter(getMainActivity(), SocketConnectionHandler.getSocketConnectionHandler().getPlugins());
        mListView.setAdapter(mPluginsAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPluginsAdapter.setSelectedIndex(position);
                SocketConnectionHandler.getSocketConnectionHandler().setActivePlugin(position);
            }
        });

        mPluginsAdapter.setSelectedIndex(SocketConnectionHandler.getSocketConnectionHandler().getActivePlugin());
        return v;
    }

    @Override
    public int getDrawerId() {
        return R.id.nav_plugins;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SocketConnectionHandler.getSocketConnectionHandler().addSocketConnectionListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SocketConnectionHandler.getSocketConnectionHandler().removeSocketConnectionListener(this);
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void onPluginsChanged(Collection<Plugin> plugins) {
        //mPlugins.clear();
        //mPlugins.addAll(plugins);
        if(mPluginsAdapter != null)
            mPluginsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivePluginChanged(int index) {
        if(mPluginsAdapter != null)
            mPluginsAdapter.setSelectedIndex(index);
    }

    @Override
    public void onStateChanged(STATE state) {

    }
}
