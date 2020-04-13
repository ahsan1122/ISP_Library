package com.esp.library.exceedersesp.fragments.applications;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.esp.library.R;
import com.esp.library.exceedersesp.ESPApplication;
import com.esp.library.ipaulpro.afilechooser.utils.FileUtils;
import com.esp.library.utilities.common.Constants;
import com.esp.library.utilities.common.CustomLogs;
import com.esp.library.utilities.common.KeyboardUtils;
import com.esp.library.utilities.common.ProgressBarAnimation;
import com.esp.library.utilities.common.RealPathUtil;
import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.common.SharedPreference;
import com.esp.library.utilities.customevents.EventOptions;
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter;
import com.esp.library.exceedersesp.BaseActivity;
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
import java.util.Objects;
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
import utilities.adapters.setup.applications.ListAddApplicationSectionsAdapter;
import utilities.data.apis.APIs;
import utilities.data.applicants.CalculatedMappedFieldsDAO;
import utilities.data.applicants.addapplication.CategoryAndDefinationsDAO;
import utilities.data.applicants.addapplication.CurrencyDAO;
import utilities.data.applicants.addapplication.LookUpDAO;
import utilities.data.applicants.addapplication.ResponseFileUploadDAO;
import utilities.data.applicants.dynamics.DyanmicFormSectionFieldDetailsDAO;
import utilities.data.applicants.dynamics.DynamicFormDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldLookupValuesDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldsCardsDAO;
import utilities.data.applicants.dynamics.DynamicFormValuesDAO;
import utilities.data.applicants.dynamics.DynamicResponseDAO;
import utilities.data.applicants.dynamics.DynamicSectionValuesDAO;

import static com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter.SECTIONCONSTANT;


public class AddApplicationFragment extends Fragment implements
        ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener {

    String TAG = getClass().getSimpleName();

    BaseActivity bContext;

    boolean isServiceRunning;
    public ListAddApplicationSectionsAdapter mApplicationSectionsAdapter;

    Retrofit retrofit = null;
    Call<DynamicResponseDAO> detail_call = null;
    Call<ResponseFileUploadDAO> call_upload = null;
    Call<Integer> submit_call = null;
    DynamicResponseDAO actual_response = null;
    String actualResponseJson = null;
    InputMethodManager imm = null;
    ProgressBarAnimation anim = null;
    CategoryAndDefinationsDAO definationsDAO;
    AlertDialog dialog = null;

    private static Button btnSubmit;
    private static final int REQUEST_CHOOSER = 12345;
    private static final int REQUEST_LOOKUP = 2;
    DynamicFormSectionFieldDAO fieldToBeUpdated = null;
    SharedPreference pref;
    boolean isNotified;
    public static boolean isCalculatedField = false;
    private boolean isKeyboardVisible = false;


    LinearLayout no_application_available_div;
    RecyclerView app_list;
    TextView category_name;
    TextView txtcategory;

    DynamicFormSectionFieldDAO dynamicFormSectionFieldDAOCalculatedMapped = null;
    android.app.AlertDialog pDialog;
    RecyclerView.LayoutManager mApplicationLayoutManager;

    public AddApplicationFragment() {
        // Required empty public constructor
    }

    public static AddApplicationFragment newInstance(CategoryAndDefinationsDAO cat, Button btn_Submit) {
        AddApplicationFragment fragment = new AddApplicationFragment();
        Bundle args = new Bundle();
        args.putSerializable(CategoryAndDefinationsDAO.Companion.getBUNDLE_KEY(), cat);
        fragment.setArguments(args);

        btnSubmit = btn_Submit;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            definationsDAO = (CategoryAndDefinationsDAO) getArguments().getSerializable(CategoryAndDefinationsDAO.Companion.getBUNDLE_KEY());

        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_application, container, false);
        initailize(v);
        setGravity();


        if (definationsDAO != null) {
            category_name.setText(definationsDAO.getCategory());
        }
        if (Shared.getInstance().isWifiConnected(bContext)) {
            LoadStages();
        } else {
            Shared.getInstance().showAlertMessage(bContext.getString(R.string.internet_error_heading), bContext.getString(R.string.internet_connection_error), bContext);
        }

        KeyboardUtils.addKeyboardToggleListener(bContext, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {
                isKeyboardVisible = isVisible;
            }
        });

     /*   KeyboardUtils.addKeyboardToggleListener(this,
                object : KeyboardUtils.SoftKeyboardToggleListener {
            override fun onToggleSoftKeyboard(isVisible: Boolean) {
                submit_request?.refreshAdapter(isVisible)
            }
        })*/

        return v;
    }

    private void initailize(View v) {
        bContext = (BaseActivity) getActivity();
        pDialog = Shared.getInstance().setProgressDialog(bContext);
        pref = new SharedPreference(bContext);

        no_application_available_div = v.findViewById(R.id.no_application_available_div);
        app_list = v.findViewById(R.id.app_list);
        category_name = v.findViewById(R.id.category_name);
        txtcategory = v.findViewById(R.id.txtcategory);

        mApplicationLayoutManager = new LinearLayoutManager(getActivity());
        app_list.setHasFixedSize(true);
        app_list.setLayoutManager(mApplicationLayoutManager);
        app_list.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }


    private void LoadStages() {
        start_loading_animation();

        try {

            APIs apis = Shared.getInstance().retroFitObject(bContext);

            Call<List<CurrencyDAO>> call = apis.getCurrency();

            call.enqueue(new Callback<List<CurrencyDAO>>() {
                @Override
                public void onResponse(Call<List<CurrencyDAO>> call, Response<List<CurrencyDAO>> response) {

                    if (response.body() != null && response.body().size() > 0) {
                        ESPApplication.getInstance().setCurrencies(response.body());
                    }
                    if (Shared.getInstance().isWifiConnected(bContext)) {
                        GetApplicationFrom(definationsDAO);
                    } else {
                        Shared.getInstance().showAlertMessage(bContext.getString(R.string.internet_error_heading), bContext.getString(R.string.internet_connection_error), bContext);
                    }
                }

                @Override
                public void onFailure(Call<List<CurrencyDAO>> call, Throwable t) {
                    stop_loading_animation();
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            stop_loading_animation();
        }
    }//LoggedInUser end

    private void GetApplicationFrom(CategoryAndDefinationsDAO categoryAndDefinationsDAO) {


        try {


            final APIs apis = Shared.getInstance().retroFitObject(bContext);
            if (categoryAndDefinationsDAO.getParentApplicationInfo() != null)
                detail_call = apis.getSubDefincationForm(categoryAndDefinationsDAO.getId(), categoryAndDefinationsDAO.getParentApplicationInfo().getApplicationId());
            else
                detail_call = apis.AllDefincationForm(categoryAndDefinationsDAO.getId());
            detail_call.enqueue(new Callback<DynamicResponseDAO>() {
                @Override
                public void onResponse(Call<DynamicResponseDAO> call, Response<DynamicResponseDAO> response) {

                    stop_loading_animation();

                    if (response != null && response.body() != null) {

                        actual_response = response.body();
                        actualResponseJson = actual_response.toJson();
                        ArrayList<DynamicFormSectionDAO> sections;
                        List<DynamicSectionValuesDAO> sectionsValues = actual_response.getSectionValues();
                        if (sectionsValues != null)
                            sections = GetFieldsCards(actual_response.getForm(), sectionsValues);
                        else
                            sections = GetOnlyFieldsCards(response.body().getForm(), null);

                        if (sections != null && sections.size() > 0) {
                            mApplicationSectionsAdapter = new ListAddApplicationSectionsAdapter(sections, bContext, "", false);
                            mApplicationSectionsAdapter.setActualResponseJson(actualResponseJson);
                            app_list.setAdapter(mApplicationSectionsAdapter);
                            mApplicationSectionsAdapter.notifyDataSetChanged();
                            mApplicationSectionsAdapter.setmApplicationFieldsAdapterListener(AddApplicationFragment.this);
                            SuccessResponse();
                        } else {

                            SubmitRequest(getString(R.string.submit));

                            //   UnSuccessResponse();
                        }
                    }
                }

                @Override
                public void onFailure(Call<DynamicResponseDAO> call, Throwable t) {
                    stop_loading_animation();
                    Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), bContext);
                    return;
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            stop_loading_animation();
            Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), bContext);


        }
    }


    @Override
    public void onDestroyView() {
        if (detail_call != null) {
            detail_call.cancel();
        }

        super.onDestroyView();
    }


    private void start_loading_animation() {
        try {
            if (pDialog != null && !pDialog.isShowing())
                pDialog.show();
        } catch (Exception e) {

        }
    }

    private void stop_loading_animation() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (pDialog.isShowing())
                        pDialog.dismiss();
                } catch (Exception e) {

                }
            }
        }, 2000);


    }

    private void SuccessResponse() {
        app_list.setVisibility(View.VISIBLE);
        no_application_available_div.setVisibility(View.GONE);

    }

    private void UnSuccessResponse() {
        app_list.setVisibility(View.GONE);
        no_application_available_div.setVisibility(View.GONE);
    }


    private ArrayList<DynamicFormSectionDAO> GetFieldsCards(DynamicFormDAO response, List<DynamicSectionValuesDAO> sectionsValues) {
        ArrayList<DynamicFormSectionDAO> sections = new ArrayList<>();

        if (response.getSections() != null) {
            for (int i = 0; i < response.getSections().size(); i++) {
                DynamicFormSectionDAO sectionDAO = response.getSections().get(i);
                int sectionId = sectionDAO.getId();


                for (int j = 0; j < sectionsValues.size(); j++) {

                    int sectionValuesId = sectionsValues.get(j).getId();

                    if (sectionId == sectionValuesId) {

                        List<DynamicSectionValuesDAO.Instance> instances = sectionsValues.get(j).getInstances();

                        List<DynamicFormSectionFieldsCardsDAO> cardsList = new ArrayList<>();

                        for (int k = 0; k < (instances != null ? instances.size() : 0); k++) {

                            List<DynamicSectionValuesDAO.Instance.Value> sectionValuesAsFields = instances.get(k).getValues();
                            List<DynamicFormSectionFieldDAO> finalFields = new ArrayList<>();


                            for (int l = 0; l < (sectionValuesAsFields != null ? sectionValuesAsFields.size() : 0); l++) {

                                DynamicSectionValuesDAO.Instance.Value instanceValue = sectionValuesAsFields.get(l);

                                List<DynamicFormSectionFieldDAO> fields = sectionDAO.getFields();
                                if (sectionDAO.getFields() != null) {
                                    for (int m = 0; m < sectionDAO.getFields().size(); m++) {

                                        if (fields != null) {
                                            DynamicFormSectionFieldDAO parentSectionField = fields.get(m);

                                            if (parentSectionField.isVisible()) {
                                                DynamicFormSectionFieldDAO tempField = Shared.getInstance().setObjectValues(parentSectionField);

                                                if (tempField.getSectionCustomFieldId() == instanceValue.getSectionCustomFieldId()) {
                                                    String value = instanceValue.getValue();
                                                    if (instanceValue.getType() == 13) // lookupvalue
                                                    {
                                                        value = instanceValue.getSelectedLookupText();
                                                        if (value == null)
                                                            value = instanceValue.getValue();
                                                    }
                                                    if (instanceValue.getType() == 11 && value != null && value.isEmpty())
                                                        value = tempField.getValue();

                                                    tempField.setValue(value);
                                                    if (tempField.getType() == 7) { // for attachments only
                                                        try {
                                                            getAttachmentsDetail(tempField, instanceValue);
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }

                                                    if (tempField.getType() == 19 || tempField.getType() == 18) // calculated and mapped
                                                    {
                                                        if (instanceValue.getType() == 7) {
                                                            tempField.setType(instanceValue.getType());
                                                            getAttachmentsDetail(tempField, instanceValue);
                                                        } else if (instanceValue.getType() == 11)
                                                            tempField.setType(instanceValue.getType());
                                                        else if (instanceValue.getType() == 4)
                                                            tempField.setType(instanceValue.getType());
                                                    }

                                                    finalFields.add(tempField);
                                                    // Latest Field will be add here
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            cardsList.add(new DynamicFormSectionFieldsCardsDAO(finalFields));
                        }

                        sectionDAO.setRefreshFieldsCardsList(cardsList);
                        sections.add(sectionDAO);

                    }

                }
                // sections = Shared.getInstance().removeInvisbleFields(sectionDAO, sections);
            }
        }

        ArrayList<DynamicFormSectionDAO> dynamicFormSectionDAOS = GetOnlyFieldsCards(response, sectionsValues);
        sections.clear();
        sections.addAll(dynamicFormSectionDAOS);
        return sections;
    }

    private void getAttachmentsDetail(DynamicFormSectionFieldDAO tempField, DynamicSectionValuesDAO.Instance.Value instanceValue) {
        try {
            String attachmentFileSize = "";
            String getOutputMediaFile = Shared.getInstance().getOutputMediaFile(Objects.requireNonNull(instanceValue.getDetails()).getName()).getPath();
            boolean isFileExist = Shared.getInstance().isFileExist(getOutputMediaFile, bContext);
            if (isFileExist) {
                File file = null;
                // String path = RealPathUtil.getPath(bContext, Uri.parse(getOutputMediaFile));
                file = new File(getOutputMediaFile);
                attachmentFileSize = Shared.getInstance().getAttachmentFileSize(file);
            }


            DyanmicFormSectionFieldDetailsDAO details = new DyanmicFormSectionFieldDetailsDAO();
            details.setDownloadUrl(Objects.requireNonNull(instanceValue.getDetails()).getDownloadUrl());
            details.setMimeType(instanceValue.getDetails().getMimeType());
            details.setCreatedOn(instanceValue.getDetails().getCreatedOn());
            details.setName(instanceValue.getDetails().getName());
            details.setFileSize(attachmentFileSize);
            tempField.setDetails(details);
        } catch (Exception e) {
            //  e.printStackTrace();
        }
    }

    private ArrayList<DynamicFormSectionDAO> GetOnlyFieldsCards(DynamicFormDAO response, List<DynamicSectionValuesDAO> sectionsValues) {

        ArrayList<DynamicFormSectionDAO> sections = new ArrayList<>();

        if (response.getSections() != null) {
            //Setting Sections With FieldsCards.
            for (DynamicFormSectionDAO sectionDAO : response.getSections()) {

                if (sectionDAO.getFields() != null && sectionDAO.getFields().size() > 0) {
                    List<DynamicFormSectionFieldDAO> fields;
                    if (sectionsValues != null)
                        fields = parentFieldList(sectionDAO, true);
                    else
                        fields = Shared.getInstance().invisibleList(sectionDAO, true);
                    DynamicFormSectionFieldsCardsDAO fieldsCard = new DynamicFormSectionFieldsCardsDAO(fields);
                    sectionDAO.getFieldsCardsList().add(fieldsCard);
                    sections.add(sectionDAO);
                }

            }
        }
        return sections;
    }

    private List<DynamicFormSectionFieldDAO> parentFieldList(DynamicFormSectionDAO sectionDAO, boolean isClearMappedCalculatedFields) {
        List<DynamicFormSectionFieldDAO> fields = sectionDAO.getFields();
        List<DynamicFormSectionFieldDAO> tempFields = new ArrayList<>();
        for (int h = 0; h < (fields != null ? fields.size() : 0); h++) {
            if (fields.get(h).isVisible() && !fields.get(h).isReadOnly()) {
                if (isClearMappedCalculatedFields && (fields.get(h).getType() == 18 || fields.get(h).getType() == 19))
                    fields.get(h).setValue("");

                tempFields.add(fields.get(h));
            }
        }
        return tempFields;
    }

    public void SingleSelection(final DynamicFormSectionFieldDAO single) {


        if (single != null) {

            String[] values = null;
            if (single.getLookupValues() != null && single.getLookupValues().size() > 0) {
                values = new String[single.getLookupValues().size()];
                int i = 0;
                for (DynamicFormSectionFieldLookupValuesDAO lookup : single.getLookupValues()) {

                    /*if (lookup.isSelected()) {
                    }*/
                    values[i] = lookup.getLabel();

                    i++;
                }
            }


            if (values != null && values.length > 0) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(bContext);
                builder.setTitle(single.getLabel());

                final String[] finalValues = values;

                builder.setItems(finalValues, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {


                        if (single.getLookupValues() != null && single.getLookupValues().size() > 0) {

                            for (DynamicFormSectionFieldLookupValuesDAO lookup : single.getLookupValues()) {

                                if (lookup.getLabel().toLowerCase().equals(finalValues[i].toLowerCase())) {
                                    lookup.setSelected(true);
                                    DynamicFormValuesDAO post = new DynamicFormValuesDAO();
                                    post.setSectionCustomFieldId(single.getSectionCustomFieldId());
                                    post.setSectionId(single.getObjectId());
                                    post.setValue(lookup.getId() + "");

                                    if (single.getPost() != null) {
                                        single.setPost(null);
                                    }
                                    single.setPost(post);

                                    single.setError_field(null);


                                } else {
                                    lookup.setSelected(false);
                                }
                            }
                        }

                        single.setViewGenerated(false);

                        if (mApplicationSectionsAdapter != null) {
                            mApplicationSectionsAdapter.notifyDataSetChanged();
                        }

                        if (dialog != null) {
                            dialog.dismiss();
                        }


                    }
                });
                // AlertDialog alert = builder.create();


                dialog = builder.create();

                dialog.show();
            }


        }

        /**/
    }


    @Override
    public void onFieldValuesChanged() {

        //Check the formValidation

        List<DynamicFormSectionFieldDAO> adapter_list = null;

        if (mApplicationSectionsAdapter != null) {
            adapter_list = mApplicationSectionsAdapter.GetAllFields();
        }


        if (adapter_list != null && adapter_list.size() > 0) {

            boolean isAllFieldsValidateTrue = true;

            for (DynamicFormSectionFieldDAO dynamicFormSectionFieldDAO : adapter_list) {
                if (dynamicFormSectionFieldDAO.getSectionType() != SECTIONCONSTANT) {


                    String error = "";
                    error = Shared.getInstance().edittextErrorChecks(bContext, dynamicFormSectionFieldDAO.getValue(), error, dynamicFormSectionFieldDAO);
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

            if (isAllFieldsValidateTrue) {
                btnSubmit.setEnabled(true);
                btnSubmit.setAlpha(1);
            } else {
                btnSubmit.setEnabled(false);
                btnSubmit.setAlpha(0.5f);
            }
        }


    }

    @Override
    public void onAttachmentFieldClicked(DynamicFormSectionFieldDAO fieldDAO, int position) {

        fieldToBeUpdated = fieldDAO;
        fieldToBeUpdated.setUpdatePositionAttachment(position);


        String getAllowedValuesCriteria = fieldToBeUpdated.getAllowedValuesCriteria();


        getAllowedValuesCriteria = getAllowedValuesCriteria.replaceAll("\\.", "");

        // getAllowedValuesCriteria = "image/" + getAllowedValuesCriteria;

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

        CustomLogs.displayLogs(TAG + " getAllowedValuesCriteria: " + getAllowedValuesCriteria + " mimeTypes: " + Arrays.toString(mimeTypes));

        // Intent getContentIntent = FileUtils.createGetContentIntent();

        final Intent getContentIntent = new Intent(Intent.ACTION_GET_CONTENT);
        // The MIME data type filter

        getContentIntent.setType("*/*");

        if (getAllowedValuesCriteria.length() > 0)
            getContentIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // Only return URIs that can be opened with ContentResolver
        getContentIntent.addCategory(Intent.CATEGORY_OPENABLE);

        Intent intent = Intent.createChooser(getContentIntent, getString(R.string.selectafile));
        startActivityForResult(intent, REQUEST_CHOOSER);

    }

    @Override
    public void onLookupFieldClicked(DynamicFormSectionFieldDAO fieldDAO, int position, boolean isCalculatedMappedField) {

        try {

            if (!pDialog.isShowing()) {
                fieldToBeUpdated = fieldDAO;
                fieldToBeUpdated.setUpdatePositionAttachment(position);

                Bundle bundle = new Bundle();
                bundle.putSerializable(DynamicFormSectionFieldDAO.Companion.getBUNDLE_KEY(), fieldToBeUpdated);
                bundle.putBoolean("isCalculatedMappedField", isCalculatedMappedField);

                Intent chooseLookupOption = new Intent(bContext, ChooseLookUpOption.class);
                chooseLookupOption.putExtras(bundle);
                startActivityForResult(chooseLookupOption, REQUEST_LOOKUP);

            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == REQUEST_CHOOSER && data != null) {

                final Uri uri = data.getData();

                if (uri != null) {
                    int getMaxVal = fieldToBeUpdated.getMaxVal();
                    boolean isFileSizeValid = true;
                    if (getMaxVal > 0)
                        isFileSizeValid = getFileSize(RealPathUtil.getPath(bContext, uri), getMaxVal);

                    CustomLogs.displayLogs(TAG + " getMaxVal: " + getMaxVal + " isFileSizeValid: " +
                            isFileSizeValid + " getRealPathFromURI: " + RealPathUtil.getPath(bContext, uri));

                    if (isFileSizeValid) {

                        try {
                            UpdateLoadImageForField(fieldToBeUpdated, uri);

                        } catch (Exception e) {
                            Shared.getInstance().messageBox(getString(R.string.pleasetryagain), bContext);
                        }
                    } else {

                        Shared.getInstance().showAlertMessage("", getString(R.string.sizeshouldbelessthen) + " " + getMaxVal + " " + getString(R.string.mb), bContext);
                    }
                }

            } else if (requestCode == REQUEST_LOOKUP && data != null) {

                DynamicFormSectionFieldDAO dfs = (DynamicFormSectionFieldDAO) data.getExtras().getSerializable(DynamicFormSectionFieldDAO.Companion.getBUNDLE_KEY());
                LookUpDAO lookup = (LookUpDAO) data.getExtras().getSerializable(LookUpDAO.Companion.getBUNDLE_KEY());
                boolean isCalculatedMappedField = data.getExtras().getBoolean("isCalculatedMappedField");
                if (fieldToBeUpdated != null && lookup != null) {
                    SetUpLookUpValues(fieldToBeUpdated, lookup, isCalculatedMappedField);
                }

            }

        }

    }


    private boolean getFileSize(String path, int getMaxVal) {
        File file = new File(path);
        double bytes = file.length();
        double kilobytes = (bytes / 1024);
        double megabytes = (kilobytes / 1024);


        return getMaxVal >= megabytes;

    }

    public void SubmitRequest(String whatodo) {


        //For new API version /submitv2
        DynamicResponseDAO submit_jsonNew = new Gson().fromJson(actualResponseJson, DynamicResponseDAO.class);//Shared.getInstance().CloneAddFormWithForm(actual_response);

        if (submit_jsonNew != null) {

            List<DynamicSectionValuesDAO> sectionValuesListToPost = new ArrayList<>();
           /* if (mApplicationSectionsAdapter == null) {
                Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.formisempty), bContext);
                return;
            }*/

            if (mApplicationSectionsAdapter != null && mApplicationSectionsAdapter.getmApplications() != null) {
                for (int s = 0; s < mApplicationSectionsAdapter.getmApplications().size(); s++) {
                    DynamicFormSectionDAO updatedSection = mApplicationSectionsAdapter.getmApplications().get(s);

                    //for (DynamicFormSectionDAO updatedSection : mApplicationSectionsAdapter.getmApplications()) {

                    if (updatedSection != null && updatedSection.getFieldsCardsList().size() > 0) {

                        DynamicSectionValuesDAO sectionValuesDAO = new DynamicSectionValuesDAO();

                        sectionValuesDAO.setId(updatedSection.getId());

                        List<DynamicSectionValuesDAO.Instance> instancesList = new ArrayList<>();

                        //For Setting InstancesList
                        for (DynamicFormSectionFieldsCardsDAO dynamicFormSectionFieldsCardsDAO : updatedSection.getFieldsCardsList()) {

                            DynamicSectionValuesDAO.Instance instance = new DynamicSectionValuesDAO.Instance();

                            List<DynamicSectionValuesDAO.Instance.Value> valuesList = new ArrayList<>();

                            if (dynamicFormSectionFieldsCardsDAO.getFields() != null) {
                                //For Setting Intance->Values
                                for (DynamicFormSectionFieldDAO dynamicFormSectionFieldDAO : dynamicFormSectionFieldsCardsDAO.getFields()) {

                                    if (actual_response.getSectionValues() != null && dynamicFormSectionFieldDAO.isReadOnly())
                                        continue;

                                    DynamicSectionValuesDAO.Instance.Value value = new DynamicSectionValuesDAO.Instance.Value();
                                    DynamicFormSectionFieldDAO dynamicFormSectionFieldDAOTemp = Shared.getInstance().setObjectValues(dynamicFormSectionFieldDAO);
                                    dynamicFormSectionFieldDAOTemp.setValue(dynamicFormSectionFieldDAO.getValue());
                                    value.setSectionCustomFieldId(dynamicFormSectionFieldDAOTemp.getSectionCustomFieldId());
                                    value.setType(dynamicFormSectionFieldDAOTemp.getType());
                                    value.setSectionId(dynamicFormSectionFieldDAOTemp.getObjectId());
                                    value.setValue(dynamicFormSectionFieldDAOTemp.getValue());

                                    if (dynamicFormSectionFieldDAOTemp.getType() == 11) {
                                        String finalValue = value.getValue();
                                        if (finalValue != null && !finalValue.isEmpty())
                                            finalValue += ":" + dynamicFormSectionFieldDAOTemp.getSelectedCurrencyId() + ":" + dynamicFormSectionFieldDAOTemp.getSelectedCurrencySymbol();

                                        value.setValue(finalValue);
                                    } else if (dynamicFormSectionFieldDAOTemp.getType() == 13) {
                                        if (dynamicFormSectionFieldDAOTemp.getLookupValue() != null || !TextUtils.isEmpty(dynamicFormSectionFieldDAOTemp.getLookupValue()))
                                            value.setValue(String.valueOf(dynamicFormSectionFieldDAOTemp.getId()));
                                    }

                                    CustomLogs.displayLogs(TAG + " value.getValue(): " + value.getValue() + " getType: " + dynamicFormSectionFieldDAOTemp.getType());
                                    valuesList.add(value);

                                }
                            }
                            // Adding Instances
                            instance.setValues(valuesList);
                            instancesList.add(instance);
                        }

                        //Adding Instances To SectionValue
                        sectionValuesDAO.setInstances(instancesList);
                        sectionValuesListToPost.add(sectionValuesDAO);
                    }


                }
            }
            if (sectionValuesListToPost.size() > 0)
                submit_jsonNew.setSectionValues(sectionValuesListToPost);

            if (whatodo.equalsIgnoreCase("calculatedValues"))
                getCalculatedValues(submit_jsonNew);
            else
                SubmitForm(submit_jsonNew, whatodo);
        }

    }//END SubmitRequest

    private void UpdateLoadImageForField(DynamicFormSectionFieldDAO field, Uri uri) {
        if (field != null) {

            MultipartBody.Part body = null;

            try {

                body = Shared.getInstance().prepareFilePart(uri, bContext);
                UpLoadFile(field, body, uri);

            } catch (Exception e) {
                Shared.getInstance().errorLogWrite("FILE", e.getMessage());
            }

        }
    }

    private void SetUpLookUpValues(DynamicFormSectionFieldDAO field, LookUpDAO lookup, boolean isCalculatedMappedField) {

        field.setValue(String.valueOf(lookup.getId()));
        field.setLookupValue(lookup.getName());
        field.setId(lookup.getId());
        if (mApplicationSectionsAdapter != null) {
            mApplicationSectionsAdapter.notifyDataSetChanged();
        }
        // if (isCalculatedMappedField)
        if (field.isTigger())
            SubmitRequest("calculatedValues");

    }

    private void UpLoadFile(final DynamicFormSectionFieldDAO field, final MultipartBody.Part body, final Uri uri) {

        start_loading_animation();
        try {

            APIs apis = Shared.getInstance().retroFitObject(getContext());

            call_upload = apis.upload(body);
            call_upload.enqueue(new Callback<ResponseFileUploadDAO>() {
                @Override
                public void onResponse(Call<ResponseFileUploadDAO> call, Response<ResponseFileUploadDAO> response) {
                    stop_loading_animation();
                    if (response != null && response.body() != null) {

                        if (field != null) {

                            try {

                                //File file = FileUtils.getFile(bContext, uri);

                                File file = null;
                                String path_arslan = RealPathUtil.getPath(bContext, uri);
                                file = new File(path_arslan);

                                String attachmentFileSize = Shared.getInstance().getAttachmentFileSize(file);
                                DyanmicFormSectionFieldDetailsDAO detail = new DyanmicFormSectionFieldDetailsDAO();
                                detail.setName(file.getName());
                                detail.setDownloadUrl(response.body().getDownloadUrl());
                                detail.setMimeType(FileUtils.getMimeType(file));
                                detail.setCreatedOn(Shared.getInstance().GetCurrentDateTime());
                                detail.setFileSize(attachmentFileSize);
                                field.setDetails(detail);

                                field.setValue(response.body().getFileId());


                                if (mApplicationSectionsAdapter != null) {
                                    mApplicationSectionsAdapter.notifyDataSetChanged();
                                }
                                if (field.isTigger())
                                    SubmitRequest("calculatedValues");

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }

                    } else {

                        Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext);
                    }

                }

                @Override
                public void onFailure(Call<ResponseFileUploadDAO> call, Throwable t) {
                    stop_loading_animation();
                    Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), bContext);
                    // UploadFileInformation(fileDAO);
                }
            });

        } catch (Exception ex) {
            stop_loading_animation();
            if (ex != null) {
                Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), bContext);
                //UploadFileInformation(fileDAO);

            }
        }
    }//LoggedInUser end

    private void SubmitForm(DynamicResponseDAO post, String whatodo) {

        start_loading_animation();
        try {

            APIs apis = Shared.getInstance().retroFitObject(bContext);

            if (whatodo.equals(getString(R.string.draft))) {
                submit_call = apis.DraftApplication(post);
            } else {
                submit_call = apis.SubmitApplication(post);
            }

            submit_call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    stop_loading_animation();
                    //  if (response != null && response.body() != null) {
                    if (response.code() == 409)
                        Shared.getInstance().showAlertMessage("", getString(R.string.closingdatepassed), bContext);
                    else {
                        if (response.isSuccessful()) {
                           /* Bundle bnd = new Bundle();
                            bnd.putBoolean("whatodo", true);
                            Intent intent = new Intent();
                            intent.putExtras(bnd);
                            bContext.setResult(2, intent);
                            bContext.finish();*/

                            if (ESPApplication.getInstance().isComponent())
                                bContext.finish();
                            else {
                                Intent intent = new Intent(bContext, ApplicationsActivityDrawer.class);
                                ComponentName cn = intent.getComponent();
                                Intent mainIntent = Intent.makeRestartActivityTask(cn);
                                startActivity(mainIntent);
                            }

                        } else
                            Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext);
                    }
                    /*} else {

                        Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext);
                    }
*/
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    stop_loading_animation();
                    Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), bContext);
                    // UploadFileInformation(fileDAO);
                }
            });

        } catch (Exception ex) {
            stop_loading_animation();
            if (ex != null) {
                Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), bContext);
                //UploadFileInformation(fileDAO);

            }
        }
    }//LoggedInUser end

    private void setGravity() {
        if (pref.getLanguage().equalsIgnoreCase("ar")) {
            category_name.setGravity(Gravity.RIGHT);
            txtcategory.setGravity(Gravity.RIGHT);

        } else {
            category_name.setGravity(Gravity.LEFT);
            txtcategory.setGravity(Gravity.LEFT);
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        registerReciever();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        unRegisterReciever();

    }

   /* private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          //  dynamicFormSectionFieldDAOCalculatedMapped = (DynamicFormSectionFieldDAO) intent.getSerializableExtra("dynamicFormSectionFieldDAO");
            //  int position = intent.getIntExtra("position", 0);
            callService();
        }
    };


    private void callService() {
        if (!isServiceRunning) {
            isServiceRunning = true;
            unRegisterReciever();
            SubmitRequest("calculatedValues");
        }
    }*/

    private void registerReciever() {
       /* isServiceRunning = false;
        if (getActivity() != null) {
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver,
                    new IntentFilter("getcalculatedvalues"));
        }*/

        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

    }

    private void unRegisterReciever() {
       /* if (getActivity() != null)
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);*/
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void dataRefreshEvent(EventOptions.EventTriggerController eventTriggerController) {
        unRegisterReciever();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SubmitRequest("calculatedValues");
            }
        }, 1000);

    }

    private void getCalculatedValues(DynamicResponseDAO post) {
        try {
            if (isKeyboardVisible)
                isCalculatedField = true;
            //  start_loading_animation();
            Call<List<CalculatedMappedFieldsDAO>> calculatedMapped_call = Shared.getInstance().retroFitObject(getActivity()).getCalculatedValues(post);

            calculatedMapped_call.enqueue(new Callback<List<CalculatedMappedFieldsDAO>>() {
                @Override
                public void onResponse(Call<List<CalculatedMappedFieldsDAO>> call, Response<List<CalculatedMappedFieldsDAO>> response) {

                    if (response != null && response.body() != null) {

                        List<CalculatedMappedFieldsDAO> calculatedMappedFieldsList = response.body();

                        for (int i = 0; i < calculatedMappedFieldsList.size(); i++) {
                            CalculatedMappedFieldsDAO calculatedMappedFieldsDAO = calculatedMappedFieldsList.get(i);

                            if (mApplicationSectionsAdapter.getmApplications() != null) {
                                List<DynamicFormSectionDAO> dynamicFormSectionDAOS = mApplicationSectionsAdapter.getmApplications();
                                for (int u = 0; u < dynamicFormSectionDAOS.size(); u++) {
                                    DynamicFormSectionDAO updatedSection = dynamicFormSectionDAOS.get(u);
                                    if (updatedSection.getFieldsCardsList().size() > 0) {
                                        //For Setting InstancesList
                                        //for (DynamicFormSectionFieldsCardsDAO dynamicFormSectionFieldsCardsDAO : updatedSection.getFieldsCardsList()) {

                                        for (int g = 0; g < updatedSection.getFieldsCardsList().size(); g++) {
                                            if (calculatedMappedFieldsDAO.getSectionIndex() == g) {
                                                DynamicFormSectionFieldsCardsDAO dynamicFormSectionFieldsCardsDAO = updatedSection.getFieldsCardsList().get(g);

                                                if (dynamicFormSectionFieldsCardsDAO.getFields() != null) {

                                                    for (int p = 0; p < dynamicFormSectionFieldsCardsDAO.getFields().size(); p++) {
                                                        DynamicFormSectionFieldDAO dynamicFormSectionFieldDAO = dynamicFormSectionFieldsCardsDAO.getFields().get(p);

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
                                                                    boolean isFileExist = Shared.getInstance().isFileExist(getOutputMediaFile, bContext);
                                                                    if (isFileExist) {
                                                                        File file = null;
                                                                        file = new File(getOutputMediaFile);
                                                                        attachmentFileSize = Shared.getInstance().getAttachmentFileSize(file);
                                                                    }

                                                                    calculatedMappedFieldsDAO.getDetails().setFileSize(attachmentFileSize);
                                                                }
                                                                dynamicFormSectionFieldDAO.setDetails(calculatedMappedFieldsDAO.getDetails());
                                                            } else if (targetFieldType == 4) {
                                                                String calculatedDisplayDate = Shared.getInstance().getDisplayDate(bContext, calculatedMappedFieldsDAO.getValue(), true);
                                                                dynamicFormSectionFieldDAO.setValue(calculatedDisplayDate);


                                                            } else if (targetFieldType == 11) {
                                                                DynamicFormSectionFieldDAO fieldDAO = Shared.getInstance().populateCurrency(calculatedMappedFieldsDAO.getValue());
                                                                String concateValue = fieldDAO.getValue() + " " + fieldDAO.getSelectedCurrencySymbol();
                                                                dynamicFormSectionFieldDAO.setValue(concateValue);


                                                            } else
                                                                dynamicFormSectionFieldDAO.setValue(calculatedMappedFieldsDAO.getValue());


                                                        }

                                                    }

                                                }
                                            }
                                        }
                                    }


                                }
                            }

                        }
                        if (mApplicationSectionsAdapter != null && !isNotified)
                            mApplicationSectionsAdapter.notifyDataSetChanged();


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
                        CustomLogs.displayLogs(TAG + " null response");
                        registerReciever();
                        // stop_loading_animation();
                        Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext);
                    }


                }

                @Override
                public void onFailure(Call<List<CalculatedMappedFieldsDAO>> call, Throwable t) {
                    CustomLogs.displayLogs(TAG + " failure response");
                    registerReciever();
                    //  stop_loading_animation();
                    Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            // stop_loading_animation();
            Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext);

        }
    }//LoggedInUser end


    public void refreshAdapter(boolean visible) {
        isNotified = visible;
    }


}

