package com.esp.library.exceedersesp.controllers.fieldstype.classes;

import android.app.DatePickerDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;

import com.esp.library.R;
import com.esp.library.exceedersesp.controllers.Profile.EditSectionDetails;
import com.esp.library.exceedersesp.controllers.fieldstype.other.CalculatedMappedRequestTrigger;
import com.esp.library.exceedersesp.controllers.fieldstype.other.Validation;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.PickerTypeViewHolder;
import com.esp.library.utilities.common.CustomLogs;
import com.esp.library.utilities.common.DateTimeUtils;
import com.esp.library.utilities.common.GetValues;
import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.common.SharedPreference;
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter;
import com.google.gson.Gson;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import utilities.adapters.setup.applications.ListUsersApplicationsAdapter;
import utilities.data.applicants.ApplicationDetailFieldsDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicResponseDAO;
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO;
import utilities.interfaces.CriteriaFieldsListener;

public class DateItem {

    private String TAG = getClass().getSimpleName();
    private static DateItem dateItem = null;
    private ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener mApplicationFieldsAdapterListener;
    private CriteriaFieldsListener criteriaFieldsListener;
    private DynamicStagesCriteriaListDAO criteriaListDAO;
    private boolean isViewOnly;
    private String actualResponseJson;
    private EditSectionDetails edisectionDetailslistener;


    public static DateItem getInstance() {
        if (dateItem == null)
            return dateItem = new DateItem();
        else
            return dateItem;
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

    public void showDateTypeItemView(final PickerTypeViewHolder holder, final int position,
                                     DynamicFormSectionFieldDAO fieldDAO, boolean isviewOnly,
                                     Context mContext, DynamicStagesCriteriaListDAO dynamicStagesCriteriaListDAO,
                                     boolean isCalculatedMappedField) {
        isViewOnly = isviewOnly;
        SharedPreference pref = new SharedPreference(mContext);
        String getValue = fieldDAO.getValue();
        try {
            if (pref.getLanguage().equalsIgnoreCase("ar")) {
                holder.tilFieldLabel.setGravity(Gravity.RIGHT);
                holder.etValue.setGravity(Gravity.RIGHT);
                if (isViewOnly)
                    holder.tValueLabel.setGravity(Gravity.RIGHT);

            } else {

                holder.tilFieldLabel.setGravity(Gravity.LEFT);
                holder.etValue.setGravity(Gravity.LEFT);
                if (isViewOnly)
                    holder.tValueLabel.setGravity(Gravity.LEFT);

            }
        } catch (Exception e) {
            //  e.printStackTrace();
        }

        if (dynamicStagesCriteriaListDAO != null &&
                (!dynamicStagesCriteriaListDAO.isOwner() && dynamicStagesCriteriaListDAO.getAssessmentStatus().equalsIgnoreCase(mContext.getString(R.string.active)))) {
            holder.etValue.setText(fieldDAO.getLabel());
            holder.etValue.setEnabled(false);
            return;
        }

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
            if (fieldDAO.isReadOnly()) {
                holder.tilFieldDisableLabel.setVisibility(View.VISIBLE);
                holder.llmain.setVisibility(View.GONE);
                holder.etvalueDisable.setText(getValue);
                holder.tilFieldDisableLabel.setHint(label);
            } else {
                holder.tilFieldDisableLabel.setVisibility(View.GONE);
                holder.tilFieldLabel.setVisibility(View.VISIBLE);
                holder.tilFieldLabel.setHint(label);
            }
        }
        //

        if (actualResponseJson != null) {
            DynamicResponseDAO actualResponse = new Gson().fromJson(actualResponseJson, DynamicResponseDAO.class);
            int getSectionCustomFieldId = fieldDAO.getSectionCustomFieldId();
            if (actualResponse != null && actualResponse.getApplicationStatus() != null) {
                if (actualResponse.getApplicationStatus().toLowerCase().equalsIgnoreCase("new")) {
                    GetValues gV = new GetValues();
                    List<ApplicationDetailFieldsDAO> apd = gV.getFormValues(actualResponse, 4);

                    for (int i = 0; i < apd.size(); i++) {
                        if (apd.get(i).getType() == 4) {
                            if (getSectionCustomFieldId == apd.get(i).getSectionId()) {
                                // fieldDAO.setValue(apd.get(i).getFieldvalue());
                                CustomLogs.displayLogs(TAG + " showDateTypeItemView val: " + apd.get(i).getFieldvalue());
                                break;
                            }
                        }
                    }

                }

            }
        }


        fieldDAO.setValue(getValue);
        //Setting Value
        if (fieldDAO.getValue() != null && !TextUtils.isEmpty(fieldDAO.getValue())) {
            String displayDate = Shared.getInstance().getDisplayDate(mContext, fieldDAO.getValue(), false);

            if (isViewOnly) {
                holder.tValue.setText(displayDate);
            } else {
                holder.etValue.setText(displayDate);
                fieldDAO.setValidate(true);
                holder.ivclear.setVisibility(View.VISIBLE);
                validateForm(fieldDAO);
            }

            if (fieldDAO.isReadOnly()) {
                try {
                    holder.tilFieldDisableLabel.setVisibility(View.VISIBLE);
                    holder.llmain.setVisibility(View.GONE);
                    holder.etvalueDisable.setText(displayDate);
                } catch (Exception e) {
                }
            }

        } else {
            if (!isViewOnly) {
                //Handling Required Condition
                if (fieldDAO.isRequired())
                    fieldDAO.setValidate(false);
                else
                    fieldDAO.setValidate(true);

                validateForm(fieldDAO);
            }
        }

        if (!isViewOnly) {
            if (pref.getLanguage().equalsIgnoreCase("en"))
                holder.etValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_icons_inputs_calendar_grey, 0);
            else
                holder.etValue.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icons_inputs_calendar_grey, 0, 0, 0);
        }

        if (fieldDAO.isReadOnly()) {
            try {
                if (pref.getLanguage().equalsIgnoreCase("en"))
                    holder.etvalueDisable.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_icons_inputs_calendar_grey, 0);
                else
                    holder.etvalueDisable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icons_inputs_calendar_grey, 0, 0, 0);
            } catch (Exception e) {
            }
        }




        if (!isViewOnly) {
            if (!fieldDAO.isReadOnly()) {


                holder.ivclear.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        holder.etValue.setText("");
                        fieldDAO.setValue("");
                        fieldDAO.setValidate(false);
                        holder.ivclear.setVisibility(View.GONE);
                        validateForm(fieldDAO);
                    }
                });

                holder.etValue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        final Calendar calendar = Shared.getInstance().getTodayCalendar();
                        final int year = calendar.get(Calendar.YEAR);
                        final int month = calendar.get(Calendar.MONTH);
                        final int day = calendar.get(Calendar.DAY_OF_MONTH);


                        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year_p, int month_p, int day_p) {

                               /* String selectedDate = year_p + "-" + Shared.getInstance().AddZero((month_p + 1)) + "-" + Shared.getInstance().AddZero(day_p);
                                //Appending Time As Zero, Just for formatting.
                                selectedDate += "T" + Shared.getInstance().AddZero(0) + ":" + Shared.getInstance().AddZero(0) + ":00Z";
*/

                                String formatedDate = Shared.getInstance().getDatePickerGMTDate(datePicker);
                                holder.etValue.setText(Shared.getInstance().getDisplayDate(mContext, formatedDate, false));
                                fieldDAO.setValue(formatedDate);
                                fieldDAO.setValidate(true);

                                holder.ivclear.setVisibility(View.VISIBLE);
                                // if (isCalculatedMappedField)
                                if (fieldDAO.isTigger())
                                    CalculatedMappedRequestTrigger.submitCalculatedMappedRequest(mContext, isViewOnly, fieldDAO);

                                validateForm(fieldDAO);

                            }
                        }, year, month, day);


                        if (fieldDAO.getMinDate() != null) {
                            Date minDate = DateTimeUtils.iso8601ToJavaDate(fieldDAO.getMinDate());
                            if (minDate == null)
                                minDate = Calendar.getInstance().getTime();

                            datePickerDialog.getDatePicker().setMinDate(minDate.getTime());
                        }

                        if (fieldDAO.getMaxDate() != null) {
                            Date maxDate = DateTimeUtils.iso8601ToJavaDate(fieldDAO.getMaxDate());
                            if (maxDate == null)
                                maxDate = Calendar.getInstance().getTime();

                            datePickerDialog.getDatePicker().setMaxDate(maxDate.getTime());
                        }

                        datePickerDialog.show();

                    }
                });
            }
        }
        if (holder.etValue != null) {
            if (fieldDAO.isReadOnly()) {
                holder.etValue.setEnabled(false);
                holder.tilFieldLabel.setBackgroundResource(R.drawable.draw_bg_disable_fields);
            } else
                holder.etValue.setEnabled(true);
        }
    }

    private void validateForm(DynamicFormSectionFieldDAO fieldDAO) {
        Validation validation = new Validation(mApplicationFieldsAdapterListener, criteriaFieldsListener,
                criteriaListDAO, fieldDAO);
        validation.setSectionListener(edisectionDetailslistener);
        validation.validateForm();
    }

}
