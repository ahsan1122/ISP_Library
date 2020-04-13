package com.esp.library.exceedersesp.controllers.fieldstype.other;

import com.esp.library.exceedersesp.controllers.Profile.EditSectionDetails;
import com.esp.library.utilities.setup.applications.ApplicationFieldsRecyclerAdapter;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO;
import utilities.interfaces.CriteriaFieldsListener;

public class Validation {

    private ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener mApplicationFieldsAdapterListener;
    private CriteriaFieldsListener criteriaFieldsListener;
    private DynamicStagesCriteriaListDAO criteriaListDAO;
    private DynamicFormSectionFieldDAO fieldDAO;
    EditSectionDetails editSectionDetailslistener;

    public Validation(ApplicationFieldsRecyclerAdapter.ApplicationFieldsAdapterListener applicationFieldsAdapterListener,
                      CriteriaFieldsListener criteriafieldsListener, DynamicStagesCriteriaListDAO criterialistDAO,
                      DynamicFormSectionFieldDAO fielddAO) {

        mApplicationFieldsAdapterListener = applicationFieldsAdapterListener;
        criteriaFieldsListener = criteriafieldsListener;
        criteriaListDAO = criterialistDAO;
        fieldDAO = fielddAO;
    }

    public void setSectionListener(EditSectionDetails edisectionDetailslistener)
    {
        editSectionDetailslistener=edisectionDetailslistener;
    }

    public void validateForm() {

        if (criteriaListDAO != null && criteriaFieldsListener != null)
            setCriteriaValidation();

        if (mApplicationFieldsAdapterListener != null)
            mApplicationFieldsAdapterListener.onFieldValuesChanged();

        if(editSectionDetailslistener!=null)
            editSectionDetailslistener.onFieldValuesChanged();
    }

    private void setCriteriaValidation() {
        try {
            if (criteriaFieldsListener != null) {
                criteriaListDAO.setValidate(fieldDAO.isValidate());
                criteriaFieldsListener.validateCriteriaFields(criteriaListDAO);
            }
        } catch (Exception e) {

        }
    }
}
