package com.esp.library.exceedersesp.controllers.applications.filters;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.esp.library.R;
import com.esp.library.exceedersesp.ESPApplication;
import com.esp.library.exceedersesp.controllers.applications.ApplicationActivityTabs;
import com.esp.library.exceedersesp.fragments.applications.UsersApplicationsFragment;
import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.common.SharedPreference;
import com.esp.library.exceedersesp.BaseActivity;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utilities.data.apis.APIs;
import utilities.data.filters.FilterDAO;
import utilities.data.filters.FilterDefinitionSortDAO;
import utilities.interfaces.ApplicationsFilterListener;

public class FilterScreenActivity extends BaseActivity implements ApplicationsFilterListener {

    public static String ACTIVITY_NAME = "controllers.applications.filters.FilterScreenActivity";
    BaseActivity bContext;
    List<AppCompatCheckBox> statuses_checkboxes;
    SharedPreference pref;

    Toolbar toolbar;
    LinearLayout status_layout;
    LinearLayout lldivider;
    LinearLayout status_row;
    TextView statuses;
    ImageView status_btn;
    TextView reset_filter;
    TextView txtfilter;
    TextView txtbystatus;
    RecyclerView rvdefintionList;
    RecyclerView rvsortbyList;
    LinearLayout definition_row;
    LinearLayout sortby_row;
    ShimmerFrameLayout shimmer_view_container;
    ImageView definition_btn;
    ImageView sort_btn;
    TextView txtdefinitionstatuses;
    TextView txtsortbystatuses;


    FilterDefinitionAdapter filterDefinitionAdapter;
    FilterSortByAdapter filterSortByAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_filter);
        initialize();
        setGravity();


        boolean activityShowing = UsersApplicationsFragment.Companion.isShowingActivity();
        if (!activityShowing) {
            TabLayout tabLayout = ApplicationActivityTabs.Companion.getTabLayout();
            if (tabLayout != null) {
                int tab_position = tabLayout.getSelectedTabPosition();
                if (tab_position == 0) {
                    status_row.setVisibility(View.GONE);
                    lldivider.setVisibility(View.GONE);
                } else {
                    status_row.setVisibility(View.VISIBLE);
                    lldivider.setVisibility(View.VISIBLE);
                }

            }
        }


        reset_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ESPApplication.getInstance().setFilter(Shared.getInstance().ResetApplicationFilter(bContext));
                FilterScreenActivity.this.UpdateView(false);
            }
        });

        status_row.setOnClickListener(v -> {

            String row_status = (String) status_layout.getTag();

            if (row_status.equals(getString(R.string.hidden))) {
                status_layout.setTag(getString(R.string.shown));
                status_layout.setVisibility(View.VISIBLE);
                status_btn.setBackground(bContext.getResources().getDrawable(R.drawable.ic_arrow_up));
            } else if (row_status.equals(getString(R.string.shown))) {
                status_layout.setTag(getString(R.string.hidden));
                status_layout.setVisibility(View.GONE);
                status_btn.setBackground(bContext.getResources().getDrawable(R.drawable.ic_arrow_down));
            }

        });


        sortby_row.setOnClickListener(v -> {

            if (rvsortbyList.getVisibility() == View.GONE) {
                rvsortbyList.setVisibility(View.VISIBLE);
                sort_btn.setBackground(bContext.getResources().getDrawable(R.drawable.ic_arrow_up));
            } else {
                rvsortbyList.setVisibility(View.GONE);
                sort_btn.setBackground(bContext.getResources().getDrawable(R.drawable.ic_arrow_down));
            }

        });

        definition_row.setOnClickListener(v -> {

            if (rvdefintionList.getVisibility() == View.GONE) {
                rvdefintionList.setVisibility(View.VISIBLE);
                definition_btn.setBackground(bContext.getResources().getDrawable(R.drawable.ic_arrow_up));
            } else {
                rvdefintionList.setVisibility(View.GONE);
                definition_btn.setBackground(bContext.getResources().getDrawable(R.drawable.ic_arrow_down));
            }

        });
        initiateStatus();
        getDefinitionList();
        sorByList();

    }

    private void initialize() {
        bContext = FilterScreenActivity.this;
        pref = new SharedPreference(bContext);

        toolbar = findViewById(R.id.toolbar);
        status_layout = findViewById(R.id.status_layout);
        lldivider = findViewById(R.id.lldivider);
        status_row = findViewById(R.id.status_row);
        statuses = findViewById(R.id.statuses);
        status_btn = findViewById(R.id.status_btn);
        reset_filter = findViewById(R.id.reset_filter);
        txtfilter = findViewById(R.id.txtfilter);
        txtbystatus = findViewById(R.id.txtbystatus);
        rvdefintionList = findViewById(R.id.rvdefintionList);
        rvsortbyList = findViewById(R.id.rvsortbyList);
        definition_row = findViewById(R.id.definition_row);
        sortby_row = findViewById(R.id.sort_row);
        shimmer_view_container = findViewById(R.id.shimmer_view_container);
        definition_btn = findViewById(R.id.definition_btn);
        sort_btn = findViewById(R.id.sort_btn);
        txtdefinitionstatuses = findViewById(R.id.txtdefinitionstatuses);
        txtsortbystatuses = findViewById(R.id.txtsortbystatuses);

        status_layout.setTag(getString(R.string.hidden));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle("");

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_nav_back));
        toolbar.getNavigationIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        RecyclerView.LayoutManager mApplicationLayoutManager = new LinearLayoutManager(bContext);
        rvdefintionList.setHasFixedSize(true);
        rvdefintionList.setLayoutManager(mApplicationLayoutManager);
        rvdefintionList.setItemAnimator(new DefaultItemAnimator());


        RecyclerView.LayoutManager mApplicationLayoutManagerSortby = new LinearLayoutManager(bContext);
        rvsortbyList.setHasFixedSize(true);
        rvsortbyList.setLayoutManager(mApplicationLayoutManagerSortby);
        rvsortbyList.setItemAnimator(new DefaultItemAnimator());
    }

    private void sorByList() {
        List<FilterDefinitionSortDAO> filterDefinitionSortDAOSList = new ArrayList<>();
        filterDefinitionSortDAOSList.add(new FilterDefinitionSortDAO(getString(R.string.suboldestfirst), false, 1));
        filterDefinitionSortDAOSList.add(new FilterDefinitionSortDAO(getString(R.string.subnewestfirst), false, 4));
        filterDefinitionSortDAOSList.add(new FilterDefinitionSortDAO(getString(R.string.assignoldestfirst), false, 2));
        filterDefinitionSortDAOSList.add(new FilterDefinitionSortDAO(getString(R.string.assignnewestfirst), false, 5));
        filterDefinitionSortDAOSList.add(new FilterDefinitionSortDAO(getString(R.string.duefirst), false, 6));
        filterDefinitionSortDAOSList.add(new FilterDefinitionSortDAO(getString(R.string.duelast), false, 7));


        preSelectedSortBy(filterDefinitionSortDAOSList);

    }

    private void preSelectedSortBy(List<FilterDefinitionSortDAO> filterDefinitionSortDAOSList) {
        for (int i = 0; i < filterDefinitionSortDAOSList.size(); i++) {
            if (ESPApplication.getInstance().getFilter().getSortBy() == filterDefinitionSortDAOSList.get(i).getId()) {
                filterDefinitionSortDAOSList.get(i).setCheck(true);
                txtsortbystatuses.setText(filterDefinitionSortDAOSList.get(i).getName());
            } else
                filterDefinitionSortDAOSList.get(i).setCheck(false);
        }
        filterSortByAdapter = new FilterSortByAdapter(filterDefinitionSortDAOSList, bContext);
        rvsortbyList.setAdapter(filterSortByAdapter);

    }

    public void getDefinitionList() {
        ArrayList<String> list = new ArrayList<>();
        list.add("0");

        FilterDAO cloneFilter = new FilterDAO();
        FilterDAO filter = ESPApplication.getInstance().getFilter();
        cloneFilter.setSearch("");
        cloneFilter.setStatuses(list);
        cloneFilter.setMySpace(true);
        cloneFilter.setApplicantId(filter.getApplicantId());


        start_loading_animation();
        FilterDAO filterDAO = Shared.getInstance().CloneFilter(cloneFilter);
        filterDAO.setMySpace(true);
        final APIs apis = Shared.getInstance().retroFitObject(bContext);
        Call<List<FilterDefinitionSortDAO>> definition_call = apis.getDefinitioList(filterDAO);
        definition_call.enqueue(new Callback<List<FilterDefinitionSortDAO>>() {
            @Override
            public void onResponse(Call<List<FilterDefinitionSortDAO>> call, Response<List<FilterDefinitionSortDAO>> response) {

                if (response != null && response.body() != null) {
                    List<Integer> applcaition_checkboxes = new ArrayList<>();
                    List<FilterDefinitionSortDAO> filterDefinitionSortDAOSList = new ArrayList<>();
                    for (int i = 0; i < response.body().size(); i++) {
                        FilterDefinitionSortDAO filterDefinitionSortDAO = response.body().get(i);
                        filterDefinitionSortDAO.setId(filterDefinitionSortDAO.getId());
                        filterDefinitionSortDAO.setName(filterDefinitionSortDAO.getName());
                        if (ESPApplication.getInstance().getFilter().getDefinitionIds() == null ||
                                ESPApplication.getInstance().getFilter().getDefinitionIds().size() == 0) {
                            filterDefinitionSortDAO.setCheck(true);
                        }

                        applcaition_checkboxes.add(filterDefinitionSortDAO.getId());
                        filterDefinitionSortDAOSList.add(filterDefinitionSortDAO);
                    }

                    if (filterDefinitionSortDAOSList.size() > 0) {
                        FilterDefinitionSortDAO filterDefinitionSortDAO = new FilterDefinitionSortDAO();
                        filterDefinitionSortDAO.setId(1122); // 1122 = id of All
                        filterDefinitionSortDAO.setName(getString(R.string.all));
                        if (ESPApplication.getInstance().getFilter().getDefinitionIds() == null ||
                                ESPApplication.getInstance().getFilter().getDefinitionIds().size() == 0) {
                            filterDefinitionSortDAO.setCheck(true);
                        }
                        filterDefinitionSortDAOSList.add(0, filterDefinitionSortDAO);
                        filterDefinitionAdapter = new FilterDefinitionAdapter(filterDefinitionSortDAOSList, bContext);
                        rvdefintionList.setAdapter(filterDefinitionAdapter);


                        if (filterDefinitionSortDAOSList.size() > 0 && filterDefinitionSortDAOSList.get(0).isCheck()) {
                            txtdefinitionstatuses.setText(getString(R.string.all));
                            /*ArrayList<Integer> tempList = new ArrayList<>();
                            tempList.add(0);
                            ESPApplication.getInstance().getFilter().setDefinitionIds(tempList);*/
                        }

                        preSelectedValues(filterDefinitionSortDAOSList, applcaition_checkboxes);


                    }

                    stop_loading_animation();
                } else {
                    stop_loading_animation();
                    Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), bContext);
                }
            }

            @Override
            public void onFailure(Call<List<FilterDefinitionSortDAO>> call, Throwable t) {
                stop_loading_animation();
                Shared.getInstance().showAlertMessage(pref.getlabels().getApplication(), getString(R.string.some_thing_went_wrong), bContext);
            }
        });

    }


    private void preSelectedValues(List<FilterDefinitionSortDAO> filterDefinitionSortDAOSList, List<Integer> applcaition_checkboxes) {
        if (ESPApplication.getInstance().getFilter().getDefinitionIds() != null &&
                ESPApplication.getInstance().getFilter().getDefinitionIds().get(0) != 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int k = 0; k < ESPApplication.getInstance().getFilter().getDefinitionIds().size(); k++) {
                int definitionId = ESPApplication.getInstance().getFilter().getDefinitionIds().get(k);
                for (int j = 0; j < filterDefinitionSortDAOSList.size(); j++) {
                    FilterDefinitionSortDAO filterDefinitionSortDAO1 = filterDefinitionSortDAOSList.get(j);
                    if (definitionId == filterDefinitionSortDAO1.getId()) {
                        filterDefinitionSortDAO1.setCheck(true);
                        stringBuilder.append(filterDefinitionSortDAO1.getName());
                        stringBuilder.append(", ");

                    }
                }
            }
            checkAllText(filterDefinitionSortDAOSList);
            setAllStatus(filterDefinitionSortDAOSList, stringBuilder.toString());

            if (filterDefinitionAdapter != null)
                filterDefinitionAdapter.notifyDataSetChanged();
        } else {
            ESPApplication.getInstance().getFilter().setDefinitionIds(applcaition_checkboxes);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initiateStatus() {
        String[] status = bContext.getResources().getStringArray(R.array.status);
        String[] status_enum = bContext.getResources().getStringArray(R.array.status_enum);

        if (status != null && status.length > 0) {
            statuses_checkboxes = new ArrayList<>();

            for (int i = 0; i < status.length; i++) {
                final AppCompatCheckBox checkBox = new AppCompatCheckBox(bContext);
                checkBox.setText(status[i]);
                checkBox.setTextSize(16f);
                checkBox.setButtonDrawable(bContext.getResources().getDrawable(R.drawable.checkbox_button_selector));
                checkBox.setTag(i);
                checkBox.setPadding(70, 0, 0, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                params.setMargins(0, 0, 0, 0);
                checkBox.setLayoutParams(params);

                if (i == 1 || i == 2) // 1 = new and 2 = pending
                {
                    checkBox.setVisibility(View.GONE);
                }
                if (ESPApplication.getInstance().getFilter().getStatuses().contains(i + "")) {
                    checkBox.setChecked(true);
                }

                checkBox.setOnClickListener(v -> {
                    int tage = (int) v.getTag();

                    if (statuses_checkboxes.get(tage).isChecked()) {

                        if (tage == 0) {
                            ESPApplication.getInstance().getFilter().getStatuses().clear();
                            CheckAllStatuses(true);
                            statuses.setText(getString(R.string.all));

                        } else {

                            if (!ESPApplication.getInstance().getFilter().getStatuses().contains(tage + "")) {
                                ESPApplication.getInstance().getFilter().getStatuses().add(tage + "");
                            }

                        }

                        //  checkBox.setTextColor(bContext.getResources().getColor(R.color.green));

                    } else {
                        //  checkBox.setTextColor(bContext.getResources().getColor(R.color.dark_grey));
                        //UN CHECKED
                        if (tage == 0) {
                            ESPApplication.getInstance().getFilter().getStatuses().clear();
                            CheckAllStatuses(false);
                            statuses.setText("");
                        } else {
                            statuses_checkboxes.get(0).setChecked(false);
                            ESPApplication.getInstance().getFilter().getStatuses().remove(tage + "");


                        }

                    }
                    UpdateView(true);
                });

                statuses_checkboxes.add(checkBox);
                status_layout.addView(checkBox);
            }

            UpdateView(true);


        }


    }

    private void UpdateView(boolean ischecked) {


        if (ESPApplication.getInstance().getFilter().getStatuses() != null) {

            String fitlerType = "";
            int count = 0;
            if (ESPApplication.getInstance().getFilter().getStatuses().size() == 0) {
                fitlerType = "";
            } else if (ESPApplication.getInstance().getFilter().getStatuses().size() == 5) {
                CheckAllStatuses(ischecked);
                fitlerType = getString(R.string.all);
            } else {

                for (String s : ESPApplication.getInstance().getFilter().getStatuses()) {
                    if (s != "0") {
                        if (fitlerType.length() == 0) {
                            fitlerType = statuses_checkboxes.get(Integer.parseInt(s)).getText().toString();
                        } else {

                            if (fitlerType.equalsIgnoreCase(getString(R.string.neww)))
                                fitlerType = "";

                            String status = statuses_checkboxes.get(Integer.parseInt(s)).getText().toString();
                            if (status.equalsIgnoreCase(getString(R.string.accepted)) ||
                                    status.equalsIgnoreCase(getString(R.string.rejected)) ||
                                    status.equalsIgnoreCase(getString(R.string.cancelled))) {
                                fitlerType += ", " + status;
                            }

                        }
                    }
                }

            }

            statuses.setText(fitlerType);
        }
    }

    private void CheckAllStatuses(boolean checked) {

        if (statuses_checkboxes != null && statuses_checkboxes.size() > 0) {
            for (AppCompatCheckBox cb : statuses_checkboxes) {
                cb.setChecked(checked);
                int tage = (int) cb.getTag();
                if (checked) {
                    if (tage != 0) {
                        if (!ESPApplication.getInstance().getFilter().getStatuses().contains(tage + "")) {
                            ESPApplication.getInstance().getFilter().getStatuses().add(tage + "");
                        }
                    }

                    cb.setTextColor(bContext.getResources().getColor(R.color.black));
                } else {
                    ESPApplication.getInstance().getFilter().getStatuses().remove(tage + "");
                    cb.setTextColor(bContext.getResources().getColor(R.color.dark_grey));
                }

            }
        }
    }

    private void ApplyFilter() {

        if (ESPApplication.getInstance().getFilter().getStatuses() == null || ESPApplication.getInstance().getFilter().getStatuses().size() == 0) {
            Shared.getInstance().showAlertMessage(getString(R.string.filter), getString(R.string.filter_error), bContext);
            return;
        } else if (filterDefinitionAdapter != null &&
                (ESPApplication.getInstance().getFilter().getDefinitionIds() == null || ESPApplication.getInstance().getFilter().getDefinitionIds().size() == 0)) {
            Shared.getInstance().showAlertMessage(getString(R.string.filter), getString(R.string.definition_filter_error), bContext);
            return;
        }


        if (ESPApplication.getInstance().getFilter().getStatuses().size() < 5
                || (ESPApplication.getInstance().getFilter().getDefinitionIds() != null
                && ESPApplication.getInstance().getFilter().getDefinitionIds().size() > 0)) {
            ESPApplication.getInstance().getFilter().setFilterApplied(true);
        } else {
            ESPApplication.getInstance().getFilter().setFilterApplied(false);
        }

        Bundle bnd = new Bundle();
        bnd.putBoolean("whatodo", true);
        Intent intent = new Intent();
        intent.putExtras(bnd);
        setResult(2, intent);
        finish();
    }

    @Override
    public void onBackPressed() {

    /*    Bundle bnd = new Bundle();
        bnd.putBoolean("whatodo", false);
        Intent intent = new Intent();
        intent.putExtras(bnd);
        setResult(2, intent);
        super.onBackPressed();*/

        ApplyFilter();
    }

    private void setGravity() {
        SharedPreference pref = new SharedPreference(bContext);
        if (pref.getLanguage().equalsIgnoreCase("ar")) {
            statuses.setGravity(Gravity.RIGHT);
            txtfilter.setGravity(Gravity.RIGHT);
            txtbystatus.setGravity(Gravity.RIGHT);
            reset_filter.setGravity(Gravity.RIGHT);

        } else {
            statuses.setGravity(Gravity.LEFT);
            txtfilter.setGravity(Gravity.LEFT);
            txtbystatus.setGravity(Gravity.LEFT);
            reset_filter.setGravity(Gravity.LEFT);
        }
    }

    private void start_loading_animation() {

        shimmer_view_container.setVisibility(View.VISIBLE);
        shimmer_view_container.startShimmerAnimation();


    }

    private void stop_loading_animation() {

        shimmer_view_container.setVisibility(View.GONE);
        shimmer_view_container.stopShimmerAnimation();

    }


    @Override
    public void selectedValues(List<FilterDefinitionSortDAO> filterDefinitionList, int position, boolean checked) {
        List<Integer> applcaition_checkboxes = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < filterDefinitionList.size(); i++) {
            FilterDefinitionSortDAO filterDefinitionSortDAO = filterDefinitionList.get(i);
            if (filterDefinitionSortDAO.isCheck() && filterDefinitionSortDAO.getId() != 1122) // 1122 = id of All
            {
                applcaition_checkboxes.add(filterDefinitionSortDAO.getId());
                stringBuilder.append(filterDefinitionList.get(i).getName());
                stringBuilder.append(", ");
            }
        }


        ESPApplication.getInstance().getFilter().setDefinitionIds(applcaition_checkboxes);
        checkUncheck(checked, filterDefinitionList, position, stringBuilder.toString());


    }

    @Override
    public void selectedSortValues(FilterDefinitionSortDAO filterDefinitionSortDAO, List<FilterDefinitionSortDAO> filterSortByListSort, int position) {
        ESPApplication.getInstance().getFilter().setSortBy(filterDefinitionSortDAO.getId());
        preSelectedSortBy(filterSortByListSort);
    }

    private void checkUncheck(boolean isCheck, List<FilterDefinitionSortDAO> filterDefinitionList, int position, String selectedStrings) {
        if (position == 0) {
            for (int i = 0; i < filterDefinitionList.size(); i++) {
                filterDefinitionList.get(i).setCheck(isCheck);
            }

            if (filterDefinitionAdapter != null && !rvdefintionList.isComputingLayout())
                filterDefinitionAdapter.notifyDataSetChanged();
        } else {
            checkAllText(filterDefinitionList);
        }

        setAllStatus(filterDefinitionList, selectedStrings);

    }

    private void checkAllText(List<FilterDefinitionSortDAO> filterDefinitionList) {
        if (filterDefinitionList.size() > 0 &&
                (ESPApplication.getInstance().getFilter().getDefinitionIds().size() == (filterDefinitionList.size() - 1))
                && !filterDefinitionList.get(0).isCheck()) {
            filterDefinitionList.get(0).setCheck(true);
            if (filterDefinitionAdapter != null)
                filterDefinitionAdapter.notifyItemChanged(0);
        }


    }

    private void setAllStatus(List<FilterDefinitionSortDAO> filterDefinitionList, String selectedStrings) {
        for (int i = 0; i < filterDefinitionList.size(); i++) {

            if (!filterDefinitionList.get(i).isCheck()) {
                txtdefinitionstatuses.setText(selectedStrings.replaceAll(", $", ""));
                break;
            } else {
                txtdefinitionstatuses.setText(getString(R.string.all));
            }

        }
    }
}
