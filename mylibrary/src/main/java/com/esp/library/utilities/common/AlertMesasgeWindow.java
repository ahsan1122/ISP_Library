package com.esp.library.utilities.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;


import com.esp.library.R;



public class AlertMesasgeWindow extends DialogFragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param3";

    private String mParam1;
    private String mParam2;
    private String mParam3;
    private String mParam4;


    Button close;
    TextView heading_text;
    TextView detail_text;
    ImageView alert_img;




    public AlertMesasgeWindow() {

    }

    public static AlertMesasgeWindow newInstance(String param1, String param2, String param3, String param4) {
        AlertMesasgeWindow fragment = new AlertMesasgeWindow();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        args.putString(ARG_PARAM4, param4);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 = getArguments().getString(ARG_PARAM3);
            mParam4 = getArguments().getString(ARG_PARAM4);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.alert_message_window, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        detail_text = v.findViewById(R.id.detail);
        heading_text = v.findViewById(R.id.heading);
        alert_img = v.findViewById(R.id.alert_icon);
        close = v.findViewById(R.id.close);


        heading_text.setText(mParam1);
        detail_text.setText(mParam2);
        if (mParam3.equals(getString(R.string.alert))) {
            alert_img.setBackgroundResource(R.drawable.ic_warning_red);
        } else {
            alert_img.setBackgroundResource(R.drawable.ic_warning_white);
        }

        close.setText(mParam4);


        close.setOnClickListener(view -> dismiss());

        return v;
    }

}
