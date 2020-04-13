package com.esp.library.exceedersesp.controllers.fieldstype.classes;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.afollestad.materialdialogs.MaterialDialog;
import com.esp.library.R;
import com.esp.library.exceedersesp.ESPApplication;
import com.esp.library.exceedersesp.controllers.Profile.EditSectionDetails;
import com.esp.library.exceedersesp.controllers.fieldstype.other.CalculatedMappedRequestTrigger;
import com.esp.library.exceedersesp.controllers.fieldstype.other.Validation;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.CurrencyEditTextTypeViewHolder;
import com.esp.library.utilities.common.CustomLogs;
import com.esp.library.utilities.common.GetValues;
import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.common.SharedPreference;
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.util.List;

import utilities.adapters.setup.applications.ListUsersApplicationsAdapter;
import utilities.data.applicants.ApplicationDetailFieldsDAO;
import utilities.data.applicants.addapplication.CurrencyDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicResponseDAO;
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO;
import utilities.interfaces.CriteriaFieldsListener;

public class CurrencyItem {

    private String TAG = getClass().getSimpleName();
    private static CurrencyItem currencyItem = null;
    private boolean isViewOnly;
    private ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener mApplicationFieldsAdapterListener;
    private CriteriaFieldsListener criteriaFieldsListener;
    private DynamicStagesCriteriaListDAO criteriaListDAO;
    private String actualResponseJson;
    private EditSectionDetails edisectionDetailslistener;
    AlertDialog materialAlertDialogBuilder = null;
    int mSelectedIndex = 0;


    public static CurrencyItem getInstance() {
        if (currencyItem == null)
            return currencyItem = new CurrencyItem();
        else
            return currencyItem;
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

    public void showCurrencyEditTextItemView(final CurrencyEditTextTypeViewHolder holder, final int position,
                                             DynamicFormSectionFieldDAO fieldDAO, boolean isviewOnly,
                                             Context mContext, DynamicStagesCriteriaListDAO dynamicStagesCriteriaListDAO,
                                             boolean isCalculatedMappedField) {
        isViewOnly = isviewOnly;
        SharedPreference pref = new SharedPreference(mContext);
        try {
            if (pref.getLanguage().equalsIgnoreCase("ar")) {
                holder.tilFieldLabel.setGravity(Gravity.RIGHT);
                holder.etValue.setGravity(Gravity.RIGHT);

            } else {

                holder.tilFieldLabel.setGravity(Gravity.LEFT);
                holder.etValue.setGravity(Gravity.LEFT);

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

        if (!isViewOnly) {

            DynamicFormSectionFieldDAO finalFieldDAO2 = fieldDAO;
            holder.etValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean isfocusable) {


                    if (!isfocusable) {
                        //  if (isCalculatedMappedField)
                        if (finalFieldDAO2.isTigger())
                            CalculatedMappedRequestTrigger.submitCalculatedMappedRequest(mContext, isViewOnly, finalFieldDAO2);

                    }

                }
            });

            holder.etValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        //Clear focus here from edittext
                        holder.etValue.clearFocus();
                    }
                    return false;
                }
            });

            DynamicFormSectionFieldDAO finalFieldDAO = fieldDAO;
            holder.etValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    String outputedText = editable.toString();
                    String error = "";
                    finalFieldDAO.setValidate(false);
                    holder.tilFieldLabel.setError(null);
                    holder.tilFieldLabel.setErrorEnabled(false);
                    if (!finalFieldDAO.isReadOnly()) {
                        error = Shared.getInstance().edittextErrorChecks(mContext, outputedText, error, finalFieldDAO);
                    }

                    if (error.length() > 0) {
                        holder.tilFieldLabel.setErrorEnabled(true);
                        holder.tilFieldLabel.setError(error);
                        finalFieldDAO.setValue("");
                    } else {

                        finalFieldDAO.setValue(outputedText);
                        finalFieldDAO.setValidate(true);

                        if (finalFieldDAO.isRequired() && outputedText.isEmpty())
                            finalFieldDAO.setValidate(false);


                    }
                    validateForm(finalFieldDAO);
                }
            });


        }

        //Setting Label
        String label = fieldDAO.getLabel();
        if (isViewOnly) {
            if (ListUsersApplicationsAdapter.Companion.isSubApplications()) {
                label = fieldDAO.getLabel() + ":";
                holder.tValueLabel.setText(label);
            } else
                holder.tValueLabel.setText(mContext.getString(R.string.value));
        } else {
            if (fieldDAO.isRequired()) {
                label += " *";
            }
            holder.tCurrencyLabel.setVisibility(View.VISIBLE);
            holder.tCurrencyLabel.setText(label);
        }
        //


        //  holder.etValue.setText(getValue);
        try {
            fieldDAO = Shared.getInstance().populateCurrencyByObject(fieldDAO);
        } catch (Exception e) {
            if (actualResponseJson != null) {
                DynamicResponseDAO actualResponse = new Gson().fromJson(actualResponseJson, DynamicResponseDAO.class);
                int getSectionCustomFieldId = fieldDAO.getSectionCustomFieldId();
                if (actualResponse.getApplicationStatus() != null) {
                    if (actualResponse.getApplicationStatus().toLowerCase().equalsIgnoreCase(mContext.getString(R.string.neww))) {
                        GetValues gV = new GetValues();
                        List<ApplicationDetailFieldsDAO> apd = gV.getFormValues(actualResponse, 11);

                        for (int i = 0; i < apd.size(); i++) {
                            if (apd.get(i).getType() == 11) {
                                if (getSectionCustomFieldId == apd.get(i).getSectionId()) {

                                    try {
                                        String value = "";
                                        fieldDAO = apd.get(i).getFieldsDAO();
                                        if (apd.get(i).getFieldsDAO().getValue() != null)
                                            value = apd.get(i).getFieldsDAO().getValue();
                                        CustomLogs.displayLogs(TAG + " showCurrencyEditTextItemView val: " + apd.get(i).getFieldsDAO() + " value: " + value);
                                        break;
                                    } catch (Exception ee) {

                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

        //Setting Currency
        CurrencyDAO selectedCurrency = null;
        if (fieldDAO != null) {
            if (fieldDAO.getValue() != null && !TextUtils.isEmpty(fieldDAO.getValue())
                    && (fieldDAO.getSelectedCurrencySymbol() == null || TextUtils.isEmpty(fieldDAO.getSelectedCurrencySymbol()))) {


                selectedCurrency = Shared.getInstance().getCurrencyById(Integer.valueOf(fieldDAO.getValue()));
                fieldDAO.setSelectedCurrencyId(selectedCurrency.getId());
                fieldDAO.setSelectedCurrencySymbol(selectedCurrency.getSymobl());
                //Setting empty value for the required Validation.
                if (isViewOnly) {
                    if (ListUsersApplicationsAdapter.Companion.isSubApplications())
                        holder.tValue.setText("");
                    else {
                        holder.tValue.setText("");
                        holder.tCurrencyLabel.setText("");
                        holder.tCurrencyLabel.setVisibility(View.GONE);
                    }
                } else
                    holder.etValue.setText("");
            } else {
                if (isViewOnly) {
                    if (ListUsersApplicationsAdapter.Companion.isSubApplications())
                        holder.tValue.setText(fieldDAO.getValue());
                    else {
                        holder.tValue.setText(fieldDAO.getSelectedCurrencyId() + " (" + fieldDAO.getSelectedCurrencySymbol() + ") " + fieldDAO.getValue());
                        holder.tCurrencyLabel.setText(label);
                        holder.tCurrencyLabel.setVisibility(View.VISIBLE);
                    }
                } else
                    holder.etValue.setText(fieldDAO.getValue());

            }

            try {
                //Try to get the currency from SelectedCurrencyId.
                if (selectedCurrency == null && fieldDAO.getSelectedCurrencyId() > 0) {
                    selectedCurrency = Shared.getInstance().getCurrencyById(fieldDAO.getSelectedCurrencyId());
                    fieldDAO.setSelectedCurrencyId(selectedCurrency.getId());
                    fieldDAO.setSelectedCurrencySymbol(selectedCurrency.getSymobl());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (selectedCurrency != null) {
                if (isViewOnly) {
                    if (ListUsersApplicationsAdapter.Companion.isSubApplications())
                        holder.tValue.setText(selectedCurrency.getCode() + " (" + selectedCurrency.getSymobl() + ") " + holder.tValue.getText());
                    else {
                        holder.tValue.setText(selectedCurrency.getCode() + " (" + selectedCurrency.getSymobl() + ") " + fieldDAO.getValue());
                        holder.tCurrencyLabel.setText(label);
                        holder.tCurrencyLabel.setVisibility(View.VISIBLE);
                    }
                } else
                    holder.etCurrency.setText(selectedCurrency.getCode() + " (" + selectedCurrency.getSymobl() + ")");
            } else {
                if (isViewOnly) {
                    holder.tValue.setText("");
                    holder.tCurrencyLabel.setText("");
                    holder.tCurrencyLabel.setVisibility(View.GONE);
                } else
                    holder.etCurrency.setText("");
            }
            //

            int maxLength = 1000;

            if (fieldDAO.getMaxVal() > 0) {
                maxLength = fieldDAO.getMaxVal();
            }

            InputFilter[] FilterArray = new InputFilter[1];
            FilterArray[0] = new InputFilter.LengthFilter(maxLength);
            if (isViewOnly)
                holder.tValue.setFilters(FilterArray);
            else
                holder.etValue.setFilters(FilterArray);

            if (!isViewOnly) {
                DynamicFormSectionFieldDAO finalFieldDAO1 = fieldDAO;
                holder.btnClickArea.setOnClickListener(view -> {

                    if (materialAlertDialogBuilder == null) {
                        List<String> currencyCodesList = Shared.getInstance().getCurrencyCodesList(finalFieldDAO1);
                        String[] singleChoiceArr = currencyCodesList.toArray(new String[currencyCodesList.size()]);
                        mSelectedIndex = currencyCodesList.indexOf(holder.etCurrency.getText().toString());
                        if (currencyCodesList.size() <= 1)
                            return;
                        materialAlertDialogBuilder = new MaterialAlertDialogBuilder(mContext, R.style.AlertDialogTheme)
                                .setTitle("Currency")
                                .setCancelable(false)
                                .setSingleChoiceItems(singleChoiceArr, mSelectedIndex, (dialogInterface, currencyPosition) -> {

                                    mSelectedIndex = materialAlertDialogBuilder.getListView().getCheckedItemPosition();
                                    CurrencyDAO currencyDAO = ESPApplication.getInstance().getCurrencies().get(currencyPosition);
                                    CurrencyDAO selectedCurrency1 = Shared.getInstance().getCurrencyByCode(currencyDAO.getCode());

                                    holder.etCurrency.setText(selectedCurrency1.getCode() + " (" + selectedCurrency1.getSymobl() + ")");

                                    finalFieldDAO1.setSelectedCurrencyId(selectedCurrency1.getId());
                                    finalFieldDAO1.setSelectedCurrencySymbol(selectedCurrency1.getSymobl());

                                    // if (isCalculatedMappedField)
                                    if (finalFieldDAO1.isTigger())
                                        CalculatedMappedRequestTrigger.submitCalculatedMappedRequest(mContext, isViewOnly, finalFieldDAO1);
                                    materialAlertDialogBuilder = null;
                                    dialogInterface.cancel();
                                })
                                .setNegativeButton(mContext.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        materialAlertDialogBuilder = null;
                                        dialogInterface.cancel();
                                    }
                                })
                                .create();
                        materialAlertDialogBuilder.show();

                    }



                });
            }

            /*if (!isViewOnly) {
                if (!fieldDAO.isReadOnly()) {
                    List<String> currency = Shared.getInstance().getCurrencyCodesList(fieldDAO);
                    CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(mContext, R.layout.row_custom_spinner, currency);
                    holder.msCurrency.setAdapter(adapter);
                    if (selectedCurrency != null) {
                        int preSelectedIndex = -1;
                        for (int i = 0; i < currency.size(); i++) {
                            String currencyCode = currency.get(i);
                            if (currencyCode.equals(selectedCurrency.getCode() + " (" + selectedCurrency.getSymobl() + ")")) {
                                preSelectedIndex = i;
                                break;
                            }
                        }
                        if (preSelectedIndex != -1) {
                            holder.msCurrency.setSelection(preSelectedIndex);
                            adapter.setSelectedIndex(preSelectedIndex);
                        }
                    }
                    DynamicFormSectionFieldDAO finalFieldDAO1 = fieldDAO;
                    holder.msCurrency.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(@NotNull MaterialSpinner parent, @org.jetbrains.annotations.Nullable View view, int spinnerPos, long id) {

                            CurrencyDAO currencyDAO = ESPApplication.getInstance().getCurrencies().get(spinnerPos);
                            CurrencyDAO selectedCurrency = Shared.getInstance().getCurrencyByCode(currencyDAO.getCode());

                            holder.etCurrency.setText(selectedCurrency.getCode() + " (" + selectedCurrency.getSymobl() + ")");

                            finalFieldDAO1.setSelectedCurrencyId(selectedCurrency.getId());
                            finalFieldDAO1.setSelectedCurrencySymbol(selectedCurrency.getSymobl());

                            adapter.setSelectedIndex(spinnerPos);

                            if (isCalculatedMappedField)
                                CalculatedMappedRequestTrigger.submitCalculatedMappedRequest(mContext, isViewOnly, position);

                        }

                        @Override
                        public void onNothingSelected(@NotNull MaterialSpinner parent) {

                        }
                    });
                }

            }*/
            if (holder.etValue != null) {
                if (fieldDAO.isReadOnly())
                    holder.etValue.setEnabled(false);
                else
                    holder.etValue.setEnabled(true);
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
