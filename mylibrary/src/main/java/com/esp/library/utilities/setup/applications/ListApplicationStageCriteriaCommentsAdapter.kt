package utilities.adapters.setup.applications

import android.content.Intent
import android.net.Uri
import android.os.StrictMode
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.esp.library.R
import com.esp.library.utilities.common.RoundedPicasso
import com.esp.library.utilities.common.Shared
import com.esp.library.exceedersesp.BaseActivity
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import utilities.data.applicants.dynamics.DynamicStagesCriteriaCommentsListDAO
import java.io.File


class ListApplicationStageCriteriaCommentsAdapter(private val mApplications: List<DynamicStagesCriteriaCommentsListDAO>?, con: BaseActivity, internal var searched_text: String) : androidx.recyclerview.widget.RecyclerView.Adapter<ListApplicationStageCriteriaCommentsAdapter.ParentViewHolder>() {

    internal var mstatus: CriteriaStatusChange? = null
    private var context: BaseActivity

    interface CriteriaStatusChange {
        fun StatusChange(update: DynamicStagesCriteriaCommentsListDAO)
    }

    open class ParentViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v)

    inner class ActivitiesList(v: View) : ParentViewHolder(v) {

        internal var user_img: ImageView
        internal var date_feedback: TextView
        internal var feedback: TextView
        internal var edit_feedback: ImageView
        internal var dynamic_fields_div: LinearLayout

        init {
            user_img = itemView.findViewById(R.id.user_img)
            edit_feedback = itemView.findViewById(R.id.edit_feedback)
            date_feedback = itemView.findViewById(R.id.date_feedback)
            feedback = itemView.findViewById(R.id.feedback)
            dynamic_fields_div = itemView.findViewById(R.id.dynamic_fields_div)
        }

    }


    init {
        context = con

        try {
            mstatus = context as CriteriaStatusChange
        } catch (e: ClassCastException) {
            throw ClassCastException("lisnter" + " must implement on Activity")
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val v: View
        v = LayoutInflater.from(parent.context).inflate(R.layout.repeater_application_stages_criteria_comments, parent, false)
        return ActivitiesList(v)
    }


    override fun onBindViewHolder(holder_parent: ParentViewHolder, position: Int) {

        val holder = holder_parent as ActivitiesList

        holder.date_feedback.text = Shared.getInstance().getDisplayDate(context, mApplications!![position].createdOn, true)

        if (mApplications[position].comment!!.length > 150) {
            holder.feedback.text = Shared.getInstance().toSubStr(mApplications[position].comment, 150)
            holder.feedback.tag = context.getString(R.string.hidden)

        } else {
            holder.feedback.text = mApplications[position].comment
            holder.feedback.tag = context.getString(R.string.shown)
        }

        holder.feedback.setOnClickListener {
            val status = holder.feedback.tag as String

            if (status != null) {

                if (status == context.getString(R.string.hidden)) {
                    holder.feedback.text = mApplications[position].comment
                    holder.feedback.tag = context.getString(R.string.shown)
                } else {
                    holder.feedback.text = Shared.getInstance().toSubStr(mApplications[position].comment, 150)
                    holder.feedback.tag = context.getString(R.string.hidden)
                }
            }
        }


        if (mApplications[position].imageUrl != null && mApplications[position].imageUrl!!.length > 0) {
            Picasso.with(context)
                    .load(mApplications[position].imageUrl)
                    .placeholder(R.drawable.ic_contact_default)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .transform(RoundedPicasso())
                    .resize(30, 30)
                    .into(holder.user_img)
        }

        var ScropId: String? = null

        try {
            if (Shared.getInstance().ReadPref("scropId", "login_info", context) != null) {
                ScropId = Shared.getInstance().ReadPref("scropId", "login_info", context)
            }

        } catch (e: Exception) {
        }

        if (ScropId != null) {

            val CommentsId = mApplications[position].commentUserId.toString() + ""
            if (CommentsId == ScropId) {
              //  holder.edit_feedback.visibility = View.VISIBLE
                holder.edit_feedback.visibility = View.GONE
                holder.edit_feedback.setOnClickListener {
                    if (mstatus != null) {
                        mstatus!!.StatusChange(mApplications[position])
                    }
                }

            } else {
                holder.edit_feedback.visibility = View.GONE
            }

        } else {
            holder.edit_feedback.visibility = View.GONE
        }


        /*
		holder.field_value.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if(mApplications.get(position).getAttachments()!=null){

					if(Shared.getInstance().isFileExist(Constants.FOLDER_PATH+"/"+Constants.FOLDER_NAME,mApplications.get(position).getAttachments().getName(),context)){
						OpenFile(Constants.FOLDER_PATH+"/"+Constants.FOLDER_NAME+"/"+mApplications.get(position).getAttachments().getName());

					}else{

						if(mApplications.get(position).getAttachments().getDownloadUrl().length()>0){

							holder.is_file_downloaded.setVisibility(View.GONE);
							holder.progressbar.setVisibility(View.VISIBLE);

							mApplications.get(position).getAttachments().setFileDownling(true);
							DownloadAttachment(mApplications.get(position));
							RefreshList();
						}

					}

				}else{

					if(mApplications.get(position).getAttachments().getDownloadUrl().length()>0){

						holder.is_file_downloaded.setVisibility(View.GONE);
						holder.progressbar.setVisibility(View.VISIBLE);

						mApplications.get(position).getAttachments().setFileDownling(true);
						DownloadAttachment(mApplications.get(position));
						RefreshList();
					}
				}

				//Shared.getInstance().messageBox(mApplications.get(position).getPhoto_detail().getDownloadUrl(),context);
			}
		});*/


    }//End Holder Class



    override fun getItemCount(): Int {
        return mApplications?.size ?: 0

    }

    override fun getItemId(position: Int): Long {
        return super.getItemId(position)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    private fun RefreshList() {
        notifyDataSetChanged()
    }

    /*
	private DynamicStagesCriteriaCommentsListDAO DownLoadFile(InputStream inputStream, DynamicStagesCriteriaCommentsListDAO attachmentsDAO){

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		String fileName = "";

		if(attachmentsDAO.getAttachments()!=null && attachmentsDAO.getAttachments().getName()!=null && attachmentsDAO.getAttachments().getName().length()>0){
			fileName = attachmentsDAO.getAttachments().getName();
		}

		if(attachmentsDAO.getAttachments()!=null && attachmentsDAO.getAttachments().getName()!=null && attachmentsDAO.getAttachments().getName().length()>0){
			fileName = attachmentsDAO.getAttachments().getName();
		}


		File file = new File(Constants.FOLDER_PATH+"/"+Constants.FOLDER_NAME,fileName );
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
			attachmentsDAO.getAttachments().setFileDownling(false);
			attachmentsDAO.getAttachments().setFileDownloaded(false);
			//RefreshList();

			return attachmentsDAO;
		} finally {
			try {
				if (output != null) {
					output.close();
				}
				else{
				}
			} catch (IOException e){
				attachmentsDAO.getAttachments().setFileDownling(false);
				attachmentsDAO.getAttachments().setFileDownloaded(false);
				//RefreshList();
				return attachmentsDAO;
			}
		}

		attachmentsDAO.getAttachments().setFileDownloaded(true);
		attachmentsDAO.getAttachments().setFileDownling(false);
		return attachmentsDAO;
	}

	*/private fun OpenFile(filePath: String) {
        try {
            val file = File(filePath)
            if (file != null) {

                val builder = StrictMode.VmPolicy.Builder()
                StrictMode.setVmPolicy(builder.build())

                val i = Intent(Intent.ACTION_VIEW)
                i.setDataAndType(Uri.fromFile(file), Shared.getInstance().getMimeType(file.path))
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(i)
            }


        } catch (e: Exception) {
            Shared.getInstance().messageBox(context.getString(R.string.appnotavailable), context)

        }

    }

    companion object {

        private val LOG_TAG = "ListApplicationFeedBackAdapter"


    }
    /*

	private void DownloadAttachment(final DynamicStagesCriteriaCommentsListDAO attachment){

		OkHttpClient client = new OkHttpClient();
		String imgURL = "";

		if(attachment.getAttachments()!=null && attachment.getAttachments().getDownloadUrl()!=null && attachment.getAttachments().getDownloadUrl().length()>0){
			imgURL = attachment.getAttachments().getDownloadUrl();
		}

		if(attachment.getAttachments()!=null && attachment.getAttachments().getDownloadUrl()!=null && attachment.getAttachments().getDownloadUrl().length()>0){
			imgURL = attachment.getAttachments().getDownloadUrl();
		}

		final Request request = new Request.Builder()
				.url(imgURL)
				.build();

		client.newCall(request).enqueue(new okhttp3.Callback() {


			@Override
			public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {

				if(!response.isSuccessful()){
					attachment.getAttachments().setFileDownloaded(false);
					attachment.getAttachments().setFileDownling(false);
					attachment.getAttachments().setFileDownloaded(false);

				}else{

					final DynamicStagesCriteriaCommentsListDAO attachmentsDAO = DownLoadFile(response.body().byteStream(),attachment);

					context.runOnUiThread(new Runnable() {
						@Override
						public void run() {

							if(attachmentsDAO!=null){

								attachment.getAttachments().setFileDownloaded(true);

								RefreshList();

								if(attachment.getAttachments()!=null && attachment.getAttachments().getDownloadUrl()!=null && attachment.getAttachments().getDownloadUrl().length()>0){
									OpenFile(Constants.FOLDER_PATH+"/"+Constants.FOLDER_NAME+"/"+attachmentsDAO.getAttachments().getName());
								}

								if(attachment.getAttachments()!=null && attachment.getAttachments().getDownloadUrl()!=null && attachment.getAttachments().getDownloadUrl().length()>0){
									OpenFile(Constants.FOLDER_PATH+"/"+Constants.FOLDER_NAME+"/"+attachmentsDAO.getAttachments().getName());
								}

							}
						}
					});


				}
			}

			@Override
			public void onFailure(okhttp3.Call call, IOException e) {

				attachment.getAttachments().setFileDownloaded(false);
				attachment.getAttachments().setFileDownling(false);
				attachment.getAttachments().setFileDownloaded(false);

				context.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						RefreshList();
					}
				});
			}

		});



	}//End Download
*/

}
