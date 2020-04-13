package com.esp.library.exceedersesp.controllers.fieldstype.classes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.RatingBar;

import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.core.graphics.drawable.DrawableCompat;

import com.esp.library.exceedersesp.controllers.Profile.EditSectionDetails;
import com.esp.library.exceedersesp.controllers.fieldstype.other.CalculatedMappedRequestTrigger;
import com.esp.library.exceedersesp.controllers.fieldstype.other.Validation;
import com.esp.library.exceedersesp.controllers.fieldstype.viewholders.RatingTypeViewHolder;
import com.esp.library.utilities.common.CustomLogs;
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter;

import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO;
import utilities.interfaces.CriteriaFieldsListener;

public class RatingItem {

    private String TAG = getClass().getSimpleName();
    private static RatingItem ratingItem = null;
    private ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener mApplicationFieldsAdapterListener;
    private CriteriaFieldsListener criteriaFieldsListener;
    private DynamicStagesCriteriaListDAO criteriaListDAO;
    private boolean isViewOnly;
    private EditSectionDetails edisectionDetailslistener;

    public static RatingItem getInstance() {
        if (ratingItem == null)
            return ratingItem = new RatingItem();
        else
            return ratingItem;
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

    public void getAdapter(EditSectionDetails edisectionDetails) {
        edisectionDetailslistener = edisectionDetails;
    }

    public void showRatingBarItemView(final RatingTypeViewHolder holder, final int position,
                                      DynamicFormSectionFieldDAO fieldDAO, boolean isviewOnly,
                                      Context mContext, DynamicStagesCriteriaListDAO dynamicStagesCriteriaListDAO,
                                      boolean isCalculatedMappedField) {
        isViewOnly = isviewOnly;
        // SharedPreference pref = new SharedPreference(mContext);

        String label = fieldDAO.getLabel();
        AppCompatRatingBar ratingBar = holder.ratingBar;

        if (fieldDAO.getMaxVal() > 5) {
            ratingBar = new AppCompatRatingBar(mContext, null, android.R.attr.ratingBarStyleSmall);
            ratingBar.setIsIndicator(false);
            ratingBar.setStepSize(1);
            holder.parentlayout.addView(ratingBar);
            holder.ratingBar.setVisibility(View.GONE);
            holder.parentlayout.setVisibility(View.VISIBLE);
        }

        if (fieldDAO.isRequired() && !isViewOnly) {
            label += " *";
        }
        holder.tValueLabel.setText(label);
        ratingBar.setNumStars(fieldDAO.getMaxVal());
        ratingBar.setMax(fieldDAO.getMaxVal());


        if (fieldDAO.getValue() != null && !TextUtils.isEmpty(fieldDAO.getValue())) {
            ratingBar.setRating(Integer.parseInt(fieldDAO.getValue()));
            fieldDAO.setValidate(true);
        }

        if (fieldDAO.getAllowedValuesCriteria() != null) {
            String ratingColor = fieldDAO.getAllowedValuesCriteria();
            Drawable drawable = ratingBar.getProgressDrawable();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                DrawableCompat.setTint(drawable, Color.parseColor(ratingColor));
            } else {
                drawable.setColorFilter(Color.parseColor(ratingColor), PorterDuff.Mode.SRC_IN);
            }
            //drawable.setColorFilter(Color.parseColor(ratingColor), PorterDuff.Mode.SRC_ATOP);
        }

        if (isViewOnly)
            ratingBar.setEnabled(false);


        if (!isViewOnly) {
            if (!fieldDAO.isReadOnly()) {
                ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                    public void onRatingChanged(RatingBar ratingBar, float rating,
                                                boolean fromUser) {
                        int ratingValue = Math.round(rating);
                        CustomLogs.displayLogs(TAG + " rating_value: " + ratingValue);
                        fieldDAO.setValue(String.valueOf(ratingValue));

                        if (rating > 0)
                            fieldDAO.setValidate(true);
                        else
                            fieldDAO.setValidate(false);

                        // if (isCalculatedMappedField)
                        if (fieldDAO.isTigger())
                            CalculatedMappedRequestTrigger.submitCalculatedMappedRequest(mContext, isViewOnly, fieldDAO);

                        validateForm(fieldDAO);

                    }
                });
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
