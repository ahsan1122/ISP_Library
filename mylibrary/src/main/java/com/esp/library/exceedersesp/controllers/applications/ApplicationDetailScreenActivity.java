package com.esp.library.exceedersesp.controllers.applications;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.esp.library.R;
import com.esp.library.exceedersesp.ESPApplication;
import com.esp.library.ipaulpro.afilechooser.utils.FileUtils;
import com.esp.library.utilities.common.AlertActionWindow;
import com.esp.library.utilities.common.Constants;
import com.esp.library.utilities.common.CustomLogs;
import com.esp.library.utilities.common.Enums;
import com.esp.library.utilities.common.KeyboardUtils;
import com.esp.library.utilities.common.ProgressBarAnimation;
import com.esp.library.utilities.common.RealPathUtil;
import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.common.SharedPreference;
import com.esp.library.utilities.customevents.EventOptions;
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter;
import com.esp.library.exceedersesp.BaseActivity;
import com.esp.library.exceedersesp.controllers.feedback.ApplicationFeedbackAdapter;
import com.esp.library.exceedersesp.controllers.feedback.FeedbackForm;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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
import utilities.adapters.setup.applications.ApplicationStagesAdapter;
import utilities.adapters.setup.applications.ApplicationStatusAdapter;
import utilities.adapters.setup.applications.ListAddApplicationSectionsAdapter;
import utilities.adapters.setup.applications.ListUsersApplicationsAdapter;
import utilities.data.CriteriaRejectionfeedback.FeedbackDAO;
import utilities.data.apis.APIs;
import utilities.data.applicants.ApplicationSingleton;
import utilities.data.applicants.ApplicationsDAO;
import utilities.data.applicants.CalculatedMappedFieldsDAO;
import utilities.data.applicants.LinkApplicationsDAO;
import utilities.data.applicants.addapplication.CurrencyDAO;
import utilities.data.applicants.addapplication.LookUpDAO;
import utilities.data.applicants.addapplication.PostApplicationsStatusDAO;
import utilities.data.applicants.addapplication.ResponseFileUploadDAO;
import utilities.data.applicants.dynamics.DyanmicFormSectionFieldDetailsDAO;
import utilities.data.applicants.dynamics.DynamicFormDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldsCardsDAO;
import utilities.data.applicants.dynamics.DynamicFormValuesDAO;
import utilities.data.applicants.dynamics.DynamicResponseDAO;
import utilities.data.applicants.dynamics.DynamicSectionValuesDAO;
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO;
import utilities.data.applicants.dynamics.DynamicStagesDAO;
import utilities.data.applicants.feedback.ApplicationsFeedbackAttachmentsDAO;
import utilities.data.applicants.feedback.ApplicationsFeedbackDAO;
import utilities.interfaces.CriteriaFieldsListener;
import utilities.interfaces.FeedbackSubmissionClick;


public class ApplicationDetailScreenActivity extends BaseActivity implements ApplicationFieldsRecyclerAdapter.ApplicationDetailFieldsAdapterListener,
        AlertActionWindow.ActionInterface, FeedbackSubmissionClick, CriteriaFieldsListener {

    public static String ACTIVITY_NAME = "controllers.applications.ApplicationDetailScreenActivity";
    BaseActivity bContext;
    Call<DynamicResponseDAO> detail_call = null;
    ProgressBarAnimation anim = null;
    ApplicationsDAO mApplication;
    private ListAddApplicationSectionsAdapter mApplicationSectionsAdapter;
    // public static String appStatus;
    int statusId;
    DynamicFormSectionFieldDAO fieldToBeUpdated = null;
    private static final int REQUEST_CHOOSER = 12345;
    private static final int REQUEST_LOOKUP = 2;
    Call<Integer> submit_call = null;
    String actualResponseJson;
    SharedPreference pref;
    boolean isComingFromService, isClosingDatePassed;


    Toolbar toolbar;
    TextView categoryy;
    TextView toolbarheading;
    TextView definitionName;
    TextView startedOn;
    TextView applicationNumber;
    LinearLayout rejected_status;
    Button submit_btn;
    RecyclerView app_list;
    RecyclerView status_list;
    TextView txtrequest;
    TextView txtsubmittedon;
    TextView acceptedOn;
    TextView acceptedontext;
    RelativeLayout rlaccepreject;
    TextView categorytext;
    TextView heading;
    View linkdefinitioncard;
    SwitchCompat switchsubmissionallow;
    TextView definitionnameval;
    TextView submittedval;
    TextView accepted;
    TextView rejected;
    TextView txtmoreinfo;
    ImageView ivainforrow;
    RelativeLayout rldescription;
    TextView txtinfodescription;
    View pendinglineview;
    TextView txtrequestdetail;
    LinearLayout lldetail;
    TextView txtfeedback;
    ImageView ivnorecord;
    TextView txtfeedbacknorecord;
    LinearLayout llrows;
    RelativeLayout rldetailrow;
    RelativeLayout rlsubmissionrow;
    TextView txtsubmissions;
    LinearLayout llmaindetail;
    View topcardview;
    RelativeLayout main_content;
    RelativeLayout rlpendingfor;
    TextView pendingfor;
    RecyclerView rvStagesFieldsCards;
    LinearLayout llstages;
    TextView txtapprovalStages;
    TextView status;
    NestedScrollView nestedscrollview;
    ShimmerFrameLayout shimmer_view_container;
    ImageView ivdetailarrow;
    SwipeRefreshLayout swipeRefreshLayout;
    View vsperator;
    LinearLayout lldraftcard;
    TextView definitionNameTitle;
    TextView definitionDescription;
    TextView txtdetailrowtext;
    LinearLayout llnofeedbackrecord;
    RecyclerView rvFeedbackCommentsList;
    View llfeedback;
    TextView submissionallowedtext;
    RelativeLayout rlcategory;

    public boolean isCalculatedField = false;
    public boolean isKeyboardVisible = false;
    public static boolean criteriaWasLoaded = false;
    AlertDialog pDialog;
    boolean isAddStages = false;
    boolean isResubmit, isSubApplications, isComingfromAssessor;
    ApplicationStagesAdapter applicationStagesAdapter;
    boolean isServiceRunning, isNotified;
    DynamicResponseDAO actual_response = null;


    @Override
    public void mActionTo(String whattodo) {
        if (whattodo.equals(getString(R.string.draft))) {
            SubmitRequest(getString(R.string.draft));
        } else {
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_detail);
        initailizeIds();
        initailize();
        setGravity();
        loadData();



        swipeRefreshLayout.setOnRefreshListener(() -> loadData());

        txtfeedback.setText(pref.getlabels().getApplication() + " " + getString(R.string.feedback));
        Bundle bnd = getIntent().getExtras();
        if (bnd != null) {
            mApplication = (ApplicationsDAO) bnd.getSerializable(ApplicationsDAO.Companion.getBUNDLE_KEY());
            //   appStatus = bnd.getString("appStatus");
            statusId = bnd.getInt("statusId");
            isResubmit = bnd.getBoolean("isResubmit");
            isSubApplications = bnd.getBoolean("isSubApplications");
            isComingfromAssessor = bnd.getBoolean("isComingfromAssessor");
            UpdateTopView();


        }

        // in case of draft we do below work

        if ((statusId == 1 || isResubmit) && !isComingfromAssessor) // 1= New or draft
        {
            if (mApplication != null) {
                definitionNameTitle.setText(mApplication.getDefinitionName());

            }

            rldetailrow.setVisibility(View.GONE);
            llfeedback.setVisibility(View.GONE);
            lldraftcard.setVisibility(View.VISIBLE);
            llmaindetail.setVisibility(View.VISIBLE);
            lldetail.setVisibility(View.GONE);
            topcardview.setVisibility(View.GONE);
            rlsubmissionrow.setVisibility(View.GONE);
            vsperator.setVisibility(View.GONE);
            main_content.setBackgroundColor(ContextCompat.getColor(bContext, R.color.white));
        }

        definitionDescription.setOnClickListener(v -> {
            if (definitionDescription.getMaxLines() == 3)
                definitionDescription.setMaxLines(100);
            else
                definitionDescription.setMaxLines(3);
        });


        try {
            Shared.getInstance().createFolder(Constants.FOLDER_PATH, Constants.FOLDER_NAME, bContext);
        } catch (Exception e) {
            e.printStackTrace();
        }

        submit_btn.setOnClickListener(v -> {
            SubmitRequest(getString(R.string.submit));
        });

        rldetailrow.setOnClickListener(v -> {
            //  llrows.setVisibility(View.GONE);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    detailClick();
                }
            }, 300);


        });
        rlsubmissionrow.setOnClickListener(v -> {
            DynamicResponseDAO dynamicResponseDAO = new Gson().fromJson(actualResponseJson, DynamicResponseDAO.class);
            Intent i = new Intent(bContext, UserSubApplicationsActivity.class);
            i.putExtra("dynamicResponseDAO", dynamicResponseDAO);
            startActivity(i);
        });
        txtmoreinfo.setOnClickListener(v ->
        {
            if (txtmoreinfo.getText().toString().equalsIgnoreCase(getString(R.string.moreinformation))) {
                rldescription.setVisibility(View.VISIBLE);
                pendinglineview.setVisibility(View.VISIBLE);
                txtmoreinfo.setText(getString(R.string.lessinformation));
                ivainforrow.setImageResource(R.drawable.icons_blue_arrow_up);

            } else {

                rldescription.setVisibility(View.GONE);
                pendinglineview.setVisibility(View.GONE);
                txtmoreinfo.setText(getString(R.string.moreinformation));
                ivainforrow.setImageResource(R.drawable.iconsarrowdownblue);

            }

        });


        KeyboardUtils.addKeyboardToggleListener(this, new KeyboardUtils.SoftKeyboardToggleListener() {
            @Override
            public void onToggleSoftKeyboard(boolean isVisible) {
                isNotified = isVisible;
                isKeyboardVisible = isVisible;
            }
        });


        switchsubmissionallow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isComingFromService) {
                    if (isChecked)
                        submissionDialog(getString(R.string.allowsubmissiontext), getString(R.string.allowsubmission), getString(R.string.allowsubmission));
                    else
                        submissionDialog(getString(R.string.stopsubmissiontext), getString(R.string.stopsubmission), getString(R.string.stopsubmission));
                } else
                    isComingFromService = false;
            }
        });


    }

    private void initailizeIds() {

        toolbar = findViewById(R.id.toolbar);
        categoryy = findViewById(R.id.category);
        toolbarheading = findViewById(R.id.toolbarheading);
        definitionName = findViewById(R.id.definitionName);
        startedOn = findViewById(R.id.startedOn);
        acceptedOn = findViewById(R.id.acceptedOn);
        acceptedontext = findViewById(R.id.acceptedontext);
        rlaccepreject = findViewById(R.id.rlaccepreject);
        applicationNumber = findViewById(R.id.applicationNumber);
        rejected_status = findViewById(R.id.rejected_status);
        submit_btn = findViewById(R.id.submit_btn);
        status = findViewById(R.id.txtstatus);
        app_list = findViewById(R.id.app_list);
        status_list = findViewById(R.id.status_list);
        txtrequest = findViewById(R.id.applicationNumbertext);
        txtsubmittedon = findViewById(R.id.startedOntext);
        categorytext = findViewById(R.id.categorytext);
        heading = findViewById(R.id.heading);
        linkdefinitioncard = findViewById(R.id.linkcard);
        switchsubmissionallow = findViewById(R.id.switchsubmissionallow);
        definitionnameval = findViewById(R.id.definitionnameval);
        submittedval = findViewById(R.id.submittedval);
        accepted = findViewById(R.id.accepted);
        rejected = findViewById(R.id.rejected);
        txtmoreinfo = findViewById(R.id.txtmoreinfo);
        ivainforrow = findViewById(R.id.ivainforrow);
        rldescription = findViewById(R.id.rldescription);
        txtinfodescription = findViewById(R.id.txtinfodescription);
        pendinglineview = findViewById(R.id.pendinglineview);
        txtrequestdetail = findViewById(R.id.txtrequestdetail);
        lldetail = findViewById(R.id.lldetail);
        txtfeedback = findViewById(R.id.txtfeedback);
        ivnorecord = findViewById(R.id.ivnorecord);
        txtfeedbacknorecord = findViewById(R.id.txtfeedbacknorecord);
        llrows = findViewById(R.id.llrows);
        rldetailrow = findViewById(R.id.rldetailrow);
        rlsubmissionrow = findViewById(R.id.rlsubmissionrow);
        txtsubmissions = findViewById(R.id.txtsubmissions);
        llmaindetail = findViewById(R.id.llmaindetail);
        topcardview = findViewById(R.id.topcardview);
        main_content = findViewById(R.id.main_content);
        rlpendingfor = findViewById(R.id.rlpendingfor);
        pendingfor = findViewById(R.id.pendingfor);
        rvStagesFieldsCards = findViewById(R.id.rvStagesFieldsCards);
        llstages = findViewById(R.id.llstages);
        txtapprovalStages = findViewById(R.id.txtapprovalStages);
        nestedscrollview = findViewById(R.id.nestedscrollview);
        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        ivdetailarrow = findViewById(R.id.ivdetailarrow);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        vsperator = findViewById(R.id.vsperator);
        lldraftcard = findViewById(R.id.lldraftcard);
        definitionNameTitle = findViewById(R.id.definitionNameTitle);
        definitionDescription = findViewById(R.id.definitionDescription);
        txtdetailrowtext = findViewById(R.id.txtdetailrowtext);
        llnofeedbackrecord = findViewById(R.id.llnofeedbackrecord);
        rvFeedbackCommentsList = findViewById(R.id.rvFeedbackCommentsList);
        llfeedback = findViewById(R.id.llfeedback);
        submissionallowedtext = findViewById(R.id.submissionallowedtext);
        rlcategory = findViewById(R.id.rlcategory);
    }

    private void initailize() {
        bContext = ApplicationDetailScreenActivity.this;
        pref = new SharedPreference(bContext);
        pDialog = Shared.getInstance().setProgressDialog(bContext);

        RecyclerView.LayoutManager mApplicationLayoutManager = new LinearLayoutManager(bContext);
        app_list.setHasFixedSize(true);
        app_list.setLayoutManager(mApplicationLayoutManager);
        app_list.setItemAnimator(new DefaultItemAnimator());

        //status_list = findViewById(R.id.status_list);
        status_list.setHasFixedSize(true);
        status_list.setLayoutManager(new LinearLayoutManager(bContext, LinearLayoutManager.HORIZONTAL, false));
        status_list.setItemAnimator(new DefaultItemAnimator());


        RecyclerView.LayoutManager mApplicationLayoutManagerStages = new LinearLayoutManager(bContext);
        rvStagesFieldsCards.setHasFixedSize(true);
        rvStagesFieldsCards.setLayoutManager(mApplicationLayoutManagerStages);
        rvStagesFieldsCards.setItemAnimator(new DefaultItemAnimator());

        RecyclerView.LayoutManager mApplicationLayoutManagerComments = new LinearLayoutManager(bContext);
        rvFeedbackCommentsList.setHasFixedSize(true);
        rvFeedbackCommentsList.setLayoutManager(mApplicationLayoutManagerComments);
        rvFeedbackCommentsList.setItemAnimator(new DefaultItemAnimator());


        setupToolbar(toolbar);

    }

    private void detailClick() {
        if (llmaindetail.getVisibility() == View.VISIBLE && !isSubApplications) {
            llrows.setVisibility(View.VISIBLE);
            submit_btn.setVisibility(View.GONE);
            llstages.setVisibility(View.VISIBLE);

            if (Shared.getInstance().hasLinkDefinitionId(actual_response))
                linkdefinitioncard.setVisibility(View.VISIBLE);

            rldetailrow.setVisibility(View.VISIBLE);
            llfeedback.setVisibility(View.VISIBLE);
            llmaindetail.setVisibility(View.GONE);
            lldraftcard.setVisibility(View.GONE);
            main_content.setBackgroundColor(ContextCompat.getColor(bContext, R.color.pale_grey));
            topcardview.setVisibility(View.VISIBLE);
            if (actual_response != null && Shared.getInstance().hasLinkDefinitionId(actual_response))
                rlsubmissionrow.setVisibility(View.VISIBLE);
            // ivdetailarrow.setImageResource(R.drawable.ic_arrow_down);
        } else {

            // ivdetailarrow.setImageResource(R.drawable.ic_arrow_up);
            if ((statusId == 1 || isResubmit) && !isComingfromAssessor) {
                if (!isClosingDatePassed)
                    submit_btn.setVisibility(View.VISIBLE);

                lldraftcard.setVisibility(View.VISIBLE);
            }
            llstages.setVisibility(View.GONE);
            llmaindetail.setVisibility(View.VISIBLE);


            main_content.setBackgroundColor(ContextCompat.getColor(bContext, R.color.white));

            if (isSubApplications) {
                topcardview.setVisibility(View.VISIBLE);
                lldetail.setVisibility(View.VISIBLE);
            } else {
                txtrequestdetail.setVisibility(View.GONE);
                topcardview.setVisibility(View.GONE);
                llfeedback.setVisibility(View.GONE);
                rldetailrow.setVisibility(View.GONE);
                linkdefinitioncard.setVisibility(View.GONE);
                rlsubmissionrow.setVisibility(View.GONE);
                lldetail.setVisibility(View.GONE);


                ListUsersApplicationsAdapter.Companion.setSubApplications(false);
            }
        }

    }

    private void loadData() {
        if (Shared.getInstance().isWifiConnected(bContext)) {
            LoadStages();
        } else {
            Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), bContext);
        }
    }

    private void setupToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle("");
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_nav_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            ((ViewGroup.MarginLayoutParams) v.getLayoutParams()).bottomMargin =
                    insets.getSystemWindowInsetTop();
            return insets.consumeSystemWindowInsets();
        });
        toolbarheading = toolbar.findViewById(R.id.toolbarheading);
        UpdateTopView();
    }

    private void UpdateTopView() {
        if (mApplication != null) {
            String displayDate;
            if (mApplication.getSubmittedOn() != null && mApplication.getSubmittedOn().length() > 0) {
                displayDate = Shared.getInstance().getDisplayDate(bContext, mApplication.getSubmittedOn(), true);
            } else {
                displayDate = Shared.getInstance().getDisplayDate(bContext, mApplication.getCreatedOn(), true);
            }
            startedOn.setText(displayDate);
            long days = Shared.getInstance().fromStringToDate(bContext, displayDate);
            String daysVal = getString(R.string.day);
            if (days > 1)
                daysVal = getString(R.string.days);

            pendingfor.setText(days + " " + daysVal);

            if (isSubApplications) {
                categoryy.setText(mApplication.getApplicantName());
                categorytext.setText(getString(R.string.applicantcolon));
                rlpendingfor.setVisibility(View.VISIBLE);
                llmaindetail.setVisibility(View.VISIBLE);
                rldetailrow.setVisibility(View.GONE);
                status.setVisibility(View.GONE);
                txtrequestdetail.setVisibility(View.VISIBLE);
                toolbarheading.setText(getString(R.string.submissiondetails));
            } else {

                if (mApplication.getCategory().isEmpty())
                    rlcategory.setVisibility(View.GONE);

                categoryy.setText(mApplication.getCategory());
                rlpendingfor.setVisibility(View.GONE);
                status.setVisibility(View.VISIBLE);
                status.setText(mApplication.getStatus());
                ApplicationStatusAdapter statusAdapter = new ApplicationStatusAdapter(mApplication.getStageStatuses(), bContext);
                status_list.setAdapter(statusAdapter);
                setStatusColor(mApplication.getStatusId());
                toolbarheading.setText(pref.getlabels().getApplication() + " " + getString(R.string.details));
            }
            applicationNumber.setText(mApplication.getApplicationNumber());
            definitionName.setText(mApplication.getDefinitionName());


            /*categoryy.setText(mApplication.getCategory());*/


        }
    }

    public void GetApplicationDetail(String id) {

        start_loading_animation(false);

        final APIs apis = Shared.getInstance().retroFitObject(bContext);
        detail_call = apis.GetApplicationDetailv2(id);
        detail_call.enqueue(new Callback<DynamicResponseDAO>() {
            @Override
            public void onResponse(Call<DynamicResponseDAO> call, Response<DynamicResponseDAO> response) {

                if (response != null && response.body() != null) {
                    isClosingDatePassed = false;
                    status.setText(response.body().getApplicationStatus());
                    setStatusColor(response.body().getApplicationStatusId());
                    ApplicationSingleton.Companion.getInstace().setApplication(response.body());
                    txtinfodescription.setText(response.body().getDescription());


                    if (mApplication == null) {
                        mApplication = new ApplicationsDAO();
                        mApplication.setId(response.body().getApplicationId());
                        mApplication.setApplicationNumber(response.body().getApplicationNumber());
                        mApplication.setApplicantName(response.body().getApplicantName());
                        mApplication.setCategory(response.body().getCategory());
                        mApplication.setCreatedOn(response.body().getCreatedOn());
                        mApplication.setStatus(response.body().getApplicationStatus());
                        mApplication.setStatusId(response.body().getApplicationStatusId());
                        UpdateTopView();
                    }


                    if ((statusId == 1 || isResubmit) && !isComingfromAssessor) // 1= New or draft
                    {

                        definitionDescription.setText(response.body().getDescription());

                        stop_loading_animation(false);
                        // if (llrows.getVisibility() == View.GONE)
                        submit_btn.setVisibility(View.VISIBLE);
                        actual_response = response.body();
                        actualResponseJson = response.body().toJson();


                        List<DynamicSectionValuesDAO> sectionsValues = actual_response.getSectionValues();
                        ArrayList<DynamicFormSectionDAO> sections = GetFieldsCards(actual_response.getForm(), sectionsValues,
                                actual_response.getStages(), false, false);


                        if (sections != null && sections.size() > 0) {

                            mApplicationSectionsAdapter = new ListAddApplicationSectionsAdapter(sections, bContext, "", false);
                            mApplicationSectionsAdapter.setActualResponseJson(actualResponseJson);
                            app_list.setAdapter(mApplicationSectionsAdapter);
                            // mApplicationSectionsAdapter.notifyDataSetChanged();
                            mApplicationSectionsAdapter.setmApplicationFieldsAdapterListener2(ApplicationDetailScreenActivity.this);

                        }
                        checkExpiry(response);
                    } else {

                        if (Shared.getInstance().hasLinkDefinitionId(response.body())) {
                            GetLinkApplicationInfo(mApplication.getId() + "");
                            if (rldetailrow.getVisibility() == View.VISIBLE) {
                                rlsubmissionrow.setVisibility(View.VISIBLE);
                                linkdefinitioncard.setVisibility(View.VISIBLE);
                            }


                        } else {
                            rlsubmissionrow.setVisibility(View.GONE);
                            stop_loading_animation(false);
                        }


                        //lldetail.setVisibility(View.GONE);
                        submit_btn.setVisibility(View.GONE);
                        actual_response = response.body();
                        actualResponseJson = response.body().toJson();
                        List<DynamicSectionValuesDAO> sectionsValues = actual_response.getSectionValues();
                        ArrayList<DynamicFormSectionDAO> sections = GetFieldsCards(actual_response.getForm(),
                                sectionsValues, actual_response.getStages(), true, true);

                        if (sections.size() > 0) {
                            mApplicationSectionsAdapter = new ListAddApplicationSectionsAdapter(sections, bContext, "", true);
                            mApplicationSectionsAdapter.setActualResponseJson(actualResponseJson);
                            app_list.setAdapter(mApplicationSectionsAdapter);
                            //  mApplicationSectionsAdapter.notifyDataSetChanged();
                            mApplicationSectionsAdapter.setmApplicationFieldsAdapterListener2(ApplicationDetailScreenActivity.this);
                        }
                    }

                    getApplicationFeedBack(id);

                } else {
                    stop_loading_animation(false);
                    Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), bContext);
                }
            }

            @Override
            public void onFailure(Call<DynamicResponseDAO> call, Throwable t) {
                t.printStackTrace();
                stop_loading_animation(false);
                Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), bContext);
            }
        });

    }

    private void checkExpiry(Response<DynamicResponseDAO> response) {
        String endDateFormated = Shared.getInstance().getDisplayDate(bContext, response.body().getEndDate(), false);
        if (endDateFormated != null && !endDateFormated.isEmpty()) {
            String currentDateFormated = Shared.getInstance().GetCurrentDateTime();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
                Date endDate = sdf.parse(endDateFormated);
                String dateStr = sdf.format(sdf1.parse(currentDateFormated));
                Date currentDate = sdf.parse(dateStr);
                CustomLogs.displayLogs(ACTIVITY_NAME + " endDate: " + endDate + " currentDate: " + currentDate);

                if (currentDate.after(endDate)) {

                    stop_loading_animation(false);
                    String message = getString(R.string.notacceptrequest) + " " + mApplication.getDefinitionName() + " " +
                            getString(R.string.contactadministrator);
                    Shared.getInstance().showAlertMessage("", message, bContext);
                    submit_btn.setVisibility(View.GONE);
                    isClosingDatePassed = true;
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void GetLinkApplicationInfo(String id) {

        start_loading_animation(false);

        final APIs apis = Shared.getInstance().retroFitObject(bContext);
        Call<LinkApplicationsDAO> detail_call = apis.GetLinkApplicationInfo(id);
        detail_call.enqueue(new Callback<LinkApplicationsDAO>() {
            @Override
            public void onResponse(Call<LinkApplicationsDAO> call, Response<LinkApplicationsDAO> response) {
                if (response != null && response.body() != null) {
                    LinkApplicationsDAO body = response.body();
                    definitionnameval.setText(body.getLinkDefinitionName());

                    int submissioncount = body.getPendingLinkApplications() + body.getAcceptedLinkApplications() + body.getRejectedLinkApplications();
                    // if (submissioncount > 0)
                    //    rlsubmissionrow.setVisibility(View.VISIBLE);

                    txtsubmissions.setText(getString(R.string.submissions) + " (" + submissioncount + ")");


                    submittedval.setText(String.valueOf(body.getPendingLinkApplications()));
                    accepted.setText(String.valueOf(body.getAcceptedLinkApplications()));
                    rejected.setText(String.valueOf(body.getRejectedLinkApplications()));


                    if (statusId == 3 || statusId == 4) // 3 = accepted 4 = rejected
                    {
                        isComingFromService = true;
                        // switchsubmissionallow.setChecked(false);
                        submissionallowedtext.setText(getString(R.string.submissionsstopped));
                        switchsubmissionallow.setVisibility(View.INVISIBLE);
                    } else {
                        if (body.isSubmissionAllowed()) {
                            isComingFromService = true;
                            switchsubmissionallow.setChecked(true);
                        } else {
                            //  switchsubmissionallow.setChecked(false);
                            submissionallowedtext.setText(getString(R.string.submissionsstopped));
                            switchsubmissionallow.setVisibility(View.INVISIBLE);
                        }
                    }


                    stop_loading_animation(false);

                } else {
                    stop_loading_animation(false);
                    switchsubmissionallow.setEnabled(false);

                    //   Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), bContext);
                }
            }

            @Override
            public void onFailure(Call<LinkApplicationsDAO> call, Throwable t) {
                t.printStackTrace();
                stop_loading_animation(false);
                Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), bContext);
                return;
            }
        });
    }

    private void getApplicationFeedBack(String id) {

        start_loading_animation(false);

        APIs apis = Shared.getInstance().retroFitObject(bContext);

        Call<List<ApplicationsFeedbackDAO>> detail_call = apis.GetApplicationFeedBack(id);

        detail_call.enqueue(new Callback<List<ApplicationsFeedbackDAO>>() {
            @Override
            public void onResponse(Call<List<ApplicationsFeedbackDAO>> call, Response<List<ApplicationsFeedbackDAO>> response) {
                stop_loading_animation(false);
                if (response.body() != null && response.body().size() > 0) {
                    rvFeedbackCommentsList.setVisibility(View.VISIBLE);
                    llnofeedbackrecord.setVisibility(View.GONE);
                    ArrayList<FeedbackDAO> feedbackList = new ArrayList<FeedbackDAO>();
                    List<ApplicationsFeedbackDAO> body = response.body();
                    for (int i = 0; i < body.size(); i++) {
                        ApplicationsFeedbackDAO applicationsFeedbackDAO = body.get(i);
                        FeedbackDAO feedbackDao = new FeedbackDAO();
                        feedbackDao.setUserName(applicationsFeedbackDAO.getFullName());
                        feedbackDao.setComment(applicationsFeedbackDAO.getComment());
                        feedbackDao.setCheck(true);
                        feedbackDao.setUserImage(applicationsFeedbackDAO.getImageUrl());
                        String role = getString(R.string.member);
                        if (applicationsFeedbackDAO.isAdmin())
                            role = Enums.assessor.toString();

                       /* if (role.equalsIgnoreCase(bContext.getString(R.string.applicant)))
                            role = getString(R.string.member);*/


                        feedbackDao.setUserType(role);

                        for (int j = 0; j < applicationsFeedbackDAO.getAttachments().size(); j++) {
                            ApplicationsFeedbackAttachmentsDAO applicationsFeedbackAttachmentsDAO = applicationsFeedbackDAO.getAttachments().get(j);
                            DyanmicFormSectionFieldDetailsDAO dyanmicFormSectionFieldDetailsDAO = new DyanmicFormSectionFieldDetailsDAO();
                            dyanmicFormSectionFieldDetailsDAO.setMimeType(applicationsFeedbackAttachmentsDAO.getMimeType());
                            dyanmicFormSectionFieldDetailsDAO.setName(applicationsFeedbackAttachmentsDAO.getName());
                            dyanmicFormSectionFieldDetailsDAO.setDownloadUrl(applicationsFeedbackAttachmentsDAO.getDownloadUrl());
                            dyanmicFormSectionFieldDetailsDAO.setCreatedOn(applicationsFeedbackAttachmentsDAO.getCreatedOn());
                            feedbackDao.setAttachemntDetails(dyanmicFormSectionFieldDetailsDAO);
                        }
                        feedbackList.add(feedbackDao);
                    }
                    ApplicationFeedbackAdapter feedbackAdapter = new ApplicationFeedbackAdapter(feedbackList, bContext, false);
                    rvFeedbackCommentsList.setAdapter(feedbackAdapter);
                } else {
                    llnofeedbackrecord.setVisibility(View.VISIBLE);
                    rvFeedbackCommentsList.setVisibility(View.GONE);
                }

                //   loadData();
            }

            @Override
            public void onFailure(Call<List<ApplicationsFeedbackDAO>> call, Throwable t) {
                stop_loading_animation(false);
                llnofeedbackrecord.setVisibility(View.VISIBLE);
                rvFeedbackCommentsList.setVisibility(View.GONE);
            }
        });


    }

    public void postLinkDefinitionData(int definitionId, boolean isallowsubmission) {


        start_loading_animation(true);
        try {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("locale", Shared.getInstance().getLanguage(getBContext()))
                            .header("Authorization", "bearer " + ESPApplication.getInstance().getUser().getLoginResponse().getAccess_token());
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });

            httpClient.connectTimeout(10, TimeUnit.SECONDS);
            httpClient.readTimeout(10, TimeUnit.SECONDS);
            httpClient.writeTimeout(10, TimeUnit.SECONDS);

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();


            final APIs apis = retrofit.create(APIs.class);

            Call<LinkApplicationsDAO> status_call;
            status_call = apis.saveLinkApplicationInfo(definitionId, isallowsubmission);


            status_call.enqueue(new Callback<LinkApplicationsDAO>() {
                @Override
                public void onResponse(Call<LinkApplicationsDAO> call, Response<LinkApplicationsDAO> response) {
                    stop_loading_animation(true);
                    if (!response.body().isSubmissionAllowed()) {

                        submissionallowedtext.setText(getString(R.string.submissionsstopped));
                        switchsubmissionallow.setVisibility(View.INVISIBLE);
                        loadData();
                    }
                }

                @Override
                public void onFailure(Call<LinkApplicationsDAO> call, Throwable t) {
                    t.printStackTrace();
                    stop_loading_animation(true);
                    if (getBContext() != null) {
                        Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), bContext);
                    }
                }
            });

        } catch (Exception ex) {

            stop_loading_animation(true);
            if (getBContext() != null) {
                Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), bContext);
            }
        }
    }

    public void LoadStages() {


        start_loading_animation(false);
        try {

            Call<List<CurrencyDAO>> call = Shared.getInstance().retroFitObject(bContext).getCurrency();

            call.enqueue(new Callback<List<CurrencyDAO>>() {
                @Override
                public void onResponse(Call<List<CurrencyDAO>> call, Response<List<CurrencyDAO>> response) {

                    if (response.body() != null && response.body().size() > 0) {
                        ESPApplication.getInstance().setCurrencies(response.body());

                        if (mApplication != null) {

                            if (Shared.getInstance().isWifiConnected(bContext)) {
                                if (mApplication == null)
                                    GetApplicationDetail(getIntent().getIntExtra("applicationId", 0) + "");
                                else
                                    GetApplicationDetail(mApplication.getId() + "");
                            } else {
                                Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), bContext);
                            }

                        }

                    } else
                        stop_loading_animation(false);
                }

                @Override
                public void onFailure(Call<List<CurrencyDAO>> call, Throwable t) {
                    stop_loading_animation(false);
                }
            });

        } catch (Exception ex) {
            stop_loading_animation(false);
        }
    }//LoggedInUser end

    private void setStatusColor(int statusId) {

        rejected_status.setVisibility(View.GONE);
        status.setBackgroundResource(R.drawable.status_background);
        GradientDrawable drawable = (GradientDrawable) status.getBackground();
        switch (statusId) {
            case 0: // Invited
                status.setText(getString(R.string.invited));
                status.setTextColor(getResources().getColor(R.color.status_invited));
                drawable.setColor(ContextCompat.getColor(bContext, R.color.status_invited_background));
                break;
            case 1: // New as Draft
                status.setText(getString(R.string.draftcaps));
                status.setTextColor(getResources().getColor(R.color.status_draft));
                drawable.setColor(ContextCompat.getColor(bContext, R.color.status_draft_background));
                break;
            case 2: // Pending
                status.setText(getString(R.string.inprogress));
                status.setTextColor(getResources().getColor(R.color.status_pending));
                drawable.setColor(ContextCompat.getColor(bContext, R.color.status_pending_background));
                break;
            case 3: // Accepted
                status.setText(getString(R.string.accepted));
                status.setTextColor(getResources().getColor(R.color.status_accepted));
                drawable.setColor(ContextCompat.getColor(bContext, R.color.status_accepted_background));
                break;
            case 4:  // Rejected
                status.setText(getString(R.string.rejected));
                status.setTextColor(getResources().getColor(R.color.status_rejected));
                drawable.setColor(ContextCompat.getColor(bContext, R.color.status_rejected_background));
                submit_btn.setText(getString(R.string.resubmit));
                break;

            case 5:  // Cancelled
                status.setText(getString(R.string.cancelled));
                status.setTextColor(getResources().getColor(R.color.status_draft));
                drawable.setColor(ContextCompat.getColor(bContext, R.color.status_draft_background));
                break;
        }
    }

    private ArrayList<DynamicFormSectionDAO> GetFieldsCards(DynamicFormDAO response, List<DynamicSectionValuesDAO> sectionsValues,
                                                            List<DynamicStagesDAO> responseStages, boolean isAddStage, boolean isSubmit) {
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
                                                        tempField.setLookupValue(value);
                                                        if (instanceValue.getValue() != null && Shared.getInstance().isNumeric(instanceValue.getValue()))
                                                            tempField.setId(Integer.parseInt(instanceValue.getValue()));
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
                                                        } else if (instanceValue.getType() == 11 && isAddStages) // here isAddStages bit is used for isviewonly
                                                            tempField.setType(instanceValue.getType());
                                                        else if (instanceValue.getType() == 4 && isAddStages)  // here isAddStages bit is used for isviewonly
                                                            tempField.setType(instanceValue.getType());
                                                    }

                                                    sectionDAO.getFields().set(m, tempField);
                                                    if (isSubmit) {
                                                        DynamicFormSectionFieldDAO fieldDAO = sectionDAO.getFields().get(m);
                                                        finalFields.add(fieldDAO);
                                                    }

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (!isSubmit) // drafted case
                            {
                                if (sectionDAO.getFields() != null) {
                                    finalFields.clear();
                                    finalFields.addAll(sectionDAO.getFields());
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

        // both assessor and applicant are same then show stages

        if (isAddStage) {
            isAddStages = true;
            populateStagesData(responseStages);

        }

        return sections;
    }

    private void populateStagesData(List<DynamicStagesDAO> responseStages) {
        if (pref.getSelectedUserRole().equalsIgnoreCase(Enums.assessor.toString()))
            isComingfromAssessor = true;

        if (isComingfromAssessor || actual_response.getStageVisibilityApplicant().equalsIgnoreCase(Enums.all.toString())
                || actual_response.getStageVisibilityApplicant().equalsIgnoreCase(Enums.allwithfeedback.toString())
                || actual_response.getStageVisibilityApplicant().equalsIgnoreCase(Enums.current.toString())) {
            isComingfromAssessor = true; // if status = ALL or Current or All with feedback
            txtapprovalStages.setVisibility(View.VISIBLE);
        } else
            txtapprovalStages.setVisibility(View.GONE);


        List<DynamicStagesDAO> tempStages = new ArrayList<>();
        for (int i = 0; i < responseStages.size(); i++) {
            if (responseStages.get(i).isEnabled()) {
                tempStages.add(responseStages.get(i));
            }
        }

        if (tempStages.size() > 0) {
            applicationStagesAdapter = new ApplicationStagesAdapter(isComingfromAssessor, tempStages,
                    actualResponseJson, bContext, nestedscrollview);
            rvStagesFieldsCards.setAdapter(applicationStagesAdapter);
            applicationStagesAdapter.notifyDataSetChanged();
            if (actual_response.getApplicationStatusId() == 4 || actual_response.getApplicationStatusId() == 3)
            // getApplicationStatusId = 4 for application rejected
            // getStatusId = 2 for stage rejected
            // getApplicationStatusId = 3 for application accepted
            // getStatusId = 1 for stage accepted
            {
                String getDate = "";
                String getDisplayString = "";
                for (int i = 0; i < tempStages.size(); i++) {
                    if ((tempStages.get(i).getStatusId() == 2 || tempStages.get(i).getStatusId() == 1) && (tempStages.get(i).getCompletedOn() != null || !tempStages.get(i).getCompletedOn().isEmpty())) {
                        getDate = tempStages.get(i).getCompletedOn();
                        if (tempStages.get(i).getStatusId() == 1)
                            getDisplayString = getString(R.string.acceptedon);
                        else
                            getDisplayString = getString(R.string.rejectedon);
                    }
                }

                String displayDate = Shared.getInstance().getDisplayDate(bContext, getDate, true);
                rlaccepreject.setVisibility(View.VISIBLE);
                acceptedontext.setText(getDisplayString);
                acceptedOn.setText(displayDate);
            }

        } else {
            txtapprovalStages.setVisibility(View.GONE);
            if (actual_response.getApplicationSubmittedDate() != null && actual_response.getApplicationSubmittedDate().length() > 0) {
                String displayDate = Shared.getInstance().getDisplayDate(bContext, actual_response.getApplicationSubmittedDate(), true);
                acceptedOn.setText(displayDate);
            }


        }
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

    public void SubmitRequest(String whatodo) {

        DynamicResponseDAO submit_jsonNew = new Gson().fromJson(actualResponseJson, DynamicResponseDAO.class);

        if (submit_jsonNew != null) {

            List<DynamicSectionValuesDAO> sectionValuesListToPost = new ArrayList<>();

            if (mApplicationSectionsAdapter == null) {
                Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.formisempty), bContext);
                return;
            }

            for (DynamicFormSectionDAO updatedSection : mApplicationSectionsAdapter.getmApplications()) {

                if (updatedSection.getFieldsCardsList() != null && updatedSection.getFieldsCardsList().size() > 0) {

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

                                DynamicSectionValuesDAO.Instance.Value value = new DynamicSectionValuesDAO.Instance.Value();

                                value.setSectionCustomFieldId(dynamicFormSectionFieldDAO.getSectionCustomFieldId());
                                value.setType(dynamicFormSectionFieldDAO.getType());
                                value.setValue(dynamicFormSectionFieldDAO.getValue());

                                if (dynamicFormSectionFieldDAO.getType() == 11) {
                                    String finalValue = value.getValue();
                                    if (finalValue != null && !finalValue.isEmpty())
                                        finalValue += ":" + dynamicFormSectionFieldDAO.getSelectedCurrencyId() + ":" + dynamicFormSectionFieldDAO.getSelectedCurrencySymbol();

                                    value.setValue(finalValue);
                                }
                                CustomLogs.displayLogs(ACTIVITY_NAME + " value.getValue(): " + value.getValue());

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

            if (sectionValuesListToPost.size() > 0)
                submit_jsonNew.setSectionValues(sectionValuesListToPost);


            CustomLogs.displayLogs(ACTIVITY_NAME + " post.getApplicationStatus(): " + submit_jsonNew.toJson());


            if (whatodo.equalsIgnoreCase("calculatedValues"))
                getCalculatedValues(submit_jsonNew);
            else
                SubmitForm(submit_jsonNew, whatodo);

        }

    }//END SubmitRequest

    @Override
    public void onFieldValuesChanged() {
        List<DynamicFormSectionFieldDAO> adapter_list = null;

        if (mApplicationSectionsAdapter != null) {
            adapter_list = mApplicationSectionsAdapter.GetAllFields();
        }
        if (adapter_list != null && adapter_list.size() > 0) {
            boolean isAllFieldsValidateTrue = true;
            for (DynamicFormSectionFieldDAO dynamicFormSectionFieldDAO : adapter_list) {

                if (dynamicFormSectionFieldDAO.getSectionType() != ApplicationFieldsRecyclerAdapter.SECTIONCONSTANT) {

                    String error = "";
                    error = Shared.getInstance().edittextErrorChecks(bContext, dynamicFormSectionFieldDAO.getValue(), error, dynamicFormSectionFieldDAO);
                    if (error.length() > 0) {
                        isAllFieldsValidateTrue = false;
                        break;
                    }

                    if (!dynamicFormSectionFieldDAO.isShowToUserOnly())  // if fields are not for displayed then validate
                    {
                        if (dynamicFormSectionFieldDAO.isRequired()) {
                            if (!dynamicFormSectionFieldDAO.isValidate()) {
                                isAllFieldsValidateTrue = false;
                                break;
                            }
                        }
                    }
                }
            }

            if (isAllFieldsValidateTrue) {
                submit_btn.setEnabled(true);
                submit_btn.setAlpha(1);

            } else {
                submit_btn.setEnabled(false);
                submit_btn.setAlpha(0.5f);
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

        CustomLogs.displayLogs(ACTIVITY_NAME + " getAllowedValuesCriteria: " + getAllowedValuesCriteria + " mimeTypes: " + Arrays.toString(mimeTypes));

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
                        isFileSizeValid = Shared.getInstance().getFileSize(RealPathUtil.getPath(bContext, uri), getMaxVal);

                    CustomLogs.displayLogs(ACTIVITY_NAME + " getMaxVal: " + getMaxVal + " isFileSizeValid: " +
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
                LookUpDAO lookup = (LookUpDAO) data.getExtras().getSerializable(LookUpDAO.Companion.getBUNDLE_KEY());
                boolean isCalculatedMappedField = data.getExtras().getBoolean("isCalculatedMappedField");
                if (fieldToBeUpdated != null && lookup != null) {
                    SetUpLookUpValues(fieldToBeUpdated, lookup, isCalculatedMappedField);
                }

            }

        }

    }

    public void UpdateLoadImageForField(DynamicFormSectionFieldDAO field, Uri uri) {
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

    public void SetUpLookUpValues(DynamicFormSectionFieldDAO field, LookUpDAO lookup, boolean isCalculatedMappedField) {

        field.setValue(String.valueOf(lookup.getId()));
        field.setLookupValue(lookup.getName());
        field.setId(lookup.getId());


        if (applicationStagesAdapter != null)
            applicationStagesAdapter.getCriteriaAdapter().notifyOnly(fieldToBeUpdated.getUpdatePositionAttachment());
        else if (mApplicationSectionsAdapter != null) {
            mApplicationSectionsAdapter.notifyDataSetChanged();
            // if (isCalculatedMappedField)
            if (field.isTigger() && ApplicationFieldsRecyclerAdapter.isCalculatedMappedField)
                SubmitRequest("calculatedValues");

        }

    }

    private void UpLoadFile(final DynamicFormSectionFieldDAO field,
                            final MultipartBody.Part body, final Uri uri) {

        start_loading_animation(true);
        try {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
            if (Constants.WRITE_LOG) {
                httpClient.addInterceptor(logging);
            }
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("locale", Shared.getInstance().getLanguage(bContext))
                            .header("Authorization", "bearer " + ESPApplication.getInstance().getUser().getLoginResponse().getAccess_token());
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });


            httpClient.connectTimeout(5, TimeUnit.MINUTES);
            httpClient.readTimeout(5, TimeUnit.MINUTES);
            httpClient.writeTimeout(5, TimeUnit.MINUTES);

            /* retrofit builder and call web service*/
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();

            /* APIs Mapping respective Object*/
            APIs apis = retrofit.create(APIs.class);

            Call<ResponseFileUploadDAO> call_upload = apis.upload(body);
            call_upload.enqueue(new Callback<ResponseFileUploadDAO>() {
                @Override
                public void onResponse(Call<ResponseFileUploadDAO> call, Response<ResponseFileUploadDAO> response) {
                    stop_loading_animation(true);
                    if (response != null && response.body() != null) {

                        if (field != null) {

                            try {

                                //File file = FileUtils.getFile(bContext, uri);

                                File file = null;
                                String path = RealPathUtil.getPath(bContext, uri);
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


                                if (applicationStagesAdapter != null)
                                    applicationStagesAdapter.getCriteriaAdapter().notifyOnly(fieldToBeUpdated.getUpdatePositionAttachment());
                                else if (mApplicationSectionsAdapter != null) {
                                    mApplicationSectionsAdapter.notifyDataSetChanged();
                                    if (ApplicationFieldsRecyclerAdapter.isCalculatedMappedField)
                                        SubmitRequest("calculatedValues");
                                }


                            } catch (Exception e) {
                            }
                        }

                    } else {

                        Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext);
                    }

                }

                @Override
                public void onFailure(Call<ResponseFileUploadDAO> call, Throwable t) {
                    stop_loading_animation(true);
                    Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), bContext);
                    // UploadFileInformation(fileDAO);
                }
            });

        } catch (Exception ex) {
            stop_loading_animation(true);
            if (ex != null) {
                Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), bContext);
                //UploadFileInformation(fileDAO);

            }
        }
    }//LoggedInUser end

    private void SubmitForm(DynamicResponseDAO post, String whatodo) {

        if (post.getApplicationStatus().equalsIgnoreCase(Enums.rejected.toString()))
            post.setApplicationId(0);


        start_loading_animation(true);
        try {

            APIs apis = Shared.getInstance().retroFitObject(bContext);

            if (whatodo.equalsIgnoreCase(getString(R.string.draft))) {
                submit_call = apis.DraftApplication(post);
            } else {
                submit_call = apis.SubmitApplication(post);
            }

            submit_call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {
                    stop_loading_animation(true);

                    if (response.code() == 409)
                        Shared.getInstance().showAlertMessage("", getString(R.string.closingdatepassed), bContext);
                    else {
                        if (response != null && response.body() != null) {

                           /* Bundle bnd = new Bundle();
                            bnd.putBoolean("whatodo", true);
                            Intent intent = new Intent();
                            intent.putExtras(bnd);
                            setResult(2, intent);
                            finish();*/

                            if (ESPApplication.getInstance().isComponent())
                                finish();
                            else {
                                Intent intent = new Intent(bContext, ApplicationsActivityDrawer.class);
                                ComponentName cn = intent.getComponent();
                                Intent mainIntent = Intent.makeRestartActivityTask(cn);
                                startActivity(mainIntent);
                            }
                        } else {

                            Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext);
                        }
                    }

                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    stop_loading_animation(true);
                    Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), bContext);
                    // UploadFileInformation(fileDAO);
                }
            });

        } catch (Exception ex) {
            stop_loading_animation(true);
            if (ex != null) {
                Shared.getInstance().showAlertMessage(getString(R.string.error), getString(R.string.some_thing_went_wrong), bContext);
                //UploadFileInformation(fileDAO);

            }
        }
    }//LoggedInUser end

    public void stagefeedbackSubmitForm(PostApplicationsStatusDAO post) {


        start_loading_animation(true);
        try {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("locale", Shared.getInstance().getLanguage(bContext))
                            .header("Authorization", "bearer " + ESPApplication.getInstance().getUser().getLoginResponse().getAccess_token());
                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });

            httpClient.connectTimeout(10, TimeUnit.SECONDS);
            httpClient.readTimeout(10, TimeUnit.SECONDS);
            httpClient.writeTimeout(10, TimeUnit.SECONDS);

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();


            final APIs apis = retrofit.create(APIs.class);

            Call<Integer> status_call = apis.AcceptRejectApplication(post);


            status_call.enqueue(new Callback<Integer>() {
                @Override
                public void onResponse(Call<Integer> call, Response<Integer> response) {

                    stop_loading_animation(true);
                    CustomLogs.displayLogs(ACTIVITY_NAME + " stagefeedbackSubmitForm: " + response);
                    GetApplicationDetail(mApplication.getId() + "");
                }

                @Override
                public void onFailure(Call<Integer> call, Throwable t) {
                    stop_loading_animation(true);
                    if (t != null && bContext != null) {
                        Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), bContext);
                    }
                }
            });

        } catch (Exception ex) {
            if (ex != null) {
                stop_loading_animation(true);
                if (ex != null && bContext != null) {
                    Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), bContext);
                }
            }
        }
    }

    private void setGravity() {
        if (pref.getLanguage().equalsIgnoreCase("ar")) {
            definitionName.setGravity(Gravity.RIGHT);
            categoryy.setGravity(Gravity.RIGHT);
            applicationNumber.setGravity(Gravity.RIGHT);
            txtrequest.setGravity(Gravity.RIGHT);
            startedOn.setGravity(Gravity.RIGHT);
            txtsubmittedon.setGravity(Gravity.RIGHT);
            status.setGravity(Gravity.RIGHT);
            heading.setGravity(Gravity.RIGHT);
            categorytext.setGravity(Gravity.RIGHT);
            ivdetailarrow.setImageResource(R.drawable.ic_left_arrow);

        } else {
            definitionName.setGravity(Gravity.LEFT);
            categoryy.setGravity(Gravity.LEFT);
            applicationNumber.setGravity(Gravity.LEFT);
            txtrequest.setGravity(Gravity.LEFT);
            startedOn.setGravity(Gravity.LEFT);
            txtsubmittedon.setGravity(Gravity.LEFT);
            status.setGravity(Gravity.LEFT);
            heading.setGravity(Gravity.LEFT);
            categorytext.setGravity(Gravity.LEFT);
            ivdetailarrow.setImageResource(R.drawable.ic_arrow_right);
        }
    }

    private void submissionDialog(String msg, String buttonText, String title) {

        new MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                .setTitle(title)
                .setCancelable(false)
                .setMessage(msg)
                .setPositiveButton(buttonText, (dialogInterface, i) -> {
                    if (Shared.getInstance().isWifiConnected(bContext)) {
                        if (title.equalsIgnoreCase(getString(R.string.allowsubmission)))
                            postLinkDefinitionData(mApplication.getId(), true);
                        else
                            postLinkDefinitionData(mApplication.getId(), false);
                    } else {
                        Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), bContext);
                    }
                    dialogInterface.dismiss();
                })
                .setNeutralButton(getString(R.string.cancel), (dialogInterface, i) -> {
                    isComingFromService = true;
                    if (title.equalsIgnoreCase(getString(R.string.allowsubmission))) {
                        switchsubmissionallow.setChecked(false);
                    } else {
                        switchsubmissionallow.setChecked(true);
                    }
                    dialogInterface.dismiss();
                })
                .show();


    }

    public void SubmitStageRequest(boolean isAccepted, DynamicStagesCriteriaListDAO criteriaListDAO) {

        DynamicResponseDAO submit_jsonNew = new Gson().fromJson(actualResponseJson, DynamicResponseDAO.class);//Shared.getInstance().CloneAddFormWithForm(actual_response);
        List<DynamicFormValuesDAO> criteriaFormValues = getCriteriaFormValues(criteriaListDAO);

        // CustomLogs.displayLogs(ACTIVITY_NAME + " post.ApplicationSingleton(): " + ApplicationSingleton.getInstace().getApplication().getApplicationId());
        PostApplicationsStatusDAO post = new PostApplicationsStatusDAO();

        criteriaListDAO.setFormValues(criteriaFormValues);
        post.setAccepted(isAccepted);
        post.setApplicationId(submit_jsonNew.getApplicationId());
        post.setAssessmentId(criteriaListDAO.getAssessmentId());
        post.setComments("");
        post.setStageId(criteriaListDAO.getStageId());
        post.setValues(criteriaFormValues);


        CustomLogs.displayLogs(ACTIVITY_NAME + " post.getApplicationStatus(): " + post.toJson() + " toString: " + post.toString());
        stagefeedbackSubmitForm(post);


    }//END SubmitRequest

    private List<DynamicFormValuesDAO> getCriteriaFormValues(DynamicStagesCriteriaListDAO criteriaListDAO) {
        int sectionId = 0;
        List<DynamicFormValuesDAO> formValuesList = new ArrayList<>();
        DynamicFormDAO form = criteriaListDAO.getForm();
        if (form.getSections() != null) {
            for (DynamicFormSectionDAO sections : form.getSections()) {
                CustomLogs.displayLogs(ACTIVITY_NAME + " sections.getId(): " + sections.getId());
                sectionId = sections.getId();
                if (sections.getFields() != null) {
                    for (DynamicFormSectionFieldDAO dynamicFormSectionFieldDAO : sections.getFields()) {
                        DynamicFormValuesDAO value = new DynamicFormValuesDAO();
                        value.setSectionCustomFieldId(dynamicFormSectionFieldDAO.getSectionCustomFieldId());
                        value.setType(dynamicFormSectionFieldDAO.getType());
                        value.setValue(dynamicFormSectionFieldDAO.getValue());
                        value.setSectionId(sectionId);
                        value.setDetails(value.getDetails());
                        if (dynamicFormSectionFieldDAO.getType() == 11) {
                            String finalValue = value.getValue();
                            if (finalValue != null && !finalValue.isEmpty())
                                finalValue += ":" + dynamicFormSectionFieldDAO.getSelectedCurrencyId() + ":" + dynamicFormSectionFieldDAO.getSelectedCurrencySymbol();

                            value.setValue(finalValue);
                        }
                        CustomLogs.displayLogs(ACTIVITY_NAME + " value.getValue(): " + value.getValue());
                        formValuesList.add(value);
                    }
                }
            }
        }

        return formValuesList;
    }

    @Override
    public void feedbackClick(boolean isAccepted, @Nullable DynamicStagesCriteriaListDAO criteriaListDAO, @Nullable DynamicStagesDAO dynamicStagesDAO, int position) {

        boolean isApproved = isAccepted;

        if (dynamicStagesDAO != null) {
            int count = 0;
            DynamicResponseDAO actualResponseJsonsubmitJson = new Gson().fromJson(actualResponseJson, DynamicResponseDAO.class);
            DynamicStagesDAO dynamicStagesDAO1 = actualResponseJsonsubmitJson.getStages().get(actualResponseJsonsubmitJson.getStages().size() - 1);
            int size = dynamicStagesDAO1.getCriteriaList().size();
            if (dynamicStagesDAO.isAll()) {

                //if last stage and last criteria then open feedback on approve button
                //if last stage and any criteria then open feedback on reject button

                int getCount = criteriaCount(dynamicStagesDAO1, count, size);
                if (isApproved && getCount == size - 1)
                    isApproved = false;

            } else {

                //if last stage and last criteria then open feedback on reject button
                //if last stage and any criteria then open feedback on approve button

                if (!isApproved) {
                    int getCount = criteriaCount(dynamicStagesDAO1, count, size);
                    if (getCount != size - 1)
                        isApproved = true;
                } else if (dynamicStagesDAO.getId() == dynamicStagesDAO1.getId())
                    isApproved = false;


            }
        }


        if (!isApproved) {
            Intent intent = new Intent(this, FeedbackForm.class);
            intent.putExtra("actualResponseJson", actualResponseJson);
            intent.putExtra("criteriaListDAO", criteriaListDAO);
            intent.putExtra("isAccepted", isAccepted);
            startActivity(intent);
        } else {
            SubmitStageRequest(isAccepted, criteriaListDAO);
        }


    }

    private int criteriaCount(DynamicStagesDAO dynamicStagesDAO1, int count, int size) {
        for (int i = 0; i < size; i++) {
            if (dynamicStagesDAO1.getCriteriaList() != null) {
                DynamicStagesCriteriaListDAO dynamicStagesCriteriaListDAO = dynamicStagesDAO1.getCriteriaList().get(i);

                if (dynamicStagesCriteriaListDAO.getAssessmentStatus() != null && (dynamicStagesCriteriaListDAO.getAssessmentStatus().equalsIgnoreCase(getString(R.string.accepted))
                        || dynamicStagesCriteriaListDAO.getAssessmentStatus().equalsIgnoreCase(getString(R.string.rejected)))) {
                    count++;
                }
            }
        }

        return count;
    }

    @Override
    public void validateCriteriaFields(DynamicStagesCriteriaListDAO dynamicStagesCriteriaList) {

        List<DynamicFormSectionFieldDAO> adapter_list = null;
        if (applicationStagesAdapter != null) {
            adapter_list = applicationStagesAdapter.getAllCriteriaFields();
        }
        boolean isAllFieldsValidateTrue = true;

        int criteriaId = dynamicStagesCriteriaList.getId();

        for (int i = 0; i < dynamicStagesCriteriaList.form.getSections().size(); i++) {
            DynamicFormSectionDAO dynamicFormSectionDAO = dynamicStagesCriteriaList.form.getSections().get(i);

            for (int k = 0; k < dynamicFormSectionDAO.getFields().size(); k++) {
                int id = dynamicFormSectionDAO.getFields().get(k).getId();

                if (adapter_list != null && adapter_list.size() > 0) {

                    for (DynamicFormSectionFieldDAO dynamicFormSectionFieldDAO : adapter_list) {

                        if (dynamicFormSectionFieldDAO.getId() == id) {

                            if (dynamicFormSectionFieldDAO.isRequired()) {
                                if (!dynamicFormSectionFieldDAO.isValidate()) {
                                    isAllFieldsValidateTrue = false;
                                    break;
                                }

                            }


                        }
                    }
                }
            }


        }

        for (int p = 0; p < applicationStagesAdapter.getStagesList().size(); p++) {
            if (applicationStagesAdapter.getStagesList().get(p).getCriteriaList() != null) {
                for (int q = 0; q < applicationStagesAdapter.getStagesList().get(p).getCriteriaList().size(); q++) {

                    int id = applicationStagesAdapter.getStagesList().get(p).getCriteriaList().get(q).getId();
                    if (criteriaId == id) {
                        applicationStagesAdapter.getStagesList().get(p).getCriteriaList().get(q).setValidate(isAllFieldsValidateTrue);
                        try {
                            applicationStagesAdapter.notifyChangeIfAny(criteriaId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    private void start_loading_animation(boolean isShowDialog) {

        swipeRefreshLayout.setRefreshing(true);

        if (isShowDialog) {
            try {
                if (!pDialog.isShowing())
                    pDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            shimmer_view_container.setVisibility(View.VISIBLE);
            shimmer_view_container.startShimmerAnimation();
        }

    }

    private void stop_loading_animation(boolean isShowDialog) {
        swipeRefreshLayout.setRefreshing(false);
        if (isShowDialog) {
            try {
                if (pDialog.isShowing())
                    pDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            shimmer_view_container.setVisibility(View.GONE);
            shimmer_view_container.stopShimmerAnimation();
        }

    }

    @Override
    public void onBackPressed() {

        if (shimmer_view_container.getVisibility() == View.VISIBLE)
            return;

        if (submit_btn.getVisibility() == View.VISIBLE) {
            AlertActionWindow action_window = AlertActionWindow.newInstance(getString(R.string.save_draft), getString(R.string.your) + " " + pref.getlabels().getApplication() + " " + getString(R.string.wasnotsubmitted), getString(R.string.save_draft_ok), getString(R.string.discard) + " " + pref.getlabels().getApplication(), getString(R.string.draft));
            action_window.show(getSupportFragmentManager(), "");
            action_window.setCancelable(true);
        } else if (topcardview.getVisibility() == View.GONE) {
            detailClick();
        } else {
            super.onBackPressed();
            criteriaWasLoaded = false;
            if (!isSubApplications)
                ListUsersApplicationsAdapter.Companion.setSubApplications(false);
            /*Intent intent = new Intent(bContext, ApplicationsActivityDrawer.class);
            ComponentName cn = intent.getComponent();
            Intent mainIntent = Intent.makeRestartActivityTask(cn);
            startActivity(mainIntent);*/


        }
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
        if (((statusId != 1 || !isResubmit) && isComingfromAssessor) || FeedbackForm.Companion.isComingFromFeedbackFrom()) {
            FeedbackForm.Companion.setComingFromFeedbackFrom(false);
            loadData();
        }
    }

    /*public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            CustomLogs.displayLogs(ACTIVITY_NAME + " BroadcastReceiver" + " " + intent.getStringExtra("position"));
            callService();
        }
    };

    private void callService() {

        if (!isServiceRunning && ApplicationFieldsRecyclerAdapter.isCalculatedMappedField) {
            unRegisterReciever();
            isServiceRunning = true;
            SubmitRequest("calculatedValues");
        }
    }
*/
    private void registerReciever() {
       /* isServiceRunning = false;
        LocalBroadcastManager.getInstance(bContext).registerReceiver(mMessageReceiver,
                new IntentFilter("getcalculatedvalues"));*/
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    private void unRegisterReciever() {
        //LocalBroadcastManager.getInstance(bContext).unregisterReceiver(mMessageReceiver);
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void dataRefreshEvent(EventOptions.EventTriggerController eventTriggerController) {
        unRegisterReciever();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ApplicationFieldsRecyclerAdapter.isCalculatedMappedField)
                    SubmitRequest("calculatedValues");
            }
        }, 1000);
    }

    public void getCalculatedValues(DynamicResponseDAO post) {

        if (isKeyboardVisible)
            isCalculatedField = true;

        try {
            if (isAddStages)
                post = getCriteriaFormPost();


            //  start_loading_animation(true);
            Call<List<CalculatedMappedFieldsDAO>> submit_call = Shared.getInstance().retroFitObject(bContext).getCalculatedValues(post);

            submit_call.enqueue(new Callback<List<CalculatedMappedFieldsDAO>>() {
                @Override
                public void onResponse(Call<List<CalculatedMappedFieldsDAO>> call, Response<List<CalculatedMappedFieldsDAO>> response) {

                    if (response != null && response.body() != null) {
                        //  List<DynamicSectionValuesDAO> sectionValuesListToPost = new ArrayList<>();
                        List<CalculatedMappedFieldsDAO> calculatedMappedFieldsList = response.body();

                        for (int i = 0; i < calculatedMappedFieldsList.size(); i++) {
                            CalculatedMappedFieldsDAO calculatedMappedFieldsDAO = calculatedMappedFieldsList.get(i);


                            if (isAddStages)
                                populateCriteriaCalculatedFields(calculatedMappedFieldsDAO);
                            else {
                                if (mApplicationSectionsAdapter.getmApplications() != null) {
                                    List<DynamicFormSectionDAO> dynamicFormSectionDAOS = mApplicationSectionsAdapter.getmApplications();
                                    for (int u = 0; u < dynamicFormSectionDAOS.size(); u++) {
                                        DynamicFormSectionDAO updatedSection = dynamicFormSectionDAOS.get(u);
                                        if (updatedSection.getFieldsCardsList().size() > 0) {
                                            //For Setting InstancesList
                                            //   for (DynamicFormSectionFieldsCardsDAO dynamicFormSectionFieldsCardsDAO : updatedSection.getFieldsCardsList()) {
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
                                                                    String calculatedDisplayDate = Shared.getInstance().getDisplayDate(bContext, calculatedMappedFieldsDAO.getValue(), false);
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
                        }


                        if (txtapprovalStages.getVisibility() == View.GONE && applicationStagesAdapter != null) {
                            applicationStagesAdapter.getCriteriaAdapter().setCriterias(applicationStagesAdapter.getCriteriaAdapter().getCriteriaList());
                            applicationStagesAdapter.notifyDataSetChanged();
                            // populateStagesData(actual_response.getStages());
                        }

                        if (mApplicationSectionsAdapter != null && !isNotified)
                            mApplicationSectionsAdapter.notifyDataSetChanged();


                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                registerReciever();
                                // stop_loading_animation(true);
                            }
                        }, 1000);

                        if (ChooseLookUpOption.Companion.isOpen())
                            EventBus.getDefault().post(new EventOptions.EventTriggerController());

                    } else {
                        //stop_loading_animation(true);
                        CustomLogs.displayLogs(ACTIVITY_NAME + " null response");
                        registerReciever();
                        Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext);
                    }


                }

                @Override
                public void onFailure(Call<List<CalculatedMappedFieldsDAO>> call, Throwable t) {
                    // stop_loading_animation(true);
                    CustomLogs.displayLogs(ACTIVITY_NAME + " failure response");
                    registerReciever();
                    Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext);
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            //  stop_loading_animation(true);
            registerReciever();
            Shared.getInstance().messageBox(getString(R.string.some_thing_went_wrong), bContext);
        }
    }//LoggedInUser end


    private void populateCriteriaCalculatedFields(CalculatedMappedFieldsDAO calculatedMappedFieldsDAO) {

        if (applicationStagesAdapter.getCriteriaAdapter() != null &&
                applicationStagesAdapter.getCriteriaAdapter().getCriteriaList() != null) {
            for (int y = 0; y < applicationStagesAdapter.getCriteriaAdapter().getCriteriaList().size(); y++) {
                DynamicStagesCriteriaListDAO dynamicStagesCriteriaListDAO = applicationStagesAdapter.getCriteriaAdapter().getCriteriaList().get(y);
                DynamicFormDAO form = dynamicStagesCriteriaListDAO.getForm();
                if (form.getSections() != null) {
                    for (int r = 0; r < form.getSections().size(); r++) {
                        DynamicFormSectionDAO updatedSection = form.getSections().get(r);
                        if (updatedSection.getFieldsCardsList().size() > 0) {
                            //For Setting InstancesList
                            //   for (DynamicFormSectionFieldsCardsDAO dynamicFormSectionFieldsCardsDAO : updatedSection.getFieldsCardsList()) {
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
                                                    String calculatedDisplayDate = Shared.getInstance().getDisplayDate(bContext, calculatedMappedFieldsDAO.getValue(), false);
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
        }
    }

    private DynamicResponseDAO getCriteriaFormPost() {
        DynamicResponseDAO actual_response_temp = null;
        if (applicationStagesAdapter.getCriteriaAdapter() != null) {

            List<DynamicStagesCriteriaListDAO> criteriaList = applicationStagesAdapter.getCriteriaAdapter().getCriteriaList();
            actual_response_temp = actual_response;
            if (actual_response_temp.getStages() != null) {
                for (int i = 0; i < actual_response_temp.getStages().size(); i++) {
                    List<DynamicStagesCriteriaListDAO> criteriaListTemp = new ArrayList<>();
                    int stageId = actual_response_temp.getStages().get(i).getId();
                    for (int j = 0; j < criteriaList.size(); j++) {
                        DynamicStagesCriteriaListDAO dynamicStagesCriteriaListDAO = criteriaList.get(j);
                        List<DynamicFormValuesDAO> criteriaFormValues = getCriteriaFormValues(dynamicStagesCriteriaListDAO);
                        dynamicStagesCriteriaListDAO.setFormValues(criteriaFormValues);

                        List<DynamicFormSectionDAO> sections = dynamicStagesCriteriaListDAO.getForm().getSections();

                        if (sections != null) {
                            for (int w = 0; w < sections.size(); w++) {
                                sections.get(w).setDynamicStagesCriteriaListDAO(null);
                            }
                        }
                        if (stageId == dynamicStagesCriteriaListDAO.getStageId()) {
                            criteriaListTemp.add(dynamicStagesCriteriaListDAO);
                        }
                    }

                    actual_response_temp.getStages().get(i).setCriteriaList(criteriaListTemp);

                }

            }

        }
        return actual_response_temp;
    }

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
                                if (mApplicationSectionsAdapter != null && isCalculatedField)
                                    mApplicationSectionsAdapter.notifyDataSetChanged();
                            }
                        }, 500);
                    }

                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApplicationFieldsRecyclerAdapter.isCalculatedMappedField = false;
    }
}
