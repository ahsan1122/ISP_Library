package com.esp.library.exceedersesp.controllers.fieldstype.viewholders;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;

import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.recyclerview.widget.RecyclerView;

import com.esp.library.R;
import com.esp.library.utilities.customcontrols.BodyText;


public class RatingTypeViewHolder extends RecyclerView.ViewHolder {

    public BodyText tValueLabel;
    public AppCompatRatingBar ratingBar;
    public LinearLayout parentlayout;

    public RatingTypeViewHolder(View itemView) {
        super(itemView);


        tValueLabel = itemView.findViewById(R.id.tValueLabel);
        ratingBar = itemView.findViewById(R.id.ratingBar);
        parentlayout = itemView.findViewById(R.id.parentlayout);

    }
}
