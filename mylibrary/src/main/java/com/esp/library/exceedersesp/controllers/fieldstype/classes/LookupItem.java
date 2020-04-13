package com.esp.library.exceedersesp.controllers.fieldstype.classes;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;


import com.esp.library.R;
import com.esp.library.exceedersesp.controllers.Profile.EditSectionDetails;
import com.esp.library.exceedersesp.controllers.fieldstype.other.Validation;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.PickerTypeViewHolder;
import com.esp.library.utilities.common.CustomLogs;
import com.esp.library.utilities.common.SharedPreference;
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter;

import utilities.adapters.setup.applications.ListUsersApplicationsAdapter;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO;
import utilities.interfaces.CriteriaFieldsListener;

public class LookupItem {

    private String TAG = getClass().getSimpleName();
    private static LookupItem lookupItem = null;
    private ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener mApplicationFieldsAdapterListener;
    private CriteriaFieldsListener criteriaFieldsListener;
    private DynamicStagesCriteriaListDAO criteriaListDAO;
    private boolean isViewOnly;
    private String actualResponseJson;
    EditSectionDetails edisectionDetailslistener;


    public static LookupItem getInstance() {
        if (lookupItem == null)
            return lookupItem = new LookupItem();
        else
            return lookupItem;
    }


    public void mApplicationFieldsAdapterListener(ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener applicationFieldsAdapterListener) {
        mApplicationFieldsAdapterListener = applicationFieldsAdapterListener;
    }

    public void criteriaFieldsListener(CriteriaFieldsListener criteriafieldsListener) {
        criteriaFieldsListener = criteriafieldsListener;
    }

    public void criteriaListDAO(DynamicStagesCriteriaListDAO criterialistDAO) {
        criteriaListDAO = criterialistDAO;
    }

    public void getactualResponseJson(String actualresponseJson) {
        actualResponseJson = actualresponseJson;
    }

    public void getAdapter(EditSectionDetails edisectionDetails) {
        edisectionDetailslistener = edisectionDetails;
    }


    public void showLookUpTypeItemView(final PickerTypeViewHolder holder, final int position,
                                       DynamicFormSectionFieldDAO fieldDAO, boolean isviewOnly,
                                       Context mContext, DynamicStagesCriteriaListDAO dynamicStagesCriteriaListDAO,
                                       boolean isCalculatedMappedField) {

        isViewOnly = isviewOnly;
        SharedPreference pref = new SharedPreference(mContext);
        String getValue = fieldDAO.getValue();
        CustomLogs.displayLogs(TAG + " showLookUpTypeItemView getValue: " + getValue);
        try {
            if (pref.getLanguage().equalsIgnoreCase("ar")) {
                holder.tilFieldLabel.setGravity(Gravity.RIGHT);
                holder.etValue.setGravity(Gravity.RIGHT);
                if (isViewOnly) {
                    holder.tValue.setGravity(Gravity.RIGHT);
                    holder.tValueLabel.setGravity(Gravity.RIGHT);
                }

            } else {

                holder.tilFieldLabel.setGravity(Gravity.LEFT);
                holder.etValue.setGravity(Gravity.LEFT);
                if (isViewOnly) {
                    holder.tValue.setGravity(Gravity.LEFT);
                    holder.tValueLabel.setGravity(Gravity.LEFT);
                }

            }
        } catch (Exception e) {
            // e.printStackTrace();
        }

        if (dynamicStagesCriteriaListDAO != null &&
                (!dynamicStagesCriteriaListDAO.isOwner() && dynamicStagesCriteriaListDAO.getAssessmentStatus().equalsIgnoreCase(mContext.getString(R.string.active)))) {
            holder.etValue.setText(fieldDAO.getLabel());
            holder.etValue.setEnabled(false);
            return;
        }


        if (isViewOnly) {

            String label = fieldDAO.getLabel();
            if (ListUsersApplicationsAdapter.Companion.isSubApplications())
                label = fieldDAO.getLabel() + ":";

            holder.tValueLabel.setText(label);
            holder.tValue.setText(getValue);
            fieldDAO.setValidate(true);
            return;
        }


        //Setting Label
        String label = fieldDAO.getLabel();

        if (fieldDAO.isRequired() && !isViewOnly) {
            label += " *";
        }

        holder.tilFieldLabel.setHint(label);

        //

        //Setting pre-filled Value. If Have
        String lookupValue = "";
        int lookupId = 0;

        if (fieldDAO.getLookupValue() != null && !TextUtils.isEmpty(fieldDAO.getLookupValue())) {
            lookupValue = fieldDAO.getLookupValue();
            lookupId = fieldDAO.getId();
        } else {

           /* if(actualResponseJson!=null) {
                DynamicResponseDAO actualResponse = new Gson().fromJson(actualResponseJson, DynamicResponseDAO.class);
                int getSectionCustomFieldId = fieldDAO.getSectionCustomFieldId();
                if (actualResponse.getApplicationStatus() != null) {
                    if (actualResponse.getApplicationStatus().toLowerCase().equalsIgnoreCase(mContext.getString(R.string.neww))
                            || actualResponse.getApplicationStatus().toLowerCase().equalsIgnoreCase(mContext.getString(R.string.rejectedsmall))) {
                        GetValues gV = new GetValues();
                        List<ApplicationDetailFieldsDAO> apd = gV.getFormValues(actualResponse, 13);

                        for (int i = 0; i < apd.size(); i++) {
                            if (apd.get(i).getType() == 13) {
                                if (getSectionCustomFieldId == apd.get(i).getSectionId()) {
                                    lookupValue = apd.get(i).getFieldvalue();
                                    lookupId = apd.get(i).getLookupId();
                                    CustomLogs.displayLogs(TAG + " showLookUpTypeItemView val: " + apd.get(i).getFieldsDAO());
                                    break;
                                }
                            }
                        }

                    }
                }
            }*/
        }
        CustomLogs.displayLogs(TAG + " showLookUpTypeItemView lookupValue: " + lookupValue);
        if (lookupValue == null || lookupValue.replaceAll("\\s", "").length() == 0)
            lookupValue = getValue;

        if (!TextUtils.isEmpty(lookupValue)) {

            holder.etValue.setText(lookupValue);
            fieldDAO.setValue(String.valueOf(lookupId));
            fieldDAO.setValidate(true);
            holder.ivclear.setVisibility(View.VISIBLE);

            validateForm(fieldDAO);

        } else {

            //Handling Required Condition
            if (fieldDAO.isRequired())
                fieldDAO.setValidate(false);
            else
                fieldDAO.setValidate(true);

            validateForm(fieldDAO);

        }
        //


        if (!isViewOnly) {
            if (!fieldDAO.isReadOnly()) {

                holder.ivclear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.etValue.setText("");
                        fieldDAO.setValue("");
                        fieldDAO.setLookupValue("");
                        fieldDAO.setValidate(false);
                        holder.ivclear.setVisibility(View.GONE);
                        validateForm(fieldDAO);
                      //  setDrawable(holder, pref);
                    }
                });

                holder.etValue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mApplicationFieldsAdapterListener != null)
                            mApplicationFieldsAdapterListener.onLookupFieldClicked(fieldDAO, position, isCalculatedMappedField);
                        else if (edisectionDetailslistener != null)
                            edisectionDetailslistener.onLookupFieldClicked(fieldDAO, position, isCalculatedMappedField);


                    }
                });
            }
        }

        if (holder.etValue != null) {
            if (fieldDAO.isReadOnly())
                holder.etValue.setEnabled(false);
            else
                holder.etValue.setEnabled(true);
        }

        setDrawable(holder,pref);

    }

    private void setDrawable(PickerTypeViewHolder holder, SharedPreference pref)
    {
        //if (holder.ivclear.getVisibility() == View.GONE) {
            if (pref.getLanguage().equalsIgnoreCase("en"))
                holder.etValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_right_picker_arrow, 0);
            else
                holder.etValue.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_left_picker_arrow, 0, 0, 0);
      //  }
    }

    private void validateForm(DynamicFormSectionFieldDAO fieldDAO) {
        Validation validation = new Validation(mApplicationFieldsAdapterListener, criteriaFieldsListener,
                criteriaListDAO, fieldDAO);
        validation.setSectionListener(edisectionDetailslistener);
        validation.validateForm();
    }

}
