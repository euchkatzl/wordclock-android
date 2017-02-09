package bm.wordclock.android;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Collection;

import bm.wordclock.Helper.SocketConnectionHandler;

import bm.wordclock.Helper.WCCommCallbacks;
import bm.wordclock.fragments.BaseFragment;
import bm.wordclock.fragments.ConnectionFragment;
import bm.wordclock.fragments.ControllerFragment;
import bm.wordclock.fragments.PluginsListFragment;
import bm.wordclock.model.Plugin;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        WCCommCallbacks
        {

    private ConnectionFragment mConnectionFragment;
    private PluginsListFragment mPluginListFragment;
    private ControllerFragment mControllerFragment;
    private boolean mIsConnecting;
    NavigationView mNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mConnectionFragment = new ConnectionFragment();
        mPluginListFragment = new PluginsListFragment();
        mControllerFragment = new ControllerFragment();

        selectFragment(mConnectionFragment);
    }

    private void selectFragment(BaseFragment fragment) {
        mIsConnecting = (fragment == mConnectionFragment);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, fragment)
                .commitAllowingStateLoss();

    }

    public void onFragmentResume(int drawerId)
    {
        mNavigationView.setCheckedItem(drawerId);
        //Call this to force NavigationView reloading its List
        mNavigationView.setItemTextColor(mNavigationView.getItemTextColor());
    }

    @Override
    public void onPause() {
        super.onPause();
        selectFragment(mConnectionFragment);
        SocketConnectionHandler.getSocketConnectionHandler().stop();
        SocketConnectionHandler.getSocketConnectionHandler().removeSocketConnectionListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SocketConnectionHandler.getSocketConnectionHandler().addSocketConnectionListener(this);
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        SocketConnectionHandler.getSocketConnectionHandler().start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (!mIsConnecting) {
            // Handle navigation view item clicks here.
            int id = item.getItemId();

            if (id == R.id.nav_plugins) {
                selectFragment(mPluginListFragment);
            } else if (id == R.id.nav_keyboard) {
                selectFragment(mControllerFragment);
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }







    @Override
    public void onStateChanged(STATE state) {
        switch (state) {
            case CONNECTED:
                selectFragment(mPluginListFragment);
                break;
            case DISCONNECTED:
                Toast.makeText(this, R.string.connect_connectionlost, Toast.LENGTH_SHORT).show();
                mConnectionFragment.hideText();
                selectFragment(mConnectionFragment);
                break;
            case COULD_NOT_CONNECT:
                mConnectionFragment.setText(getText(R.string.connect_couldnotconnect));
                break;
            case HOST_UNKNOWN:
                mConnectionFragment.setText(getText(R.string.connect_unknownhost));
                break;
        }
    }

    @Override
    public void onPluginsChanged(Collection<Plugin> plugins) {

    }

    @Override
    public void onActivePluginChanged(int index) {

    }
}
