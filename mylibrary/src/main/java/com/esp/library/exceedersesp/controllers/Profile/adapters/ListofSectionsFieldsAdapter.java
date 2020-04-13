package com.esp.library.exceedersesp.controllers.Profile.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;


import com.esp.library.R;
import com.esp.library.exceedersesp.controllers.Profile.EditSectionDetails;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.AttachmentItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.CurrencyItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.DateItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.EdittextItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.LookupItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.MultiSelectionItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.RatingItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.SingleSelectionItem;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.AttachmentTypeViewHolder;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.CurrencyEditTextTypeViewHolder;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.EditTextTypeViewHolder;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.MultipleSelectionTypeViewHolder;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.PickerTypeViewHolder;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.RatingTypeViewHolder;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.SingleSelectionTypeViewHolder;
import com.esp.library.utilities.common.SharedPreference;

import java.util.List;

import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;


public class ListofSectionsFieldsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    String TAG = getClass().getSimpleName();

    List<DynamicFormSectionFieldDAO> sectionsFields;
    Context mContext;
    boolean isViewOnly;
    boolean ischeckerror;
    SharedPreference pref;
    EditSectionDetails edisectionDetails;


    public ListofSectionsFieldsAdapter(List<DynamicFormSectionFieldDAO> sectionsFields, Context context,
                                       boolean ischeckerror, boolean isviewOnly) {
        this.sectionsFields = sectionsFields;
        this.ischeckerror = ischeckerror;
        this.isViewOnly = isviewOnly;
        pref = new SharedPreference(context);
    }

    public void getListenerContext(EditSectionDetails editSectionDetails) {
        edisectionDetails = editSectionDetails;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.mContext = parent.getContext();

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_add_application_field_type_separator, parent, false);

        RecyclerView.ViewHolder itemView = new SeparatorTypeViewHolder(v);

        switch (viewType) {
            case 1://Short EditText
            case 2://Multi EditText
            case 3://Numbers EditText
            case 10://Email EditText
            case 15://HyperLink EditText
            case 16://PhoneNumber EditText
            case 17://Rollup EditText
            case 18://Calculated EditText
            case 19://Map field

                if (isViewOnly) {
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_add_application_field_type_text_view, parent, false);

                } else {
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_add_application_field_type_edit_text, parent, false);

                }
                itemView = new EditTextTypeViewHolder(v);
                break;

            case 11://Currency Type
                if (isViewOnly) {
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_add_application_field_type_text_view, parent, false);

                } else {
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_add_application_field_type_currency, parent, false);
                }
                itemView = new CurrencyEditTextTypeViewHolder(v);

                break;
            case 9://ratingbar Type

                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_add_application_field_type_rating, parent, false);
                itemView = new RatingTypeViewHolder(v);

                break;

            case 5: //SingleSelection Type
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_add_application_field_type_single_selection, parent, false);
                itemView = new SingleSelectionTypeViewHolder(v);
                break;
            case 6: //MultiSelection Type
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_add_application_field_type_multi_selection, parent, false);
                itemView = new MultipleSelectionTypeViewHolder(v);
                break;
            case 7: //Attachment Type

                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_add_application_field_type_attachment, parent, false);

                itemView = new AttachmentTypeViewHolder(v);

                break;
            case 4: //DateType
            case 13: //Lookup Type
                if (isViewOnly) {
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_add_application_field_type_text_view, parent, false);

                } else {
                    v = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.item_add_application_field_type_picker, parent, false);
                }
                itemView = new PickerTypeViewHolder(v);

                break;

        }


        return itemView;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DynamicFormSectionFieldDAO fieldDAO = sectionsFields.get(position);
        if (isViewOnly)
            fieldDAO.setShowToUserOnly(true);


        switch (fieldDAO.getType()) {
            case 1://Short EditText
            case 2://Multi EditText
            case 3://Numbers EditText
            case 10://Email EditText
            case 15://HyperLink EditText
            case 16://PhoneNumber EditText
            case 17://Rollup EditText
            case 18://Calculated EditText
            case 19://Map Field
                populateEditTextItem(position, holder);
                break;

            case 11://Currency Type
                populateCurrencyItem(position, holder);
                break;

            case 4://DateType
                populateDateItem(position, holder);
                break;

            case 7://Attachment Type
                populateAttachmentItem(position, holder);
                break;
            case 13://Lookup Type
                populateLookupItem(position, holder);
                break;

            case 5://SingleSelection Type
                populateSingleItem(position, holder);
                break;

            case 6://MultiSelection Type
                populateMultiItem(position, holder);
                break;

            case 9://Ratingbar Type
                populateRatingItem(position, holder);
                break;
        }


    }

    private void populateEditTextItem(int position, RecyclerView.ViewHolder holder) {
        EdittextItem.getInstance().showEditTextItemView((EditTextTypeViewHolder) holder, position,
                sectionsFields.get(position), isViewOnly, mContext, null,
                false);
        EdittextItem.getInstance().getAdapter(edisectionDetails);
        EdittextItem.getInstance().getProfileAdapter(this);
    }

    private void populateCurrencyItem(int position, RecyclerView.ViewHolder holder) {
        CurrencyItem.getInstance().showCurrencyEditTextItemView((CurrencyEditTextTypeViewHolder) holder, position,
                sectionsFields.get(position), isViewOnly, mContext, null,
                false);
        CurrencyItem.getInstance().getAdapter(edisectionDetails);
    }

    private void populateDateItem(int position, RecyclerView.ViewHolder holder) {
        DateItem.getInstance().showDateTypeItemView((PickerTypeViewHolder) holder, position,
                sectionsFields.get(position), isViewOnly, mContext, null,
                false);
        DateItem.getInstance().getAdapter(edisectionDetails);
    }


    private void populateSingleItem(int position, RecyclerView.ViewHolder holder) {
        SingleSelectionItem.getInstance().showSingleSelectionTypeItemView((SingleSelectionTypeViewHolder) holder, position,
                sectionsFields.get(position), isViewOnly, mContext, null,
                false);
        SingleSelectionItem.getInstance().getAdapter(edisectionDetails);
    }

    private void populateMultiItem(int position, RecyclerView.ViewHolder holder) {
        MultiSelectionItem.getInstance().showMultiSelectionTypeItemView((MultipleSelectionTypeViewHolder) holder, position,
                sectionsFields.get(position), isViewOnly, mContext, null,
                false);
        MultiSelectionItem.getInstance().getAdapter(edisectionDetails);
    }


    private void populateRatingItem(int position, RecyclerView.ViewHolder holder) {
        RatingItem.getInstance().showRatingBarItemView((RatingTypeViewHolder) holder, position,
                sectionsFields.get(position), isViewOnly, mContext, null,
                false);
        RatingItem.getInstance().getAdapter(edisectionDetails);
    }


    private void populateLookupItem(int position, RecyclerView.ViewHolder holder) {
        LookupItem.getInstance().showLookUpTypeItemView((PickerTypeViewHolder) holder, position,
                sectionsFields.get(position), isViewOnly, mContext, null,
                false);
        LookupItem.getInstance().getAdapter(edisectionDetails);
    }


    private void populateAttachmentItem(int position, RecyclerView.ViewHolder holder) {
        AttachmentItem.getInstance().showAttachmentTypeItemView((AttachmentTypeViewHolder) holder, position,
                sectionsFields.get(position), isViewOnly, mContext, null,
                false);
        AttachmentItem.getInstance().getAdapter(edisectionDetails);
        AttachmentItem.getInstance().getAdapter(this);
    }


    @Override
    public int getItemViewType(int position) {
        return sectionsFields.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return sectionsFields == null ? 0 : sectionsFields.size();
    }

    protected class SeparatorTypeViewHolder extends RecyclerView.ViewHolder {

        public SeparatorTypeViewHolder(View itemView) {
            super(itemView);


        }

    }

    public static class ParentViewHolder extends RecyclerView.ViewHolder {
        public ParentViewHolder(View v) {
            super(v);
        }
    }

    public class ActivitiesList extends ParentViewHolder {

        ImageButton ibRemoveCard;
        RecyclerView rvFields;

        public ActivitiesList(View v) {

            super(v);
            rvFields = itemView.findViewById(R.id.rvFields);
            ibRemoveCard = itemView.findViewById(R.id.ibRemoveCard);

        }

    }


}
