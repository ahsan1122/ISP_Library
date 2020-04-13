package com.esp.library.exceedersesp.controllers.Profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.esp.library.R;
import com.esp.library.exceedersesp.ESPApplication;
import com.esp.library.exceedersesp.controllers.Profile.adapters.ListofSectionsFieldsAdapter;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.EditTextTypeViewHolder;
import com.esp.library.ipaulpro.afilechooser.utils.FileUtils;
import com.esp.library.utilities.common.Constants;
import com.esp.library.utilities.common.CustomLogs;
import com.esp.library.utilities.common.KeyboardUtils;
import com.esp.library.utilities.common.RealPathUtil;
import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.common.SharedPreference;
import com.esp.library.utilities.customevents.EventOptions;
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter;
import com.esp.library.exceedersesp.BaseActivity;
import com.esp.library.exceedersesp.controllers.Profile.BasicDAO;
import com.esp.library.exceedersesp.controllers.applications.ApplicationsActivityDrawer;
import com.esp.library.exceedersesp.controllers.applications.ChooseLookUpOption;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import utilities.data.apis.APIs;
import utilities.data.applicants.CalculatedMappedFieldsDAO;
import utilities.data.applicants.addapplication.CurrencyDAO;
import utilities.data.applicants.addapplication.LookUpDAO;
import utilities.data.applicants.addapplication.ResponseFileUploadDAO;
import utilities.data.applicants.dynamics.DyanmicFormSectionFieldDetailsDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.profile.ApplicationProfileDAO;
import utilities.data.applicants.profile.RealTimeValuesDAO;

public class EditSectionDetails extends BaseActivity implements ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener {

    String TAG = getClass().getSimpleName();


    TextView txtheader;
    TextView txtsave;
    TextView txtcancel;
    LinearLayout layoutMain;
    RecyclerView rvFields;

    boolean isServiceRunning;
    LayoutInflater inflate;
    BaseActivity context;
    DynamicFormSectionDAO dynamicFormSectionDAO;
    SharedPreference pref;
    DynamicFormSectionFieldDAO fieldToBeUpdated;
    boolean isCalculatedField;
    boolean isKeyboardVisible;
    private static EditText val;
    List<DynamicFormSectionFieldDAO> fieldsList = new ArrayList<>();
    int count = 0;
    boolean ischeckerror;
    String basicName;
    ApplicationProfileDAO dataapplicant;
    com.esp.library.utilities.customcontrols.CustomButton btadd;
    AlertDialog pDialog;
    RelativeLayout rltoolbar;
    private static final int HIDE_THRESHOLD = 20;
    ListofSectionsFieldsAdapter listofSectionsFieldsAdapter;
    private static final int REQUEST_LOOKUP = 2;
    private static final int REQUEST_CHOOSER = 12345;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sectiondetail);
        initailize();

        ischeckerror = getIntent().getBooleanExtra("ischeckerror", false);
        if (Shared.getInstance().isWifiConnected(getBContext())) {
            loadCurrencies();
        } else {
            Shared.getInstance().showAlertMessage(getBContext().getString(R.string.internet_error_heading), getBContext().getString(R.string.internet_connection_error), getBContext());
        }

        txtsave.setOnClickListener(v -> {

            if (txtsave.getText().equals(getString(R.string.add))) {
                ArrayList<ApplicationProfileDAO.Values> updateValues = getUpdateValues();
                postUpdatedData(updateValues, false, false);
            } else {
                if (dynamicFormSectionDAO.isDefault())
                    postBasicData();
                else
                    postSectionData();
            }

        });


        txtcancel.setOnClickListener(v -> {
            onBackPressed();
        });

        KeyboardUtils.addKeyboardToggleListener(context, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {
                isKeyboardVisible = isVisible;
            }
        });

    }

    private void initailize() {
        context = EditSectionDetails.this;
        pDialog = Shared.getInstance().setProgressDialog(getBContext());

        txtheader = findViewById(R.id.txtheader);
        txtsave = findViewById(R.id.txtsave);
        txtcancel = findViewById(R.id.txtcancel);
        layoutMain = findViewById(R.id.lllayout);
        rvFields = findViewById(R.id.rvFields);

        dataapplicant = (ApplicationProfileDAO) getIntent().getSerializableExtra("dataapplicant");
        dynamicFormSectionDAO = (DynamicFormSectionDAO) getIntent().getSerializableExtra("data");
        pref = new SharedPreference(context);
        inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rltoolbar = findViewById(R.id.rltoolbar);
        rvFields.setHasFixedSize(true);
        rvFields.setLayoutManager(new LinearLayoutManager(context));
        rvFields.setItemAnimator(new DefaultItemAnimator());
    }


    private void postSectionData() {
        if (ischeckerror) {
            ArrayList<ApplicationProfileDAO.Values> updateValues = getUpdateValues();
            postUpdatedData(updateValues, true, false);
        } else {
            //   ApplicationProfileDAO.Applicant values = getValues();
            //CustomLogs.displayLogs(TAG + " joobjtosting: " + values.toJson());
            ArrayList<ApplicationProfileDAO.Values> updateValues = getUpdateValues();
            postUpdatedData(updateValues, true, true);

            //  postData(values);
        }
    }

    private ApplicationProfileDAO.Applicant getValues() {

        List<ApplicationProfileDAO.ApplicationSection> applicantSectionsList = dataapplicant.getApplicant().getApplicantSections();
        List<DynamicFormSectionFieldDAO> fields = dynamicFormSectionDAO.getFields();
        ApplicationProfileDAO.ApplicationSection applicationSection = null;
        ArrayList<ApplicationProfileDAO.Values> valueList = new ArrayList<>();

        if (fields != null) {
            for (int j = 0; j < fields.size(); j++) {
                DynamicFormSectionFieldDAO dynamicFormSectionFieldDAO = fields.get(j);

                int getSectionCustomFieldId = fields.get(j).getSectionCustomFieldId();
                if (fields.get(j).isVisible()) {
                    String value = fields.get(j).getValue();
                    int id = fields.get(j).getId();
                    int fieldsSectionId = dynamicFormSectionDAO.getId();

                    if (applicantSectionsList != null) {
                        for (int i = 0; i < applicantSectionsList.size(); i++) {
                            applicationSection = applicantSectionsList.get(i);
                            int sectionId = applicationSection.getSectionId();

                            if (fieldsSectionId == sectionId) {

                                if (dynamicFormSectionFieldDAO.getType() == 11) {
                                    value += ":" + dynamicFormSectionFieldDAO.getSelectedCurrencyId() + ":" + dynamicFormSectionFieldDAO.getSelectedCurrencySymbol();
                                    dynamicFormSectionFieldDAO.setValue(value);
                                }

                                if (dynamicFormSectionFieldDAO.getType() == 13) {

                                    if (dynamicFormSectionFieldDAO.getLookUpDAO() == null) {
                                        List<ApplicationProfileDAO.Values> values = applicationSection.getValues();
                                        for (int g = 0; g < values.size(); g++) {
                                            if (getSectionCustomFieldId == values.get(g).getSectionFieldId())
                                                value = values.get(g).getValue();
                                        }
                                    } else {
                                        value = String.valueOf(dynamicFormSectionFieldDAO.getLookUpDAO().getId());
                                    }
                                }


                                ApplicationProfileDAO.Values val = new ApplicationProfileDAO.Values();
                                val.setSectionFieldId(getSectionCustomFieldId);
                                val.setValue(value);
                                valueList.add(val);

                            }


                        }
                    }
                    applicationSection.setValues(valueList);
                    dataapplicant.getApplicant().setApplicantSections(applicantSectionsList);
                }
            }
        }

        return dataapplicant.getApplicant();
    }

    private ArrayList<ApplicationProfileDAO.Values> getUpdateValues() {
        //  ApplicationProfileDAO dataapplicant = (ApplicationProfileDAO) getIntent().getSerializableExtra("dataapplicant");
        List<ApplicationProfileDAO.ApplicationSection> applicantSectionsList = dataapplicant.getApplicant().getApplicantSections();
        List<DynamicFormSectionDAO> sections = dataapplicant.getSections();
        ArrayList<ApplicationProfileDAO.Values> valueList = new ArrayList<>();

        for (int jj = 0; jj < sections.size(); jj++) {
            int parentSectionId = sections.get(jj).getId();

            List<DynamicFormSectionFieldDAO> fields = dynamicFormSectionDAO.getFields();
            ApplicationProfileDAO.ApplicationSection applicationSection = null;


            for (int j = 0; j < fields.size(); j++) {
                DynamicFormSectionFieldDAO dynamicFormSectionFieldDAO = fields.get(j);
                int getSectionCustomFieldId = dynamicFormSectionFieldDAO.getSectionCustomFieldId();
                if (dynamicFormSectionFieldDAO.isVisible()) {

                    int id = dynamicFormSectionFieldDAO.getId();
                    int fieldsSectionId = dynamicFormSectionDAO.getId();


                    for (int i = 0; i < applicantSectionsList.size(); i++) {
                        applicationSection = applicantSectionsList.get(i);
                        int sectionId = applicationSection.getSectionId();

                        if (parentSectionId == sectionId) {


                            if (fieldsSectionId == sectionId) {

                       /* CustomLogs.displayLogs(TAG + " getSectionCustomFieldId: " + getSectionCustomFieldId +
                                " sectionId: " + sectionId + " id: " + id + " fieldsSectionId: " + fieldsSectionId
                                + " value: " + value);*/
                                String value = dynamicFormSectionFieldDAO.getValue();
                                if (dynamicFormSectionFieldDAO.getType() == 11) {

                                    value += ":" + dynamicFormSectionFieldDAO.getSelectedCurrencyId() + ":" + dynamicFormSectionFieldDAO.getSelectedCurrencySymbol();

                                    dynamicFormSectionFieldDAO.setValue(value);
                                }

                              /*  if (dynamicFormSectionFieldDAO.getType() == 13) {

                                    if (dynamicFormSectionFieldDAO.getLookUpDAO() == null) {
                                        List<ApplicationProfileDAO.Values> values = applicationSection.getValues();
                                        for (int g = 0; g < values.size(); g++) {
                                            if (getSectionCustomFieldId == values.get(g).getSectionFieldId())
                                                value = values.get(g).getValue();
                                        }
                                    } else {
                                        value = String.valueOf(dynamicFormSectionFieldDAO.getLookUpDAO().getId());
                                    }
                                }*/


                                ApplicationProfileDAO.Values val = new ApplicationProfileDAO.Values();
                                val.setSectionFieldId(getSectionCustomFieldId);
                                val.setValue(value);
                                valueList.add(val);
                            }
                            CustomLogs.displayLogs(TAG + " parentSectionId: " + parentSectionId);
                            break;
                        }

                    }
                    applicationSection.setValues(valueList);
                    dataapplicant.getApplicant().setApplicantSections(applicantSectionsList);
                }
            }


        }


        for (int j = 0; j < valueList.size(); j++) {
            CustomLogs.displayLogs(TAG + " valueList: " + valueList.get(j).toJson());
        }


        CustomLogs.displayLogs(TAG + " getApplicantSections: " + dataapplicant.getApplicant().getApplicantSections());

        /*for (Integer key : sectionsIndex.keySet()) {
            CustomLogs.displayLogs(TAG + " key: " + key);
        }*/

        return valueList;
    }


    private void populateData() {
        LinearLayout subLayout = subLayout();

        txtheader.setText(dynamicFormSectionDAO.getDefaultName());

        for (int j = 0; j < dynamicFormSectionDAO.getFields().size(); j++) {
            DynamicFormSectionFieldDAO dynamicFormSectionFieldDAO = dynamicFormSectionDAO.getFields().get(j);
            DynamicFormSectionFieldDAO dynamicFormSectionFieldDAO1 = Shared.getInstance().setObjectValues(dynamicFormSectionFieldDAO);

            fieldsList.add(dynamicFormSectionFieldDAO1);

        }
        dynamicFormSectionDAO.setFields(fieldsList);

        if (dynamicFormSectionDAO.isDefault()) {
            LinearLayout layoutName = (LinearLayout) inflate.inflate(R.layout.item_add_application_field_type_edit_text, null);
            LinearLayout.LayoutParams layoutParamsName = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParamsName.setMargins(0, 40, 0, 0);
            layoutName.setLayoutParams(layoutParamsName);
            RecyclerView.ViewHolder itemViewName = new SeparatorTypeViewHolder(layoutName);
            itemViewName = new EditTextTypeViewHolder(layoutName);
            // basicName = ESPApplication.getInstance().getUser().getLoginResponse().getName();
            basicName = dataapplicant.getApplicant().getName();
            ((EditTextTypeViewHolder) itemViewName).etValue.setText(basicName);
            ((EditTextTypeViewHolder) itemViewName).tilFieldLabel.setHint(getString(R.string.name) + " *");

            ((EditTextTypeViewHolder) itemViewName).etValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    basicName = String.valueOf(s);
                    onFieldValuesChanged();

                }
            });

            LinearLayout layout_email = (LinearLayout) inflate.inflate(R.layout.item_add_application_field_type_edit_text, null);
            LinearLayout.LayoutParams layoutParamsEmail = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParamsEmail.setMargins(0, 40, 0, 0);
            layout_email.setLayoutParams(layoutParamsEmail);
            RecyclerView.ViewHolder itemViewEmail = new SeparatorTypeViewHolder(layout_email);
            itemViewEmail = new EditTextTypeViewHolder(layout_email);
            ((EditTextTypeViewHolder) itemViewEmail).tilFieldLabel.setHint(getString(R.string.login_email_label));
            ((EditTextTypeViewHolder) itemViewEmail).etValue.setText(dataapplicant.getApplicant().getEmailAddress());
            ((EditTextTypeViewHolder) itemViewEmail).etValue.setEnabled(false);
            ((EditTextTypeViewHolder) itemViewEmail).etValue.setTextColor(ContextCompat.getColor(context, R.color.cooltwogrey));

            LinearLayout layout_profiletype = (LinearLayout) inflate.inflate(R.layout.item_add_application_field_type_edit_text, null);
            LinearLayout.LayoutParams layoutParamsProfileType = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParamsProfileType.setMargins(0, 40, 0, 0);
            layout_profiletype.setLayoutParams(layoutParamsProfileType);
            RecyclerView.ViewHolder itemViewProfileType = new SeparatorTypeViewHolder(layout_profiletype);
            itemViewProfileType = new EditTextTypeViewHolder(layout_profiletype);
            ((EditTextTypeViewHolder) itemViewProfileType).tilFieldLabel.setHint(getString(R.string.profiletype));
            ((EditTextTypeViewHolder) itemViewProfileType).etValue.setText(dataapplicant.getApplicant().getProfileTemplateString());
            ((EditTextTypeViewHolder) itemViewProfileType).etValue.setEnabled(false);
            ((EditTextTypeViewHolder) itemViewProfileType).etValue.setTextColor(ContextCompat.getColor(context, R.color.cooltwogrey));

            subLayout.addView(layoutName);
            subLayout.addView(layout_email);
            subLayout.addView(layout_profiletype);
        }

        List<DynamicFormSectionFieldDAO> fields = dynamicFormSectionDAO.getFields();

        listofSectionsFieldsAdapter = new ListofSectionsFieldsAdapter(fields, context, ischeckerror, false);
        listofSectionsFieldsAdapter.getListenerContext(this);
        rvFields.setAdapter(listofSectionsFieldsAdapter);

        /*for (int i = 0; i < fields.size(); i++) {
            DynamicFormSectionFieldDAO field = fields.get(i);
            int type = field.getType();
            count = i;
            if (field.isVisible()) {
                LinearLayout layout = populateFields(type, field, i);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                layoutParams.setMargins(0, 40, 0, 0);
                layout.setLayoutParams(layoutParams);
                subLayout.addView(layout);
            }
        }*/


        layoutMain.addView(subLayout);
        boolean isaddmore = getIntent().getBooleanExtra("isaddmore", false);
        if (isaddmore) // coming from SectionDetailScreen.class
            txtsave.setText(getString(R.string.add));
        else
            txtsave.setText(getString(R.string.save));


    }

    private LinearLayout subLayout() {
        LinearLayout ll = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams1.setMargins(0, 0, 0, 0);
        ll.setLayoutParams(layoutParams1);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundColor(Color.WHITE);
        return ll;
    }

    public void loadCurrencies() {
        start_loading_animation();

        try {
            /* APIs Mapping respective Object*/
            APIs apis = Shared.getInstance().retroFitObject(context);

            Call<List<CurrencyDAO>> call = apis.getCurrency();

            call.enqueue(new Callback<List<CurrencyDAO>>() {
                @Override
                public void onResponse(Call<List<CurrencyDAO>> call, Response<List<CurrencyDAO>> response) {

                    if (response.body() != null && response.body().size() > 0) {
                        ESPApplication.getInstance().setCurrencies(response.body());
                    }


                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            stop_loading_animation();
                        }
                    }, 2000);

                    populateData();
                }

                @Override
                public void onFailure(Call<List<CurrencyDAO>> call, Throwable t) {
                    stop_loading_animation();
                }
            });

        } catch (Exception ex) {
            stop_loading_animation();
        }
    }


    @Override
    public void onFieldValuesChanged() {

        List<DynamicFormSectionFieldDAO> fields = dynamicFormSectionDAO.getFields();
        boolean isAllFieldsValidateTrue = true;

        if (fields != null) {
            for (int i = 0; i < fields.size(); i++) {
                DynamicFormSectionFieldDAO dynamicFormSectionFieldDAO = fields.get(i);
                if (!dynamicFormSectionFieldDAO.isShowToUserOnly())  // if fields are not for displayed then validate
                {
                    String error = "";
                    error = Shared.getInstance().edittextErrorChecks(context, dynamicFormSectionFieldDAO.getValue(), error, dynamicFormSectionFieldDAO);
                    if (error.length() > 0) {
                        isAllFieldsValidateTrue = false;
                        break;
                    }


                    if (dynamicFormSectionFieldDAO.isRequired()) {
                        if (!dynamicFormSectionFieldDAO.isValidate()) {
                            isAllFieldsValidateTrue = false;
                            break;
                        }
                    }
                }
            }
        }

        if (basicName != null) {
            validateFields(isAllFieldsValidateTrue, basicName);
        } else {
            if (isAllFieldsValidateTrue) {
                txtsave.setEnabled(true);
                txtsave.setAlpha(1);
                if (btadd != null) {
                    btadd.setEnabled(true);
                    btadd.setAlpha(1);
                }

            } else {
                txtsave.setEnabled(false);
                txtsave.setAlpha(0.5f);
                if (btadd != null) {
                    btadd.setEnabled(false);
                    btadd.setAlpha(0.5f);
                }
            }
        }
    }

    private void validateFields(boolean isAllFieldsValidateTrue, String basicName) {
        if (isAllFieldsValidateTrue && basicName.length() > 0) {
            txtsave.setEnabled(true);
            txtsave.setAlpha(1);
            if (btadd != null) {
                btadd.setEnabled(true);
                btadd.setAlpha(1);
            }

        } else {
            txtsave.setEnabled(false);
            txtsave.setAlpha(0.5f);
            if (btadd != null) {
                btadd.setEnabled(false);
                btadd.setAlpha(0.5f);
            }
        }
    }

    @Override
    public void onAttachmentFieldClicked(DynamicFormSectionFieldDAO fieldDAO, int position) {


        fieldToBeUpdated = fieldDAO;
        fieldToBeUpdated.setUpdatePositionAttachment(position);


        String getAllowedValuesCriteria = fieldToBeUpdated.getAllowedValuesCriteria();


        getAllowedValuesCriteria = getAllowedValuesCriteria.replaceAll("\\.", "");


        String[] values = getAllowedValuesCriteria.split(",");
        List<String> valuesList = new ArrayList<String>(Arrays.asList(values));
        ArrayList<String> refineValuesList = new ArrayList<>();
        for (int j = 0; j < valuesList.size(); j++) {
            if (!valuesList.get(j).equals("-2"))
                refineValuesList.add(valuesList.get(j));
        }

        String[] mimeTypes = new String[refineValuesList.size()];
        for (int i = 0; i < refineValuesList.size(); i++) {
            String type = refineValuesList.get(i);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(type.toLowerCase());
            if (mimeType != null)
                mimeTypes[i] = mimeType;
            else
                mimeTypes[i] = type;
        }


        final Intent getContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
        // The MIME data type filter

        getContentIntent.setType("*/*");
        if (getAllowedValuesCriteria.length() > 0)
            getContentIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // Only return URIs that can be opened with ContentResolver
        getContentIntent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent intent = Intent.createChooser(getContentIntent, getString(R.string.selectafile));
        intent.putExtra("position", position);
        startActivityForResult(intent, REQUEST_CHOOSER);
    }

    @Override
    public void onLookupFieldClicked(DynamicFormSectionFieldDAO fieldDAO, int position, boolean isCalculatedMappedField) {

        if (!pDialog.isShowing()) {
            fieldToBeUpdated = fieldDAO;
            fieldToBeUpdated.setUpdatePositionAttachment(position);
            Bundle bundle = new Bundle();
            bundle.putSerializable(DynamicFormSectionFieldDAO.Companion.getBUNDLE_KEY(), fieldToBeUpdated);

            Intent chooseLookupOption = new Intent(context, ChooseLookUpOption.class);
            chooseLookupOption.putExtras(bundle);
            startActivityForResult(chooseLookupOption, REQUEST_LOOKUP);
        }
    }


    protected class SeparatorTypeViewHolder extends RecyclerView.ViewHolder {

        public SeparatorTypeViewHolder(View itemView) {
            super(itemView);


        }

    }


    public void SetUpLookUpValues(DynamicFormSectionFieldDAO field, LookUpDAO lookup) {

        field.setValue(String.valueOf(lookup.getId()));
        field.setLookupValue(lookup.getName());
        field.setId(lookup.getId());

        if (listofSectionsFieldsAdapter != null)
            listofSectionsFieldsAdapter.notifyItemChanged(field.getUpdatePositionAttachment());

        if (field.isTigger())
            callService();


    }


    public void UpdateLoadImageForField(DynamicFormSectionFieldDAO field, Uri uri) {
        if (field != null) {

            MultipartBody.Part body = null;

            try {

                body = Shared.getInstance().prepareFilePart(uri, context);
                UpLoadFile(field, body, uri);

            } catch (Exception e) {
                Shared.getInstance().errorLogWrite("FILE", e.getMessage());
            }

        }
    }

    private void UpLoadFile(final DynamicFormSectionFieldDAO field, final MultipartBody.Part body, final Uri uri) {

        start_loading_animation();

        try {

            /* APIs Mapping respective Object*/
            APIs apis = Shared.getInstance().retroFitObject(context);

            Call<ResponseFileUploadDAO> call_upload = apis.upload(body);
            call_upload.enqueue(new Callback<ResponseFileUploadDAO>() {
                @Override
                public void onResponse(Call<ResponseFileUploadDAO> call, Response<ResponseFileUploadDAO> response) {
                    stop_loading_animation();
                    if (response != null && response.body() != null) {

                        if (field != null) {

                            try {

                                File file = null;
                                String path = RealPathUtil.getPath(context, uri);
                                file = new File(path);


                                String attachmentFileSize = Shared.getInstance().getAttachmentFileSize(file);
                                DyanmicFormSectionFieldDetailsDAO detail = new DyanmicFormSectionFieldDetailsDAO();
                                detail.setName(file.getName());
                                detail.setDownloadUrl(response.body().getDownloadUrl());
                                detail.setMimeType(FileUtils.getMimeType(file));
                                detail.setCreatedOn(Shared.getInstance().GetCurrentDateTime());
                                detail.setFileSize(attachmentFileSize);
                                field.setDetails(detail);

                                field.setValue(response.body().getFileId());


                                if (listofSectionsFieldsAdapter != null)
                                    listofSectionsFieldsAdapter.notifyItemChanged(fieldToBeUpdated.getUpdatePositionAttachment());

                                if (field.isTigger())
                                    callService();

                                CustomLogs.displayLogs(TAG + " attachment id: " + field.getId());
                            } catch (Exception e) {
                            }


                        }

                    } else {
                        Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), context);
                    }

                }

                @Override
                public void onFailure(Call<ResponseFileUploadDAO> call, Throwable t) {
                    stop_loading_animation();
                    Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), context);
                    // UploadFileInformation(fileDAO);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            stop_loading_animation();
            Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), context);
        }
    }//LoggedInUser end


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (REQUEST_CHOOSER == requestCode && data != null) {
                final Uri uri = data.getData();

                if (uri != null) {
                    int getMaxVal = fieldToBeUpdated.getMaxVal();
                    boolean isFileSizeValid = true;
                    if (getMaxVal > 0)
                        isFileSizeValid = Shared.getInstance().getFileSize(RealPathUtil.getPath(context, uri), getMaxVal);

                    if (isFileSizeValid) {

                        try {
                            UpdateLoadImageForField(fieldToBeUpdated, uri);

                        } catch (Exception e) {
                            Shared.getInstance().messageBox(getString(R.string.pleasetryagain), this);
                        }
                    } else {

                        Shared.getInstance().showAlertMessage("", getString(R.string.sizeshouldbelessthen) + " " + getMaxVal + " " + getString(R.string.mb), context);
                    }
                }

            } else if (REQUEST_LOOKUP == requestCode && data != null) {
                LookUpDAO lookup = (LookUpDAO) data.getExtras().getSerializable(LookUpDAO.Companion.getBUNDLE_KEY());
                if (fieldToBeUpdated != null && lookup != null) {
                    SetUpLookUpValues(fieldToBeUpdated, lookup);
                }

            }

        }

    }


    private void start_loading_animation() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void stop_loading_animation() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    public void postBasicData() {

        BasicDAO basicDAO = new BasicDAO();
        basicDAO.setName(basicName);

        start_loading_animation();
        try {
            final APIs apis = Shared.getInstance().retroFitObject(context);

            Call<BasicDAO> status_call;
            status_call = apis.saveBasicData(basicDAO);


            status_call.enqueue(new Callback<BasicDAO>() {
                @Override
                public void onResponse(Call<BasicDAO> call, Response<BasicDAO> response) {
                    ESPApplication.getInstance().getUser().getLoginResponse().setName(basicName);
                    postSectionData();


                }

                @Override
                public void onFailure(Call<BasicDAO> call, Throwable t) {
                    stop_loading_animation();
                    if (t != null && getBContext() != null) {
                        Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), context);
                    }
                }
            });

        } catch (Exception ex) {
            if (ex != null) {
                stop_loading_animation();
                if (ex != null && getBContext() != null) {
                    Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), context);
                }
            }
        }
    }

   /* public void postData(ApplicationProfileDAO.Applicant post) {


        start_loading_animation();
        try {
            final APIs apis = Shared.getInstance().retroFitObject(context);

            Call<Integer> status_call;
            status_call = apis.saveApplicantData(post);


            status_call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    stop_loading_animation();
                    CustomLogs.displayLogs(TAG + " postData success: " + response.body());
                    Shared.getInstance().callIntentClearAllActivities(ApplicationsActivityDrawer.class, getBContext(), null);

                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    t.printStackTrace();
                    stop_loading_animation();
                    if (t != null && getBContext() != null) {
                        Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), context);
                    }
                }
            });

        } catch (Exception ex) {
            if (ex != null) {
                stop_loading_animation();
                if (ex != null && getBContext() != null) {
                    Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), context);
                }
            }
        }
    }*/

    public void getApplicant() {

        try {

            start_loading_animation();

            final APIs apis = Shared.getInstance().retroFitObject(context);


            Call<ApplicationProfileDAO> labels_call = apis.Getapplicant();

            labels_call.enqueue(new Callback<ApplicationProfileDAO>() {
                @Override
                public void onResponse(Call<ApplicationProfileDAO> call, Response<ApplicationProfileDAO> response) {
                    stop_loading_animation();
                    ApplicationProfileDAO body = response.body();
                    Intent mainIntent = new Intent(getBContext(), ProfileMainActivity.class);
                    mainIntent.putExtra("dataapplicant", body);
                    mainIntent.putExtra("ischeckerror", true);
                    mainIntent.putExtra("isprofile", true);
                    startActivity(mainIntent);
                    finish();

                }

                @Override
                public void onFailure(Call<ApplicationProfileDAO> call, Throwable t) {
                    stop_loading_animation();
                    t.printStackTrace();
                    Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), getBContext());
                }
            });


        } catch (Exception ex) {
            ex.printStackTrace();
            stop_loading_animation();
            Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), getBContext());
        }
    }

    public void postUpdatedData(ArrayList<ApplicationProfileDAO.Values> post, boolean isUpdateSection,
                                boolean isMoveToMainScreen) {


        start_loading_animation();
        try {
            final APIs apis = Shared.getInstance().retroFitObject(context);

            if (isUpdateSection) {
                int index = getIntent().getIntExtra("position", -1); // coming from EditSectionDetail.class use for updating mulisection index
                if (index == -1)
                    index = 0;
                Call<ApplicationProfileDAO.Values> status_call_Update = apis.updateApplicantDataBySectionId(dynamicFormSectionDAO.getId(), index, post);

                status_call_Update.enqueue(new Callback<ApplicationProfileDAO.Values>() {
                    @Override
                    public void onResponse(Call<ApplicationProfileDAO.Values> call, Response<ApplicationProfileDAO.Values> response) {
                        // stop_loading_animation();
                        CustomLogs.displayLogs(TAG + " updateApplicantDataBySectionId success: " + response.body());
                        //Shared.getInstance().callIntentClearAllActivities(ApplicationsActivityDrawer.ACTIVITY_NAME, bContext, null);

                        if (isMoveToMainScreen) {
                            stop_loading_animation();
                            Shared.getInstance().callIntentClearAllActivities(ApplicationsActivityDrawer.class, getBContext(), null);
                        } else
                            getApplicant();
                    }

                    @Override
                    public void onFailure(Call<ApplicationProfileDAO.Values> call, Throwable t) {
                        stop_loading_animation();
                        t.printStackTrace();
                        if (t != null && getBContext() != null) {
                            Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), context);
                        }
                    }
                });

            } else {
                Call<Integer> status_call_post = apis.saveApplicantDataBySectionId(dynamicFormSectionDAO.getId(), post);

                status_call_post.enqueue(new Callback<Integer>() {
                    @Override
                    public void onResponse(Call<Integer> call, Response<Integer> response) {
                        //  stop_loading_animation();
                        // Shared.getInstance().callIntentClearAllActivities(ApplicationsActivityDrawer.ACTIVITY_NAME, bContext, null);

                        if (getIntent().getBooleanExtra("isprofile", false))
                            getApplicant();
                        else {
                            stop_loading_animation();
                            Shared.getInstance().callIntentClearAllActivities(ApplicationsActivityDrawer.class, getBContext(), null);
                        }

                        CustomLogs.displayLogs(TAG + " saveApplicantDataBySectionId success: " + response.body());
                    }

                    @Override
                    public void onFailure(Call<Integer> call, Throwable t) {
                        stop_loading_animation();
                        t.printStackTrace();
                        if (t != null && getBContext() != null) {
                            Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), context);
                        }
                    }
                });
            }


        } catch (Exception ex) {

            ex.printStackTrace();
            stop_loading_animation();
            if (getBContext() != null) {
                Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), context);
            }

        }
    }


    /*public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            *//*DynamicFormSectionFieldDAO dynamicFormSectionFieldDAO = (DynamicFormSectionFieldDAO) intent.getSerializableExtra("dynamicFormSectionFieldDAO");
            CustomLogs.displayLogs(TAG + " BroadcastReceiver sectionCustomFieldId: " + dynamicFormSectionFieldDAO.getSectionCustomFieldId() + " " +
                    dynamicFormSectionFieldDAO.getValue());*//*
            callService();
        }
    };

    private void callService() {

        if (!isServiceRunning) {
            unRegisterReciever();
            isServiceRunning = true;
            getRealTimeValues();
        }
    }
*/
    private void registerReciever() {
       /* isServiceRunning = false;
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver,
                new IntentFilter("getcalculatedvalues"));*/
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    private void unRegisterReciever() {
        //   LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unRegisterReciever();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReciever();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void dataRefreshEvent(EventOptions.EventTriggerController eventTriggerController) {
        callService();
    }

    private void callService() {
        unRegisterReciever();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getRealTimeValues();
            }
        }, 1000);
    }

    public void getRealTimeValues() {
        try {
            if (isKeyboardVisible)
                isCalculatedField = true;

            //   start_loading_animation();
            List<RealTimeValuesDAO> realTimeValuesDAOList = new ArrayList<>();

            if (dynamicFormSectionDAO.getFields() != null) {
                for (int i = 0; i < dynamicFormSectionDAO.getFields().size(); i++) {
                    DynamicFormSectionFieldDAO fieldDAO = dynamicFormSectionDAO.getFields().get(i);

                    if (fieldDAO.getType() == 11) {
                        String finalValue = fieldDAO.getValue();
                        if (finalValue != null && !finalValue.isEmpty())
                            finalValue += ":" + fieldDAO.getSelectedCurrencyId() + ":" + fieldDAO.getSelectedCurrencySymbol();

                        fieldDAO.setValue(finalValue);
                    }

                    RealTimeValuesDAO realTimeValuesDAO = new RealTimeValuesDAO();
                    realTimeValuesDAO.setSectionFieldId(fieldDAO.getSectionCustomFieldId());
                    realTimeValuesDAO.setValue(fieldDAO.getValue());
                    realTimeValuesDAOList.add(realTimeValuesDAO);
                }
            }


            Call<List<CalculatedMappedFieldsDAO>> submit_call = Shared.getInstance().retroFitObject(context).getRealTimeValues(dynamicFormSectionDAO.getId(), realTimeValuesDAOList);

            submit_call.enqueue(new Callback<List<CalculatedMappedFieldsDAO>>() {
                @Override
                public void onResponse(Call<List<CalculatedMappedFieldsDAO>> call, Response<List<CalculatedMappedFieldsDAO>> response) {

                    if (response != null && response.body() != null) {
                        //  List<DynamicSectionValuesDAO> sectionValuesListToPost = new ArrayList<>();
                        List<CalculatedMappedFieldsDAO> calculatedMappedFieldsList = response.body();

                        for (int i = 0; i < calculatedMappedFieldsList.size(); i++) {
                            CalculatedMappedFieldsDAO calculatedMappedFieldsDAO = calculatedMappedFieldsList.get(i);
                            List<ApplicationProfileDAO.ApplicationSection> applicantSectionsList = dataapplicant.getApplicant().getApplicantSections();

                            if (applicantSectionsList != null) {
                                for (int u = 0; u < applicantSectionsList.size(); u++) {
                                    //    if (dynamicFormSectionDAO.getFieldsCardsList().size() > 0) {
                                    //For Setting InstancesList
                                    //   for (DynamicFormSectionFieldsCardsDAO dynamicFormSectionFieldsCardsDAO : updatedSection.getFieldsCardsList()) {
                                    //  for (int g = 0; g < dynamicFormSectionDAO.getFieldsCardsList().size(); g++) {

                                    //   if (calculatedMappedFieldsDAO.getSectionIndex() == g) {
                                    //     DynamicFormSectionFieldsCardsDAO dynamicFormSectionFieldsCardsDAO = dynamicFormSectionDAO.getFieldsCardsList().get(g);
                                    if (dynamicFormSectionDAO.getFields() != null) {
                                        for (int p = 0; p < dynamicFormSectionDAO.getFields().size(); p++) {
                                            DynamicFormSectionFieldDAO dynamicFormSectionFieldDAO = dynamicFormSectionDAO.getFields().get(p);

                                            if (dynamicFormSectionFieldDAO.getSectionCustomFieldId() == calculatedMappedFieldsDAO.getSectionCustomFieldId()) {
                                                int targetFieldType = calculatedMappedFieldsDAO.getTargetFieldType();

                                                if (targetFieldType == 13) {
                                                    List<LookUpDAO> servicelookupItems = calculatedMappedFieldsDAO.getLookupItems();
                                                    if (dynamicFormSectionFieldDAO.getLookupValue() != null && !dynamicFormSectionFieldDAO.getLookupValue().isEmpty()) {

                                                        List<String> servicelookupItemsTemp = new ArrayList<>();
                                                        if (servicelookupItems != null) {
                                                            for (int s = 0; s < servicelookupItems.size(); s++) {
                                                                servicelookupItemsTemp.add(servicelookupItems.get(s).getName());
                                                            }
                                                            if (!servicelookupItemsTemp.contains(dynamicFormSectionFieldDAO.getLookupValue()))
                                                                dynamicFormSectionFieldDAO.setLookupValue("");
                                                        }

                                                    }
                                                    calculatedMappedFieldsDAO.setLookupItems(servicelookupItems);
                                                    Shared.getInstance().saveLookUpItems(calculatedMappedFieldsDAO.getSectionCustomFieldId(), servicelookupItems);
                                                }

                                                if (targetFieldType == 7 || targetFieldType == 15) {

                                                    dynamicFormSectionFieldDAO.setMappedCalculatedField(true);
                                                    dynamicFormSectionFieldDAO.setType(calculatedMappedFieldsDAO.getTargetFieldType());
                                                    dynamicFormSectionFieldDAO.setValue(calculatedMappedFieldsDAO.getValue());
                                                    String attachmentFileSize = "";
                                                    if (calculatedMappedFieldsDAO.getDetails() != null) {
                                                        String getOutputMediaFile = Shared.getInstance().getOutputMediaFile(calculatedMappedFieldsDAO.getDetails().getName()).getPath();
                                                        boolean isFileExist = Shared.getInstance().isFileExist(getOutputMediaFile, context);
                                                        if (isFileExist) {
                                                            File file = null;
                                                            file = new File(getOutputMediaFile);
                                                            attachmentFileSize = Shared.getInstance().getAttachmentFileSize(file);
                                                        }

                                                        calculatedMappedFieldsDAO.getDetails().setFileSize(attachmentFileSize);
                                                    }
                                                    dynamicFormSectionFieldDAO.setDetails(calculatedMappedFieldsDAO.getDetails());
                                                } else if (targetFieldType == 4) {
                                                    String calculatedDisplayDate = Shared.getInstance().getDisplayDate(context, calculatedMappedFieldsDAO.getValue(), false);
                                                    dynamicFormSectionFieldDAO.setValue(calculatedDisplayDate);


                                                } else if (targetFieldType == 11) {

                                                    DynamicFormSectionFieldDAO fieldDAO = Shared.getInstance().populateCurrency(calculatedMappedFieldsDAO.getValue());
                                                    //  CurrencyDAO currencyById = Shared.getInstance().getCurrencyById(Integer.valueOf(calculatedMappedFieldsDAO.getValue()));
                                                    String concateValue = fieldDAO.getValue() + " " + fieldDAO.getSelectedCurrencySymbol();
                                                    // String concateValue = fieldDAO.getValue() + " " + currencyById.getSymobl();
                                                    dynamicFormSectionFieldDAO.setValue(concateValue);


                                                } else
                                                    dynamicFormSectionFieldDAO.setValue(calculatedMappedFieldsDAO.getValue());


                                            }

                                        }
                                    }
                                    //    }


                                    //  }
                                    //   }


                                }
                            }

                        }
                       /* if (listofSectionsFieldsAdapter != null)
                            listofSectionsFieldsAdapter.notifyDataSetChanged();*/

                        List<DynamicFormSectionFieldDAO> fields = dynamicFormSectionDAO.getFields();
                        listofSectionsFieldsAdapter = new ListofSectionsFieldsAdapter(fields, context, ischeckerror, false);
                        listofSectionsFieldsAdapter.getListenerContext(EditSectionDetails.this);
                        rvFields.setAdapter(listofSectionsFieldsAdapter);

                        View v = getCurrentFocus();
                        if (v != null) {
                            v.clearFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                registerReciever();
                            }
                        }, 1000);
                        if (ChooseLookUpOption.Companion.isOpen())
                            EventBus.getDefault().post(new EventOptions.EventTriggerController());

                    } else {
                        // stop_loading_animation();
                        CustomLogs.displayLogs(TAG + " null response");
                        registerReciever();
                        if (getBContext() != null)
                            Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), getBContext());
                    }


                }

                @Override
                public void onFailure(Call<List<CalculatedMappedFieldsDAO>> call, Throwable t) {
                    //  stop_loading_animation();
                    CustomLogs.displayLogs(TAG + " failure response");
                    registerReciever();
                    if (getBContext() != null)
                        Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), getBContext());
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            // stop_loading_animation();
            registerReciever();
            if (getBContext() != null)
                Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), getBContext());
        }
    }//LoggedInUser end

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    if (isCalculatedField) {
                        isCalculatedField = false;
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (listofSectionsFieldsAdapter != null && isCalculatedField)
                                    listofSectionsFieldsAdapter.notifyDataSetChanged();
                            }
                        }, 500);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
