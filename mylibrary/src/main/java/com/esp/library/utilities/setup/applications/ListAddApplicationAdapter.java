package com.esp.library.utilities.setup.applications;

import android.app.DatePickerDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.esp.library.R;
import com.esp.library.exceedersesp.BaseActivity;
import com.esp.library.exceedersesp.ESPApplication;
import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.common.SharedPreference;
import com.esp.library.utilities.customcontrols.BodyEditText;
import com.esp.library.utilities.customcontrols.BodyText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import utilities.data.applicants.addapplication.CurrencyDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldLookupValuesDAO;
import utilities.data.applicants.dynamics.DynamicFormValuesDAO;


public class ListAddApplicationAdapter extends RecyclerView.Adapter<ListAddApplicationAdapter.ParentViewHolder> {

    private static String LOG_TAG = "ListAddApplicationAdapter";
    private List<DynamicFormSectionFieldDAO> mApplications;

    private static BaseActivity context;
    String searched_text;
    SharedPreference pref;

    CategorySelection mCat;

    private OnFieldValueChangeListener onFieldValueChangeListener;

    public interface CategorySelection {
        void StatusChange(DynamicFormSectionFieldDAO update);

        void SingleSelection(DynamicFormSectionFieldDAO update);

        void LookUp(DynamicFormSectionFieldDAO update);
    }

    public interface OnFieldValueChangeListener {
        void onFieldValuesChanged();
    }

    public void setOnFieldValueChangeListener(OnFieldValueChangeListener onFieldValueChangeListener) {
        this.onFieldValueChangeListener = onFieldValueChangeListener;
    }

    public static class ParentViewHolder extends RecyclerView.ViewHolder {
        public ParentViewHolder(View v) {
            super(v);
        }
    }

    public class ActivitiesList extends ParentViewHolder {


        LinearLayout dynamic_fields_div;
        LinearLayout dynamic_currency_div;
        TextView field_label;
        TextView error;

        public ActivitiesList(View v) {

            super(v);
            dynamic_fields_div = itemView.findViewById(R.id.dynamic_fields_div);
            dynamic_currency_div = itemView.findViewById(R.id.dynamic_currency_div);
            field_label = itemView.findViewById(R.id.field_label);
            error = itemView.findViewById(R.id.error);

        }

    }


    public ListAddApplicationAdapter(List<DynamicFormSectionFieldDAO> myDataset, BaseActivity con, String searchedText) {

        mApplications = myDataset;
        context = con;
        searched_text = searchedText;
        pref = new SharedPreference(context);

        try {
            mCat = (CategorySelection) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("lisnter" + " must implement on Activity");
        }
    }

    @Override
    public ParentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.repeater_add_application_field, parent, false);
        return new ActivitiesList(v);
    }


    @Override
    public void onBindViewHolder(final ParentViewHolder holder_parent, final int position) {

        final ActivitiesList holder = (ActivitiesList) holder_parent;
        try {
            if (pref.getLanguage().equalsIgnoreCase("ar")) {
                holder.field_label.setGravity(Gravity.RIGHT);
                holder.error.setGravity(Gravity.RIGHT);
            } else {
                holder.field_label.setGravity(Gravity.LEFT);
                holder.error.setGravity(Gravity.LEFT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.field_label.setText(mApplications.get(position).getLabel());


        if (mApplications.get(position).getError_field() != null && mApplications.get(position).getError_field().length() > 0) {
            holder.error.setVisibility(View.VISIBLE);
            holder.error.setText(mApplications.get(position).getError_field());
        } else {
            holder.error.setVisibility(View.GONE);
            holder.error.setText("");
        }

        switch (mApplications.get(position).getType()) {

            //Text = 1,
            case 1:
                //MultiLine = 2,
            case 2:
                if (!mApplications.get(position).isViewGenerated()) {
                    holder.field_label.setVisibility(View.GONE);

                    final BodyEditText text_1 = new BodyEditText(context, "");

                    text_1.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            String outputedText = editable.toString();

                            holder.error.setVisibility(View.GONE);
                            holder.field_label.setVisibility(View.GONE);

                            String error = "";
                            mApplications.get(position).setValidate(false);

                            if (!outputedText.isEmpty()) {

                                int minValueCount = mApplications.get(position).getMinVal();
                                int maxValueCount = mApplications.get(position).getMaxVal();

                                holder.error.setText("");

                                if (minValueCount > 0 && maxValueCount > 0) {

                                    if (outputedText.length() < minValueCount || outputedText.length() > maxValueCount) {
                                        error = "Value must be between " + mApplications.get(position).getMinVal() + " and " + mApplications.get(position).getMaxVal() + " characters";
                                    }

                                } else if (minValueCount > 0) {

                                    if (outputedText.length() < minValueCount) {
                                        error = "Value must be greater than " + minValueCount + " characters";
                                    }
                                } else if (maxValueCount > 0) {

                                    if (outputedText.length() > maxValueCount) {
                                        error = "Value must be less than " + maxValueCount + " characters";
                                    }
                                }

                                holder.field_label.setVisibility(View.VISIBLE);

                            } else {

                                /*error = "Required";

                                holder.field_label.setVisibility(View.VISIBLE);*/
                            }

                            if (error.length() > 0) {
                                holder.error.setText(error);
                                holder.error.setVisibility(View.VISIBLE);
                            } else {

                                mApplications.get(position).setValue(outputedText);
                                mApplications.get(position).setValidate(true);

                                if (mApplications.get(position).isRequired() && outputedText.isEmpty())
                                    mApplications.get(position).setValidate(false);
                            }

                            if (onFieldValueChangeListener != null)
                                onFieldValueChangeListener.onFieldValuesChanged();


                            /*if (outputedText != null && outputedText.length() > 0) {


                                String error = "";


                                if (mApplications.get(position).getMinVal() > 0 && mApplications.get(position).getMaxVal() > 0) {
                                    if (outputedText.length() < mApplications.get(position).getMinVal() || outputedText.length() > mApplications.get(position).getMaxVal()) {
                                        error = "Value must be between " + mApplications.get(position).getMinVal() + " and " + mApplications.get(position).getMaxVal() + " characters";
                                    }
                                    if (error.length() > 0) {
                                        mApplications.get(position).setError_field(error);
                                        holder.error.setText(error);
                                        holder.error.setVisibility(View.VISIBLE);

                                    } else {
                                        mApplications.get(position).setError_field(null);
                                        holder.error.setText("");
                                        holder.error.setVisibility(View.GONE);
                                    }
                                }


                                if (mApplications.get(position).isRequired()) {

                                    if (outputedText.length() > 0) {
                                        mApplications.get(position).setError_field(error);
                                        holder.error.setText(error);
                                        holder.error.setVisibility(View.VISIBLE);
                                    } else {
                                        mApplications.get(position).setError_field(null);
                                        holder.error.setText("");
                                        holder.error.setVisibility(View.GONE);
                                    }

                                }

                                holder.field_label.setVisibility(View.VISIBLE);

                                DynamicFormValuesDAO post = new DynamicFormValuesDAO();
                                post.setSectionCustomFieldId(mApplications.get(position).getSectionCustomFieldId());
                                post.setSectionId(mApplications.get(position).getObjectId());
                                post.setValue(text_1.getText().toString());
                                mApplications.get(position).setValue(text_1.getText().toString());
                                mApplications.get(position).setPost(post);

                                mApplications.get(position).setError_field(null);
                                holder.error.setText("");
                                holder.error.setVisibility(View.GONE);


                            } else {

                                if (mApplications.get(position).getPost() != null) {
                                    mApplications.get(position).setPost(null);
                                }

                                holder.field_label.setVisibility(View.GONE);
                            }*/
                        }
                    });

                    if (mApplications.get(position).getValue() != null && mApplications.get(position).getValue().length() > 0) {
                        text_1.setText(mApplications.get(position).getValue());
                        holder.field_label.setVisibility(View.VISIBLE);
                    } else {
                        text_1.setText("");
                    }

                    if (mApplications.get(position).isRequired()) {
                        text_1.setHint(mApplications.get(position).getLabel() + " *");
                    } else {
                        text_1.setHint(mApplications.get(position).getLabel());
                    }

                    holder.field_label.setText(text_1.getHint().toString());

                    text_1.setBackgroundDrawable(null);
                    text_1.setFocusable(true);
                    text_1.setTextSize(16f);

                    if (mApplications.get(position).getType() == 1) {
                        text_1.setSingleLine(true);
                        text_1.setLines(1);
                        text_1.setHorizontallyScrolling(true);
                        text_1.setInputType(InputType.TYPE_CLASS_TEXT);
                    } else if (mApplications.get(position).getType() == 2) {
                        text_1.setSingleLine(false);
                        text_1.setLines(5);
                        text_1.setHorizontallyScrolling(false);
                        text_1.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    }
                    //text_1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_text, 0);

                    if (mApplications.get(position).getMaxVal() > 0) {
                        InputFilter[] FilterArray = new InputFilter[1];
                        FilterArray[0] = new InputFilter.LengthFilter(mApplications.get(position).getMaxVal());
                        text_1.setFilters(FilterArray);
                    }


                    LinearLayout.LayoutParams params_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params_1.setMargins(0, 0, 0, 0);
                    text_1.setLayoutParams(params_1);

                    holder.dynamic_fields_div.addView(text_1);

                    //mApplications.get(position).setViewGenerated(true);
                }

                break;

            //MultiLine = 2,
            /*case 2:

                if (!mApplications.get(position).isViewGenerated()) {
                    holder.field_label.setVisibility(View.GONE);
                    final BodyEditText text_2 = new BodyEditText(context, "");
                    if (mApplications.get(position).getValue() != null && mApplications.get(position).getValue().length() > 0) {
                        text_2.setText(Html.fromHtml(mApplications.get(position).getValue()));
                        holder.field_label.setVisibility(View.VISIBLE);
                    }
                    if (mApplications.get(position).isRequired()) {
                        text_2.setHint(mApplications.get(position).getLabel() + " *");
                    } else {
                        text_2.setHint(mApplications.get(position).getLabel());
                    }

                    text_2.setBackgroundDrawable(null);
                    text_2.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    text_2.setFocusable(true);
                    text_2.setTextSize(16f);
                    text_2.setSingleLine(false);
                    //text_2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_text, 0);
                    if (mApplications.get(position).getMinVal() > 0 && mApplications.get(position).getMaxVal() > 0) {
                        //	text.setFilters(new InputFilter[]{ new InputFilterMinMax(mApplications.get(position).getMinVal()+"", mApplications.get(position).getMaxVal()+"")});
                    }
                    LinearLayout.LayoutParams params_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params_1.setMargins(0, 0, 0, 0);
                    text_2.setLayoutParams(params_1);
                    holder.dynamic_fields_div.addView(text_2);

                    text_2.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            String outputedText = editable.toString();
                            if (outputedText != null && outputedText.length() > 0) {

                                int entered_value = 0;
                                String error = "";

                                try {
                                    entered_value = Integer.parseInt(outputedText);
                                } catch (Exception e) {
                                }


                                if (mApplications.get(position).getMinVal() > 0 && mApplications.get(position).getMaxVal() > 0) {

                                    if (outputedText.length() < mApplications.get(position).getMinVal() || outputedText.length() > mApplications.get(position).getMaxVal()) {
                                        error = "Value must be between " + mApplications.get(position).getMinVal() + " and " + mApplications.get(position).getMaxVal() + " characters";
                                    }
                                    if (error.length() > 0) {
                                        mApplications.get(position).setError_field(error);
                                        holder.error.setText(error);
                                        holder.error.setVisibility(View.VISIBLE);
                                    } else {
                                        mApplications.get(position).setError_field(null);
                                        holder.error.setText("");
                                        holder.error.setVisibility(View.GONE);
                                    }
                                }


                                holder.field_label.setVisibility(View.VISIBLE);

                                DynamicFormValuesDAO post = new DynamicFormValuesDAO();
                                post.setSectionCustomFieldId(mApplications.get(position).getSectionCustomFieldId());
                                post.setSectionId(mApplications.get(position).getObjectId());
                                post.setValue(outputedText);
                                mApplications.get(position).setValue(outputedText);
                                mApplications.get(position).setPost(post);

                                mApplications.get(position).setError_field(null);
                                holder.error.setText("");
                                holder.error.setVisibility(View.GONE);

                            } else {
                                if (mApplications.get(position).getPost() != null) {
                                    mApplications.get(position).setPost(null);

                                }
                                holder.field_label.setVisibility(View.GONE);
                            }
                        }
                    });

                    //mApplications.get(position).setViewGenerated(true);
                }

                break;*/

            //	Number = 3,
            case 3:

                if (!mApplications.get(position).isViewGenerated()) {
                    holder.field_label.setVisibility(View.GONE);

                    final BodyEditText text_3 = new BodyEditText(context, "");
                    if (mApplications.get(position).getValue() != null && mApplications.get(position).getValue().length() > 0) {
                        text_3.setText(Shared.getInstance().getUAEAmountFromDecimal(Double.parseDouble(mApplications.get(position).getValue())));
                        holder.field_label.setVisibility(View.VISIBLE);
                    }
                    if (mApplications.get(position).isRequired()) {
                        text_3.setHint(mApplications.get(position).getLabel() + " *");
                    } else {
                        text_3.setHint(mApplications.get(position).getLabel());
                    }
                    text_3.setSingleLine(true);
                    text_3.setFocusable(true);
                    text_3.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    text_3.setTextSize(16f);
                    //text_3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_text, 0);
                    text_3.setBackgroundDrawable(null);
                    text_3.setInputType(InputType.TYPE_CLASS_NUMBER);
                    if (mApplications.get(position).getMinVal() > 0 && mApplications.get(position).getMaxVal() > 0) {
                        //	text.setFilters(new InputFilter[]{ new InputFilterMinMax(mApplications.get(position).getMinVal()+"", mApplications.get(position).getMaxVal()+"")});
                        text_3.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mApplications.get(position).getMaxVal())});
                    }
                    LinearLayout.LayoutParams params_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params_1.setMargins(0, 0, 0, 0);
                    text_3.setLayoutParams(params_1);
                    holder.dynamic_fields_div.addView(text_3);

                    text_3.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            String outputedText = editable.toString();
                            if (outputedText != null && outputedText.length() > 0) {

                                int entered_value = 0;
                                String error = "";

                                try {
                                    entered_value = Integer.parseInt(outputedText);
                                } catch (Exception e) {
                                }


                                if (mApplications.get(position).getMinVal() > 0 && mApplications.get(position).getMaxVal() > 0) {

                                    if (entered_value < mApplications.get(position).getMinVal() || entered_value > mApplications.get(position).getMaxVal()) {
                                        error = "Value must be between " + mApplications.get(position).getMinVal() + " and " + mApplications.get(position).getMaxVal();
                                    }
                                    if (error.length() > 0) {
                                        mApplications.get(position).setError_field(error);
                                        holder.error.setText(error);
                                        holder.error.setVisibility(View.VISIBLE);
                                    } else {
                                        mApplications.get(position).setError_field(null);
                                        holder.error.setText("");
                                        holder.error.setVisibility(View.GONE);
                                    }
                                }


                                holder.field_label.setVisibility(View.VISIBLE);

                                DynamicFormValuesDAO post = new DynamicFormValuesDAO();
                                post.setSectionCustomFieldId(mApplications.get(position).getSectionCustomFieldId());
                                post.setSectionId(mApplications.get(position).getObjectId());
                                post.setValue(text_3.getText().toString());
                                mApplications.get(position).setValue(text_3.getText().toString());
                                mApplications.get(position).setPost(post);

                                mApplications.get(position).setError_field(null);
                                holder.error.setText("");
                                holder.error.setVisibility(View.GONE);

                            } else {

                                if (mApplications.get(position).getPost() != null) {
                                    mApplications.get(position).setPost(null);
                                }
                                holder.field_label.setVisibility(View.GONE);
                            }
                        }
                    });


                    //mApplications.get(position).setViewGenerated(true);
                }

                break;

            //DateTime = 4,
            case 4:

                if (!mApplications.get(position).isViewGenerated()) {
                    holder.field_label.setVisibility(View.GONE);

                    final BodyText text_4 = new BodyText(context, "");
                    if (mApplications.get(position).getValue() != null && mApplications.get(position).getValue().length() > 0) {
                        text_4.setText(Shared.getInstance().getDisplayDate(context, mApplications.get(position).getValue(), false));
                        holder.field_label.setVisibility(View.VISIBLE);

                    }
                    //text_4.setHint(mApplications.get(position).getLabel());

                    if (mApplications.get(position).isRequired()) {
                        text_4.setHint(mApplications.get(position).getLabel() + " *");
                    } else {
                        text_4.setHint(mApplications.get(position).getLabel());
                    }


                    text_4.setSingleLine(true);
                    text_4.setTextSize(16f);
                    text_4.setFocusable(false);
                    text_4.setTextColor(context.getResources().getColor(R.color.dark_grey));
                    text_4.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_txt_date, 0);
                    text_4.setBackgroundDrawable(null);


                    if (mApplications.get(position).getMinVal() > 0 && mApplications.get(position).getMaxVal() > 0) {
                        //	text.setFilters(new InputFilter[]{ new InputFilterMinMax(mApplications.get(position).getMinVal()+"", mApplications.get(position).getMaxVal()+"")});
                    }

                    LinearLayout.LayoutParams params_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params_1.setMargins(10, 20, 10, 20);
                    text_4.setLayoutParams(params_1);

                    text_4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final Calendar calendar = Shared.getInstance().getTodayCalendar();
                            final int year = calendar.get(Calendar.YEAR);
                            final int month = calendar.get(Calendar.MONTH);
                            final int day = calendar.get(Calendar.DAY_OF_MONTH);
                            final int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                            final int mMinute = calendar.get(Calendar.MINUTE);

                            new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int year_p, int month_p, int day_p) {
                                    //text_4.setText(Shared.getInstance().AddZero((month_p+1))+"/"+Shared.getInstance().AddZero((day_p))+"/"+ year_p);
                                   /* text_4.setTag(year_p + "-" + Shared.getInstance().AddZero((month_p + 1)) + "-" + Shared.getInstance().AddZero(day_p));

                                    String selected_value = text_4.getTag() + "T" + Shared.getInstance().AddZero(0) + ":" + Shared.getInstance().AddZero(0) + ":00Z";
                                    */

                                    String formatedDate=Shared.getInstance().getDatePickerGMTDate(datePicker);
                                    text_4.setText(Shared.getInstance().getDisplayDate(context, formatedDate, false));
                                    holder.field_label.setVisibility(View.VISIBLE);

                                    DynamicFormValuesDAO post = new DynamicFormValuesDAO();
                                    post.setSectionCustomFieldId(mApplications.get(position).getSectionCustomFieldId());
                                    post.setSectionId(mApplications.get(position).getObjectId());
                                    post.setValue(formatedDate);
                                    mApplications.get(position).setValue(formatedDate);
                                    mApplications.get(position).setPost(post);

                                    mApplications.get(position).setError_field(null);
                                    holder.error.setText("");
                                    holder.error.setVisibility(View.GONE);

                                    /*new TimePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                                        @Override
                                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                            String selected_value = ((String) text_4.getTag()) + "T" + Shared.getInstance().AddZero(hourOfDay) + ":" + Shared.getInstance().AddZero(minute) + ":00Z";
                                            text_4.setText(Shared.getInstance().getDisplayDate(selected_value, true));
                                            holder.field_label.setVisibility(View.VISIBLE);

                                            DynamicFormValuesDAO post = new DynamicFormValuesDAO();
                                            post.setSectionCustomFieldId(mApplications.get(position).getSectionCustomFieldId());
                                            post.setSectionId(mApplications.get(position).getObjectId());
                                            post.setValue(selected_value);
                                            mApplications.get(position).setValue(selected_value);
                                            mApplications.get(position).setPost(post);

                                            mApplications.get(position).setError_field(null);
                                            holder.error.setText("");
                                            holder.error.setVisibility(View.GONE);

                                        }


                                    }, mHour, mMinute, true).show();*/


                                }


                            }, year, month, day).show();


                        }
                    });

                    holder.dynamic_fields_div.addView(text_4);


                    //mApplications.get(position).setViewGenerated(true);
                }

                break;

            //SingleSelection = 5,
            case 5:


                if (!mApplications.get(position).isViewGenerated()) {

					/*
					RadioGroup ll = new RadioGroup(context);
					ll.setOrientation(LinearLayout.HORIZONTAL);

					if(mApplications.get(position).getLookupValues()!=null){
						if(mApplications.get(position).getLookupValues()!=null && mApplications.get(position).getLookupValues().size()>0){

							for(DynamicFormSectionFieldLookupValuesDAO df: mApplications.get(position).getLookupValues()){
								RadioButton rdbtn = new RadioButton(context);
								rdbtn.setId(df.getId());
								rdbtn.setText(" "+ df.getLabel()+" ");
								rdbtn.setTextSize(16f);
								rdbtn.setButtonDrawable(context.getResources().getDrawable(R.drawable.radio_selector));

								if(df.isSelected()){
									rdbtn.setChecked(true);

									DynamicFormValuesDAO post = new DynamicFormValuesDAO();
									post.setSectionCustomFieldId(mApplications.get(position).getSectionCustomFieldId());
									post.setSectionId(mApplications.get(position).getObjectId());
									post.setValue(df.getId()+"");
									mApplications.get(position).setValue(df.getId()+"");
									mApplications.get(position).setPost(post);

								}
								rdbtn.setOnClickListener(new View.OnClickListener() {
									@Override
									public void onClick(View view) {
										int SelecteId = 0;
										SelecteId = ((RadioButton)view).getId();

										DynamicFormValuesDAO post = new DynamicFormValuesDAO();
										post.setSectionCustomFieldId(mApplications.get(position).getSectionCustomFieldId());
										post.setSectionId(mApplications.get(position).getObjectId());
										post.setValue(SelecteId+"");
										mApplications.get(position).setValue(SelecteId+"");

										if(mApplications.get(position).getPost()!=null){
											mApplications.get(position).setPost(null);
										}

										mApplications.get(position).setPost(post);
									}
								});
								ll.addView(rdbtn);
							}
						}
					}

					holder.dynamic_fields_div.addView(ll);

*/

                    holder.field_label.setVisibility(View.GONE);

                    final BodyText text_5 = new BodyText(context, "");

                    //text_5.setHint(mApplications.get(position).getLabel());

                    if (mApplications.get(position).isRequired()) {
                        text_5.setHint(mApplications.get(position).getLabel() + " *");
                    } else {
                        text_5.setHint(mApplications.get(position).getLabel());
                    }

                    text_5.setSingleLine(true);
                    text_5.setFocusable(false);
                    text_5.setTextSize(16f);
                    text_5.setTextColor(context.getResources().getColor(R.color.dark_grey));
                    text_5.setBackgroundDrawable(null);
                    text_5.setInputType(InputType.TYPE_CLASS_TEXT);
                    text_5.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);

                    String values = "";
                    if (mApplications.get(position).getLookupValues() != null && mApplications.get(position).getLookupValues().size() > 0) {
                        for (DynamicFormSectionFieldLookupValuesDAO lookup : mApplications.get(position).getLookupValues()) {
                            if (lookup.isSelected()) {
                                values = lookup.getLabel();
                                holder.field_label.setVisibility(View.VISIBLE);

                                DynamicFormValuesDAO post = new DynamicFormValuesDAO();
                                post.setSectionCustomFieldId(mApplications.get(position).getSectionCustomFieldId());
                                post.setSectionId(mApplications.get(position).getObjectId());
                                post.setValue(lookup.getId() + "");
                                if (mApplications.get(position).getPost() != null) {
                                    mApplications.get(position).setPost(null);
                                }
                                mApplications.get(position).setPost(post);

                                mApplications.get(position).setError_field(null);
                                holder.error.setText("");
                                holder.error.setVisibility(View.GONE);


                                break;
                            }
                        }
                    }

                    if (values.length() > 0) {
                        text_5.setText(values);
                    }

                    LinearLayout.LayoutParams params_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params_1.setMargins(0, 15, 10, 10);
                    text_5.setLayoutParams(params_1);


                    text_5.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCat.SingleSelection(mApplications.get(position));
                        }
                    });
                    holder.dynamic_fields_div.removeAllViews();
                    holder.dynamic_fields_div.addView(text_5);

                    //mApplications.get(position).setViewGenerated(true);
                }

/*

				if(!mApplications.get(position).isViewGenerated()){
					final Spinner singleSelection = new Spinner(context);
					ArrayAdapter<String> adapter = null;
					String[] values = null;
					if(mApplications.get(position).getLookupValues()!=null){
						if(mApplications.get(position).getLookupValues()!=null && mApplications.get(position).getLookupValues().size()>0){

							values = new String[mApplications.get(position).getLookupValues().size()];

							int i=0;
							for(DynamicFormSectionFieldLookupValuesDAO df: mApplications.get(position).getLookupValues()){
								values[i] = df.getLabel();
								i++;
							}
						}
					}

					if(values!=null){
						adapter = new ArrayAdapter<String>(context, R.layout.spinner_text, values);
					}


					if(adapter!=null){
						singleSelection.setAdapter(adapter);
						if(mApplications.get(position).getValue()!=null && mApplications.get(position).getValue().length()>0){
							singleSelection.setSelection(Integer.parseInt(mApplications.get(position).getValue()));
						}

					}

				*/
/*	if(singleSelection!=null && singleSelection.getSelectedItem()!=null){
						mApplications.get(position).setSingleSelectedValue(singleSelection.getSelectedItem().toString());
					}
*//*


					LinearLayout.LayoutParams params_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
					params_1.setMargins(10, 0, 10, 0);
					singleSelection.setLayoutParams(params_1);

					singleSelection.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
							String selectValue = singleSelection.getSelectedItem().toString();
							mApplications.get(position).setSingleSelectedValue(selectValue);

							*/
/*	if(mApplications.get(position).getLookupValues()!=null && mApplications.get(position).getLookupValues().size()>0){

								int SelecteId = 0;
								for(DynamicFormSectionFieldLookupValuesDAO df: mApplications.get(position).getLookupValues()){
									if(df.getLabel().toLowerCase().equals(selectValue.toLowerCase())){
										SelecteId = df.getId();

										DynamicFormValuesDAO post = new DynamicFormValuesDAO();
										post.setSectionCustomFieldId(mApplications.get(position).getSectionCustomFieldId());
										post.setSectionId(mApplications.get(position).getObjectId());
										post.setValue(SelecteId+"");
										mApplications.get(position).setValue(SelecteId+"");
										mApplications.get(position).setPost(post);

										break;
									}
								}

							}*//*


						}
					});


					holder.dynamic_fields_div.addView(singleSelection);

					//mApplications.get(position).setViewGenerated(true);
				}

*/


                break;

            //MultiSelection = 6,
            case 6:

                holder.field_label.setVisibility(View.VISIBLE);
                HorizontalScrollView horizontalScrollView = new HorizontalScrollView(context);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(0, 15, 10, 10);
                horizontalScrollView.setLayoutParams(layoutParams);
                LinearLayout linearLayout = new LinearLayout(context);
                LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.setLayoutParams(linearParams);
                horizontalScrollView.addView(linearLayout);


                if (!mApplications.get(position).isViewGenerated()) {
                    if (mApplications.get(position).getLookupValues() != null) {
                        if (mApplications.get(position).getLookupValues().size() > 0 && !mApplications.get(position).isViewGenerated()) {


                            for (DynamicFormSectionFieldLookupValuesDAO df : mApplications.get(position).getLookupValues()) {

                                final AppCompatCheckBox ch = new AppCompatCheckBox(context);
                                ch.setText(df.getLabel());
                                ch.setFocusable(false);
                                ch.setTextSize(16f);
                                if (df.isSelected()) {
                                    ch.setChecked(true);
                                } else {
                                    ch.setChecked(false);
                                }

                                ch.setTextColor(context.getResources().getColor(R.color.dark_grey));
                                ch.setButtonDrawable(context.getResources().getDrawable(R.drawable.checkbox_selector));
                                ch.setTag(df.getId());

                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                params.setMargins(0, 5, 15, 5);
                                ch.setLayoutParams(params);

                                ch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton checkbox, boolean b) {

                                        if (checkbox.isChecked()) {
                                            if (mApplications.get(position).getLookupValues() != null && mApplications.get(position).getLookupValues().size() > 0) {
                                                for (DynamicFormSectionFieldLookupValuesDAO df : mApplications.get(position).getLookupValues()) {
                                                    if ((int) checkbox.getTag() == df.getId()) {
                                                        df.setSelected(true);
                                                        break;
                                                    }
                                                }
                                            }
                                        } else {
                                            if (mApplications.get(position).getLookupValues() != null && mApplications.get(position).getLookupValues().size() > 0) {
                                                for (DynamicFormSectionFieldLookupValuesDAO df : mApplications.get(position).getLookupValues()) {
                                                    if ((int) checkbox.getTag() == df.getId()) {
                                                        df.setSelected(false);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                });

                                linearLayout.addView(ch);


                            }


                        }

						/*DynamicFormValuesDAO post = new DynamicFormValuesDAO();
						post.setSectionCustomFieldId(mApplications.get(position).getSectionCustomFieldId());
						post.setSectionId(mApplications.get(position).getObjectId());
						post.setValue("501,502");
						mApplications.get(position).setPost(post);*/


                        if (horizontalScrollView != null) {
                            holder.dynamic_fields_div.addView(horizontalScrollView);
                        }


                        //mApplications.get(position).setViewGenerated(true);
                    }


                }


                break;

            //Attachment = 7,
            case 7:
                if (!mApplications.get(position).isViewGenerated()) {
                    holder.field_label.setVisibility(View.GONE);
                    holder.dynamic_fields_div.removeAllViews();

                    BodyText text_7 = new BodyText(context, "");
                    text_7.setFocusable(false);
                    //text_7.setHint(mApplications.get(position).getLabel());
                    if (mApplications.get(position).isRequired()) {
                        text_7.setHint(mApplications.get(position).getLabel() + " *");
                    } else {
                        text_7.setHint(mApplications.get(position).getLabel());
                    }
                    text_7.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_txt_attach, 0);

                    if (mApplications.get(position).getDetails() != null && mApplications.get(position).getDetails().getName() != null && mApplications.get(position).getDetails().getName().length() > 0) {
                        text_7.setText(mApplications.get(position).getDetails().getName());
                        text_7.setTextSize(16f);

                        text_7.setTextColor(context.getResources().getColor(R.color.dark_grey));
                        holder.field_label.setVisibility(View.VISIBLE);
                    }

                    text_7.setSingleLine(true);
                    text_7.setFocusable(true);
                    text_7.setBackgroundDrawable(null);

                    if (mApplications.get(position).getMinVal() > 0 && mApplications.get(position).getMaxVal() > 0) {
                        //	text.setFilters(new InputFilter[]{ new InputFilterMinMax(mApplications.get(position).getMinVal()+"", mApplications.get(position).getMaxVal()+"")});
                    }

                    LinearLayout.LayoutParams params_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params_1.setMargins(10, 20, 10, 20);
                    text_7.setLayoutParams(params_1);

                    text_7.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCat.StatusChange(mApplications.get(position));
                        }
                    });


                    holder.dynamic_fields_div.addView(text_7);

                    //mApplications.get(position).setViewGenerated(true);
                }


                break;

            //Date = 8,
            case 8:
                if (!mApplications.get(position).isViewGenerated()) {
                    holder.field_label.setVisibility(View.GONE);
                    final BodyText text_8 = new BodyText(context, "");
                    //text_8.setHint(mApplications.get(position).getLabel());
                    if (mApplications.get(position).isRequired()) {
                        text_8.setHint(mApplications.get(position).getLabel() + " *");
                    } else {
                        text_8.setHint(mApplications.get(position).getLabel());
                    }

                    if (mApplications.get(position).getValue() != null && mApplications.get(position).getValue().length() > 0) {
                        text_8.setText(Shared.getInstance().getDisplayDate(context, mApplications.get(position).getValue(), false));
                        text_8.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_txt_date, 0);
                        holder.field_label.setVisibility(View.VISIBLE);

                    }


                    text_8.setSingleLine(true);
                    text_8.setTextColor(context.getResources().getColor(R.color.dark_grey));
                    text_8.setTextSize(16f);
                    text_8.setFocusable(false);
                    text_8.setBackgroundDrawable(null);
                    if (mApplications.get(position).getMinVal() > 0 && mApplications.get(position).getMaxVal() > 0) {
                        //	text.setFilters(new InputFilter[]{ new InputFilterMinMax(mApplications.get(position).getMinVal()+"", mApplications.get(position).getMaxVal()+"")});
                    }

                    LinearLayout.LayoutParams params_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params_1.setMargins(10, 20, 10, 20);
                    text_8.setLayoutParams(params_1);


                    text_8.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            final Calendar calendar = Shared.getInstance().getTodayCalendar();
                            final int year = calendar.get(Calendar.YEAR);
                            final int month = calendar.get(Calendar.MONTH);
                            final int day = calendar.get(Calendar.DAY_OF_MONTH);

                            new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int year_p, int month_p, int day_p) {

                                    //String selected_value = year_p + "-" + Shared.getInstance().AddZero((month_p + 1)) + "-" + Shared.getInstance().AddZero(day_p) + "T" + Shared.getInstance().GetCurrenTime() + "Z";
                                    String formatedDate=Shared.getInstance().getDatePickerGMTDate(datePicker);


                                    text_8.setText(Shared.getInstance().getDisplayDate(context, formatedDate, false));
                                    holder.field_label.setVisibility(View.VISIBLE);

                                    DynamicFormValuesDAO post = new DynamicFormValuesDAO();
                                    post.setSectionCustomFieldId(mApplications.get(position).getSectionCustomFieldId());
                                    post.setSectionId(mApplications.get(position).getObjectId());
                                    post.setValue(formatedDate);
                                    mApplications.get(position).setValue(formatedDate);
                                    mApplications.get(position).setPost(post);

                                    mApplications.get(position).setError_field(null);
                                    holder.error.setText("");
                                    holder.error.setVisibility(View.GONE);

                                }


                            }, year, month, day).show();


                        }
                    });
                    holder.dynamic_fields_div.addView(text_8);

                    //mApplications.get(position).setViewGenerated(true);
                }

                break;

            //Separator = 9,
            case 9:
                break;
            //Email,RadioButtons = 10,
            case 10:
                if (!mApplications.get(position).isViewGenerated()) {
                    holder.field_label.setVisibility(View.GONE);

                    final BodyEditText text_1 = new BodyEditText(context, "");
                    if (mApplications.get(position).getValue() != null && mApplications.get(position).getValue().length() > 0) {
                        text_1.setText(mApplications.get(position).getValue());
                        holder.field_label.setVisibility(View.VISIBLE);
                    }

                    if (mApplications.get(position).isRequired()) {
                        text_1.setHint(mApplications.get(position).getLabel() + " *");
                    } else {
                        text_1.setHint(mApplications.get(position).getLabel());
                    }


                    text_1.setSingleLine(true);
                    text_1.setFocusable(true);
                    text_1.setTextSize(16f);
                    text_1.setBackgroundDrawable(null);
                    text_1.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                    //text_1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_edit_text, 0);


                    LinearLayout.LayoutParams params_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params_1.setMargins(0, 0, 0, 0);
                    text_1.setLayoutParams(params_1);


                    holder.dynamic_fields_div.addView(text_1);

                    text_1.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            String outputedText = editable.toString();
                            if (outputedText != null && outputedText.length() > 0) {

                                String error = "";

                                if (!Shared.getInstance().isValidEmailAddress(outputedText)) {
                                    error = context.getString(R.string.invalidemail);
                                }

                                if (error.length() > 0) {
                                    mApplications.get(position).setError_field(error);
                                    holder.error.setText(error);
                                    holder.error.setVisibility(View.VISIBLE);

                                } else {
                                    mApplications.get(position).setError_field(null);
                                    holder.error.setText("");
                                    holder.error.setVisibility(View.GONE);
                                }

                                if (mApplications.get(position).isRequired()) {

                                    if (outputedText.length() > 0) {
                                        mApplications.get(position).setError_field(error);
                                        holder.error.setText(error);
                                        holder.error.setVisibility(View.VISIBLE);
                                    } else {
                                        mApplications.get(position).setError_field(null);
                                        holder.error.setText("");
                                        holder.error.setVisibility(View.GONE);
                                    }

                                }

                                holder.field_label.setVisibility(View.VISIBLE);

                                DynamicFormValuesDAO post = new DynamicFormValuesDAO();
                                post.setSectionCustomFieldId(mApplications.get(position).getSectionCustomFieldId());
                                post.setSectionId(mApplications.get(position).getObjectId());
                                post.setValue(text_1.getText().toString());
                                mApplications.get(position).setValue(text_1.getText().toString());
                                mApplications.get(position).setPost(post);

                                mApplications.get(position).setError_field(null);
                                holder.error.setText("");
                                holder.error.setVisibility(View.GONE);


                            } else {

                                if (mApplications.get(position).getPost() != null) {
                                    mApplications.get(position).setPost(null);
                                } else {
                                    mApplications.get(position).setValue("");
                                }

                                holder.field_label.setVisibility(View.GONE);
                            }
                        }
                    });

                    //mApplications.get(position).setViewGenerated(true);
                }

                break;
            //Currency = 11,
            case 11:

                if (!mApplications.get(position).isViewGenerated()) {
                    holder.field_label.setVisibility(View.GONE);
                    final BodyEditText text_11 = new BodyEditText(context, "");

					/*if(mApplications.get(position).getValue()!=null && mApplications.get(position).getValue().length()>0){
						text_11.setText(Shared.getInstance().getUAEAmountFromDecimal(Double.parseDouble(mApplications.get(position).getValue())));
						holder.field_label.setVisibility(View.VISIBLE);
					}*/

                    //text_11.setHint(mApplications.get(position).getLabel());
                    if (mApplications.get(position).isRequired()) {
                        text_11.setHint(mApplications.get(position).getLabel() + " *");
                    } else {
                        text_11.setHint(mApplications.get(position).getLabel());
                    }
                    text_11.setSingleLine(true);
                    text_11.setFocusable(true);
                    text_11.setTextSize(16f);
                    text_11.setBackgroundDrawable(null);
                    text_11.setInputType(InputType.TYPE_CLASS_NUMBER);
                    if (mApplications.get(position).getMinVal() > 0 && mApplications.get(position).getMaxVal() > 0) {
                        //	text.setFilters(new InputFilter[]{ new InputFilterMinMax(mApplications.get(position).getMinVal()+"", mApplications.get(position).getMaxVal()+"")});
                    }

                    LinearLayout.LayoutParams params_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params_1.setMargins(10, 0, 10, 0);
                    text_11.setLayoutParams(params_1);

                    holder.dynamic_fields_div.addView(text_11);


                    //////////CURRENCY////////////
                    ArrayAdapter<String> adapter = null;
                    String[] values = null;
                    List<CurrencyDAO> final_values = null;
                    List<String> allowed_array = null;
                    if (ESPApplication.getInstance().getCurrencies() != null) {
                        if (ESPApplication.getInstance().getCurrencies() != null && ESPApplication.getInstance().getCurrencies().size() > 0) {
                            final_values = new ArrayList<CurrencyDAO>();
                            values = new String[ESPApplication.getInstance().getCurrencies().size()];

                            int i = 0;

                            String[] splitedId = mApplications.get(position).getAllowedValuesCriteria().split(",");

                            if (splitedId != null) {
                                allowed_array = new ArrayList<String>();
                                for (int c = 0; c < splitedId.length; c++) {
                                    allowed_array.add(splitedId[c]);
                                }
                            }

                            if (allowed_array != null && allowed_array.size() > 0) {
                                values = new String[allowed_array.size()];
                            }

                            for (CurrencyDAO df : ESPApplication.getInstance().getCurrencies()) {

                                String Id = df.getId() + "";
                                if (allowed_array != null && allowed_array.size() > 0) {
                                    if (allowed_array.contains(Id)) {
                                        final_values.add(df);
                                    }
                                } else {
                                    values[i] = df.getCode();
                                    i++;
                                }


                            }
                        }
                    }

                    if (final_values != null && final_values.size() > 0) {
                        values = new String[final_values.size()];
                        int i = 0;
                        for (CurrencyDAO currencyDAO : final_values) {
                            values[i] = currencyDAO.getCode();
                            i++;
                        }
                    }


                    if (values != null && values.length > 0) {
                        adapter = new ArrayAdapter<String>(context, R.layout.spinner_text, values);
                    }


                    final Spinner singleSelection_currency = new Spinner(context);
                    if (adapter != null) {
                        singleSelection_currency.setAdapter(adapter);
                        if (mApplications.get(position).getValue() != null && mApplications.get(position).getValue().length() > 0) {

                            if (mApplications.get(position).getValue() != "-1") {
                                //singleSelection_currency.setSelection(Integer.parseInt(mApplications.get(position).getValue()));
                            }

                        }

                    }


                    LinearLayout.LayoutParams params_2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params_2.setMargins(10, 0, 10, 0);
                    singleSelection_currency.setLayoutParams(params_2);

                    holder.dynamic_currency_div.addView(singleSelection_currency);


                    text_11.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            String outputedText = editable.toString();

                            if (outputedText != null && outputedText.length() > 0) {

                                holder.field_label.setVisibility(View.VISIBLE);

                                if (singleSelection_currency != null) {

                                    String selectValue = singleSelection_currency.getSelectedItem().toString();

                                    int entered_value = 0;
                                    String error = "";

                                    try {
                                        entered_value = Integer.parseInt(outputedText);
                                    } catch (Exception e) {
                                    }

                                    if (mApplications.get(position).getMinVal() > 0 && mApplications.get(position).getMaxVal() > 0) {

                                        if (entered_value < mApplications.get(position).getMinVal() || entered_value > mApplications.get(position).getMaxVal()) {
                                            error = context.getString(R.string.valuebetween) + " " + mApplications.get(position).getMinVal() + " " + context.getString(R.string.and) + " " + mApplications.get(position).getMaxVal();
                                        }
                                        if (error.length() > 0) {
                                            mApplications.get(position).setError_field(error);
                                            holder.error.setText(error);
                                            holder.error.setVisibility(View.VISIBLE);
                                        } else {
                                            mApplications.get(position).setError_field(null);
                                            holder.error.setText("");
                                            holder.error.setVisibility(View.GONE);
                                        }
                                    }


                                    int SelecteId = 0;

                                    if (selectValue != null && selectValue.length() > 0) {
                                        for (CurrencyDAO df : ESPApplication.getInstance().getCurrencies()) {
                                            if (df.getCode().toLowerCase().equals(selectValue.toLowerCase())) {
                                                SelecteId = df.getId();
                                                break;
                                            }
                                        }

                                        if (SelecteId > 0) {

                                            mApplications.get(position).setValue(text_11.getText().toString());
                                            mApplications.get(position).setSelectedCurrencyId(SelecteId);
                                            mApplications.get(position).setSelectedCurrencySymbol(singleSelection_currency.getSelectedItem().toString());

                                            DynamicFormValuesDAO post = new DynamicFormValuesDAO();
                                            post.setSectionCustomFieldId(mApplications.get(position).getSectionCustomFieldId());
                                            post.setSectionId(mApplications.get(position).getObjectId());
                                            post.setValue(text_11.getText().toString() + ":" + SelecteId + ":" + singleSelection_currency.getSelectedItem().toString());

                                            mApplications.get(position).setPost(post);

                                            mApplications.get(position).setError_field(null);
                                            holder.error.setText("");
                                            holder.error.setVisibility(View.GONE);


                                        }

                                    }

                                }


                            } else {
                                holder.field_label.setVisibility(View.GONE);
                            }
                        }
                    });

                    holder.dynamic_currency_div.setVisibility(View.VISIBLE);

                    //////////CURRENCY////////////
                    //mApplications.get(position).setViewGenerated(true);
                }


                break;

            case 12:
                break;
            //Lookup = 13
            case 13:

                if (!mApplications.get(position).isViewGenerated()) {
                    holder.field_label.setVisibility(View.GONE);

                    BodyText text_13 = new BodyText(context, "");
                    if (mApplications.get(position).isRequired()) {
                        text_13.setHint(mApplications.get(position).getLabel() + " *");
                    } else {
                        text_13.setHint(mApplications.get(position).getLabel());
                    }
                    if (mApplications.get(position).getValue() != null && mApplications.get(position).getValue().length() > 0) {
                        text_13.setText(mApplications.get(position).getValue());
                        holder.field_label.setVisibility(View.VISIBLE);

                    }
                    text_13.setSingleLine(true);
                    text_13.setTextSize(16f);
                    text_13.setFocusable(false);
                    text_13.setTextColor(context.getResources().getColor(R.color.dark_grey));
                    text_13.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right, 0);
                    text_13.setBackgroundDrawable(null);


                    if (mApplications.get(position).getMinVal() > 0 && mApplications.get(position).getMaxVal() > 0) {
                        //	text.setFilters(new InputFilter[]{ new InputFilterMinMax(mApplications.get(position).getMinVal()+"", mApplications.get(position).getMaxVal()+"")});
                    }

                    LinearLayout.LayoutParams params_1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params_1.setMargins(10, 20, 10, 20);
                    text_13.setLayoutParams(params_1);

                    text_13.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCat.LookUp(mApplications.get(position));
                        }
                    });
                    holder.dynamic_fields_div.removeAllViews();
                    holder.dynamic_fields_div.addView(text_13);
                    //mApplications.get(position).setViewGenerated(true);
                }
                break;
        }


    }//End Holder Class


    @Override
    public int getItemCount() {
        if (mApplications != null) {
            return mApplications.size();
        }
        return 0;

    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void RefreshList() {
        notifyDataSetChanged();
    }

    public List<DynamicFormSectionFieldDAO> GetAllFields() {
        if (mApplications != null) {
            return mApplications;
        }

        return null;
    }


}
