package bm.wordclock.fragments;

import android.support.v4.app.Fragment;

import bm.wordclock.android.MainActivity;

/**
 * Created by phenze on 09.02.17.
 */

public abstract class BaseFragment extends Fragment {


    public abstract int getDrawerId();

    public MainActivity getMainActivity()
    {
        return (MainActivity) getActivity();
    }


    @Override
    public void onResume() {
        super.onResume();
        getMainActivity().onFragmentResume(getDrawerId());
    }
}
