package com.esp.library.exceedersesp.controllers.fieldstype.classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.core.content.ContextCompat;


import com.esp.library.R;
import com.esp.library.exceedersesp.controllers.Profile.EditSectionDetails;
import com.esp.library.exceedersesp.controllers.Profile.adapters.ListofSectionsFieldsAdapter;
import com.esp.library.exceedersesp.controllers.fieldstype.other.CalculatedMappedRequestTrigger;
import com.esp.library.exceedersesp.controllers.fieldstype.other.Validation;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.EditTextTypeViewHolder;
import com.esp.library.utilities.common.CustomLogs;
import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.common.SharedPreference;
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter;

import utilities.adapters.setup.applications.ListUsersApplicationsAdapter;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO;
import utilities.interfaces.CriteriaFieldsListener;

public class EdittextItem {

    private String TAG = getClass().getSimpleName();
    private static EdittextItem edittextItem = null;
    private int maxLines = 5;
    private ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener mApplicationFieldsAdapterListener;
    private CriteriaFieldsListener criteriaFieldsListener;
    private DynamicStagesCriteriaListDAO criteriaListDAO;
    private boolean isViewOnly, isCalculatedMappedField;
    private ApplicationFieldsRecyclerAdapter applicationFieldsRecyclerAdapter;
    EditSectionDetails edisectionDetailslistener;
    SharedPreference pref;

    ListofSectionsFieldsAdapter listofSectionsFieldsAdapter;

    public static EdittextItem getInstance() {
        if (edittextItem == null)
            return edittextItem = new EdittextItem();
        else
            return edittextItem;
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

    public void getAdapter(ApplicationFieldsRecyclerAdapter applicationfieldsRecyclerAdapter) {
        applicationFieldsRecyclerAdapter = applicationfieldsRecyclerAdapter;
    }

    public void getAdapter(EditSectionDetails edisectionDetails) {
        edisectionDetailslistener = edisectionDetails;
    }

    public void getProfileAdapter(ListofSectionsFieldsAdapter listofsectionsFieldsAdapter) {
        listofSectionsFieldsAdapter = listofsectionsFieldsAdapter;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void showEditTextItemView(final EditTextTypeViewHolder holder, final int position,
                                     DynamicFormSectionFieldDAO fieldDAO, boolean isviewOnly,
                                     Context mContext, DynamicStagesCriteriaListDAO dynamicStagesCriteriaListDAO,
                                     boolean iscalculatedMappedField) {
        isViewOnly = isviewOnly;
        isCalculatedMappedField = iscalculatedMappedField;
        pref = new SharedPreference(mContext);
        // final DynamicFormSectionFieldDAO fieldDAO = fieldDAO;

        if (!isViewOnly) {
            if (fieldDAO.getType() != 10 && fieldDAO.getType() != 15) // 10 = Email 15 = hyperlink
                holder.etValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            else
                holder.etValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }

        String getValue = fieldDAO.getValue();

        String dateFormat = Shared.getInstance().getDisplayDate(mContext, getValue, true);

        if (dateFormat != null && !dateFormat.isEmpty())
            getValue = dateFormat;


        if (dynamicStagesCriteriaListDAO != null &&
                (!dynamicStagesCriteriaListDAO.isOwner() && dynamicStagesCriteriaListDAO.getAssessmentStatus().equalsIgnoreCase(mContext.getString(R.string.active)))) {
            holder.etValue.setText(fieldDAO.getLabel());
            holder.etValue.setEnabled(false);
            return;
        }


        if (isViewOnly || fieldDAO.isMappedCalculatedField()) {

            if (fieldDAO.getType() == 15 && getValue != null && !getValue.isEmpty()) {
                holder.tValue.setSingleLine(true);
                holder.tValue.setEllipsize(TextUtils.TruncateAt.END);
                if (!getValue.startsWith("http"))
                    getValue = "http://" + getValue;
            }

            if (holder.onlyviewlayout != null) {
                holder.onlyviewlayout.setVisibility(View.VISIBLE);
                if (holder.onlyviewlayout.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) holder.onlyviewlayout.getLayoutParams();
                    p.setMargins(0, 5, 0, 0);
                    holder.onlyviewlayout.requestLayout();
                    holder.onlyviewlayout.setLayoutParams(p);
                }

                holder.tilFieldLabel.setVisibility(View.GONE);
            }

            String label = fieldDAO.getLabel();
            if (ListUsersApplicationsAdapter.Companion.isSubApplications())
                label = fieldDAO.getLabel() + ":";
            holder.tValueLabel.setText(label);
            holder.tValue.setText(getValue);
            fieldDAO.setValidate(true);


            try {
                if (fieldDAO.getType() == 17) {
                    if (getValue == null || getValue.replaceAll("\\s", "").length() == 0)
                        holder.tValue.setVisibility(View.GONE);
                    else
                        holder.tValue.setVisibility(View.VISIBLE);
                    holder.ivicon.setVisibility(View.GONE);
                    holder.ivicon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_show_rollup));
                } else if (fieldDAO.getType() == 18) {
                    if (getValue == null || getValue.replaceAll("\\s", "").length() == 0)
                        holder.tValue.setVisibility(View.GONE);
                    else
                        holder.tValue.setVisibility(View.VISIBLE);
                    holder.ivicon.setVisibility(View.GONE);
                    holder.ivicon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_show_calculated));
                } else if (fieldDAO.getType() == 19) {


                    if (getValue == null || getValue.replaceAll("\\s", "").length() == 0)
                        holder.tValue.setVisibility(View.GONE);
                    else
                        holder.tValue.setVisibility(View.VISIBLE);
                } else if (fieldDAO.getType() == 2) {

                    /*CommonMethodsKotlin.applyCustomEllipsizeSpanning(maxLines, holder.tValue, mContext);

                    holder.tValue.setOnClickListener(v -> {

                        if (holder.tValue.getText().toString().contains("[ ... ]")) {
                            maxLines = 1000;
                        } else {
                            maxLines = 5;
                        }
                       // CommonMethodsKotlin.applyCustomEllipsizeSpanning(maxLines, holder.tValue, mContext);
                        if (applicationFieldsRecyclerAdapter != null)
                            applicationFieldsRecyclerAdapter.notifyItemChanged(position);

                        if (listofSectionsFieldsAdapter != null)
                            listofSectionsFieldsAdapter.notifyItemChanged(position);


                    });*/

                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            validateForm(fieldDAO);

            return;
        }


        try {
            if (pref.getLanguage().equalsIgnoreCase("ar")) {
                holder.tilFieldLabel.setGravity(Gravity.END);
                holder.etValue.setGravity(Gravity.END);
                if (isViewOnly) {
                    holder.tValueLabel.setGravity(Gravity.END);
                    holder.tValue.setGravity(Gravity.END);
                }

            } else {

                holder.tilFieldLabel.setGravity(Gravity.START);
                holder.etValue.setGravity(Gravity.START);
                if (isViewOnly) {
                    holder.tValueLabel.setGravity(Gravity.START);
                    holder.tValue.setGravity(Gravity.START);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        holder.etValue.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean isfocusable) {

                if (fieldDAO.getType() == 17 || fieldDAO.getType() == 18 || fieldDAO.getType() == 19) {

                    fieldDAO.setValue("");
                    fieldDAO.setValidate(true);
                    return;
                }
                if (!isfocusable) {
                    // if (isCalculatedMappedField )
                    if (fieldDAO.isTigger())
                        CalculatedMappedRequestTrigger.submitCalculatedMappedRequest(mContext, isViewOnly, fieldDAO);
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
                fieldDAO.setValidate(false);
                holder.tilFieldLabel.setError(null);
                holder.tilFieldLabel.setErrorEnabled(false);
                if (!fieldDAO.isReadOnly() && fieldDAO.getType() != 19) {
                    error = Shared.getInstance().edittextErrorChecks(mContext, outputedText, error, fieldDAO);
                }

                if (error.length() > 0) {
                    holder.tilFieldLabel.setErrorEnabled(true);
                    holder.tilFieldLabel.setError(error);
                    fieldDAO.setValue(outputedText);
                } else {

                    fieldDAO.setValue(outputedText);
                    fieldDAO.setValidate(true);

                    if (fieldDAO.isRequired() && outputedText.isEmpty())
                        fieldDAO.setValidate(false);


                }
                validateForm(fieldDAO);
            }
        });

        String label = fieldDAO.getLabel();

        if (fieldDAO.isRequired() && !isViewOnly) {
            label += " *";
        }


        holder.tilFieldLabel.setHint(label);
        holder.etValue.setText(getValue);

        if (getValue == null || getValue.isEmpty())  // used for 1 case... disable criteria approve button if criteria has only one required field
        {

            final Handler handler = new Handler();
            String finalGetValue = getValue;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    holder.etValue.setText("a");
                    holder.etValue.setText(finalGetValue);
                }
            }, 500);


        }


        holder.etValue.setSingleLine(false);
        if (fieldDAO.isReadOnly() || fieldDAO.getType() == 17 || fieldDAO.getType() == 18
                || fieldDAO.getType() == 19) {
            CustomLogs.displayLogs(TAG + " getTargetFieldType: " + fieldDAO.getTargetFieldType());
            //setPropertiesBasedOnType(fieldDAO.getTargetFieldType(), holder, fieldDAO);
            holder.etValue.setEnabled(false);
            if (fieldDAO.isReadOnly()) {
                holder.tilFieldDisableLabel.setVisibility(View.VISIBLE);
                holder.tilFieldLabel.setVisibility(View.GONE);
                holder.etvalueDisable.setText(getValue);
                holder.tilFieldDisableLabel.setHint(label);
            } else {
                holder.tilFieldDisableLabel.setVisibility(View.GONE);
                holder.tilFieldLabel.setVisibility(View.VISIBLE);
                holder.tilFieldLabel.setHint(label);
            }
        } else
            holder.etValue.setEnabled(true);
        int maxLength = 1000;
        switch (fieldDAO.getType()) {
            case 1:// Short EditText
                holder.etValue.setSingleLine(true);
                holder.etValue.setImeOptions(EditorInfo.IME_ACTION_DONE);
                break;
            case 2:// MultiEditText
                holder.etValue.setLines(5);

                holder.etValue.setOnTouchListener(new View.OnTouchListener() {

                    public boolean onTouch(View v, MotionEvent event) {
                        if (holder.etValue.hasFocus()) {
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_SCROLL) {
                                v.getParent().requestDisallowInterceptTouchEvent(false);
                                return true;
                            }
                        }
                        return false;
                    }
                });

                break;
            case 3:// NumbersEditText
                //  holder.etValue.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

                holder.etValue.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                holder.etValue.setKeyListener(DigitsKeyListener.getInstance("0123456789."));
                holder.etValue.setImeOptions(EditorInfo.IME_ACTION_DONE);
                break;
            case 10:// EmailEditText
                if (!isViewOnly) {
                    if (pref.getLanguage().equalsIgnoreCase("en")) {
                        if (fieldDAO.isReadOnly())
                            holder.etvalueDisable.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_icons_message_grey, 0);
                        else
                            holder.etValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_icons_message_grey, 0);
                    } else {
                        if (fieldDAO.isReadOnly())
                            holder.etvalueDisable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icons_message_grey, 0, 0, 0);
                        else
                            holder.etValue.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icons_message_grey, 0, 0, 0);
                    }
                }
                holder.etValue.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                holder.etValue.setImeOptions(EditorInfo.IME_ACTION_DONE);
                break;
            case 15:// HyperLink EditText
                if (!isViewOnly) {
                    if (pref.getLanguage().equalsIgnoreCase("en")) {
                        if (fieldDAO.isReadOnly())
                            holder.etvalueDisable.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_icons_event_link_grey, 0);
                        else
                            holder.etValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_icons_event_link_grey, 0);
                    } else {
                        if (fieldDAO.isReadOnly())
                            holder.etvalueDisable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icons_event_link_grey, 0, 0, 0);
                        else
                            holder.etValue.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icons_event_link_grey, 0, 0, 0);
                    }
                }
                holder.etValue.setSingleLine(true);
                holder.etValue.setImeOptions(EditorInfo.IME_ACTION_DONE);
                break;
            case 16:// PhoneNumber EditText
                if (!isViewOnly) {
                    if (pref.getLanguage().equalsIgnoreCase("en")) {
                        if (fieldDAO.isReadOnly())
                            holder.etvalueDisable.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_icons_phone_grey, 0);
                        else
                            holder.etValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_icons_phone_grey, 0);
                    } else {
                        if (fieldDAO.isReadOnly())
                            holder.etvalueDisable.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icons_phone_grey, 0, 0, 0);
                        else
                            holder.etValue.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icons_phone_grey, 0, 0, 0);
                    }
                }
                holder.etValue.setInputType(InputType.TYPE_CLASS_PHONE);
                holder.etValue.setImeOptions(EditorInfo.IME_ACTION_DONE);
                maxLength = 15;
                break;
        }

        //    setPropertiesBasedOnType(fieldDAO.getType(), holder, fieldDAO);

        if (fieldDAO.getMaxVal() > 0) {
            maxLength = fieldDAO.getMaxVal();
        }


        if (fieldDAO.getType() == 3)
            maxLength = 9;

        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(maxLength);
        holder.etValue.setFilters(FilterArray);

        validateForm(fieldDAO);


    }

    private void validateForm(DynamicFormSectionFieldDAO fieldDAO) {
        Validation validation = new Validation(mApplicationFieldsAdapterListener, criteriaFieldsListener,
                criteriaListDAO, fieldDAO);
        validation.setSectionListener(edisectionDetailslistener);
        validation.validateForm();
    }


}
