package com.esp.library.utilities.customcontrols;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esp.library.R;
import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.customevents.OnEnableListener;

@SuppressLint("AppCompatCustomView")
public class EditTextWacher extends EditText {

    private OnEnableListener _enableListner;
    private String fontName;
    private String IsFilled = null;
    private boolean IsLableShow = false;
    Context Mycontext;

    public EditTextWacher(Context context, String customFont) {
        super(context);
        if(!isInEditMode())
        {
            setFont(context, null);
        }

    }

    public EditTextWacher(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context,attrs);

    }

    private void initialize(Context context, AttributeSet attrs)
    {
        if(!isInEditMode())
        {
            Mycontext = context;
            String packageName = "http://schemas.android.com/apk/res-auto";
            fontName = attrs.getAttributeValue(packageName, "customfont");
            IsFilled = attrs.getAttributeValue(packageName, "isfilled");
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

        fontName = Shared.getInstance().fontName(context, fontName);

        Typeface tf = Typeface.createFromAsset(context.getAssets(), "font/"+fontName);
        setTypeface(tf);
        addTextChangedListener(watcher);
    }


    TextWatcher watcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //YOUR CODE
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //YOUR CODE
        }

        @Override
        public void afterTextChanged(Editable s) {
            String outputedText = s.toString();

            LayoutInflater inflater = null;
            View view = null;
            TextView tv = null;
            LinearLayout r = null;
            int position = 0;

            inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.label_text, null);
            tv = view.findViewById(R.id.label);

            if(IsFilled != null ){
            tv.setTextColor(getResources().getColor(R.color.light_grey));
            }
            r = (LinearLayout)  getParent();



            if(outputedText !=null) {
             if(!IsLableShow && tv!=null){

                    IsLableShow = true;
                    if(getHint()!=null){
                        tv.setText(getHint().toString());
                    }

                    setTextSize(14);
                    r.addView(tv,position);

                 if (outputedText.length() > 0) {
                     outputedText = String.valueOf(outputedText.charAt(0)).toUpperCase() + outputedText.subSequence(1, outputedText.length());
                 }
                 setText(outputedText);
                 setSelection(outputedText.length(),outputedText.length());

                }
            }else{

                IsLableShow = false;
                r.removeViewAt(position);
                setTextSize(16);
            }



        }
    };
}
