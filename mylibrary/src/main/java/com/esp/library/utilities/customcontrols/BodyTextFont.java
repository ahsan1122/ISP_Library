package com.esp.library.utilities.customcontrols;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


import com.esp.library.R;
import com.esp.library.utilities.customevents.OnEnableListener;



public class BodyTextFont extends TextView {

    private OnEnableListener _enableListner;
    private String fontName;

    public BodyTextFont(Context context, String customFont) {
        super(context);
        if(!isInEditMode())
        {
        	setFont(context, null);
        }

    }

    public BodyTextFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context,attrs);

    }

    private void initialize(Context context, AttributeSet attrs)
    {
        if(!isInEditMode())
        {
            String packageName = "http://schemas.android.com/apk/res-auto";
            fontName = attrs.getAttributeValue(packageName, "customfont");
            setFont(context, fontName);
        }

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(_enableListner != null)
        {
            _enableListner.onEnable(enabled);
        }
    }


    public void setOnEnableListner(OnEnableListener enableListner)
    {
        _enableListner = enableListner;
    }

    public void setFont(Context context,String fname)
    {
        fontName = context.getString(R.string.flaticon);

        Typeface tf = Typeface.createFromAsset(context.getAssets(), "font/flaticon.ttf");
        setTypeface(tf);
    }
}
