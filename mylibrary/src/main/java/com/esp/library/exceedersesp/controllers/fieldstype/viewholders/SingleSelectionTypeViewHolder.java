package com.esp.library.exceedersesp.controllers.fieldstype.viewholders;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.RadioGroup;

import com.esp.library.R;
import com.esp.library.utilities.customcontrols.BodyText;


public class SingleSelectionTypeViewHolder extends RecyclerView.ViewHolder {


    public BodyText tValueLabel;
    public RadioGroup radioGroup;

    public SingleSelectionTypeViewHolder(View itemView) {
        super(itemView);

        tValueLabel = itemView.findViewById(R.id.tValueLabel);
        radioGroup = itemView.findViewById(R.id.radioGroup);
    }
}
