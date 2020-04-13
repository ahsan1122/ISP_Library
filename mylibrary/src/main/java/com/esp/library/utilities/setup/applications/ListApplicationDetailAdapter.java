package com.esp.library.utilities.setup.applications;


import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.esp.library.R;
import com.esp.library.exceedersesp.BaseActivity;
import com.esp.library.utilities.common.CustomLogs;
import com.esp.library.utilities.common.Shared;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import utilities.data.applicants.ApplicationDetailFieldsDAO;
import utilities.data.applicants.dynamics.DynamicCriteriaCustomFieldsLookUpsDAO;


public class ListApplicationDetailAdapter extends RecyclerView.Adapter<ListApplicationDetailAdapter.ParentViewHolder> {

    private static String LOG_TAG = "ListApplicationDetailAdapter";
    private List<ApplicationDetailFieldsDAO> mApplications;
    private static BaseActivity context;
    String searched_text;


    public static class ParentViewHolder extends RecyclerView.ViewHolder {
        public ParentViewHolder(View v) {
            super(v);
        }
    }

    public class ActivitiesList extends ParentViewHolder {

       // HorizontalScrollView dynamic_fields_scroll;
    //    LinearLayout dynamic_fields_div;
        TextView field_value;
        TextView field_label, field_section;
        ImageView is_file_downloaded, ivicon;
        ProgressBar progressbar;
        RelativeLayout rlsection;
        LinearLayout lllayout;

        public ActivitiesList(View v) {

            super(v);
         //   dynamic_fields_scroll = itemView.findViewById(R.id.dynamic_fields_scroll);
          //  dynamic_fields_div = itemView.findViewById(R.id.dynamic_fields_div);
            progressbar = itemView.findViewById(R.id.progressbar);
            field_value = itemView.findViewById(R.id.field_value);
            field_label = itemView.findViewById(R.id.field_label);
            field_section = itemView.findViewById(R.id.field_section);
            rlsection = itemView.findViewById(R.id.rlsection);
            lllayout = itemView.findViewById(R.id.lllayout);
            is_file_downloaded = itemView.findViewById(R.id.is_file_downloaded);
            ivicon = itemView.findViewById(R.id.ivicon);
        }

    }

    public ListApplicationDetailAdapter(List<ApplicationDetailFieldsDAO> myDataset, BaseActivity con, String searchedText) {

        mApplications = myDataset;
        context = con;
        searched_text = searchedText;
    }

    @Override
    public ParentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.repeater_application_field, parent, false);
        return new ActivitiesList(v);
    }


    @Override
    public void onBindViewHolder(final ParentViewHolder holder_parent, final int position) {


        final ActivitiesList holder = (ActivitiesList) holder_parent;
        holder.field_label.setText(mApplications.get(position).getFieldName());
        if (mApplications.get(position).getSectionname() != null) {
            if (mApplications.get(position).getSectionname().replaceAll("\\s", "").length() > 0) {
                holder.field_label.setVisibility(View.GONE);
                holder.lllayout.setVisibility(View.GONE);
                holder.rlsection.setVisibility(View.VISIBLE);

                holder.field_section.setText(mApplications.get(position).getSectionname());
            } else {
                holder.field_label.setVisibility(View.GONE);
                holder.lllayout.setVisibility(View.GONE);
                holder.rlsection.setVisibility(View.GONE);
            }
        } else {
            holder.field_label.setVisibility(View.VISIBLE);
            holder.lllayout.setVisibility(View.VISIBLE);
            holder.rlsection.setVisibility(View.GONE);
        }

        if (mApplications.get(position).getFieldName() != null) {
            if (mApplications.get(position).getFieldName().toLowerCase().equals(context.getString(R.string.gender))) {
                mApplications.get(position).getType();
            }
        }

        switch (mApplications.get(position).getType()) {
            //Text = 1,
            case 1:
                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(mApplications.get(position).getFieldvalue());
                }

                holder.field_value.setVisibility(View.VISIBLE);
             //   holder.dynamic_fields_scroll.setVisibility(View.GONE);
            //    holder.dynamic_fields_div.setVisibility(View.GONE);

                break;

            //MultiLine = 2,

            case 2:
                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(Html.fromHtml(mApplications.get(position).getFieldvalue()));
                }

                holder.field_value.setVisibility(View.VISIBLE);
              //  holder.dynamic_fields_scroll.setVisibility(View.GONE);
             //   holder.dynamic_fields_div.setVisibility(View.GONE);

                break;

            //	Number = 3,
            case 3:
                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(mApplications.get(position).getFieldvalue());
                }

                holder.field_value.setVisibility(View.VISIBLE);
              //  holder.dynamic_fields_scroll.setVisibility(View.GONE);
              //  holder.dynamic_fields_div.setVisibility(View.GONE);


                break;

            //DateTime = 4,
            case 4:
                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(Shared.getInstance().getDisplayDate(context, mApplications.get(position).getFieldvalue(), false));
                }

                holder.field_value.setVisibility(View.VISIBLE);
              //  holder.dynamic_fields_scroll.setVisibility(View.GONE);
              //  holder.dynamic_fields_div.setVisibility(View.GONE);


                break;
            //SingleSelection = 5,

            case 5:
                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(mApplications.get(position).getFieldvalue());
                }

                holder.field_value.setVisibility(View.VISIBLE);
            //    holder.dynamic_fields_scroll.setVisibility(View.GONE);
             //   holder.dynamic_fields_div.setVisibility(View.GONE);


                break;

            //MultiSelection = 6,

            case 6:

                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(mApplications.get(position).getFieldvalue());
                }
                holder.field_value.setVisibility(View.VISIBLE);
              //  holder.dynamic_fields_scroll.setVisibility(View.GONE);
             //   holder.dynamic_fields_div.setVisibility(View.GONE);

                break;

            //Attachment = 7,

            case 7:


                holder.field_value.setText(mApplications.get(position).getFieldvalue());

                final String getOutputMediaFile = getOutputMediaFile(mApplications.get(position).getFieldvalue()).getPath();
                final boolean isFileExist = Shared.getInstance().isFileExist(getOutputMediaFile, context);
                CustomLogs.displayLogs(LOG_TAG + " getOutputMediaFile: " + getOutputMediaFile + " isFileExist: " + isFileExist);
                //if (Shared.getInstance().isFileExist(Constants.FOLDER_PATH + "/" + Constants.FOLDER_NAME, mApplications.get(position).getFieldvalue(), context)) {
                if (isFileExist) {
                    holder.is_file_downloaded.setVisibility(View.GONE);
                } else {
                    holder.is_file_downloaded.setVisibility(View.VISIBLE);
                }


                if (mApplications.get(position).isFileDownling()) {
                    holder.progressbar.setVisibility(View.VISIBLE);
                    holder.is_file_downloaded.setVisibility(View.GONE);
                } else {
                    holder.progressbar.setVisibility(View.GONE);

                    // if (Shared.getInstance().isFileExist(Constants.FOLDER_PATH + "/" + Constants.FOLDER_NAME, mApplications.get(position).getFieldvalue(), context)) {
                    if (isFileExist) {
                        holder.is_file_downloaded.setVisibility(View.GONE);
                    } else {
                        holder.is_file_downloaded.setVisibility(View.VISIBLE);
                    }
                }

                if (mApplications.get(position).getFieldvalue() == null)
                    holder.is_file_downloaded.setVisibility(View.GONE);

                holder.field_value.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (mApplications.get(position).isFileDownloaded()) {

                            //  if (Shared.getInstance().isFileExist(Constants.FOLDER_PATH + "/" + Constants.FOLDER_NAME, mApplications.get(position).getFieldvalue(), context)) {
                            if (isFileExist) {
                                //OpenFile(Constants.FOLDER_PATH + "/" + Constants.FOLDER_NAME + "/" + mApplications.get(position).getFieldvalue());

                                OpenFile(getOutputMediaFile);

                            } else {

                                if (mApplications.get(position).getDownloadURL().length() > 0) {

                                    holder.is_file_downloaded.setVisibility(View.GONE);
                                    holder.progressbar.setVisibility(View.VISIBLE);

                                    mApplications.get(position).setFileDownling(true);
                                    DownloadAttachment(mApplications.get(position));
                                    RefreshList();
                                }

                            }

                        } else {

                            if (isFileExist) {
                                //OpenFile(Constants.FOLDER_PATH + "/" + Constants.FOLDER_NAME + "/" + mApplications.get(position).getFieldvalue());
                                OpenFile(getOutputMediaFile);

                            } else if (mApplications.get(position).getDownloadURL() != null && mApplications.get(position).getDownloadURL().length() > 0) {

                                holder.is_file_downloaded.setVisibility(View.GONE);
                                holder.progressbar.setVisibility(View.VISIBLE);

                                mApplications.get(position).setFileDownling(true);
                                DownloadAttachment(mApplications.get(position));
                                RefreshList();
                            }
                        }

                        //Shared.getInstance().messageBox(mApplications.get(position).getPhoto_detail().getDownloadUrl(),context);
                    }
                });


                holder.field_value.setVisibility(View.VISIBLE);
             //   holder.dynamic_fields_scroll.setVisibility(View.GONE);
            //    holder.dynamic_fields_div.setVisibility(View.GONE);


                break;

            //Date = 8,

            case 8:
                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(Shared.getInstance().getDisplayDate(context, mApplications.get(position).getFieldvalue(), false));
                }

                holder.field_value.setVisibility(View.VISIBLE);
             //   holder.dynamic_fields_scroll.setVisibility(View.GONE);
              //  holder.dynamic_fields_div.setVisibility(View.GONE);


                break;

            //separator = 9,

            case 9:
                break;

            //RadioButtons = 10,

            case 10:
                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(mApplications.get(position).getFieldvalue());
                }

                holder.field_value.setVisibility(View.VISIBLE);
            //    holder.dynamic_fields_scroll.setVisibility(View.GONE);
             //   holder.dynamic_fields_div.setVisibility(View.GONE);
                break;
            case 16:
                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(mApplications.get(position).getFieldvalue());
                }

                holder.field_value.setVisibility(View.VISIBLE);
              //  holder.dynamic_fields_scroll.setVisibility(View.GONE);
             //   holder.dynamic_fields_div.setVisibility(View.GONE);
                break;

            //Money = 11,

            case 11:
                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    //":2:0:1:AED"
                    holder.field_value.setText(mApplications.get(position).getFieldvalue());
                }

                holder.field_value.setVisibility(View.VISIBLE);
             //   holder.dynamic_fields_scroll.setVisibility(View.GONE);
             //   holder.dynamic_fields_div.setVisibility(View.GONE);

                break;

            case 12:
                break;

            //Lookup = 13
            case 13:

                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    //":2:0:1:AED"
                    holder.field_value.setText(mApplications.get(position).getFieldvalue());
                }

                holder.field_value.setVisibility(View.VISIBLE);
             //   holder.dynamic_fields_scroll.setVisibility(View.GONE);
             //   holder.dynamic_fields_div.setVisibility(View.GONE);
                break;
            case 15:
                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(mApplications.get(position).getFieldvalue());
                }

                holder.field_value.setVisibility(View.VISIBLE);
             //   holder.dynamic_fields_scroll.setVisibility(View.GONE);
             //   holder.dynamic_fields_div.setVisibility(View.GONE);
                break;



            //Roll up = 17,
            case 17:
                try {

                    if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                        holder.field_value.setText(mApplications.get(position).getFieldvalue());
                    }

                    holder.field_value.setVisibility(View.VISIBLE);
               //     holder.dynamic_fields_scroll.setVisibility(View.GONE);
               //     holder.dynamic_fields_div.setVisibility(View.GONE);


                    if (mApplications.get(position).getFieldvalue() == null || mApplications.get(position).getFieldvalue().replaceAll("\\s", "").length() == 0)
                        holder.field_value.setVisibility(View.GONE);
                    else
                        holder.field_value.setVisibility(View.VISIBLE);
                    holder.ivicon.setVisibility(View.VISIBLE);
                    holder.ivicon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_show_rollup));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            //Calculated = 18,
            case 18:

                try {
                    if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                        holder.field_value.setText(mApplications.get(position).getFieldvalue());
                    }

                    holder.field_value.setVisibility(View.VISIBLE);
                 //   holder.dynamic_fields_scroll.setVisibility(View.GONE);
               //     holder.dynamic_fields_div.setVisibility(View.GONE);

                    if (mApplications.get(position).getFieldvalue() == null || mApplications.get(position).getFieldvalue().replaceAll("\\s", "").length() == 0)
                        holder.field_value.setVisibility(View.GONE);
                    else
                        holder.field_value.setVisibility(View.VISIBLE);
                    holder.ivicon.setVisibility(View.VISIBLE);
                    holder.ivicon.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_show_calculated));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;


            //Criteria  - 1 - SINGLE LINE - type 51
            case 51:
                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(mApplications.get(position).getFieldvalue());
                }

                holder.field_value.setVisibility(View.VISIBLE);
             //   holder.dynamic_fields_scroll.setVisibility(View.GONE);
             //   holder.dynamic_fields_div.setVisibility(View.GONE);


                break;


            //Criteria  - 2 - MULTILINE - type 52
            case 52:
                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(Html.fromHtml(mApplications.get(position).getFieldvalue()));
                }
                holder.field_value.setVisibility(View.VISIBLE);
             //   holder.dynamic_fields_scroll.setVisibility(View.GONE);
             //   holder.dynamic_fields_div.setVisibility(View.GONE);


                break;
            //Criteria  - 3 - TEXT - type 53
            case 53:
                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(mApplications.get(position).getFieldvalue());
                }
                holder.field_value.setVisibility(View.VISIBLE);
              //  holder.dynamic_fields_scroll.setVisibility(View.GONE);
             //   holder.dynamic_fields_div.setVisibility(View.GONE);


                break;

            //Criteria  - 4 - DATE - type 54
            case 54:

                if (mApplications.get(position).getFieldvalue() != null && mApplications.get(position).getFieldvalue().length() > 0) {
                    holder.field_value.setText(Shared.getInstance().getDisplayDate(context, mApplications.get(position).getFieldvalue(), false));
                }

                holder.field_value.setVisibility(View.VISIBLE);
              //  holder.dynamic_fields_scroll.setVisibility(View.GONE);
              //  holder.dynamic_fields_div.setVisibility(View.GONE);


                break;

            //Criteria  - 5 - RADIO BUTTON - type 55
            case 55:
                if (mApplications.get(position).getSingleSelectionCriteria() != null && mApplications.get(position).getSingleSelectionCriteria().size() > 0 && !mApplications.get(position).isViewGenerated()) {
                    String select_value = "";


                    for (DynamicCriteriaCustomFieldsLookUpsDAO df : mApplications.get(position).getSingleSelectionCriteria()) {

                        //RadioButton ch = new RadioButton(context);
                        //ch.setText(df.getLabel());

                        if (df.isSelected()) {
                            select_value = df.getLabel();
                            //ch.setChecked(true);
                        } else {
                            //	ch.setChecked(false);
                        }

						/*ch.setEnabled(false);
						ch.setTextColor(context.getResources().getColor(R.color.dark_grey));
						ch.setButtonDrawable(context.getResources().getDrawable(R.drawable.radio_selector));

						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
						params.setMargins(0, 5, 15, 5);
						ch.setLayoutParams(params);


						holder.dynamic_fields_div.addView(ch);*/
                    }


                    if (!mApplications.get(position).isViewGenerated()) {
                        mApplications.get(position).setViewGenerated(true);
                    }
                    holder.field_value.setText(select_value);
                }

                holder.field_value.setVisibility(View.VISIBLE);
              //  holder.dynamic_fields_scroll.setVisibility(View.GONE);
             //   holder.dynamic_fields_div.setVisibility(View.GONE);

                break;


            // Criteria - 6 - MULTISELCTION 56
            case 56:

                if (mApplications.get(position).getMultiselectionCriteria() != null && mApplications.get(position).getMultiselectionCriteria().size() > 0 && !mApplications.get(position).isViewGenerated()) {

                    String values = "";
                    for (DynamicCriteriaCustomFieldsLookUpsDAO dccflu : mApplications.get(position).getMultiselectionCriteria()) {

                        //CheckBox ch = new CheckBox(context);
                        //ch.setText(dccflu.getLabel());

                        if (dccflu.isSelected()) {
                            if (values.length() == 0) {
                                values = dccflu.getLabel();
                            } else {
                                values += ", " + dccflu.getLabel();
                            }
                            //ch.setChecked(true);
                        } else {
                            //ch.setChecked(false);
                        }

						/*ch.setButtonDrawable(context.getResources().getDrawable(R.drawable.checkbox_selector));
						ch.setTextColor(context.getResources().getColor(R.color.dark_grey));
						ch.setEnabled(false);

						LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
						params.setMargins(0, 5, 15, 5);
						ch.setLayoutParams(params);

						holder.dynamic_fields_div.addView(ch);*/

                    }
                    if (!mApplications.get(position).isViewGenerated()) {
                        mApplications.get(position).setViewGenerated(true);
                    }

                    holder.field_value.setText(values);
                }

                holder.field_value.setVisibility(View.VISIBLE);
             //   holder.dynamic_fields_scroll.setVisibility(View.GONE);
             //   holder.dynamic_fields_div.setVisibility(View.GONE);

                break;

        }


    }//End Holder Class


    @Override
    public int getItemCount() {
        if (mApplications != null) {
            return mApplications.size();
        }
        return 0;

    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private void RefreshList() {
        notifyDataSetChanged();
    }

    private ApplicationDetailFieldsDAO DownLoadFile(InputStream inputStream, ApplicationDetailFieldsDAO attachmentsDAO) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String fileName = "";

        if (attachmentsDAO.getPhoto_detail() != null && attachmentsDAO.getPhoto_detail().getName() != null && attachmentsDAO.getPhoto_detail().getName().length() > 0) {
            fileName = attachmentsDAO.getPhoto_detail().getName();
        }

        if (attachmentsDAO.getPhoto_detailCriteria() != null && attachmentsDAO.getPhoto_detailCriteria().getName() != null && attachmentsDAO.getPhoto_detailCriteria().getName().length() > 0) {
            fileName = attachmentsDAO.getPhoto_detailCriteria().getName();
        }


        //   File file = new File(Constants.FOLDER_PATH + "/" + Constants.FOLDER_NAME, fileName);


        fileName = attachmentsDAO.getFieldvalue();

        File file = getOutputMediaFile(fileName);
        CustomLogs.displayLogs(LOG_TAG + " DownLoadFile file: " + file.getPath() + " fileName: " + fileName);
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);

            byte[] buffer = new byte[1024]; // or other buffer size
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        } catch (IOException e) {
            attachmentsDAO.setFileDownling(false);
            attachmentsDAO.setFileDownloaded(false);
            //RefreshList();

            return attachmentsDAO;
        } finally {
            try {
                if (output != null) {
                    output.close();
                } else {
                }
            } catch (IOException e) {
                attachmentsDAO.setFileDownling(false);
                attachmentsDAO.setFileDownloaded(false);
                //RefreshList();
                return attachmentsDAO;
            }
        }

        attachmentsDAO.setFileDownloaded(true);
        attachmentsDAO.setFileDownling(false);
        return attachmentsDAO;
    }

    public File getOutputMediaFile(String fileName) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath() + "/ESP/");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        File newPath = new File(mediaStorageDir.getPath() + File.separator +
                fileName);


        return newPath;
    }

    private void OpenFile(String filePath) {
        try {
           /* File file = new File(filePath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
                filePath = uri.getPath();
            }*/
            filePath = "file://" + filePath;
            CustomLogs.displayLogs(LOG_TAG + " OpenFile filePath: " + filePath);
            if (filePath != null) {

                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

              /*  Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(Uri.fromFile(file), Shared.getInstance().getMimeType(file.getPath()));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);*/

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(filePath), "image/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }


        } catch (Exception e) {
            Shared.getInstance().messageBox("Open this file, Application is not available", context);

        }
    }

    private void DownloadAttachment(final ApplicationDetailFieldsDAO attachment) {

        OkHttpClient client = new OkHttpClient();
        String imgURL = "";

        if (attachment.getDownloadURL() != null && attachment.getDownloadURL().length() > 0) {
            imgURL = attachment.getDownloadURL();
        }

        /*if (attachment.getPhoto_detailCriteria() != null && attachment.getPhoto_detailCriteria().getDownloadUrl() != null && attachment.getPhoto_detailCriteria().getDownloadUrl().length() > 0) {
            imgURL = attachment.getPhoto_detailCriteria().getDownloadUrl();
        }*/

        CustomLogs.displayLogs(LOG_TAG + " imgURL: " + imgURL);

        final Request request = new Request.Builder()
                .url(imgURL)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {


            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {

                if (!response.isSuccessful()) {
                    attachment.setFileDownloaded(false);
                    attachment.setFileDownling(false);
                    attachment.setFileDownloaded(false);

                } else {

                    final ApplicationDetailFieldsDAO attachmentsDAO = DownLoadFile(response.body().byteStream(), attachment);

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            if (attachmentsDAO != null) {

                                attachment.setFileDownloaded(true);

                                RefreshList();

                                CustomLogs.displayLogs("DownLoadFile Constants.FOLDER_PATH : " + getOutputMediaFile(attachmentsDAO.getFieldvalue()));

                                /*if (attachment.getDownloadURL() != null && attachment.getDownloadURL().length() > 0) {
                                    //    OpenFile(Constants.FOLDER_PATH + "/" + Constants.FOLDER_NAME + "/" + attachmentsDAO.getFieldvalue());
                                    OpenFile(getOutputMediaFile(attachmentsDAO.getFieldvalue()).getPath());
                                }

                                if (attachment.getPhoto_detailCriteria() != null && attachment.getPhoto_detailCriteria().getDownloadUrl() != null && attachment.getPhoto_detailCriteria().getDownloadUrl().length() > 0) {
                                    //  OpenFile(Constants.FOLDER_PATH + "/" + Constants.FOLDER_NAME + "/" + attachmentsDAO.getPhoto_detailCriteria().getName());
                                    OpenFile(getOutputMediaFile(attachmentsDAO.getPhoto_detailCriteria().getName()).getPath());
                                }*/

                            }
                        }
                    });


                }
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

                attachment.setFileDownloaded(false);
                attachment.setFileDownling(false);
                attachment.setFileDownloaded(false);

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        RefreshList();
                    }
                });
            }

        });


    }//End Download

}
