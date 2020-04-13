package com.esp.library.utilities.common;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import utilities.data.setup.IdenediAuthDAO;
import utilities.model.Labels;


public class SharedPreference {

    private String PREFS_NAME = "savepref";
    String labels = "labels";
    String language = "language";
    String locales = "locales";
    String fbId = "firebaseId";
    String IdenediCode = "IdenediCode";
    String idenediAuthDAOObject = "idenediAuthDAOObject";
    String firebaseToken = "firebaseToken";
    String refreshToken = "refreshToken";
    String personid = "personid";
    String organizationid = "organizationid";
    String selectedUserRole = "selectedUserRole";
    String idenediClientId = "idenediClientId";
    String idenediCode = "idenediCode";
    SharedPreferences pref;
    SharedPreferences.Editor editor;


    public SharedPreference(Context context) {
        super();
        pref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void savelabels(Labels value) {
        Gson gson = new Gson();
        String json = gson.toJson(value);
        editor.putString(labels, json);
        editor.commit();
    }

    public Labels getlabels() {
        Gson gson = new Gson();
        String json = pref.getString(labels, null);
        Labels obj = gson.fromJson(json, Labels.class);

        return obj;
    }

    public void savelanguage(String value) {
        editor.putString(language, value);
        editor.commit();
    }

    public String getLanguage() {
        return pref.getString(language, "");
    }


    public void saveLocales(String locale) {
        editor.putString(locales, locale);
        editor.commit();
    }

    public String getLocales() {
        return pref.getString(locales, "");
    }

    public void saveFirebaseId(int id) {
        editor.putInt(fbId, id);
        editor.commit();
    }

    public int getFirebaseId() {
        return pref.getInt(fbId, 0);
    }

    public void savePersonaId(int id) {
        editor.putInt(personid, id);
        editor.commit();
    }

    public int getPersonaId() {
        return pref.getInt(personid, 0);
    }

    public void saveOrganizationId(int id) {
        editor.putInt(organizationid, id);
        editor.commit();
    }

    public int getOrganizationId() {
        return pref.getInt(organizationid, 0);
    }

    public void saveFirebaseToken(String token) {
        editor.putString(firebaseToken, token);
        editor.commit();
    }

    public String getFirebaseToken() {
        return pref.getString(firebaseToken, "");
    }

    public void saveidenediClientId(String idenediclientId) {
        editor.putString(idenediClientId, idenediclientId);
        editor.commit();
    }

    public String getidenediClientId() {
        return pref.getString(idenediClientId, null);
    }


    public void saveRefreshToken(String refreshtoken) {
        editor.putString(refreshToken, refreshtoken);
        editor.commit();
    }

    public String getRefreshToken() {
        return pref.getString(refreshToken, null);
    }


    public void saveSelectedUserRole(String role) {
        editor.putString(selectedUserRole, role);
        editor.commit();
    }

    public String getSelectedUserRole() {
        return pref.getString(selectedUserRole, "");
    }

    public void saveIdenediCode(String role) {
        editor.putString(idenediCode, role);
        editor.commit();
    }

    public String getIdenediCode() {
        return pref.getString(idenediCode, "");
    }


    public void saveidenediAuthDAO(IdenediAuthDAO tokenDAO) {

        Gson gson = new Gson();
        String json = gson.toJson(tokenDAO);
        editor.putString(idenediAuthDAOObject, json);
        editor.commit();
    }

    public IdenediAuthDAO getidenediAuthDAO() {

        Gson gson = new Gson();
        String json = pref.getString(idenediAuthDAOObject, "");
        IdenediAuthDAO obj = gson.fromJson(json, IdenediAuthDAO.class);
        return obj;

    }


    public void clearPref() {



        editor.putString(labels, null);
        editor.putString(locales, null);
        editor.putString(firebaseToken, null);
        editor.putString(fbId, null);
        editor.putString(personid, null);
        editor.putString(organizationid, null);
        editor.putString(IdenediCode, null);
        editor.putString(idenediAuthDAOObject, null);
        editor.putString(refreshToken, null);
        editor.putString(idenediCode, null);
        editor.putString(selectedUserRole, null);
        editor.putString(idenediClientId, null);
        editor.commit();


    }


}
