package com.esp.library.utilities.common;

import android.content.Context;
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
import com.esp.library.exceedersesp.BaseActivity;



//
public class AlertActionWindow extends DialogFragment {

    BaseActivity bContext;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";

    private String mParam1;
    private String mParam2;
    private String mParam3;
    private String mParam4;
    private String mParam5;


    ActionInterface mAction;
    Button action_btn;
    Button close;
    TextView heading_text;
    TextView detail_text;


    public interface ActionInterface {
        void mActionTo(String whattodo);
    }


    public AlertActionWindow() {

    }

    public static AlertActionWindow newInstance(String heading, String description, String ok_label, String cancel_label, String ok_action) {
        AlertActionWindow fragment = new AlertActionWindow();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, heading);
        args.putString(ARG_PARAM2, description);
        args.putString(ARG_PARAM3, ok_label);
        args.putString(ARG_PARAM4, cancel_label);
        args.putString(ARG_PARAM5, ok_action);
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
            mParam5 = getArguments().getString(ARG_PARAM5);
        }



    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mAction = (ActionInterface) getActivity();
        } catch (Exception e) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.alert_action_window, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        close = v.findViewById(R.id.close);
        action_btn = v.findViewById(R.id.action_btn);
        heading_text = v.findViewById(R.id.heading);
        detail_text = v.findViewById(R.id.detail);

        heading_text.setText(mParam1);
        detail_text.setText(mParam2);
        close.setText(mParam4);
        action_btn.setText(mParam3);

        if (mParam5.equalsIgnoreCase(getString(R.string.adminsmall))) {
            action_btn.setVisibility(View.GONE);
        } else if (mParam5.equals(getString(R.string.profile))) {
            action_btn.setVisibility(View.VISIBLE);
        } else if (mParam5.equals(getString(R.string.draft))) {

            action_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mAction != null) {
                        mAction.mActionTo(AlertActionWindow.this.getString(R.string.draft));
                        AlertActionWindow.this.dismiss();
                    }
                }
            });

        }
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAction != null) {
                    mAction.mActionTo("");
                    AlertActionWindow.this.dismiss();
                }
            }
        });


        return v;
    }

}
