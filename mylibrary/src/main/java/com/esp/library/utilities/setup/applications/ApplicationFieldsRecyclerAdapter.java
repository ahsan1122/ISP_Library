package com.esp.library.utilities.setup.applications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esp.library.R;
import com.esp.library.exceedersesp.BaseActivity;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.AttachmentItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.CurrencyItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.DateItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.EdittextItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.LookupItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.MultiSelectionItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.RatingItem;
import com.esp.library.exceedersesp.controllers.fieldstype.classes.SingleSelectionItem;
import com.esp.library.exceedersesp.controllers.fieldstype.other.CalculatedMappedRequestTrigger;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.AttachmentTypeViewHolder;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.CurrencyEditTextTypeViewHolder;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.EditTextTypeViewHolder;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.MultipleSelectionTypeViewHolder;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.PickerTypeViewHolder;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.RatingTypeViewHolder;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.SingleSelectionTypeViewHolder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import utilities.adapters.setup.applications.ListUsersApplicationsAdapter;
import utilities.data.applicants.dynamics.DynamicFormSectionDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO;
import utilities.interfaces.CriteriaFieldsListener;

public class ApplicationFieldsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<DynamicFormSectionFieldDAO> mApplicationFields;
    private Context mContext;
    private ApplicationFieldsAdapterListener mApplicationFieldsAdapterListener;
    private String actualResponseJson;
    private boolean isViewOnly;
    private DynamicStagesCriteriaListDAO dynamicStagesCriteriaListDAO;
    private int sectionType;
    public static int SECTIONCONSTANT = 2;
    private DynamicStagesCriteriaListDAO criteriaListDAO;
    private CriteriaFieldsListener criteriaFieldsListener;
    public static boolean isCalculatedMappedField = false;
    List<DynamicFormSectionDAO> mApplications;

    public ApplicationFieldsRecyclerAdapter(String actualResponseJson, boolean isViewOnly, BaseActivity context,
                                            DynamicStagesCriteriaListDAO dynamicstagesCriteriaListDAO, int sectionType) {
        this.actualResponseJson = actualResponseJson;
        this.isViewOnly = isViewOnly;
        this.dynamicStagesCriteriaListDAO = dynamicstagesCriteriaListDAO;
        this.sectionType = sectionType;
        try {
            criteriaFieldsListener = (CriteriaFieldsListener) context;
        } catch (Exception e) {

        }

        if (dynamicStagesCriteriaListDAO != null && dynamicStagesCriteriaListDAO.getAssessmentStatus() == null)
            dynamicStagesCriteriaListDAO.setAssessmentStatus("");
        if (this.isViewOnly && (dynamicStagesCriteriaListDAO != null && dynamicStagesCriteriaListDAO.getAssessmentStatus().equalsIgnoreCase(context.getString(R.string.active))))
            this.isViewOnly = false;
        else if (sectionType == SECTIONCONSTANT)
            this.isViewOnly = true;
    }

    public void getCriteriaObject(DynamicStagesCriteriaListDAO criterialistDAO) {
        criteriaListDAO = criterialistDAO;
    }

    public interface ApplicationFieldsAdapterListener {
        void onFieldValuesChanged();

        void onAttachmentFieldClicked(DynamicFormSectionFieldDAO fieldDAO, int postion);

        void onLookupFieldClicked(DynamicFormSectionFieldDAO fieldDAO, int position, boolean isCalculatedMappedField);

    }


    public interface ApplicationDetailFieldsAdapterListener {
        void onFieldValuesChanged();

        void onAttachmentFieldClicked(DynamicFormSectionFieldDAO fieldDAO, int position);

        void onLookupFieldClicked(DynamicFormSectionFieldDAO fieldDAO, int position, boolean isCalculatedMappedField);


    }


    public void setmApplicationFieldsAdapterListener(ApplicationFieldsAdapterListener mApplicationFieldsAdapterListener) {
        this.mApplicationFieldsAdapterListener = mApplicationFieldsAdapterListener;
    }

    public void setRefreshList(List<DynamicFormSectionFieldDAO> dynamicFormSectionFields) {
        if (dynamicFormSectionFields == null)
            dynamicFormSectionFields = new ArrayList<>();
        this.mApplicationFields = dynamicFormSectionFields;
        notifyDataSetChanged();
    }

    public void setmApplications(List<DynamicFormSectionDAO> applications) {
        mApplications = applications;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.mContext = parent.getContext();


        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_add_application_field_type_separator, parent, false);

        RecyclerView.ViewHolder itemView = new SeparatorTypeViewHolder(v);

        int item_layout = R.layout.item_add_application_field_type_text_view;
        try {
            if (ListUsersApplicationsAdapter.Companion.isSubApplications())
                item_layout = R.layout.item_sub_application_field_type_text_view;
        } catch (Exception e) {
        }

        switch (viewType) {

            case 1://Short EditText
            case 2://Multi EditText
            case 3://Numbers EditText
            case 10://Email EditText
            case 15://HyperLink EditText
            case 16://PhoneNumber EditText
            case 17://Rollup Text
            case 18://Calculated Text
            case 19://Map field
                v = populateViews(parent, v, item_layout, R.layout.item_add_application_field_type_edit_text);
                itemView = new EditTextTypeViewHolder(v);
                break;
            case 11://Currency Type
                v = populateViews(parent, v, item_layout, R.layout.item_add_application_field_type_currency);
                itemView = new CurrencyEditTextTypeViewHolder(v);
                break;
            case 9://ratingbar Type
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_add_application_field_type_rating, parent, false);
                itemView = new RatingTypeViewHolder(v);
                break;
            case 5://SingleSelection Type
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_add_application_field_type_single_selection, parent, false);
                itemView = new SingleSelectionTypeViewHolder(v);
                break;
            case 6://MultiSelection Type
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_add_application_field_type_multi_selection, parent, false);
                itemView = new MultipleSelectionTypeViewHolder(v);
                break;
            case 7://Attachment Type
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_add_application_field_type_attachment, parent, false);
                itemView = new AttachmentTypeViewHolder(v);
                break;
            case 4://DateType
            case 13://Lookup Type
                v = populateViews(parent, v, item_layout, R.layout.item_add_application_field_type_picker);
                itemView = new PickerTypeViewHolder(v);
                break;

        }


        return itemView;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        mApplicationFields.get(position).setSectionType(sectionType);

        DynamicFormSectionFieldDAO fieldDAO = mApplicationFields.get(position);
        if (isViewOnly)
            mApplicationFields.get(position).setShowToUserOnly(true);

        // if ((fieldDAO.getType() == 18 || fieldDAO.getType() == 19) && !isCalculatedMappedField && !isViewOnly) {
        if (fieldDAO.isTigger() && !isCalculatedMappedField && !isViewOnly) {
            isCalculatedMappedField = true;
            CalculatedMappedRequestTrigger.submitCalculatedMappedRequest(mContext, isViewOnly, fieldDAO);
        } else if (fieldDAO.getType() == 13 && fieldDAO.getAllowedValuesCriteria() != null &&
                !fieldDAO.getAllowedValuesCriteria().isEmpty() && !isCalculatedMappedField) {

            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(fieldDAO.getAllowedValuesCriteria());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (jsonArray != null && jsonArray.length() > 0) {
                isCalculatedMappedField = true;
                CalculatedMappedRequestTrigger.submitCalculatedMappedRequest(mContext, isViewOnly, fieldDAO);
            }
        }

        switch (fieldDAO.getType()) {
            case 1://Short EditText
            case 2://Multi EditText
            case 3://Numbers EditText
            case 10://Email EditText
            case 15://HyperLink EditText
            case 16://PhoneNumber EditText
            case 17://Rollup Text
            case 18://Calculated Text
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

    @Override
    public int getItemViewType(int position) {
        return mApplicationFields.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return mApplicationFields == null ? 0 : mApplicationFields.size();
    }


    private View populateViews(ViewGroup parent, View v, int item_layout, int viewLayout) {
        if (isViewOnly) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(item_layout, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(viewLayout, parent, false);

        }
        return v;
    }

    private void populateEditTextItem(int position, RecyclerView.ViewHolder holder) {
        EdittextItem.getInstance().showEditTextItemView((EditTextTypeViewHolder) holder, position,
                mApplicationFields.get(position), isViewOnly, mContext, dynamicStagesCriteriaListDAO,
                isCalculatedMappedField);
        EdittextItem.getInstance().mApplicationFieldsAdapterListener(mApplicationFieldsAdapterListener);
        EdittextItem.getInstance().criteriaFieldsListener(criteriaFieldsListener);
        EdittextItem.getInstance().criteriaListDAO(criteriaListDAO);
        EdittextItem.getInstance().getAdapter(this);
    }

    private void populateCurrencyItem(int position, RecyclerView.ViewHolder holder) {
        CurrencyItem.getInstance().showCurrencyEditTextItemView((CurrencyEditTextTypeViewHolder) holder, position,
                mApplicationFields.get(position), isViewOnly, mContext, dynamicStagesCriteriaListDAO,
                isCalculatedMappedField);
        CurrencyItem.getInstance().mApplicationFieldsAdapterListener(mApplicationFieldsAdapterListener);
        CurrencyItem.getInstance().criteriaFieldsListener(criteriaFieldsListener);
        CurrencyItem.getInstance().criteriaListDAO(criteriaListDAO);
        CurrencyItem.getInstance().getactualResponseJson(actualResponseJson);
    }

    private void populateDateItem(int position, RecyclerView.ViewHolder holder) {
        DateItem.getInstance().showDateTypeItemView((PickerTypeViewHolder) holder, position,
                mApplicationFields.get(position), isViewOnly, mContext, dynamicStagesCriteriaListDAO,
                isCalculatedMappedField);
        DateItem.getInstance().mApplicationFieldsAdapterListener(mApplicationFieldsAdapterListener);
        DateItem.getInstance().criteriaFieldsListener(criteriaFieldsListener);
        DateItem.getInstance().criteriaListDAO(criteriaListDAO);
        DateItem.getInstance().getactualResponseJson(actualResponseJson);

    }

    private void populateLookupItem(int position, RecyclerView.ViewHolder holder) {
        LookupItem.getInstance().mApplicationFieldsAdapterListener(mApplicationFieldsAdapterListener);
        LookupItem.getInstance().criteriaFieldsListener(criteriaFieldsListener);
        LookupItem.getInstance().criteriaListDAO(criteriaListDAO);
        LookupItem.getInstance().getactualResponseJson(actualResponseJson);
        LookupItem.getInstance().showLookUpTypeItemView((PickerTypeViewHolder) holder, position,
                mApplicationFields.get(position), isViewOnly, mContext, dynamicStagesCriteriaListDAO,
                isCalculatedMappedField);
    }

    private void populateAttachmentItem(int position, RecyclerView.ViewHolder holder) {
        AttachmentItem.getInstance().showAttachmentTypeItemView((AttachmentTypeViewHolder) holder, position,
                mApplicationFields.get(position), isViewOnly, mContext, dynamicStagesCriteriaListDAO,
                isCalculatedMappedField);
        AttachmentItem.getInstance().mApplicationFieldsAdapterListener(mApplicationFieldsAdapterListener);
        AttachmentItem.getInstance().criteriaFieldsListener(criteriaFieldsListener);
        AttachmentItem.getInstance().criteriaListDAO(criteriaListDAO);
        AttachmentItem.getInstance().getAdapter(this);
    }

    private void populateSingleItem(int position, RecyclerView.ViewHolder holder) {
        SingleSelectionItem.getInstance().mApplicationFieldsAdapterListener(mApplicationFieldsAdapterListener);
        SingleSelectionItem.getInstance().criteriaFieldsListener(criteriaFieldsListener);
        SingleSelectionItem.getInstance().criteriaListDAO(criteriaListDAO);
        SingleSelectionItem.getInstance().getactualResponseJson(actualResponseJson);
        SingleSelectionItem.getInstance().showSingleSelectionTypeItemView((SingleSelectionTypeViewHolder) holder, position,
                mApplicationFields.get(position), isViewOnly, mContext, dynamicStagesCriteriaListDAO,
                isCalculatedMappedField);
    }

    private void populateMultiItem(int position, RecyclerView.ViewHolder holder) {
        MultiSelectionItem.getInstance().mApplicationFieldsAdapterListener(mApplicationFieldsAdapterListener);
        MultiSelectionItem.getInstance().criteriaFieldsListener(criteriaFieldsListener);
        MultiSelectionItem.getInstance().criteriaListDAO(criteriaListDAO);
        MultiSelectionItem.getInstance().getactualResponseJson(actualResponseJson);
        MultiSelectionItem.getInstance().showMultiSelectionTypeItemView((MultipleSelectionTypeViewHolder) holder, position,
                mApplicationFields.get(position), isViewOnly, mContext, dynamicStagesCriteriaListDAO,
                isCalculatedMappedField);
    }

    private void populateRatingItem(int position, RecyclerView.ViewHolder holder) {
        RatingItem.getInstance().showRatingBarItemView((RatingTypeViewHolder) holder, position,
                mApplicationFields.get(position), isViewOnly, mContext, dynamicStagesCriteriaListDAO,
                isCalculatedMappedField);
        RatingItem.getInstance().mApplicationFieldsAdapterListener(mApplicationFieldsAdapterListener);
        RatingItem.getInstance().criteriaFieldsListener(criteriaFieldsListener);
        RatingItem.getInstance().criteriaListDAO(criteriaListDAO);
    }

    protected class SeparatorTypeViewHolder extends RecyclerView.ViewHolder {

        LinearLayout llmain;

        public SeparatorTypeViewHolder(View itemView) {
            super(itemView);
            llmain = itemView.findViewById(R.id.llmain);
            llmain.setVisibility(View.GONE);
        }

    }


}
