package bm.wordclock.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import bm.wordclock.Helper.SocketConnectionHandler;
import bm.wordclock.Helper.WCProtocol;
import bm.wordclock.android.R;



public class ControllerFragment extends BaseFragment {


    public ControllerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_controller, container, false);

        ImageButton ib;
        ib = (ImageButton)v.findViewById(R.id.ctrl_btn_left);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SocketConnectionHandler.getSocketConnectionHandler().sendEvent(WCProtocol.EVENT_LEFT);
            }
        });
        ib = (ImageButton)v.findViewById(R.id.ctrl_btn_right);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SocketConnectionHandler.getSocketConnectionHandler().sendEvent(WCProtocol.EVENT_RIGHT);
            }
        });
        ib = (ImageButton)v.findViewById(R.id.ctrl_btn_return);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SocketConnectionHandler.getSocketConnectionHandler().sendEvent(WCProtocol.EVENT_RETURN);
            }
        });

        return v;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public int getDrawerId() {
        return R.id.nav_keyboard;
    }
}
