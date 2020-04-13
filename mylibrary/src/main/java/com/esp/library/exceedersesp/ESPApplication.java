package com.esp.library.exceedersesp;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

import java.util.List;

import utilities.data.applicants.addapplication.CurrencyDAO;
import utilities.data.filters.FilterDAO;
import utilities.data.setup.TokenDAO;
import utilities.data.setup.UserDAO;

public class ESPApplication extends MultiDexApplication {

    Context bContext;
    UserDAO user;
    TokenDAO tokenPersonas;
    FilterDAO filter;
    FilterDAO filterTemp;
    List<CurrencyDAO> currencies;

    boolean isComponent = false; // used for library
    String access_token = null;

    public boolean isComponent() {
        return isComponent;
    }

    public void setComponent(boolean component) {
        isComponent = component;
    }

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }


    static ESPApplication application = null;

    public static ESPApplication getInstance() {

        if (application == null)
            return application = new ESPApplication();
        else
            return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bContext = getApplicationContext();
    }

    public UserDAO getUser() {
        return user;
    }


    public TokenDAO getTokenPersonas() {
        return tokenPersonas;
    }

    public void setTokenPersonas(TokenDAO tokenPersonas) {
        this.tokenPersonas = tokenPersonas;
    }

    public void setUser(UserDAO user) {
        this.user = user;
    }

    public FilterDAO getFilter() {
        return filter;
    }

    public void setFilter(FilterDAO filter) {
        this.filter = filter;
    }

    public FilterDAO getFilterTemp() {
        return filterTemp;
    }

    public void setFilterTemp(FilterDAO filterTemp) {
        this.filterTemp = filterTemp;
    }

    public List<CurrencyDAO> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<CurrencyDAO> currencies) {
        this.currencies = currencies;
    }

}
