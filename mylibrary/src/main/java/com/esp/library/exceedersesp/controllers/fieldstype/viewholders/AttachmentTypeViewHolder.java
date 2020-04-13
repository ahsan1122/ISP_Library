package com.esp.library.exceedersesp.controllers.fieldstype.viewholders;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.esp.library.R;
import com.esp.library.utilities.customcontrols.BodyText;

public class AttachmentTypeViewHolder extends RecyclerView.ViewHolder {


    public BodyText tValueLabel, txtacctehmentname, txtextensionsize;
    public RelativeLayout llattachment, rlattachmentdetails;
    public ProgressBar progressbar;
    public ImageView is_file_downloaded, attachtypeicon, ivdots;
    public View onlyviewlayout;


    public AttachmentTypeViewHolder(View itemView) {
        super(itemView);

        tValueLabel = itemView.findViewById(R.id.tValueLabel);
        txtacctehmentname = itemView.findViewById(R.id.txtacctehmentname);
        llattachment = itemView.findViewById(R.id.llattachment);
        rlattachmentdetails = itemView.findViewById(R.id.rlattachmentdetails);
        progressbar = itemView.findViewById(R.id.progressbar);
        is_file_downloaded = itemView.findViewById(R.id.is_file_downloaded);
        ivdots = itemView.findViewById(R.id.ivdots);
        attachtypeicon = itemView.findViewById(R.id.attachtypeicon);
        txtextensionsize = itemView.findViewById(R.id.txtextensionsize);
        onlyviewlayout = itemView.findViewById(R.id.onlyviewlayout);
    }
}
