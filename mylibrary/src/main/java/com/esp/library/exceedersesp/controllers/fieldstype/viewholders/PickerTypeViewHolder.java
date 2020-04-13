package com.esp.library.exceedersesp.controllers.fieldstype.viewholders;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.esp.library.R;
import com.esp.library.utilities.customcontrols.BodyText;
import com.google.android.material.textfield.TextInputLayout;


public class PickerTypeViewHolder extends RecyclerView.ViewHolder {

    public ProgressBar progressbar;
    public Button btnClickArea;
    public AppCompatButton ivclear;
    public TextInputLayout tilFieldLabel;
    public TextInputLayout tilFieldDisableLabel;
    public com.esp.library.utilities.customcontrols.BodyEditText etValue;
    public com.esp.library.utilities.customcontrols.BodyEditText etvalueDisable;
    public BodyText tValue;
    public BodyText tValueLabel;
    public ImageView is_file_downloaded;
    public View onlyviewlayout;
    public LinearLayout llmain;
    public RelativeLayout framelayout;

    public PickerTypeViewHolder(View itemView) {
        super(itemView);

        btnClickArea = itemView.findViewById(R.id.btnClickArea);
        ivclear = itemView.findViewById(R.id.ivclear);
        etvalueDisable = itemView.findViewById(R.id.etvalueDisable);
        tilFieldLabel = itemView.findViewById(R.id.tilFieldLabel);
        tilFieldDisableLabel = itemView.findViewById(R.id.tilFieldDisableLabel);
        etValue = itemView.findViewById(R.id.etValue);
        tValue = itemView.findViewById(R.id.tValue);
        tValueLabel = itemView.findViewById(R.id.tValueLabel);
        progressbar = itemView.findViewById(R.id.progressbar);
        is_file_downloaded = itemView.findViewById(R.id.is_file_downloaded);
        onlyviewlayout = itemView.findViewById(R.id.onlyviewlayout);
        llmain = itemView.findViewById(R.id.llmain);
        framelayout = itemView.findViewById(R.id.framelayout);

    }
}
