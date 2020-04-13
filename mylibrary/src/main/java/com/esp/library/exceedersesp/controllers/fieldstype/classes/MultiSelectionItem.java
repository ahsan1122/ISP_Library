package com.esp.library.exceedersesp.controllers.fieldstype.classes;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.core.content.ContextCompat;


import com.esp.library.R;
import com.esp.library.exceedersesp.controllers.Profile.EditSectionDetails;
import com.esp.library.exceedersesp.controllers.fieldstype.other.CalculatedMappedRequestTrigger;
import com.esp.library.exceedersesp.controllers.fieldstype.other.Validation;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.MultipleSelectionTypeViewHolder;
import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.common.SharedPreference;
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import utilities.adapters.setup.applications.ListUsersApplicationsAdapter;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO;
import utilities.interfaces.CriteriaFieldsListener;

public class MultiSelectionItem {

    private String TAG = getClass().getSimpleName();
    private static MultiSelectionItem multiSelectionItem = null;
    private ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener mApplicationFieldsAdapterListener;
    private CriteriaFieldsListener criteriaFieldsListener;
    private DynamicStagesCriteriaListDAO criteriaListDAO;
    private boolean isViewOnly;
    private String actualResponseJson;
    private EditSectionDetails edisectionDetailslistener;

    public static MultiSelectionItem getInstance() {
        if (multiSelectionItem == null)
            return multiSelectionItem = new MultiSelectionItem();
        else
            return multiSelectionItem;
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

    public void showMultiSelectionTypeItemView(final MultipleSelectionTypeViewHolder holder, final int position,
                                               DynamicFormSectionFieldDAO fieldDAO, boolean isviewOnly,
                                               Context mContext, DynamicStagesCriteriaListDAO dynamicStagesCriteriaListDAO,
                                               boolean isCalculatedMappedField) {

        isViewOnly = isviewOnly;
        SharedPreference pref = new SharedPreference(mContext);
        String getValue = fieldDAO.getValue();
        try {
            if (pref.getLanguage().equalsIgnoreCase("ar")) {
                if (isViewOnly) {
                    holder.tValueLabel.setGravity(Gravity.RIGHT);
                }

            } else {

                if (isViewOnly) {
                    holder.tValueLabel.setGravity(Gravity.LEFT);

                }

            }

            if (fieldDAO.isReadOnly())
                isViewOnly = true;

            //Setting Label
            String label = fieldDAO.getLabel();

            if (isViewOnly) {
                if (ListUsersApplicationsAdapter.Companion.isSubApplications())
                    label = fieldDAO.getLabel() + ":";
                holder.tValueLabel.setText(label);
            } else {
                if (fieldDAO.isRequired()) {
                    label += " *";
                }

                holder.tValueLabel.setText(label);
            }
            //

            //Setting pre-filled Value. If Have

            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "font/lato/lato_bold.ttf");
            List<String> lookupLabelsList = Shared.getInstance().getLookupLabelsList(fieldDAO.getLookupValues());
            for (int i = 0; i < lookupLabelsList.size(); i++) {
                String getLabel = lookupLabelsList.get(i);
                CheckBox cb = new CheckBox(mContext);
                cb.setButtonDrawable(ContextCompat.getDrawable(mContext, R.drawable.checkbox_button_selector));
                cb.setTextSize(17);
                cb.setText(getLabel);
                cb.setId(i);
                cb.setTypeface(typeface);
                removeRippleEffectFromCheckBox(cb);
                cb.setPadding(30, 0, 30, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 22, 0, 0);
                holder.llcheckbox.addView(cb, params);


                if(fieldDAO.isReadOnly())
                    cb.setTextColor(ContextCompat.getColor(mContext,R.color.grey));

                if (dynamicStagesCriteriaListDAO != null &&
                        (!dynamicStagesCriteriaListDAO.isOwner() && dynamicStagesCriteriaListDAO.getAssessmentStatus().equalsIgnoreCase(mContext.getString(R.string.active)))) {
                    cb.setClickable(false);
                }


                if (!isViewOnly) {
                    if (!fieldDAO.isReadOnly()) {
                        int finalI = i;
                        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                if (fieldDAO.getLookupValues() != null)
                                    fieldDAO.getLookupValues().get(finalI).setSelected(isChecked);

                                String lookupValue = Shared.getInstance().getSelectedLookupValues(fieldDAO.getLookupValues(), false, false);
                                fieldDAO.setValue(lookupValue);

                                if (TextUtils.isEmpty(lookupValue))
                                    fieldDAO.setValidate(false);
                                else
                                    fieldDAO.setValidate(true);


                                    validateForm(fieldDAO);

                               // if (isCalculatedMappedField)
                                if (fieldDAO.isTigger())
                                    CalculatedMappedRequestTrigger.submitCalculatedMappedRequest(mContext, isViewOnly, fieldDAO);
                            }
                        });
                    }
                }

            }


            String lookupValue = "";


            if (getValue != null && getValue.length() > 0) {
                if (actualResponseJson != null)
                    lookupValue = Shared.getInstance().populateLookupValues(getValue, actualResponseJson);
                else
                    lookupValue = Shared.getInstance().populateLookupValuesForProfileSections(getValue, fieldDAO);
            } else if (fieldDAO.getLookupValues() != null && fieldDAO.getLookupValues().size() > 0) {
                lookupValue = Shared.getInstance().getSelectedLookupValues(fieldDAO.getLookupValues(), true, false);
            }

            if (actualResponseJson != null) {
                if (lookupValue == null || lookupValue.length() == 0 || lookupValue.equalsIgnoreCase("null"))
                    lookupValue = Shared.getInstance().populateStageLookupValues(getValue, actualResponseJson);
            }
            if (!TextUtils.isEmpty(lookupValue)) {
                if (isViewOnly) {
                    // holder.tValue.setText(lookupValue);
                    setCheckBoxValues(lookupLabelsList, lookupValue, holder, false, mContext);
                    fieldDAO.setValidate(true);
                } else {

                    setCheckBoxValues(lookupLabelsList, lookupValue, holder, true, mContext);

                    fieldDAO.setValidate(true);
                    validateForm(fieldDAO);
                }
            } else {
                if (!isViewOnly) {
                    //Handling Required Condition
                    if (fieldDAO.isRequired())
                        fieldDAO.setValidate(false);
                    else
                        fieldDAO.setValidate(true);

                    validateForm(fieldDAO);
                } else
                    setCheckBoxValues(lookupLabelsList, lookupValue, holder, false, mContext);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        validateForm(fieldDAO);
    }

    private void setCheckBoxValues(List<String> lookupLabelsList, String lookupValue,
                                   MultipleSelectionTypeViewHolder holder, boolean isEnable, Context mContext) {
        for (int i = 0; i < lookupLabelsList.size(); i++) {
            String getLable = lookupLabelsList.get(i);
            CheckBox cb = (CheckBox) holder.llcheckbox.getChildAt(i);
            cb.setClickable(isEnable);
            List<String> selectedValuesList = new ArrayList<String>(Arrays.asList(lookupValue.split(",")));
            for (int j = 0; j < selectedValuesList.size(); j++) {
                if (getLable.trim().equalsIgnoreCase(selectedValuesList.get(j).trim())) {
                    if (isViewOnly)
                        cb.setButtonDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_icons_checkbox_checked_disabled));
                    else
                        cb.setButtonDrawable(ContextCompat.getDrawable(mContext, R.drawable.checkbox_button_selector));
                    cb.setChecked(true);
                }
            }
        }
    }

    private void removeRippleEffectFromCheckBox(CheckBox checkBox) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = checkBox.getBackground();
            if (drawable instanceof RippleDrawable) {
                drawable = ((RippleDrawable) drawable).findDrawableByLayerId(0);
                checkBox.setBackground(drawable);
            }
        }
    }


    private void validateForm(DynamicFormSectionFieldDAO fieldDAO) {
        Validation validation = new Validation(mApplicationFieldsAdapterListener, criteriaFieldsListener,
                criteriaListDAO, fieldDAO);
        validation.setSectionListener(edisectionDetailslistener);
        validation.validateForm();
    }

}
