package com.esp.library.utilities.customcontrols;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.customevents.OnEnableListener;



public class BodyText extends androidx.appcompat.widget.AppCompatTextView {

    String TAG = getClass().getSimpleName();
    private OnEnableListener _enableListner;
    private String fontName;


    public BodyText(Context context, String customFont) {
        super(context);
        if (!isInEditMode()) {
            setFont(context, null);
        }

    }

    public BodyText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);

    }

    private void initialize(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {
            String packageName = "http://schemas.android.com/apk/res-auto";
            fontName = attrs.getAttributeValue(packageName, "customfont");
            setFont(context, fontName);
        }

    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (_enableListner != null) {
            _enableListner.onEnable(enabled);
        }
    }


    public void setOnEnableListner(OnEnableListener enableListner) {
        _enableListner = enableListner;
    }

    public void setFont(Context context, String fname) {

        fontName = Shared.getInstance().fontName(context, fontName);


        Typeface tf = Typeface.createFromAsset(context.getAssets(), "font/" + fontName);
        setTypeface(tf);
    }
}
