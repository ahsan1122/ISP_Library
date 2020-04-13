package com.esp.library.exceedersesp.controllers.fieldstype.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.core.content.ContextCompat;


import com.esp.library.R;
import com.esp.library.exceedersesp.controllers.Profile.EditSectionDetails;
import com.esp.library.exceedersesp.controllers.fieldstype.other.CalculatedMappedRequestTrigger;
import com.esp.library.exceedersesp.controllers.fieldstype.other.Validation;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.SingleSelectionTypeViewHolder;
import com.esp.library.utilities.common.CustomLogs;
import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.common.SharedPreference;
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter;

import java.util.List;

import utilities.adapters.setup.applications.ListUsersApplicationsAdapter;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldLookupValuesDAO;
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO;
import utilities.interfaces.CriteriaFieldsListener;

public class SingleSelectionItem {

    private String TAG = getClass().getSimpleName();
    private static SingleSelectionItem singleSelectionItem = null;
    private ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener mApplicationFieldsAdapterListener;
    private CriteriaFieldsListener criteriaFieldsListener;
    private DynamicStagesCriteriaListDAO criteriaListDAO;
    private boolean isViewOnly;
    private String actualResponseJson;
    private EditSectionDetails edisectionDetailslistener;

    public static SingleSelectionItem getInstance() {
        if (singleSelectionItem == null)
            return singleSelectionItem = new SingleSelectionItem();
        else
            return singleSelectionItem;
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

    @SuppressLint("ClickableViewAccessibility")
    public void showSingleSelectionTypeItemView(final SingleSelectionTypeViewHolder holder, final int position,
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
        } catch (Exception e) {
            // e.printStackTrace();
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
        try {
            Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "font/lato/lato_bold.ttf");
            List<String> lookupLabelsList = Shared.getInstance().getLookupLabelsList(fieldDAO.getLookupValues());
            for (int i = 0; i < lookupLabelsList.size(); i++) {
                String radioButtonlabel = lookupLabelsList.get(i);
                RadioButton radioButton = new RadioButton(mContext);
                radioButton.setTextSize(17);
                radioButton.setButtonDrawable(ContextCompat.getDrawable(mContext, R.drawable.radio_button_selector));
                radioButton.setText(radioButtonlabel);
                radioButton.setId(i);
                radioButton.setTypeface(typeface);

                if (fieldDAO.isReadOnly())
                    radioButton.setTextColor(ContextCompat.getColor(mContext, R.color.grey));

                removeRippleEffectFromRadioButton(radioButton);

                if (dynamicStagesCriteriaListDAO != null &&
                        (!dynamicStagesCriteriaListDAO.isOwner() && dynamicStagesCriteriaListDAO.getAssessmentStatus().equalsIgnoreCase(mContext.getString(R.string.active)))) {
                    radioButton.setClickable(false);
                }

                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 22, 0, 0);
                radioButton.setLayoutParams(params);
                radioButton.setPadding(25, 0, 25, 0);
                holder.radioGroup.addView(radioButton);


            }
            if (!isViewOnly) {
                if (!fieldDAO.isReadOnly()) {

                    holder.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                        int checkedRadioButtonId = holder.radioGroup.getCheckedRadioButtonId();
                        /* RadioButton radioBtn = group.findViewById(checkedRadioButtonId);
                          CustomLogs.displayLogs(TAG+" radioBtn.isSelected(): "+radioBtn.isSelected());*/
                        if (fieldDAO.getLookupValues() != null) {
                            for (DynamicFormSectionFieldLookupValuesDAO lookupValuesDAO : fieldDAO.getLookupValues()) {
                                lookupValuesDAO.setSelected(false);
                            }
                            fieldDAO.getLookupValues().get(checkedRadioButtonId).setSelected(true);
                            String lookupValue = "";
                            lookupValue = Shared.getInstance().getSelectedLookupValues(fieldDAO.getLookupValues(), false, true);
                            //holder.etValue.setText(lookupValue);
                            fieldDAO.setValue(lookupValue);

                            if (TextUtils.isEmpty(lookupValue))
                                fieldDAO.setValidate(false);
                            else
                                fieldDAO.setValidate(true);

                            fieldDAO.setValidate(true);

                            //if (isCalculatedMappedField)
                            if (fieldDAO.isTigger())
                                CalculatedMappedRequestTrigger.submitCalculatedMappedRequest(mContext, isViewOnly, fieldDAO);


                            validateForm(fieldDAO);
                        }
                    });
                }
            }


            String lookupValue = "";

            if (fieldDAO.getLookupValues() != null && fieldDAO.getLookupValues().size() > 0) {
                lookupValue = Shared.getInstance().getSelectedLookupValues(fieldDAO.getLookupValues(), true, true);
            }

            if (actualResponseJson != null)
                lookupValue = Shared.getInstance().populateLookupValues(getValue, actualResponseJson);
            else
                lookupValue = Shared.getInstance().populateLookupValuesForProfileSections(getValue, fieldDAO);

            if (actualResponseJson != null) {
                if (lookupValue == null || lookupValue.length() == 0 || lookupValue.isEmpty() || lookupValue.equalsIgnoreCase("") || lookupValue.equalsIgnoreCase("null"))
                    lookupValue = Shared.getInstance().populateStageLookupValues(getValue, actualResponseJson);
            }
            if (!TextUtils.isEmpty(lookupValue)) {
                if (isViewOnly) {
                    // holder.tValue.setText(lookupValue);
                    setRadioButtonValues(lookupLabelsList, lookupValue, holder, false, mContext);
                    fieldDAO.setValidate(true);
                } else {

                    setRadioButtonValues(lookupLabelsList, lookupValue, holder, true, mContext);
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
                    setRadioButtonValues(lookupLabelsList, lookupValue, holder, false, mContext);
            }
        } catch (Exception e) {

        }

        validateForm(fieldDAO);

    }


    private void setRadioButtonValues(List<String> lookupLabelsList, String lookupValue,
                                      SingleSelectionTypeViewHolder holder, boolean isEnable, Context mContext) {
        for (int i = 0; i < lookupLabelsList.size(); i++) {
            String getLable = lookupLabelsList.get(i);
            RadioButton btn = (RadioButton) holder.radioGroup.getChildAt(i);
            btn.setClickable(isEnable);
            if (getLable.trim().equalsIgnoreCase(lookupValue.trim())) {
                if (isViewOnly)
                    btn.setButtonDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_icons_controls_radio_checked_disabled));
                else
                    btn.setButtonDrawable(ContextCompat.getDrawable(mContext, R.drawable.radio_button_selector));
                btn.setChecked(true);
            }
        }

    }

    private void removeRippleEffectFromRadioButton(RadioButton radioButton) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable drawable = radioButton.getBackground();
            if (drawable instanceof RippleDrawable) {
                drawable = ((RippleDrawable) drawable).findDrawableByLayerId(0);
                radioButton.setBackground(drawable);
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
