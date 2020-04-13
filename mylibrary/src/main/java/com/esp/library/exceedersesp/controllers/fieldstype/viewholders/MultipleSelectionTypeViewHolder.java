package com.esp.library.exceedersesp.controllers.fieldstype.viewholders;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.esp.library.R;
import com.esp.library.utilities.customcontrols.BodyText;


public class MultipleSelectionTypeViewHolder extends RecyclerView.ViewHolder {


    public BodyText tValueLabel;
    public LinearLayout llcheckbox;

    public MultipleSelectionTypeViewHolder(View itemView) {
        super(itemView);

        tValueLabel = itemView.findViewById(R.id.tValueLabel);
        llcheckbox = itemView.findViewById(R.id.llcheckbox);
    }
}
