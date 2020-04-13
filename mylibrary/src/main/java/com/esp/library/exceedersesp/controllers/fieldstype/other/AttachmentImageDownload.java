package com.esp.library.exceedersesp.controllers.fieldstype.other;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.view.View;

import com.esp.library.exceedersesp.controllers.Profile.adapters.ListofSectionsFieldsAdapter;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.AttachmentTypeViewHolder;
import com.esp.library.utilities.common.CustomLogs;
import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import utilities.data.applicants.ApplicationDetailFieldsDAO;
import utilities.data.applicants.dynamics.DyanmicFormSectionFieldDetailsDAO;

public class AttachmentImageDownload {

    private String TAG = getClass().getSimpleName();
    private static AttachmentImageDownload attachmentImageDownload = null;

    public static AttachmentImageDownload getInstance() {
        if (attachmentImageDownload == null)
            return attachmentImageDownload = new AttachmentImageDownload();
        else
            return attachmentImageDownload;
    }

    public void downloadImage(final AttachmentTypeViewHolder holder, final DyanmicFormSectionFieldDetailsDAO getdetails,
                              final String uploadedFileName, Context mContext,
                              ApplicationFieldsRecyclerAdapter applicationFieldsRecyclerAdapter,
                              ListofSectionsFieldsAdapter listofSectionsFieldsAdapter, int position) {
        final String getOutputMediaFile = Shared.getInstance().getOutputMediaFile(uploadedFileName).getPath();
        final boolean isFileExist = Shared.getInstance().isFileExist(getOutputMediaFile, mContext);
        if (getdetails.isFileDownling()) {
            holder.progressbar.setVisibility(View.VISIBLE);
        } else {
            holder.progressbar.setVisibility(View.GONE);
        }


        holder.rlattachmentdetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getdetails.isFileDownloaded()) {
                    if (isFileExist) {
                        OpenImage(getOutputMediaFile, mContext);
                    } else {
                        if (getdetails.getDownloadUrl().length() > 0) {
                            holder.progressbar.setVisibility(View.VISIBLE);
                            DownloadAttachment(holder, getdetails, uploadedFileName,
                                    applicationFieldsRecyclerAdapter, listofSectionsFieldsAdapter, position);
                        }
                    }
                } else {

                    if (isFileExist) {
                        OpenImage(getOutputMediaFile, mContext);
                    } else if (getdetails.getDownloadUrl() != null && getdetails.getDownloadUrl().length() > 0) {
                        holder.progressbar.setVisibility(View.VISIBLE);
                        DownloadAttachment(holder, getdetails, uploadedFileName,
                                applicationFieldsRecyclerAdapter, listofSectionsFieldsAdapter, position);

                    }
                }

                //Shared.getInstance().messageBox(mApplications.get(position).getPhoto_detail().getDownloadUrl(),context);
            }
        });
    }

    private void DownloadAttachment(final AttachmentTypeViewHolder holder, final DyanmicFormSectionFieldDetailsDAO attachment,
                                    final String uploadedFileName,
                                    ApplicationFieldsRecyclerAdapter applicationFieldsRecyclerAdapter,
                                    ListofSectionsFieldsAdapter listofSectionsFieldsAdapter, int position) {

        OkHttpClient client = new OkHttpClient();
        String imgURL = "";

        if (attachment.getDownloadUrl() != null && attachment.getDownloadUrl().length() > 0) {
            imgURL = attachment.getDownloadUrl();
        }

        CustomLogs.displayLogs(TAG + " imgURL: " + imgURL);

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

                    holder.progressbar.setVisibility(View.GONE);
                } else {

                    final ApplicationDetailFieldsDAO attachmentsDAO = DownloadImage(response.body().byteStream(),
                            attachment, uploadedFileName, applicationFieldsRecyclerAdapter, listofSectionsFieldsAdapter, position);

                    Handler handler = new Handler(Looper.getMainLooper()); // write in onCreate function

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (attachmentsDAO != null) {

                                attachment.setFileDownloaded(true);

                                if (applicationFieldsRecyclerAdapter != null)
                                    applicationFieldsRecyclerAdapter.notifyItemChanged(position);
                                else if (listofSectionsFieldsAdapter != null)
                                    listofSectionsFieldsAdapter.notifyItemChanged(position);

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
            }

        });


    }//End Download

    public void OpenImage(String filePath, Context mContext) {
        try {
            filePath = "file://" + filePath;
            CustomLogs.displayLogs(TAG + " OpenFile filePath: " + filePath);
            if (filePath != null) {

                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(filePath), "*/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }


        } catch (Exception e) {
            // Shared.getInstance().messageBox("Open this file, Application is not available", (Activity) mContext);
            e.printStackTrace();
        }
    }


    public ApplicationDetailFieldsDAO DownloadImage(InputStream inputStream,
                                                     DyanmicFormSectionFieldDetailsDAO attachmentsDAO,
                                                     String uploadedFileName,
                                                     ApplicationFieldsRecyclerAdapter applicationFieldsRecyclerAdapter,
                                                     ListofSectionsFieldsAdapter listofSectionsFieldsAdapter, int position) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String fileName = "";


        //   File file = new File(Constants.FOLDER_PATH + "/" + Constants.FOLDER_NAME, fileName);


        fileName = uploadedFileName;

        File file = Shared.getInstance().getOutputMediaFile(fileName);
        CustomLogs.displayLogs(TAG + " DownLoadFile file: " + file.getPath() + " fileName: " + fileName);

        OutputStream output = null;
        try {
            output = new FileOutputStream(file);

            byte[] buffer = new byte[1024]; // or other buffer size
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();


            String attachmentFileSize = Shared.getInstance().getAttachmentFileSize(file);
            attachmentsDAO.setFileSize(attachmentFileSize);

            Handler handler = new Handler(Looper.getMainLooper()); // write in onCreate function
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (applicationFieldsRecyclerAdapter != null)
                        applicationFieldsRecyclerAdapter.notifyItemChanged(position);
                    else if (listofSectionsFieldsAdapter != null)
                        listofSectionsFieldsAdapter.notifyItemChanged(position);

                }
            });
        } catch (IOException e) {
            attachmentsDAO.setFileDownling(false);
            attachmentsDAO.setFileDownloaded(false);

            Handler handler = new Handler(Looper.getMainLooper()); // write in onCreate function
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (applicationFieldsRecyclerAdapter != null)
                        applicationFieldsRecyclerAdapter.notifyItemChanged(position);
                    else if (listofSectionsFieldsAdapter != null)
                        listofSectionsFieldsAdapter.notifyItemChanged(position);
                }
            });

        } finally {
            try {
                if (output != null) {
                    output.close();
                    Handler handler = new Handler(Looper.getMainLooper()); // write in onCreate function

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (applicationFieldsRecyclerAdapter != null)
                                applicationFieldsRecyclerAdapter.notifyItemChanged(position);
                            else if (listofSectionsFieldsAdapter != null)
                                listofSectionsFieldsAdapter.notifyItemChanged(position);
                        }
                    });
                } else {
                }
            } catch (IOException e) {
                attachmentsDAO.setFileDownling(false);
                attachmentsDAO.setFileDownloaded(false);
                Handler handler = new Handler(Looper.getMainLooper()); // write in onCreate function

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (applicationFieldsRecyclerAdapter != null)
                            applicationFieldsRecyclerAdapter.notifyItemChanged(position);
                        else if (listofSectionsFieldsAdapter != null)
                            listofSectionsFieldsAdapter.notifyItemChanged(position);
                    }
                });
            }
        }

        attachmentsDAO.setFileDownloaded(true);
        attachmentsDAO.setFileDownling(false);
        return null;
    }

}
