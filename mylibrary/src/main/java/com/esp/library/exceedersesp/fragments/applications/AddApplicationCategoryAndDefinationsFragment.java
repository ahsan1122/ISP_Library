package com.esp.library.exceedersesp.fragments.applications;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.esp.library.R;
import com.esp.library.utilities.common.Shared;
import com.esp.library.utilities.common.SharedPreference;
import com.esp.library.exceedersesp.BaseActivity;
import com.esp.library.exceedersesp.controllers.applications.filters.FilterActivity;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import utilities.adapters.setup.FilterItemsAdapter;
import utilities.adapters.setup.applications.ListApplicationCategoryAndDefinationAdapter;
import utilities.data.apis.APIs;
import utilities.data.applicants.addapplication.CategoryAndDefinationsDAO;
import utilities.data.applicants.addapplication.DefinationsCategoriesDAO;
import utilities.interfaces.DeleteFilterListener;

public class AddApplicationCategoryAndDefinationsFragment extends Fragment implements DeleteFilterListener {

    BaseActivity bContext;
    private ListApplicationCategoryAndDefinationAdapter mDefAdapter;


    Call<List<CategoryAndDefinationsDAO>> cat_call = null;
    Call<List<DefinationsCategoriesDAO>> def_call = null;
    InputMethodManager imm = null;
    LinearLayout no_application_available_div;
    RecyclerView defination_list;
    TextView message_error;
    TextView message_error_detail;
    ShimmerFrameLayout shimmer_view_container;
    TextView listcount;
    EditText etxtsearch;
    ImageView ivfilter;
    RecyclerView filter_horizontal_list;
    SwipeRefreshLayout swipeRefreshLayout;


    private static final int HIDE_THRESHOLD = 20;
    FilterItemsAdapter filter_adapter;
    SharedPreference pref;
    List<CategoryAndDefinationsDAO> cat_list = new ArrayList<>();
    List<CategoryAndDefinationsDAO> cat_list_filtered = new ArrayList<>();
    RecyclerView.LayoutManager mDefLayoutManager;
    List<DefinationsCategoriesDAO> actualResponse;
    public static ArrayList categoryAndDefinationsDAOFilteredList = new ArrayList<CategoryAndDefinationsDAO>();

    public AddApplicationCategoryAndDefinationsFragment() {
        // Required empty public constructor
    }

    public static AddApplicationCategoryAndDefinationsFragment newInstance() {
        AddApplicationCategoryAndDefinationsFragment fragment = new AddApplicationCategoryAndDefinationsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_application_category_definations, container, false);
        initailizeIds(v);
        initailize();



        ivfilter.setOnClickListener(view -> {
            Intent i = new Intent(getActivity(), FilterActivity.class);
            i.putExtra("categoryAndDefinationsDAOFilteredList", categoryAndDefinationsDAOFilteredList);
            i.putExtra("actualResponse", (Serializable) actualResponse);
            startActivity(i);
        });

        etxtsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() == 0) {
                    mDefAdapter = new ListApplicationCategoryAndDefinationAdapter(cat_list_filtered, bContext, getString(R.string.definition));
                    defination_list.setAdapter(mDefAdapter);
                } else {
                    mDefAdapter.getFilter().filter(s);
                }


            }

            @Override
            public void afterTextChanged(Editable s) {
                listcount.setText(mDefAdapter.getItemCount() + " " + pref.getlabels().getApplication());
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                categoryAndDefinationsDAOFilteredList.clear();
                filter_horizontal_list.setVisibility(View.GONE);
                etxtsearch.setText("");
                loadData();
            }
        });

        return v;
    }

    private void initailizeIds(View v) {
        no_application_available_div=v.findViewById(R.id.no_application_available_div);
        defination_list=v.findViewById(R.id.defination_list);
        message_error=v.findViewById(R.id.message_error);
        message_error_detail=v.findViewById(R.id.message_error_detail);
        shimmer_view_container=v.findViewById(R.id.shimmer_view_container);
        listcount=v.findViewById(R.id.listcount);
        etxtsearch=v.findViewById(R.id.etxtsearch);
        ivfilter=v.findViewById(R.id.ivfilter);
        filter_horizontal_list=v.findViewById(R.id.filter_horizontal_list);
        swipeRefreshLayout=v.findViewById(R.id.swipeRefreshLayout);
    }

    private void initailize() {
        bContext = (BaseActivity) getActivity();
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        pref = new SharedPreference(getActivity());

        filter_horizontal_list.setHasFixedSize(true);
        filter_horizontal_list.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        filter_horizontal_list.setItemAnimator(new DefaultItemAnimator());


        mDefLayoutManager = new LinearLayoutManager(getActivity());
        defination_list.setHasFixedSize(true);
        defination_list.setLayoutManager(mDefLayoutManager);
        defination_list.setItemAnimator(new DefaultItemAnimator());
        defination_list.setNestedScrollingEnabled(true);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();


    }

    private void loadData() {

        if (Shared.getInstance().isWifiConnected(bContext)) {
            LoadDefinations(0);
        } else {
            Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), bContext);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        filter_adapter = new FilterItemsAdapter(categoryAndDefinationsDAOFilteredList, getActivity());
        filter_adapter.setActivitContext(this);
        filter_horizontal_list.setAdapter(filter_adapter);
        populateFilters();
        etxtsearch.setText("");

    }

    private void populateFilters() {
        cat_list_filtered.clear();
        if (categoryAndDefinationsDAOFilteredList.size() > 0) {
            ivfilter.setColorFilter(ContextCompat.getColor(getActivity(), R.color.green), android.graphics.PorterDuff.Mode.MULTIPLY);
            filter_horizontal_list.setVisibility(View.VISIBLE);

        } else {
            ivfilter.setColorFilter(ContextCompat.getColor(getActivity(), R.color.grey), android.graphics.PorterDuff.Mode.MULTIPLY);
            filter_horizontal_list.setVisibility(View.GONE);
            cat_list_filtered.addAll(cat_list);
        }

        ArrayList<Integer> categoriesIds = new ArrayList<>();
        for (int i = 0; i < categoryAndDefinationsDAOFilteredList.size(); i++) {
            CategoryAndDefinationsDAO df = (CategoryAndDefinationsDAO) categoryAndDefinationsDAOFilteredList.get(i);
            categoriesIds.add(df.getId());

            for (int h = 0; h < cat_list.size(); h++) {
                if (cat_list.get(h).getTypeId() == df.getId()) {
                    cat_list_filtered.add(cat_list.get(h));
                }
            }

        }

        mDefAdapter = new ListApplicationCategoryAndDefinationAdapter(cat_list_filtered, bContext, getString(R.string.definition));
        defination_list.setAdapter(mDefAdapter);
        listcount.setText(mDefAdapter.getItemCount() + " " + pref.getlabels().getApplication());
/*
        if (Shared.getInstance().isWifiConnected(bContext)) {
            LoadDefinations(categoriesIds.toString().trim().replaceAll("\\[", "").replaceAll("\\]", ""));
        } else {
            Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), bContext);
        }*/
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        categoryAndDefinationsDAOFilteredList.clear();

    }


    private void LoadDefinations(int categoryId) {

        start_loading_animation();
        /* APIs Mapping respective Object*/
        APIs apis = Shared.getInstance().retroFitObject(bContext);
        //  def_call = apis.AllDefincations(categoryId);
        def_call = apis.AllWithQuery();
        def_call.enqueue(new Callback<List<DefinationsCategoriesDAO>>() {
            @Override
            public void onResponse(Call<List<DefinationsCategoriesDAO>> call, Response<List<DefinationsCategoriesDAO>> response) {
                stop_loading_animation();

                if (response.body() != null && response.body().size() > 0) {
                    actualResponse = response.body();
                    cat_list.clear();
                    List<DefinationsCategoriesDAO> body = response.body();
                    for (int i = 0; i < body.size(); i++) {
                        List<CategoryAndDefinationsDAO> category = body.get(i).getDefinitions();
                        if (category != null) {
                            for (int k = 0; k < category.size(); k++) {
                                CategoryAndDefinationsDAO categoryAndDefinationsDAO = category.get(k);
                                if (categoryAndDefinationsDAO != null) {
                                    if (categoryAndDefinationsDAO.isActive()) {
                                        cat_list.add(categoryAndDefinationsDAO);
                                    }
                                }
                            }
                        }


                    }
                    if (cat_list.size() > 0) {
                        try {
                            cat_list_filtered.addAll(cat_list);
                            mDefAdapter = new ListApplicationCategoryAndDefinationAdapter(cat_list, bContext, getString(R.string.definition));
                            defination_list.setAdapter(mDefAdapter);
                            defination_list.setVisibility(View.VISIBLE);
                            listcount.setText(mDefAdapter.getItemCount() + " " + pref.getlabels().getApplication());
                        } catch (Exception e) {
                        }
                        SuccessResponse();
                    } else {
                        UnSuccessResponse();
                    }
                } else {
                    UnSuccessResponse();
                }
            }

            @Override
            public void onFailure(Call<List<DefinationsCategoriesDAO>> call, Throwable t) {
                Shared.getInstance().messageBox(t.getMessage(), bContext);
                stop_loading_animation();
                UnSuccessResponse();
            }
        });

    }//

    @Override
    public void onDestroyView() {

        if (cat_call != null) {
            cat_call.cancel();
        }

        if (def_call != null) {
            def_call.cancel();
        }

        super.onDestroyView();
    }

    private void start_loading_animation() {
        shimmer_view_container.setVisibility(View.VISIBLE);
        shimmer_view_container.startShimmerAnimation();
    }

    private void stop_loading_animation() {
        shimmer_view_container.setVisibility(View.GONE);
        shimmer_view_container.stopShimmerAnimation();
    }

    private void SuccessResponse() {
        swipeRefreshLayout.setRefreshing(false);
        defination_list.setVisibility(View.VISIBLE);
        no_application_available_div.setVisibility(View.GONE);
    }

    private void UnSuccessResponse() {
        swipeRefreshLayout.setRefreshing(false);
        message_error.setText(bContext.getResources().getString(R.string.no_defination_error));
        message_error_detail.setText(bContext.getResources().getString(R.string.no_defination_error));
        defination_list.setVisibility(View.GONE);
        no_application_available_div.setVisibility(View.VISIBLE);
    }

    public void UpdateDefincation(CategoryAndDefinationsDAO cat) {
        if (cat != null) {
            LoadDefinations(cat.getId());
        }
    }

    @Override
    public void deleteFilters(@NotNull CategoryAndDefinationsDAO filtersList) {

        if (Shared.getInstance().isWifiConnected(bContext)) {
            if (filter_adapter != null) {
                categoryAndDefinationsDAOFilteredList.remove(filtersList);
                filter_adapter.notifyDataSetChanged();
                populateFilters();
            }
        } else {
            Shared.getInstance().showAlertMessage(getString(R.string.internet_error_heading), getString(R.string.internet_connection_error), bContext);
        }
    }


}
