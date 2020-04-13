package com.esp.library.utilities.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import utilities.data.applicants.ApplicationDetailFieldsDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldLookupValuesDAO;
import utilities.data.applicants.dynamics.DynamicFormValuesDetailsDAO;
import utilities.data.applicants.dynamics.DynamicResponseDAO;
import utilities.data.applicants.dynamics.DynamicSectionValuesDAO;

public class GetValues {


    String TAG = getClass().getSimpleName();


    public List<ApplicationDetailFieldsDAO> getFormValues(DynamicResponseDAO response, int type) {

        List<ApplicationDetailFieldsDAO> list = new ArrayList<ApplicationDetailFieldsDAO>();

        if (response != null && response.getForm() != null && response.getForm().getSections() != null && response.getForm().getSections().size() > 0) {

            for (int i = 0; i < response.getForm().getSections().size(); i++) {
                CustomLogs.displayLogs(TAG + " getSectionName: " + response.getForm().getSections().get(i).getDefaultName());

                ApplicationDetailFieldsDAO apdd = new ApplicationDetailFieldsDAO();
                apdd.setSectionname(response.getForm().getSections().get(i).getDefaultName());
                list.add(apdd);

                if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().size() > 0) {


                    for (int j = 0; j < response.getForm().getSections().get(i).getFields().size(); j++) {

                        int getType = response.getForm().getSections().get(i).getFields().get(j).getType();

                        populateFields(response, list, i, j);


                        StringBuilder sb = new StringBuilder();
                        ////////////////// TYPE 5 and 6 HANDLING ////////////////////
                        if (getType == 5 || getType == 6) {
                            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().get(j) != null) {
                                ApplicationDetailFieldsDAO apd = new ApplicationDetailFieldsDAO();
                                apd.setType(type);
                                if (response.getForm().getSections().get(i).getFields().get(j).getLabel() != null && response.getForm().getSections().get(i).getFields().get(j).getLabel().length() > 0) {
                                    apd.setFieldName(response.getForm().getSections().get(i).getFields().get(j).getLabel());
                                } else {
                                    apd.setFieldName("");
                                }


                                //  DynamicFormValuesDAO dFVDAO = GetForValueObject(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getFormValues());
                                int sectionCustomFieldId = response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId();
                                DynamicSectionValuesDAO.Instance.Value dFVDAOvalue = GetForValueObjectValues(sectionCustomFieldId, response.getSectionValues());
                                if (dFVDAOvalue != null) {

                                    if (dFVDAOvalue.getValue() != null && dFVDAOvalue.getValue().length() > 0) {

                                        if (response.getForm().getSections().get(i).getFields().get(j).getLookupValues() != null && response.getForm().getSections().get(i).getFields().get(j).getLookupValues().size() > 0) {

                                            //  List<DynamicFormSectionFieldLookupValuesDAO> multi_selection = new ArrayList<DynamicFormSectionFieldLookupValuesDAO>();


                                            List<String> myList = new ArrayList<String>(Arrays.asList(dFVDAOvalue.getValue().split(",")));
                                            for (DynamicFormSectionFieldLookupValuesDAO lookup : response.getForm().getSections().get(i).getFields().get(j).getLookupValues()) {


                                                for (int k = 0; k < myList.size(); k++) {
                                                    Integer value_ = 0;
                                                    try {
                                                        value_ = Integer.parseInt(myList.get(k));
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    if (lookup.getId() == value_) {
                                                        //   lookup.setSelected(true);
                                                        if (sb.length() > 0)
                                                            sb.append(", ");
                                                        sb.append(lookup.getLabel());

                                                    }
                                                }

                                                apd.setFieldvalue(sb.toString());
                                                apd.setSectionId(sectionCustomFieldId);
                                                //  multi_selection.add(lookup);
                                            }


                                        }
                                    } else {
                                        apd.setFieldvalue("");
                                        String multiSelectionValue = multiSelectionValue(response.getForm().getSections().get(i).getFields().get(j).getLookupValues());
                                        if (multiSelectionValue.length() > 0) {
                                            apd.setFieldvalue(multiSelectionValue);
                                        } else {
                                            apd.setFieldvalue("");
                                        }
                                    }

                                }


                                if (apd.getFieldName().length() > 0) {
                                    list.add(apd);
                                }


                            }

                        }


                        ////////////////// TYPE 7 HANDLING ////////////////////
                        if (getType == 7) {

                            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().get(j) != null) {
                                ApplicationDetailFieldsDAO apd = new ApplicationDetailFieldsDAO();
                                apd.setType(type);
                                if (response.getForm().getSections().get(i).getFields().get(j).getLabel() != null && response.getForm().getSections().get(i).getFields().get(j).getLabel().length() > 0) {
                                    apd.setFieldName(response.getForm().getSections().get(i).getFields().get(j).getLabel());
                                } else {
                                    apd.setFieldName("");
                                }


                                //  DynamicFormValuesDAO dFVDAOvalue = GetForValueObject(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getFormValues());
                                int sectionCustomFieldId = response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId();
                                DynamicFormValuesDetailsDAO dFVDAOvalue = GetForValueObjectAttachmentDetails(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getSectionValues());
                                if (dFVDAOvalue != null) {

                                    CustomLogs.displayLogs(TAG + " dFVDAOvalue.getDetails(): " + dFVDAOvalue);

                                    if (dFVDAOvalue != null) {
                                        apd.setDownloadURL(dFVDAOvalue.getDownloadUrl());
                                        apd.setFieldvalue(dFVDAOvalue.getName());
                                        apd.setSectionId(sectionCustomFieldId);
                                    } else {
                                        apd.setDownloadURL(null);
                                        apd.setFieldvalue("");
                                    }

                                }


                                if (apd.getFieldName().length() > 0) {
                                    list.add(apd);
                                }
                            }
                        }

                        ////////////////// TYPE 8 HANDLING ////////////////////
                        if (getType == 8) {

                            ////////////////// TYPE 1 HANDLING ////////////////////
                            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().get(j) != null) {
                                ApplicationDetailFieldsDAO apd = new ApplicationDetailFieldsDAO();
                                apd.setType(type);
                                if (response.getForm().getSections().get(i).getFields().get(j).getLabel() != null && response.getForm().getSections().get(i).getFields().get(j).getLabel().length() > 0) {
                                    apd.setFieldName(response.getForm().getSections().get(i).getFields().get(j).getLabel());
                                } else {
                                    apd.setFieldName("");
                                }

                                int sectionCustomFieldId = response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId();
                                //  DynamicFormValuesDAO dFVDAO = GetForValueObject(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getFormValues());
                                DynamicSectionValuesDAO.Instance.Value dFVDAOvalue = GetForValueObjectValues(sectionCustomFieldId, response.getSectionValues());
                                if (dFVDAOvalue != null) {
                                    if (dFVDAOvalue.getValue() != null && dFVDAOvalue.getValue().length() > 0) {
                                        apd.setFieldvalue(dFVDAOvalue.getValue());
                                        apd.setSectionId(sectionCustomFieldId);
                                    } else {
                                        apd.setFieldvalue("");
                                    }
                                }


                                if (apd.getFieldName().length() > 0) {
                                    list.add(apd);
                                }
                            }
                        }

                        ////////////////// TYPE 9 HANDLING ////////////////////
                        if (getType == 9) {
                            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().get(j) != null) {
                                ApplicationDetailFieldsDAO apd = new ApplicationDetailFieldsDAO();
                                apd.setType(type);
                                if (response.getForm().getSections().get(i).getFields().get(j).getLabel() != null && response.getForm().getSections().get(i).getFields().get(j).getLabel().length() > 0) {
                                    apd.setFieldName(response.getForm().getSections().get(i).getFields().get(j).getLabel());
                                } else {
                                    apd.setFieldName("");
                                }

                                int sectionCustomFieldId = response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId();
                                //  DynamicFormValuesDAO dFVDAO = GetForValueObject(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getFormValues());
                                DynamicSectionValuesDAO.Instance.Value dFVDAOvalue = GetForValueObjectValues(sectionCustomFieldId, response.getSectionValues());
                                if (dFVDAOvalue != null) {
                                    if (dFVDAOvalue.getValue() != null && dFVDAOvalue.getValue().length() > 0) {
                                        apd.setFieldvalue(dFVDAOvalue.getValue());
                                        apd.setSectionId(sectionCustomFieldId);
                                    } else {
                                        apd.setFieldvalue("");
                                    }
                                }


                                if (apd.getFieldName().length() > 0) {
                                    list.add(apd);
                                }
                            }
                        }


                        ////////////////// TYPE 11 CURRENCY ////////////////////
                        if (getType == 11) {

                            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().get(j) != null) {
                                ApplicationDetailFieldsDAO apd = new ApplicationDetailFieldsDAO();
                                apd.setType(getType);
                                if (response.getForm().getSections().get(i).getFields().get(j).getLabel() != null && response.getForm().getSections().get(i).getFields().get(j).getLabel().length() > 0) {
                                    apd.setFieldName(response.getForm().getSections().get(i).getFields().get(j).getLabel());
                                } else {
                                    apd.setFieldName("");
                                }
                                int sectionCustomFieldId = response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId();
                                //   DynamicFormValuesDAO dFVDAO = GetForValueObject(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getFormValues());
                                DynamicFormSectionFieldDAO fieldDAO = new DynamicFormSectionFieldDAO();
                                DynamicSectionValuesDAO.Instance.Value dFVDAOvalue = GetForValueObjectValues(sectionCustomFieldId, response.getSectionValues());
                                if (dFVDAOvalue != null) {
                                    if (dFVDAOvalue.getValue() != null && dFVDAOvalue.getValue().length() > 0) {

                                        String currency_val = "";

                                        String[] currency = dFVDAOvalue.getValue().split("\\:");

                                        if (currency != null) {
                                            if (currency[currency.length - 1] != null) {
                                                currency_val = currency[currency.length - 1];
                                                // fieldDAO.setValue(currency_val);
                                                fieldDAO.setSelectedCurrencySymbol(currency_val);
                                                CustomLogs.displayLogs(TAG + " currency_val length: " + currency_val);
                                            }

                                            if (currency[0] != null) {
                                                currency_val += " " + currency[0];
                                                fieldDAO.setValue(currency[0]);
                                                CustomLogs.displayLogs(TAG + " currency_val: " + currency_val);
                                            }

                                            if (currency[1] != null) {
                                                int selectedCurrency = Integer.parseInt(currency[1]);
                                                CustomLogs.displayLogs(TAG + " selectedCurrency: " + selectedCurrency);
                                                fieldDAO.setSelectedCurrencyId(selectedCurrency);
                                            }
                                        }

                                        apd.setFieldvalue(currency_val);
                                        apd.setFieldsDAO(fieldDAO);
                                        apd.setSectionId(sectionCustomFieldId);
                                    } else {
                                        apd.setFieldvalue("");
                                        apd.setSectionId(sectionCustomFieldId);
                                    }
                                } else {
                                    apd.setFieldvalue("");
                                }


                                if (apd.getFieldName().length() > 0) {
                                    list.add(apd);
                                }
                            }
                        }

                        ////////////////// TYPE 13 HANDLING ////////////////////
                        if (getType == 13) {

                            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().get(j) != null) {

                                ApplicationDetailFieldsDAO apd = new ApplicationDetailFieldsDAO();

                                apd.setType(type);

                                if (response.getForm().getSections().get(i).getFields().get(j).getLabel() != null && response.getForm().getSections().get(i).getFields().get(j).getLabel().length() > 0) {
                                    apd.setFieldName(response.getForm().getSections().get(i).getFields().get(j).getLabel());
                                } else {
                                    apd.setFieldName("");
                                }
                                int sectionCustomFieldId = response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId();
                                // DynamicFormValuesDAO dFVDAO = GetForValueObject(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getFormValues());
                                DynamicSectionValuesDAO.Instance.Value dFVDAOvalue = GetForValueObjectValues(sectionCustomFieldId, response.getSectionValues());
                                if (dFVDAOvalue != null) {

                                    if (dFVDAOvalue.getSelectedLookupText() != null
                                            && !dFVDAOvalue.getSelectedLookupText().isEmpty()
                                            && dFVDAOvalue.getSelectedLookupText().length() > 0) {
                                        apd.setFieldvalue(dFVDAOvalue.getSelectedLookupText());
                                        apd.setSectionId(sectionCustomFieldId);
                                        apd.setLookupId(Integer.parseInt(dFVDAOvalue.getValue()));
                                    } else {
                                        apd.setFieldvalue("");
                                    }

                                } else {
                                    apd.setFieldvalue("");
                                }

                                if (apd.getFieldName().length() > 0) {
                                    list.add(apd);
                                }


                            }
                        }


                    }
                }


            }

        }// END form fields


        return list;
    }


    private void addLabel(DynamicResponseDAO response, ApplicationDetailFieldsDAO apd, int getType, int i, int j) {
        apd.setType(getType);
        if (response.getForm().getSections().get(i).getFields().get(j).getLabel() != null && response.getForm().getSections().get(i).getFields().get(j).getLabel().length() > 0) {
            String label = response.getForm().getSections().get(i).getFields().get(j).getLabel();
            int getSectionCustomFieldId = response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId();
            apd.setFieldName(label);
            CustomLogs.displayLogs(TAG + " label: " + label);

        } else {
            apd.setFieldName("");
        }
    }

    private void populateFields(DynamicResponseDAO response, List<ApplicationDetailFieldsDAO> list, int i, int j) {
        int getType = response.getForm().getSections().get(i).getFields().get(j).getType();
        if (getType == 1 || getType == 2 || getType == 3 || getType == 4 || getType == 10
                || getType == 15 || getType == 16|| getType == 17|| getType == 18) {

            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().get(j) != null) {

                ApplicationDetailFieldsDAO apd = new ApplicationDetailFieldsDAO();

                addLabel(response, apd, getType, i, j);

                int sectionCustomFieldId = response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId();
                List<DynamicSectionValuesDAO.Instance.Value> dFVDAOValueArray = GetForValueObjectValuesTest(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getSectionValues());
                //  DynamicSectionValuesDAO.Instance.Value dFVDAOvalue = GetForValueObjectValues(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getSectionValues());
                for (int p = 0; p < dFVDAOValueArray.size(); p++) {
                    apd = new ApplicationDetailFieldsDAO();
                    addLabel(response, apd, getType, i, j);
                    DynamicSectionValuesDAO.Instance.Value dFVDAOvalue = dFVDAOValueArray.get(p);
                    if (dFVDAOvalue != null) {
                        if (dFVDAOvalue.getValue() != null && dFVDAOvalue.getValue().length() > 0) {
                            CustomLogs.displayLogs(TAG + " populateFields dFVDAOvalue.getValue(): " + dFVDAOvalue.getValue() + " sectionCustomFieldId: " + sectionCustomFieldId + " dFVDAOvalue getId: " + dFVDAOvalue.getId());
                            apd.setFieldvalue(dFVDAOvalue.getValue());
                            apd.setSectionId(sectionCustomFieldId);
                            apd.setTag(dFVDAOvalue.getId() + "");
                            //  apd.setTag(apd.getFieldName() + " " + sectionCustomFieldId + " " + dFVDAOvalue.getValue());
                            CustomLogs.displayLogs(TAG + " apd.getTag(): " + apd.getTag());

                        } else {
                            String defaultValue = response.getForm().getSections().get(i).getFields().get(j).getValue();
                            apd.setFieldvalue(defaultValue);
                        }
                    } else {
                        String defaultValue = response.getForm().getSections().get(i).getFields().get(j).getValue();
                        apd.setFieldvalue(defaultValue);
                    }
                    CustomLogs.displayLogs(TAG + " populateFields apd.getFieldName(): " + apd.getFieldName());
                    if (apd.getFieldName().length() > 0) {
                        list.add(apd);
                    }
                }


            }

        }


    }

    private List<DynamicSectionValuesDAO.Instance.Value> GetForValueObjectValuesTest(int Id, List<DynamicSectionValuesDAO> sectionValues) {


        List<DynamicSectionValuesDAO.Instance.Value> formValuesDAO = new ArrayList<>();

        List<List<DynamicSectionValuesDAO.Instance.Value>> formValuesDAOArray = new ArrayList<>();

        String value = "";

        for (int i = 0; i < sectionValues.size(); i++) {

            for (int j = 0; j < sectionValues.get(i).getInstances().size(); j++) {

                for (int k = 0; k < sectionValues.get(i).getInstances().get(j).getValues().size(); k++) {

                    int sectionCustomFieldId = sectionValues.get(i).getInstances().get(j).getValues().get(k).getSectionCustomFieldId();

                    if (sectionCustomFieldId == Id) {

                        value = sectionValues.get(i).getInstances().get(j).getValues().get(k).getValue();
                        // String getSelectedLookupText = sectionValues.get(i).getInstances().get(j).getValues().get(k).getSelectedLookupText();
                        int getType = sectionValues.get(i).getInstances().get(j).getValues().get(k).getType();
                        formValuesDAO.add(sectionValues.get(i).getInstances().get(j).getValues().get(k));

                        CustomLogs.displayLogs(TAG + " GetForValueObjectValuesTest sectionCustomFieldId: " + sectionCustomFieldId + " value: " + value + " type: " + getType);
                    }
                }
                formValuesDAOArray.add(formValuesDAO);
                String abc = "abc";
            }
        }


        return formValuesDAO;

    }

    private DynamicSectionValuesDAO.Instance.Value GetForValueObjectValues(int Id, List<DynamicSectionValuesDAO> sectionValues) {


        DynamicSectionValuesDAO.Instance.Value formValuesDAO = null;
        String value = "";

        for (int i = 0; i < sectionValues.size(); i++) {
            for (int j = 0; j < sectionValues.get(i).getInstances().size(); j++) {
                for (int k = 0; k < sectionValues.get(i).getInstances().get(j).getValues().size(); k++) {
                    int sectionCustomFieldId = sectionValues.get(i).getInstances().get(j).getValues().get(k).getSectionCustomFieldId();
                    if (sectionCustomFieldId == Id) {
                        value = sectionValues.get(i).getInstances().get(j).getValues().get(k).getValue();
                        String getSelectedLookupText = sectionValues.get(i).getInstances().get(j).getValues().get(k).getSelectedLookupText();
                        int getType = sectionValues.get(i).getInstances().get(j).getValues().get(k).getType();
                        formValuesDAO = sectionValues.get(i).getInstances().get(j).getValues().get(k);
                        CustomLogs.displayLogs(TAG + " sectionCustomFieldId: " + sectionCustomFieldId + " value: " + value + " type: " + getType);
                        return formValuesDAO;
                    }
                }
            }
        }


        /*if (Id > 0 && formvalues != null && formvalues.size() > 0) {
            for (DynamicFormValuesDAO df : formvalues) {
                if (df.getSectionCustomFieldId() == Id) {
                    formValuesDAO = df;
                    break;
                }
            }
        }*/

        return formValuesDAO;

    }


    public List<ApplicationDetailFieldsDAO> GetFields(DynamicResponseDAO response) {


        List<ApplicationDetailFieldsDAO> list = new ArrayList<ApplicationDetailFieldsDAO>();

        if (response != null && response.getForm() != null && response.getForm().getSections() != null && response.getForm().getSections().size() > 0) {

            for (int i = 0; i < response.getForm().getSections().size(); i++) {
                CustomLogs.displayLogs(TAG + " getSectionName: " + response.getForm().getSections().get(i).getDefaultName());

                ApplicationDetailFieldsDAO apdd = new ApplicationDetailFieldsDAO();
                apdd.setSectionname(response.getForm().getSections().get(i).getDefaultName());
                list.add(apdd);

                if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().size() > 0) {


                    for (int j = 0; j < response.getForm().getSections().get(i).getFields().size(); j++) {


                        populateFields(response, list, i, j);

                        StringBuilder sb = new StringBuilder();
                        ////////////////// TYPE 5 and 6 HANDLING ////////////////////
                        if (response.getForm().getSections().get(i).getFields().get(j).getType() == 6
                                || response.getForm().getSections().get(i).getFields().get(j).getType() == 5) {
                            int type = response.getForm().getSections().get(i).getFields().get(j).getType();
                            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().get(j) != null) {
                                ApplicationDetailFieldsDAO apd = new ApplicationDetailFieldsDAO();
                                apd.setType(type);
                                if (response.getForm().getSections().get(i).getFields().get(j).getLabel() != null && response.getForm().getSections().get(i).getFields().get(j).getLabel().length() > 0) {
                                    apd.setFieldName(response.getForm().getSections().get(i).getFields().get(j).getLabel());
                                } else {
                                    apd.setFieldName("");
                                }

                                DynamicSectionValuesDAO.Instance.Value dFVDAOvalue = GetForValueObjectValues(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getSectionValues());
                                if (dFVDAOvalue != null) {

                                    if (dFVDAOvalue.getValue() != null && dFVDAOvalue.getValue().length() > 0) {

                                        if (response.getForm().getSections().get(i).getFields().get(j).getLookupValues() != null && response.getForm().getSections().get(i).getFields().get(j).getLookupValues().size() > 0) {

                                            List<String> myList = new ArrayList<String>(Arrays.asList(dFVDAOvalue.getValue().split(",")));
                                            for (DynamicFormSectionFieldLookupValuesDAO lookup : response.getForm().getSections().get(i).getFields().get(j).getLookupValues()) {


                                                for (int k = 0; k < myList.size(); k++) {
                                                    Integer value_ = 0;
                                                    try {
                                                        value_ = Integer.parseInt(myList.get(k));
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    if (lookup.getId() == value_) {
                                                        //   lookup.setSelected(true);
                                                        if (sb.length() > 0)
                                                            sb.append(", ");
                                                        sb.append(lookup.getLabel());

                                                    }
                                                }

                                                apd.setFieldvalue(sb.toString());

                                            }

                                        }
                                    } else {
                                        apd.setFieldvalue("");
                                        String multiSelectionValue = multiSelectionValue(response.getForm().getSections().get(i).getFields().get(j).getLookupValues());
                                        if (multiSelectionValue.length() > 0) {
                                            apd.setFieldvalue(multiSelectionValue);
                                        } else {
                                            apd.setFieldvalue("");
                                        }
                                    }

                                }

                                if (apd.getFieldName().length() > 0) {
                                    list.add(apd);
                                }


                            }
                        }


                        ////////////////// TYPE 7 HANDLING ////////////////////
                        if (response.getForm().getSections().get(i).getFields().get(j).getType() == 7) {

                            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().get(j) != null) {
                                ApplicationDetailFieldsDAO apd = new ApplicationDetailFieldsDAO();
                                apd.setType(7);
                                if (response.getForm().getSections().get(i).getFields().get(j).getLabel() != null && response.getForm().getSections().get(i).getFields().get(j).getLabel().length() > 0) {
                                    apd.setFieldName(response.getForm().getSections().get(i).getFields().get(j).getLabel());
                                } else {
                                    apd.setFieldName("");
                                }

                                DynamicFormValuesDetailsDAO dFVDAOvalue = GetForValueObjectAttachmentDetails(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getSectionValues());
                                if (dFVDAOvalue != null) {

                                    CustomLogs.displayLogs(TAG + " dFVDAOvalue.getDetails(): " + dFVDAOvalue);

                                    if (dFVDAOvalue != null) {
                                        apd.setDownloadURL(dFVDAOvalue.getDownloadUrl());
                                        apd.setFieldvalue(dFVDAOvalue.getName());
                                    } else {
                                        apd.setDownloadURL(null);
                                        apd.setFieldvalue("");
                                    }

                                }


                                if (apd.getFieldName().length() > 0) {
                                    list.add(apd);
                                }
                            }
                        }

                        ////////////////// TYPE 8 HANDLING ////////////////////
                        if (response.getForm().getSections().get(i).getFields().get(j).getType() == 8) {

                            ////////////////// TYPE 1 HANDLING ////////////////////
                            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().get(j) != null) {
                                ApplicationDetailFieldsDAO apd = new ApplicationDetailFieldsDAO();
                                apd.setType(8);
                                if (response.getForm().getSections().get(i).getFields().get(j).getLabel() != null && response.getForm().getSections().get(i).getFields().get(j).getLabel().length() > 0) {
                                    apd.setFieldName(response.getForm().getSections().get(i).getFields().get(j).getLabel());
                                } else {
                                    apd.setFieldName("");
                                }
                                DynamicSectionValuesDAO.Instance.Value dFVDAOvalue = GetForValueObjectValues(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getSectionValues());
                                if (dFVDAOvalue != null) {
                                    if (dFVDAOvalue.getValue() != null && dFVDAOvalue.getValue().length() > 0) {
                                        apd.setFieldvalue(dFVDAOvalue.getValue());

                                    } else {
                                        apd.setFieldvalue("");
                                    }
                                }

                                if (apd.getFieldName().length() > 0) {
                                    list.add(apd);
                                }
                            }
                        }

                        ////////////////// TYPE 9 HANDLING ////////////////////
                        if (response.getForm().getSections().get(i).getFields().get(j).getType() == 9) {
                            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().get(j) != null) {
                                ApplicationDetailFieldsDAO apd = new ApplicationDetailFieldsDAO();
                                apd.setType(9);
                                if (response.getForm().getSections().get(i).getFields().get(j).getLabel() != null && response.getForm().getSections().get(i).getFields().get(j).getLabel().length() > 0) {
                                    apd.setFieldName(response.getForm().getSections().get(i).getFields().get(j).getLabel());
                                } else {
                                    apd.setFieldName("");
                                }
                                DynamicSectionValuesDAO.Instance.Value dFVDAOvalue = GetForValueObjectValues(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getSectionValues());
                                if (dFVDAOvalue != null) {
                                    if (dFVDAOvalue.getValue() != null && dFVDAOvalue.getValue().length() > 0) {
                                        apd.setFieldvalue(dFVDAOvalue.getValue());

                                    } else {
                                        apd.setFieldvalue("");
                                    }
                                }


                                if (apd.getFieldName().length() > 0) {
                                    list.add(apd);
                                }
                            }
                        }


                        ////////////////// TYPE 11 CURRENTY ////////////////////
                        if (response.getForm().getSections().get(i).getFields().get(j).getType() == 11) {

                            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().get(j) != null) {
                                ApplicationDetailFieldsDAO apd = new ApplicationDetailFieldsDAO();
                                apd.setType(11);
                                if (response.getForm().getSections().get(i).getFields().get(j).getLabel() != null && response.getForm().getSections().get(i).getFields().get(j).getLabel().length() > 0) {
                                    apd.setFieldName(response.getForm().getSections().get(i).getFields().get(j).getLabel());
                                } else {
                                    apd.setFieldName("");
                                }
                                DynamicSectionValuesDAO.Instance.Value dFVDAOvalue = GetForValueObjectValues(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getSectionValues());
                                if (dFVDAOvalue != null) {
                                    if (dFVDAOvalue.getValue() != null && dFVDAOvalue.getValue().length() > 0) {

                                        String currency_val = "";

                                        String[] currency = dFVDAOvalue.getValue().split("\\:");

                                        if (currency != null) {
                                            if (currency[currency.length - 1] != null) {
                                                currency_val = currency[currency.length - 1];
                                            }

                                            if (currency[0] != null) {
                                                currency_val += " " + currency[0];
                                            }
                                        }

                                        apd.setFieldvalue(currency_val);
                                    } else {
                                        apd.setFieldvalue("");
                                    }
                                } else {
                                    apd.setFieldvalue("");
                                }


                                if (apd.getFieldName().length() > 0) {
                                    list.add(apd);
                                }
                            }
                        }


                        ////////////////// TYPE 13 HANDLING ////////////////////
                        if (response.getForm().getSections().get(i).getFields().get(j).getType() == 13) {

                            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().get(j) != null) {

                                ApplicationDetailFieldsDAO apd = new ApplicationDetailFieldsDAO();

                                apd.setType(13);

                                if (response.getForm().getSections().get(i).getFields().get(j).getLabel() != null && response.getForm().getSections().get(i).getFields().get(j).getLabel().length() > 0) {
                                    apd.setFieldName(response.getForm().getSections().get(i).getFields().get(j).getLabel());
                                } else {
                                    apd.setFieldName("");
                                }
                                DynamicSectionValuesDAO.Instance.Value dFVDAOvalue = GetForValueObjectValues(response.getForm().getSections().get(i).getFields().get(j).getSectionCustomFieldId(), response.getSectionValues());
                                if (dFVDAOvalue != null) {

                                    if (dFVDAOvalue.getSelectedLookupText() != null
                                            && !dFVDAOvalue.getSelectedLookupText().isEmpty()
                                            && dFVDAOvalue.getSelectedLookupText().length() > 0) {
                                        apd.setFieldvalue(dFVDAOvalue.getSelectedLookupText());
                                    } else {
                                        apd.setFieldvalue("");
                                    }

                                } else {
                                    apd.setFieldvalue("");
                                }

                                if (apd.getFieldName().length() > 0) {
                                    list.add(apd);
                                }


                            }
                        }


                    }
                }


            }

        }// END form fields


        return list;
    }


    private String multiSelectionValue(List<DynamicFormSectionFieldLookupValuesDAO> multiselection) {

        CustomLogs.displayLogs(TAG + " multiSelectionValue multiselection: " + multiselection);

        String values = "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < multiselection.size(); i++) {
            if (multiselection.get(i).isSelected()) {
                String label = multiselection.get(i).getLabel();
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append(label);
            }
        }
        if (sb != null)
            values = String.valueOf(sb);
        return values;
    }

    private DynamicFormValuesDetailsDAO GetForValueObjectAttachmentDetails(int Id, List<DynamicSectionValuesDAO> sectionValues) {


        DynamicFormValuesDetailsDAO getPhotodetails = null;
        String value = "";


        for (int i = 0; i < sectionValues.size(); i++) {

            for (int j = 0; j < sectionValues.get(i).getInstances().size(); j++) {
                for (int k = 0; k < sectionValues.get(i).getInstances().get(j).getValues().size(); k++) {
                    int sectionCustomFieldId = sectionValues.get(i).getInstances().get(j).getValues().get(k).getSectionCustomFieldId();
                    if (sectionCustomFieldId == Id) {

                        getPhotodetails = sectionValues.get(i).getInstances().get(j).getValues().get(k).getDetails();
                        CustomLogs.displayLogs(TAG + " GetForValueObjectAttachmentDetails getPhotodetails: " + getPhotodetails);
                        return getPhotodetails;
                    }
                }
            }
        }


        return getPhotodetails;

    }

    /*private DynamicSectionValuesDAO.Instance.Value GetForValueObjectValues1(int Id, List<DynamicSectionValuesDAO> sectionValues) {


        DynamicSectionValuesDAO.Instance.Value formValuesDAO = null;
        String value = "";

        for (int i = 0; i < sectionValues.size(); i++) {
            for (int j = 0; j < sectionValues.get(i).getInstances().size(); j++) {
                for (int k = 0; k < sectionValues.get(i).getInstances().get(j).getValues().size(); k++) {
                    int sectionCustomFieldId = sectionValues.get(i).getInstances().get(j).getValues().get(k).getSectionCustomFieldId();
                    if (sectionCustomFieldId == Id) {
                        value = sectionValues.get(i).getInstances().get(j).getValues().get(k).getValue();
                        String getSelectedLookupText = sectionValues.get(i).getInstances().get(j).getValues().get(k).getSelectedLookupText();
                        int getType = sectionValues.get(i).getInstances().get(j).getValues().get(k).getType();

                        formValuesDAO = sectionValues.get(i).getInstances().get(j).getValues().get(k);
                        CustomLogs.displayLogs(TAG + " sectionCustomFieldId: " + sectionCustomFieldId + " value: " + value + " type: " + getType);
                        return formValuesDAO;
                    }
                }
            }
        }


        return formValuesDAO;

    }*/

}
