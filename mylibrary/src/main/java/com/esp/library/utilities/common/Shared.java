package com.esp.library.utilities.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.esp.library.R;
import com.esp.library.exceedersesp.BaseActivity;
import com.esp.library.exceedersesp.ESPApplication;
import com.esp.library.exceedersesp.controllers.Profile.ProfileMainActivity;
import com.esp.library.exceedersesp.controllers.SplashScreenActivity;
import com.esp.library.exceedersesp.controllers.applications.ApplicationsActivityDrawer;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import utilities.data.apis.APIs;
import utilities.data.applicants.ApplicationSingleton;
import utilities.data.applicants.addapplication.CurrencyDAO;
import utilities.data.applicants.addapplication.LookUpDAO;
import utilities.data.applicants.dynamics.DyanmicFormSectionFieldDetailsDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldLookupValuesDAO;
import utilities.data.applicants.dynamics.DynamicFormSectionFieldsCardsDAO;
import utilities.data.applicants.dynamics.DynamicResponseDAO;
import utilities.data.applicants.dynamics.DynamicStagesCriteriaCommentsListDAO;
import utilities.data.applicants.dynamics.DynamicStagesCriteriaListDAO;
import utilities.data.applicants.dynamics.DynamicStagesDAO;
import utilities.data.applicants.profile.ApplicationProfileDAO;
import utilities.data.filters.FilterDAO;
import utilities.data.setup.PersonaDAO;

public class Shared {

    String TAG = "Shared";
    static Shared shared = null;
    public int Image_Height;
    androidx.appcompat.app.AlertDialog builder;

    private final SimpleDateFormat DATE_TIME_FORMAT_FULL = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a ZZZZZ");
    private final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final SimpleDateFormat DATE_TIME_FORMAT_NEW = new SimpleDateFormat("YYYY-MM-DD'T'HH:mm:ss'Z'");
    private final SimpleDateFormat DATE_TIME_FORMAT_SQL = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
    private final SimpleDateFormat DATE_TIME_FORMAT_DATA_TIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSz");
    private final SimpleDateFormat DATE_TIME_FORMAT_FULL_T = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private final SimpleDateFormat DATE_TIME_FORMAT_SSS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private final SimpleDateFormat DATE_TIME_FORMAT_FULL_TIME = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


    public final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public final SimpleDateFormat DATE_DISPLAY_FORMAT = new SimpleDateFormat("mm dd, yyyy");
    public ProgressDialog progressDialog = null;
    private static HashMap<Integer, List<LookUpDAO>> lookUpItemHashMap;

    public static Shared getInstance() {
        if (shared == null) {
            lookUpItemHashMap = new HashMap();
            return shared = new Shared();
        } else {

            return shared;
        }
    }


    public String GetCurrenTime() {
        Date date = new Date();
        return TIME_FORMAT.format(date);
    }

    public String GetCurrentDateTime() {
        Date date = new Date();
        //return DATE_TIME_FORMAT.format(date);
        return DATE_TIME_FORMAT_SQL.format(date);


    }


    public Date parseStringToDate(String strDate) {
        Date date = null;
        try {
            //  date = DATE_TIME_FORMAT.parse(strDate);
            date = DATE_TIME_FORMAT_SQL.parse(strDate);


        } catch (Exception e) {
            Log.e("error " + strDate, "");
        }
        return date;
    }

    public Date parseStringToDateSQL(String strDate) {
        Date date = null;
        try {
            // date = DATE_TIME_FORMAT_SQL.parse(strDate);
            date = DATE_TIME_FORMAT_SQL.parse(strDate);


        } catch (Exception e) {
            Log.e("error " + strDate, "");
        }
        return date;
    }


    public Calendar DateToCalendar(Date date) {
        Calendar calendar = null;
        try {
            calendar = Calendar.getInstance();
            calendar.setTime(date);

        } catch (Exception e) {
        }

        return calendar;
    }


    public Calendar getTodayCalendar() {
        Calendar calendar = null;
        try {
            calendar = Calendar.getInstance();
        } catch (Exception e) {
        }
        return calendar;
    }


    public String GetOrgName() {

        String OrgName = "";

        if (ESPApplication.getInstance().getUser().getLoginResponse().getOrganizationId() != null) {

            List<PersonaDAO> list = null;
            String personas = ESPApplication.getInstance().getUser().getLoginResponse().getPersonas();
            Gson gson = new Gson();
            list = gson.fromJson(personas, new TypeToken<List<PersonaDAO>>() {
            }.getType());

            if (list != null && list.size() > 0) {

                for (PersonaDAO p : list) {

                    if (p != null && p.getOrgId() != null) {

                        if (ESPApplication.getInstance().getUser().getLoginResponse().getOrganizationId().equals(p.getOrgId())) {
                            OrgName = p.getOrgName();
                            break;
                        }
                    }
                }
            }

        }


        return OrgName;

    }

    public void messageBox(String message, Activity context) {

        /*LayoutInflater inflater = context.getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_alert_view,
                (ViewGroup) context.findViewById(R.id.toast_layout_root));

        TextView text = layout.findViewById(R.id.text);
        text.setText(message);

        Toast toast = new Toast(context.getApplicationContext());
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();*/
        if (!message.equalsIgnoreCase("Socket closed"))
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    /**
     * This method will create a folder on SDCARD on the given path.
     */
    public Boolean createFolder(String folderPath, String folderName, Context context) {

        File file = new File(folderPath + "/" + folderName);

        if (file.exists())
            return false;
        else {
            return file.mkdirs();
        }
    }


    public String AddZero(int values) {
        String str_return = null;
        if (values <= 9) {
            str_return = "0" + values;
        } else {
            str_return = values + "";
        }

        return str_return;
    }

    public boolean isValidEmailAddress(String emailAddress) {

        boolean IsValid = false;

        Pattern pattern = Patterns.EMAIL_ADDRESS;

        IsValid = pattern.matcher(emailAddress).matches();

        String regExpn = "[0-9._-]+@[a-z]+\\.+[a-z]+";

        Pattern pattern2 = Pattern.compile(regExpn, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern2.matcher(emailAddress);
        if (matcher.matches())
            IsValid = false;


        return IsValid;
    }

    public boolean isValidPhoneNumber(String phone) {

        String expression = "^([0-9\\+]|\\(\\d{1,3}\\))[0-9\\-\\. ]{3,15}$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(phone);

        return matcher.matches();
    }

    public boolean isNumeric(final String str) {

        // null or empty
        if (str == null || str.length() == 0) {
            return false;
        }

        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }

        return true;

    }


    public boolean isWifiConnected(Context context) {

        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected()) {
            return true;
        } else /*
         * check.setImageResource(R.drawable.tick);
         * check.setVisibility(View.VISIBLE); noInterntet.setText("");
         */return mobile.isConnected();
    }

    /**
     * This method is the implementation of AS operator in C#.NET
     */
    public <T> T as(Class<T> className, Object object) {
        if (className.isInstance(object)) {
            return className.cast(object);
        }
        return null;
    }


    public String toSubStr(String str, int max) {

        if (str.length() > max) {

            return str.substring(0, max) + "..";

        }

        return str;


    }


    public void showAlertMessage(String title, String message, final Context activity) {

        new MaterialAlertDialogBuilder(activity, R.style.AlertDialogTheme)
                .setTitle(title)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton(activity.getApplicationContext().getString(R.string.ok), (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    public void showAlertProfileMessage(String title, String message, final Context activity) {

        try {
            if (builder == null) {

                builder = new MaterialAlertDialogBuilder(activity, R.style.AlertDialogTheme)
                        .setTitle(title)
                        .setCancelable(false)
                        .setMessage(message)
                        .setPositiveButton(activity.getApplicationContext().getString(R.string.takemetoprofile), (dialogInterface, i) -> {
                            builder = null;
                            getApplicant(activity);
                            dialogInterface.dismiss();
                        })
                        .setNeutralButton(activity.getApplicationContext().getString(R.string.cancel), (dialogInterface, i) -> {
                            builder = null;
                            dialogInterface.dismiss();
                        })
                        .show();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getApplicant(Context context) {
        AlertDialog progressdialog = setProgressDialog(context);
        try {

            if (!progressdialog.isShowing())
                progressdialog.show();

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(logging);

            httpClient.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "bearer " + ESPApplication.getInstance().getUser().getLoginResponse().getAccess_token())
                            .header("locale", getLanguageSimpleContext(context));

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });

            httpClient.connectTimeout(2, TimeUnit.MINUTES);
            httpClient.readTimeout(2, TimeUnit.MINUTES);
            httpClient.writeTimeout(2, TimeUnit.MINUTES);

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();


            final APIs apis = retrofit.create(APIs.class);


            Call<ApplicationProfileDAO> labels_call = apis.Getapplicant();

            labels_call.enqueue(new Callback<ApplicationProfileDAO>() {
                @Override
                public void onResponse(Call<ApplicationProfileDAO> call, Response<ApplicationProfileDAO> response) {
                    if (progressdialog.isShowing())
                        progressdialog.dismiss();
                    ApplicationProfileDAO body = response.body();
                    boolean profileSubmitted = response.body().getApplicant().isProfileSubmitted();
                    CustomLogs.displayLogs(TAG + " response profileSubmitted: " + profileSubmitted);


                    Intent mainIntent = new Intent(context, ProfileMainActivity.class);
                    mainIntent.putExtra("dataapplicant", body);
                    mainIntent.putExtra("ischeckerror", true);
                    mainIntent.putExtra("isprofile", true);
                    context.startActivity(mainIntent);

                }

                @Override
                public void onFailure(Call<ApplicationProfileDAO> call, Throwable t) {
                    if (progressdialog.isShowing())
                        progressdialog.dismiss();
                    t.printStackTrace();
                    messageBox(context.getString(R.string.some_thing_went_wrong), (Activity) context);
                }
            });


        } catch (Exception ex) {
            ex.printStackTrace();
            if (progressdialog.isShowing())
                progressdialog.dismiss();
            messageBox(context.getString(R.string.some_thing_went_wrong), (Activity) context);
        }
    }


    public void callIntent(Class<?> activityName, Activity context) {
        Intent i = new Intent(context, activityName);
        //  String pacakageName = context.getPackageName();
        //  i.setClassName(pacakageName, pacakageName + "." + activityName);
        //  Log.e("Activity", pacakageName + "." + activityName);
        context.startActivity(i);


    }


    /***
     * @param activityName Activity Name to call
     * @param context      Activity context
     * @param milliseconds Time to wait in milliseconds e.g (1 sec equals 1000 millis)
     */
    public void callIntentWithTime(Class<?> activityName, final Activity context, final int milliseconds, final String folderPath) {

        final boolean _alive = true;

        Thread splashThread = new Thread() {

            @Override
            public void run() {
                try {
                    int splashTime = 0;
                    while (_alive && (splashTime < milliseconds)) {
                        sleep(100);
                        if (_alive) {
                            splashTime += 100;
                        }
                    }

                } catch (Exception e) {

                } finally {

                    context.finish();
                    callIntent(activityName, context);
                }

            }

        };
        splashThread.start();

    }


    public void callIntent(Class<?> activityName, Activity context, Bundle bundle) {
        String pacakageName = context.getPackageName();
        Intent intent = new Intent(context, activityName);
        intent.putExtras(bundle);
        // intent.setClassName(pacakageName, pacakageName + "." + activityName);

        // Log.e("Activity", pacakageName + "." + activityName);
        context.startActivity(intent);
    }

    public void callIntentClearAllActivities(Class<?> activityName, Activity context, Bundle bundle) {
       /* String pacakageName = context.getPackageName();
        Intent intent = new Intent();
        if (bundle != null) {
            intent.putExtras(bundle);
        }



        intent.setClassName(pacakageName, pacakageName + "." + activityName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);*/


        Intent intentToBeNewRoot = new Intent(context, activityName);
        if (bundle != null) {
            intentToBeNewRoot.putExtras(bundle);
        }
        ComponentName cn = intentToBeNewRoot.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(cn);
        context.startActivity(mainIntent);


    }

    public String getLanguage(BaseActivity bContext) {
        SharedPreference pref = new SharedPreference(bContext);
        Locale locale = new Locale(pref.getLanguage());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return locale.toLanguageTag();
        } else {
            return locale.getLanguage();
        }
    }

    public String getLanguageSimpleContext(Context bContext) {
        SharedPreference pref = new SharedPreference(bContext);
        Locale locale = new Locale(pref.getLanguage());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return locale.toLanguageTag();
        } else {
            return locale.getLanguage();
        }
    }

    public String getVersionName(Context context) {
        String version = "";
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }

    public int getVersionCode(Context context) {
        int version = 0;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return version;
    }

    public void callIntentWithResult(Class<?> activityName, Activity context, Bundle bundle, int requestCode) {
        String pacakageName = context.getPackageName();
        Intent intent = new Intent(context, activityName);

        if (bundle != null)
            intent.putExtras(bundle);

        // intent.setClassName(pacakageName, pacakageName + "." + activityName);

        // Log.e("Activity", pacakageName + "." + activityName);
        context.startActivityForResult(intent, requestCode);
    }


    public void SignOut(BaseActivity bContext, boolean IsLogOut) {

        if ((ESPApplication.getInstance()).getUser() != null) {
            (ESPApplication.getInstance()).setUser(null);
        }

        if ((ESPApplication.getInstance()).getCurrencies() != null) {
            (ESPApplication.getInstance()).setCurrencies(null);
        }


        Shared.getInstance().WritePref("Uname", null, "login_info", bContext);
        Shared.getInstance().WritePref("Pass", null, "login_info", bContext);
        Shared.getInstance().WritePref("scropId", null, "login_info", bContext);

        Shared.getInstance().ClearPref("login_info", bContext);

        SharedPreference pref = new SharedPreference(bContext);
        pref.clearPref();

        if (IsLogOut) {
            bContext.finish();
            callIntentClearAllActivities(SplashScreenActivity.class, bContext, null);
        }
    }

    public void SignOutNotClear(BaseActivity bContext, boolean IsLogOut) {

        if ((ESPApplication.getInstance()).getUser() != null) {
            (ESPApplication.getInstance()).setUser(null);
        }

        if ((ESPApplication.getInstance()).getCurrencies() != null) {
            (ESPApplication.getInstance()).setCurrencies(null);
        }

        if (IsLogOut) {
            bContext.finish();
            callIntentClearAllActivities(SplashScreenActivity.class, bContext, null);
        }
    }

    public String getUAEAmountFromDecimal(Double amt) {
        String amount = "";

        try {
            // amount = new DecimalFormat( "#0.00" , new DecimalFormatSymbols( Locale.ENGLISH)).format(amt);
            amount = new DecimalFormat("#,###", new DecimalFormatSymbols(Locale.ENGLISH)).format(amt);
        } catch (Exception e) {
        }

        return amount;
    }

    public String getUAEAmountFromDecimalRound(Double amt) {
        String amount = "";

        try {
            // amount = new DecimalFormat( "#0.00" , new DecimalFormatSymbols( Locale.ENGLISH)).format(amt);
            amount = new DecimalFormat("#.#", new DecimalFormatSymbols(Locale.ENGLISH)).format(amt);
        } catch (Exception e) {
        }

        return amount;
    }

    /***
     * This method will write the error logs in Red Color
     *
     * @param tag
     * @param msg
     */
    public void errorLogWrite(String tag, String msg) {
        Log.e(tag, msg);
    }

    /***
     * This method will write the error logs in blue color.
     *
     * @param tag
     * @param msg
     */
    public void logWrite(String tag, String msg) {

        Log.d(tag, msg);
    }


    public String ReplaceSpeicalChars(String str, String replace_char) {
        String newName = "";
        str.trim();

        newName = str.replaceAll("[^a-zA-Z.]", replace_char);
        return newName;
    }

    public FilterDAO ResetApplicationFilter(BaseActivity bContext) {
        FilterDAO filterDAO = new FilterDAO();

        filterDAO.setFilterApplied(false);
        filterDAO.setSearch("");
        List<String> empty_fitler = new ArrayList<String>();

        String[] status = bContext.getResources().getStringArray(R.array.status);

        if (status != null && status.length > 0) {

            for (int i = 0; i < status.length; i++) {
                if (i != 0) {
                    empty_fitler.add(i + "");
                }

            }
        }

        filterDAO.setStatuses(empty_fitler);

        filterDAO.setPageNo(1);
        filterDAO.setRecordPerPage(10);
        List<String> empty_cat = new ArrayList<String>();
        filterDAO.setCategoreis(empty_cat);

        return filterDAO;
    }

    public FilterDAO CloneFilter(FilterDAO forclone) {
        FilterDAO filterDAO = null;
        Gson g = new Gson();
        String json = forclone.toJson();
        filterDAO = g.fromJson(json, FilterDAO.class);
        return filterDAO;
    }


    public String ReadPref(String pref_name, String pref_root_name, Context act) {

        SharedPreferences settings = act.getSharedPreferences(pref_root_name, 0);
        String value = settings.getString(pref_name, null);
        return value;
    }

    public void WritePref(String pref_name, String pref_value, String pref_root_name, Context act) {

        SharedPreferences settings = act.getSharedPreferences(pref_root_name, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(pref_name, pref_value);

        editor.commit();
    }


    public void ClearPref(String pref_root_name, Context act) {

        SharedPreferences settings = act.getSharedPreferences(pref_root_name, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
    }


    public List<PersonaDAO> GetApplicantOrganization(List<PersonaDAO> list, Context context) {

        SharedPreference pref = new SharedPreference(context);
        List<PersonaDAO> list_org = new ArrayList<PersonaDAO>();

        if (list != null && list.size() > 0) {
            list_org.addAll(list);

            /*if (list.get(0).getType().toLowerCase().equalsIgnoreCase("app")) {
                pref.saveSelectedUserRole(context.getString(R.string.applicant));
            } else {
                pref.saveSelectedUserRole(context.getString(R.string.assessor));
            }*/
        }
        return list_org;
    }


    public void DrawermenuAction(int id, String CurrentPage, BaseActivity context) {

        if (id == 0) {
            context.finish();
            callIntentClearAllActivities(ApplicationsActivityDrawer.class, context, null);
        } else if (id == 1) {
            //context.finish();

        } else if (id == 2) {
            //context.finish();
            SignOut(context, true);
        }


    }

    public String getDatePickerGMTDate(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar.set(year, month, day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'");
        Date currentLocalTime = calendar.getTime();
        DateFormat dateee = new SimpleDateFormat("z");
        String localTime = dateee.format(currentLocalTime);
        String formatedDate = sdf.format(calendar.getTime()) + "00:00:00" + localTime.replaceAll("GMT", "");
        return formatedDate;

    }

    public String getStageDisplayDate(Context context, String date_str) {

        SharedPreference pref = new SharedPreference(context);
        Locale locale = new Locale(pref.getLanguage());

        Date date = null;
        SimpleDateFormat formatUTC = new SimpleDateFormat("dd MMM, yyyy hh:mm a", locale);


        try {
            if (date == null) {
                date = DATE_TIME_FORMAT_DATA_TIME.parse(date_str);
            }
        } catch (Exception e) {

        }


        String date_return = "";
        if (date != null) {

            try {
                date_return = formatUTC.format(date);
            } catch (Exception e) {
            }

            return date_return;

        } else {
            return date_return;
        }


    }

    public String getDisplayDate(Context context, String date_str, boolean Istime) {

        SharedPreference pref = new SharedPreference(context);
        Locale locale = new Locale(pref.getLanguage());

        Date date = null;
        SimpleDateFormat formatUTC = new SimpleDateFormat("dd MMM yyyy", locale);
        //  formatUTC.setTimeZone(TimeZone.getTimeZone("GMT"));

       /* try {
            date = DATE_TIME_FORMAT_NEW.parse(date_str);
        } catch (Exception e) {
            //Shared.getInstance().errorLogWrite("TAG",e.getMessage().toString());

        }
        try {
            date = DATE_TIME_FORMAT_FULL_TIME.parse(date_str);
        } catch (Exception e) {
            //Shared.getInstance().errorLogWrite("TAG",e.getMessage().toString());

        }

        try {
            date = DATE_TIME_FORMAT_FULL.parse(date_str);
        } catch (Exception e) {
            //Shared.getInstance().errorLogWrite("TAG",e.getMessage().toString());

        }
*/
        try {
            if (date == null) {
                date = DATE_TIME_FORMAT_SQL.parse(date_str);
            }
        } catch (Exception e) {
            // Shared.getInstance().errorLogWrite("TAG",e.getMessage().toString());
        }


        try {
            if (date == null) {
                formatUTC = new SimpleDateFormat("dd MMM, yyyy hh:mm a", locale);
                date = DATE_TIME_FORMAT_DATA_TIME.parse(date_str);
            }
        } catch (Exception e) {
            // Shared.getInstance().errorLogWrite("TAG",e.getMessage().toString());
        }


        String date_return = "";
        if (date != null) {


          /*  if (Istime) {
                try {
                    date_return = formatUTC.format(date) + " " + getDisplayTimeFromDate(date_str);
                } catch (Exception e) {
                }


            } else {*/
            try {
                date_return = formatUTC.format(date);
            } catch (Exception e) {
            }

            // }

            /*Date dateTemp = date;
            SimpleDateFormat formatter = new SimpleDateFormat("HH");
            String hour = formatter.format(dateTemp);

            if (!Istime) // here is time is use for checking whether its coming from profile or form
            {
                if (Integer.parseInt(hour) > 12) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dateTemp);
                    calendar.add(Calendar.HOUR, 24 - Integer.parseInt(hour));
                    SimpleDateFormat formatUTCC = new SimpleDateFormat("dd MMM yyyy", locale);
                    date = calendar.getTime();
                    date_return = formatUTCC.format(date);
                    CustomLogs.displayLogs(TAG + " dateString: " + date_return);
                }
            }*/


            return date_return;

        } else {
            return date_return;
        }


    }


    public long fromStringToDate(Context context, String dtStart) {
        SharedPreference pref = new SharedPreference(context);
        Locale locale = new Locale(pref.getLanguage());
        SimpleDateFormat formatUTC = new SimpleDateFormat("dd MMM yyyy", locale);
        try {
            Date date = formatUTC.parse(dtStart);
            long days = daysBetween(date, locale);
            return days;
        } catch (ParseException e) {

        }
        return 0;
    }

    private long daysBetween(Date one, Locale locale) {

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat formatUTC = new SimpleDateFormat("dd MMM yyyy", locale);
        // String formattedDate = formatUTC.format(c);
        long difference = (one.getTime() - c.getTime()) / 86400000;
        return Math.abs(difference);
    }

    public String getMimeType(String url) {
        String type;
        if (url.lastIndexOf(".") != -1) {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(ext);
        } else {
            type = null;
        }

        if (type != null && !type.isEmpty()) {

            if (type.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") || type
                    .equals("application/vnd.ms-excel")) {
                type = "application/msexcel";
            } else if (type
                    .equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
                type = "application/msword";
            }

        }

        return type;
    }

    public boolean isFileExist(String path, BaseActivity context) {

        File file = new File(path);

        try {
            return file.exists();
        } catch (Exception e) {
            messageBox(e.getMessage(), context);
        }

        return false;
    }

    public boolean isFileExist(String path, Context context) {

        File file = new File(path);

        try {
            return file.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean checkURL(CharSequence input) {
        if (TextUtils.isEmpty(input)) {
            return false;
        }
        Pattern URL_PATTERN = Patterns.WEB_URL;
        boolean isURL = URL_PATTERN.matcher(input).matches();
        if (!isURL) {
            String urlString = input + "";
            if (URLUtil.isNetworkUrl(urlString)) {
                try {
                    new URL(urlString);
                    isURL = true;
                } catch (Exception e) {
                }
            }
        }
        return isURL;
    }


    public List<DynamicStagesCriteriaListDAO> GetCriteriaListByStageId(int StageId) {

        List<DynamicStagesCriteriaListDAO> stages_criteria = null;

        if (ApplicationSingleton.Companion.getInstace().getApplication() != null) {

            if (ApplicationSingleton.Companion.getInstace().getApplication().getStages() != null && ApplicationSingleton.Companion.getInstace().getApplication().getStages().size() > 0) {

                for (DynamicStagesDAO stage : ApplicationSingleton.Companion.getInstace().getApplication().getStages()) {
                    if (stage.getId() == StageId) {
                        if (stage.getCriteriaList() != null) {
                            stages_criteria = stage.getCriteriaList();
                            break;
                        }

                    }
                }
            }

        }


        return stages_criteria;


    }


    public List<DynamicStagesCriteriaCommentsListDAO> GetCriteriaCommentsListByCriteriaId(int CriteriaId) {

        List<DynamicStagesCriteriaCommentsListDAO> stages_criteria_comments = null;

        if (ApplicationSingleton.Companion.getInstace().getApplication() != null) {

            if (ApplicationSingleton.Companion.getInstace().getApplication().getStages() != null && ApplicationSingleton.Companion.getInstace().getApplication().getStages().size() > 0) {

                for (DynamicStagesDAO stage : ApplicationSingleton.Companion.getInstace().getApplication().getStages()) {

                    for (DynamicStagesCriteriaListDAO criteria : stage.getCriteriaList()) {
                        if (criteria.getId() == CriteriaId) {
                            if (criteria.getComments() != null) {
                                stages_criteria_comments = criteria.getComments();
                                break;
                            }

                        }
                    }

                }
            }

        }


        return stages_criteria_comments;


    }


    public String fontName(Context context, String fontName) {
        String lang = getLanguageSimpleContext(context);
        if (lang.equalsIgnoreCase("en")) {
            if (fontName == null || fontName.length() == 0) {
                return context.getString(R.string.font_regular);

            } else if (fontName.equals(context.getResources().getString(R.string.regular))) {
                return context.getString(R.string.font_regular);
            } else if (fontName.equals(context.getResources().getString(R.string.light))) {
                return context.getString(R.string.font_medium);
            } else if (fontName.equals(context.getResources().getString(R.string.bold))) {
                return context.getString(R.string.font_bold);
            } else if (fontName.equals(context.getResources().getString(R.string.heavy))) {
                return context.getString(R.string.font_heavy);
            } else if (fontName.equals(context.getResources().getString(R.string.semibold))) {
                return context.getString(R.string.font_semibold);
            } else {
                return context.getString(R.string.font_regular);
            }
        } else {
            if (fontName == null || fontName.length() == 0) {
                return context.getString(R.string.font_regular);

            } else if (fontName.equals(context.getResources().getString(R.string.regular))) {
                return context.getString(R.string.font_regular_dubai);
            } else if (fontName.equals(context.getResources().getString(R.string.light))) {
                return context.getString(R.string.font_light_dubai);
            } else if (fontName.equals(context.getResources().getString(R.string.bold))) {
                return context.getString(R.string.font_bold_dubai);
            } else {
                return context.getString(R.string.font_regular_dubai);
            }
        }
    }

    public CurrencyDAO getCurrencyById(int currencyId) {
        CurrencyDAO currency = null;

        try {
            if (ESPApplication.getInstance().getCurrencies() != null && ESPApplication.getInstance().getCurrencies().size() > 0) {

                for (CurrencyDAO currencyDAO : ESPApplication.getInstance().getCurrencies()) {

                    if (currencyDAO.getId() == currencyId) {
                        currency = currencyDAO;
                        break;
                    }
                }

                //For Now.
                if (currency == null)
                    currency = ESPApplication.getInstance().getCurrencies().get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return currency;
    }

    public List<String> getCurrencyCodesList(DynamicFormSectionFieldDAO fieldDAO) {
        List<String> codes = new ArrayList<>();

        if (ESPApplication.getInstance().getCurrencies() != null && ESPApplication.getInstance().getCurrencies().size() > 0) {

            if (fieldDAO.getAllowedValuesCriteria() != null) {
                List<String> allowedIds = Arrays.asList(fieldDAO.getAllowedValuesCriteria().split(","));

                if (allowedIds.size() > 0) {

                    for (CurrencyDAO currencyDAO : ESPApplication.getInstance().getCurrencies()) {

                        if (allowedIds.contains(String.valueOf(currencyDAO.getId()))) {
                            codes.add(currencyDAO.getCode() + " (" + currencyDAO.getSymobl() + ")");
                        }
                    }
                }
            } else {
                for (CurrencyDAO currencyDAO : ESPApplication.getInstance().getCurrencies()) {
                    codes.add(currencyDAO.getCode());
                }
            }


        }
        return codes;
    }

    public CurrencyDAO getCurrencyByCode(String code) {
        CurrencyDAO currency = null;

        if (ESPApplication.getInstance().getCurrencies() != null && ESPApplication.getInstance().getCurrencies().size() > 0) {

            for (CurrencyDAO currencyDAO : ESPApplication.getInstance().getCurrencies()) {

                if (currencyDAO.getCode() != null && currencyDAO.getCode().equals(code)) {
                    currency = currencyDAO;
                    break;
                }
            }
        }

        return currency;
    }

    public DynamicFormSectionFieldDAO populateCurrency(String getValue) {
        DynamicFormSectionFieldDAO fieldDAO = new DynamicFormSectionFieldDAO();
        if (getValue != null && getValue.length() > 0) {

            String currency_val = null;

            String[] currency = getValue.split("\\:");
            CustomLogs.displayLogs(TAG + " currency.length: " + currency.length);


            if (currency.length > 0) {
                if (currency[currency.length - 1] != null) {
                    currency_val = currency[currency.length - 1];
                    // fieldDAO.setValue(currency_val);
                    fieldDAO.setSelectedCurrencySymbol(currency_val);
                    CustomLogs.displayLogs(TAG + " currency_val: " + currency_val);
                }

                if (currency[0] != null) {
                    currency_val += " " + currency[0];
                    fieldDAO.setValue(currency[0]);
                    CustomLogs.displayLogs(TAG + " currency_val: " + currency_val);
                }

                if (currency.length > 1 && currency[1] != null) {
                    int selectedCurrency = Integer.parseInt(currency[1]);
                    CustomLogs.displayLogs(TAG + " selectedCurrency: " + selectedCurrency);
                    fieldDAO.setSelectedCurrencyId(selectedCurrency);
                }
            }

        }
        return fieldDAO;
    }

    public boolean isValidUrl(String url) {

        boolean isValid = false;

        String regex = "\\(?\\b(https://|http://|www[.])[-A-Za-z0-9+&amp;@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&amp;@#/%=~_()|]";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(url);

        if (m.matches()) {
            isValid = true;
        }


        return isValid;

    }

    public String edittextErrorChecks(Context context, String outputedText, String error, DynamicFormSectionFieldDAO field) {
        try {
            if (!outputedText.isEmpty()) {

                int minValueCount = field.getMinVal();
                int maxValueCount = field.getMaxVal();

                if ((field.getType() == 3 || field.getType() == 11)) // 3 = Number 11 = currency
                {
                    if (minValueCount > 0 && maxValueCount > 0) {

                        if (outputedText.length() > 0 && (Double.parseDouble(outputedText) < minValueCount ||
                                Double.parseDouble(outputedText) > maxValueCount)) {
                            error = context.getString(R.string.valuebetween) + " " + minValueCount + " " + context.getString(R.string.and) + " " + maxValueCount;
                        }

                    } else if (minValueCount > 0) {

                        if (outputedText.length() > 0 && Double.parseDouble(outputedText) < minValueCount)
                            error = context.getString(R.string.valuegreater) + " " + minValueCount;

                    } else if (maxValueCount > 0) {

                        if (outputedText.length() > 0 && Double.parseDouble(outputedText) > maxValueCount) {
                            error = context.getString(R.string.valueless) + " " + maxValueCount;
                        }
                    }
                } else {
                    if (minValueCount > 0 && maxValueCount > 0) {

                        if (outputedText.length() > 0 && (outputedText.length() < minValueCount || outputedText.length() > maxValueCount)) {
                            error = context.getString(R.string.valuebetween) + " " + minValueCount + " " + context.getString(R.string.and) + " " + maxValueCount + " " + context.getString(R.string.characters);
                        }

                    } else if (minValueCount > 0) {

                        if (outputedText.length() > 0 && outputedText.length() < minValueCount) {
                            error = context.getString(R.string.valuegreater) + " " + minValueCount + " " + context.getString(R.string.characters);
                        }
                    } else if (maxValueCount > 0) {

                        if (outputedText.length() > 0 && outputedText.length() > maxValueCount) {
                            error = context.getString(R.string.valueless) + " " + maxValueCount + " " + context.getString(R.string.characters);
                        }
                    }
                }

                //Email Validation for Email Type
                if (field.getType() == 10) {
                    if (!isValidEmailAddress(outputedText))
                        error = context.getString(R.string.invalidemail);
                }
                //

                //HyperLink Validation
                if (field.getType() == 15) {
                    // if (!isValidUrl(outputedText)) {
                    if (!Patterns.WEB_URL.matcher(outputedText).matches()) {
                        error = context.getString(R.string.invalidlink);
                    }

                }
                //
                //Phone Number Validation
                if (field.getType() == 16) {
                    if (!isValidPhoneNumber(outputedText)) {
                        error = context.getString(R.string.invalidnumber);
                    }

                }
                //

            }
        } catch (Exception ignored) {
        }
        return error;
    }

    public File getOutputMediaFile(String fileName) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().getPath() + "/ESP/");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        File newPath = new File(mediaStorageDir.getPath() + File.separator +
                fileName);


        return newPath;
    }

    public void OpenFile(String filePath, Context context) {
        try {
            File file = new File(filePath);
            CustomLogs.displayLogs(TAG + " OpenFile filePath: " + filePath);
            if (file != null) {

                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(filePath), "image/*");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }


        } catch (Exception e) {
            Toast.makeText(context, "Open this file, Application is not available", Toast.LENGTH_SHORT).show();

        }
    }

    public boolean getFileSize(String path, int getMaxVal) {
        File file = new File(path);
        double bytes = file.length();
        double kilobytes = (bytes / 1024);
        double megabytes = (kilobytes / 1024);


        return getMaxVal >= megabytes;

    }

   /* public ProgressDialog progressdialog(Context context) {
        ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage(context.getResources().getString(R.string.pleasewait));
        pDialog.setCancelable(false);
        return pDialog;
    }*/

    public AlertDialog setProgressDialog(Context context) {

        int llPadding = 40;
        LinearLayout ll = new LinearLayout(context);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.START);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        //    progressBar.setIndeterminateDrawable(ContextCompat.getDrawable(context,R.drawable.progress));
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);

        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvText = new TextView(context);
        tvText.setText(context.getResources().getString(R.string.pleasewait));
        tvText.setTextColor(Color.BLACK);
        tvText.setTextSize(15);
        tvText.setLayoutParams(llParam);

        ll.addView(progressBar);
        ll.addView(tvText);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setView(ll);

        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }
        return dialog;
    }

    public String getSelectedLookupValues(List<DynamicFormSectionFieldLookupValuesDAO> lookupValues,
                                          boolean isForLabels,
                                          boolean isSingleSelection) {

        ArrayList<String> lookupValuesList = new ArrayList<>();
        String values = "";

        if (lookupValues != null && lookupValues.size() > 0) {
            for (DynamicFormSectionFieldLookupValuesDAO lookupValuesDAO : lookupValues) {

                if (lookupValuesDAO.isSelected()) {
                    if (isForLabels)
                        lookupValuesList.add(lookupValuesDAO.getLabel());
                    else
                        lookupValuesList.add(String.valueOf(lookupValuesDAO.getId()));

                    if (isSingleSelection)
                        break;
                }

            }

            if (lookupValuesList.size() > 0)
                values = TextUtils.join(isForLabels ? ", " : ",", lookupValuesList);
        }

        return values;
    }

    public List<String> getLookupLabelsList(List<DynamicFormSectionFieldLookupValuesDAO> lookupValues) {
        List<String> labels = new ArrayList<>();

        if (lookupValues != null && lookupValues.size() > 0) {
            for (DynamicFormSectionFieldLookupValuesDAO lookupValuesDAO : lookupValues) {
                labels.add(lookupValuesDAO.getLabel());
            }
        }

        return labels;

    }

    public int getSelectedIndexOfLookupsForSingleSelection(List<DynamicFormSectionFieldLookupValuesDAO> lookupValues) {

        int selectedIndex = -1;

        if (lookupValues != null && lookupValues.size() > 0) {

            for (int i = 0; i < lookupValues.size(); i++) {

                DynamicFormSectionFieldLookupValuesDAO lookupValuesDAO = lookupValues.get(i);

                if (lookupValuesDAO.isSelected()) {

                    selectedIndex = i;
                    break;

                }

            }

        }

        return selectedIndex;
    }

    public Integer[] getSelectedIndexOfLookupsForMultiSelection(List<DynamicFormSectionFieldLookupValuesDAO> lookupValues) {

        if (lookupValues != null && lookupValues.size() > 0) {

            List<Integer> selectedIndexesList = new ArrayList<>();

            for (int i = 0; i < lookupValues.size(); i++) {

                DynamicFormSectionFieldLookupValuesDAO lookupValuesDAO = lookupValues.get(i);

                if (lookupValuesDAO.isSelected()) {
                    selectedIndexesList.add(i);
                }

            }

            Integer[] selectedIndexes = new Integer[selectedIndexesList.size()];

            for (int i = 0; i < selectedIndexesList.size(); i++) {
                selectedIndexes[i] = selectedIndexesList.get(i);
            }

            return selectedIndexes;
        }

        return null;
    }

    public DynamicFormSectionFieldDAO setObjectValues(DynamicFormSectionFieldDAO parentSectionField) {
        DynamicFormSectionFieldDAO tempField = new DynamicFormSectionFieldDAO();
        tempField.setId(parentSectionField.getId());
        tempField.setObjectId(parentSectionField.getObjectId());
        tempField.setLabel(parentSectionField.getLabel());
        tempField.setRequired(parentSectionField.isRequired());
        tempField.setCommon(parentSectionField.isCommon());
        tempField.setVisible(parentSectionField.isVisible());
        tempField.setMinVal(parentSectionField.getMinVal());
        tempField.setMaxVal(parentSectionField.getMaxVal());
        tempField.setMinDate(parentSectionField.getMinDate());
        tempField.setMaxDate(parentSectionField.getMaxDate());
        tempField.setReadOnly(parentSectionField.isReadOnly());
        tempField.setOrder(parentSectionField.getOrder());
        tempField.setCreatedBy(parentSectionField.getCreatedBy());
        tempField.setType(parentSectionField.getType());
        tempField.setCreatedOn(parentSectionField.getCreatedOn());
        tempField.setLookupValues(parentSectionField.getLookupValues());
        tempField.setDetails(parentSectionField.getDetails());
        tempField.setSystem(parentSectionField.isSystem());
        tempField.setAllowedValuesCriteria(parentSectionField.getAllowedValuesCriteria());
        tempField.setAllowedValuesCriteriaArray(parentSectionField.getAllowedValuesCriteriaArray());
        tempField.setSelectedCurrencyId(parentSectionField.getSelectedCurrencyId());
        tempField.setSelectedCurrencySymbol(parentSectionField.getSelectedCurrencySymbol());
        tempField.setSectionTemplateFiledId(parentSectionField.getSectionTemplateFiledId());
        tempField.setSectionCustomFieldId(parentSectionField.getSectionCustomFieldId());
        tempField.setLookUpId(parentSectionField.getLookUpId());
        tempField.setLookupValue(parentSectionField.getLookupValue());
        tempField.setTitleField(parentSectionField.isTitleField());
        tempField.setCanDisabled(parentSectionField.isCanDisabled());
        tempField.setViewGenerated(parentSectionField.isViewGenerated());
        tempField.setPost(parentSectionField.getPost());
        tempField.setError_field(parentSectionField.getError_field());
        tempField.setValidate(parentSectionField.isValidate());
        tempField.setValue(parentSectionField.getValue());
        tempField.setTigger(parentSectionField.isTigger());
        return tempField;
    }


    public MultipartBody.Part prepareFilePart(Uri fileUri, Context bContext) {
        try {
            //File file = FileUtils.getFile(bContext, fileUri);

            File file = null;
            String path = RealPathUtil.getPath(bContext, fileUri);
            file = new File(path);

            //File file = new File(Shared.getInstance().getPathFromUri(bContext,fileUri));
            RequestBody requestFile = RequestBody.create(MediaType.parse(bContext.getContentResolver().getType(fileUri)), file);
            return MultipartBody.Part.createFormData("File", Shared.getInstance().ReplaceSpeicalChars(file.getName(), ""), requestFile);
        } catch (Exception e) {
            Shared.getInstance().errorLogWrite("FILE", e.getMessage());
        }

        return null;
    }

    public boolean isJSONValid(String text) {
        try {
            new JSONObject(text);
        } catch (JSONException ex) {
            // edited, to include @Arthur's comment
            // e.g. in case JSONArray is valid as well...
            try {
                new JSONArray(text);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    public void DownLoadFile(InputStream inputStream, String name) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String fileName = name;

        File file = Shared.getInstance().getOutputMediaFile(fileName);
        CustomLogs.displayLogs(TAG + " DownLoadFile file: " + file.getPath() + " fileName: " + fileName);
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);

            byte[] buffer = new byte[1024]; // or other buffer size
            int read;

            while ((read = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (output != null) {
                    output.close();
                } else {
                }
            } catch (IOException e) {

                e.printStackTrace();
            }
        }

    }

    public boolean downloadImage(InputStream inputStream, DyanmicFormSectionFieldDetailsDAO attachmentsDAO,
                                 String uploadedFileName) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String fileName = "";
        fileName = uploadedFileName;
        File file = Shared.getInstance().getOutputMediaFile(fileName);
        CustomLogs.displayLogs(TAG + " DownLoadFile file: " + file.getPath() + " fileName: " + fileName);
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);

            byte[] buffer = new byte[1024]; // or other buffer size
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            return true;

        } catch (IOException e) {
            attachmentsDAO.setFileDownling(false);
            attachmentsDAO.setFileDownloaded(false);
            return false;

        } finally {
            try {
                if (output != null) {
                    output.close();
                    return true;
                } else {
                }
            } catch (IOException e) {
                attachmentsDAO.setFileDownling(false);
                attachmentsDAO.setFileDownloaded(false);
                return false;
            }
        }
    }

    public void alertMessagePopUp(Context context, FragmentManager supportFragmentManager, String title, String msg) {
        AlertMesasgeWindow alertMesasgeWindow = AlertMesasgeWindow.newInstance(title, msg, context.getString(R.string.alert), context.getString(R.string.ok));
        alertMesasgeWindow.show(supportFragmentManager, context.getString(R.string.alert));
        alertMesasgeWindow.setCancelable(false);
    }


    public String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public void translucentStatusBar(Activity context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = context.getWindow();
            w.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }


        /*//make translucent statusBar on kitkat devices
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(context, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            context.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(context, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            context.getWindow().setStatusBarColor(Color.TRANSPARENT);
        }*/
    }

    private void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public List<DynamicFormSectionDAO> loadFeedback(DynamicFormSectionDAO sectionDAO,
                                                    List<DynamicFormSectionDAO> sections, DynamicStagesCriteriaListDAO stages) {

        sectionDAO.setDynamicStagesCriteriaListDAO(stages);
        List<DynamicFormSectionFieldDAO> fields = invisibleList(sectionDAO, false);
        DynamicFormSectionFieldsCardsDAO fieldsCard = new DynamicFormSectionFieldsCardsDAO(fields);
        sectionDAO.getFieldsCardsList().clear();
        sectionDAO.getFieldsCardsList().add(fieldsCard);
        sections.add(sectionDAO);
        return sections;
    }


    public List<DynamicFormSectionFieldDAO> invisibleList(DynamicFormSectionDAO sectionDAO, boolean isClearMappedCalculatedFields) {
        List<DynamicFormSectionFieldDAO> fields = sectionDAO.getFields();
        List<DynamicFormSectionFieldDAO> tempFields = new ArrayList<>();
        for (int h = 0; h < (fields != null ? fields.size() : 0); h++) {
            if (fields.get(h).isVisible()) {
                if (isClearMappedCalculatedFields && (fields.get(h).getType() == 18 || fields.get(h).getType() == 19))
                    fields.get(h).setValue("");

                tempFields.add(fields.get(h));
            }
        }
        return tempFields;
    }

    public boolean hasLinkDefinitionId(DynamicResponseDAO body) {

        if (body != null && body.getStages() != null) {
            for (int i = 0; i < body.getStages().size(); i++) {
                DynamicStagesDAO stages = body.getStages().get(i);
                if (stages.isEnabled()) {
                    int linkDefinitionId = stages.getLinkDefinitionId();

                    if (linkDefinitionId > 0) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public APIs retroFitObject(Context bContext) {

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

            String access_token = null;
            if (ESPApplication.getInstance().getAccess_token() != null)
                access_token = ESPApplication.getInstance().getAccess_token();
            else if (ESPApplication.getInstance().getUser() != null && ESPApplication.getInstance().getUser().getLoginResponse() != null)
                access_token = ESPApplication.getInstance().getUser().getLoginResponse().getAccess_token();

            if (access_token == null) {
                httpClient.addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = null;
                    if (ESPApplication.getInstance() != null) {
                        requestBuilder = original.newBuilder()
                                .header("locale", getLanguageSimpleContext(bContext));


                    }
                    Request request = null;
                    if (requestBuilder != null)
                        request = requestBuilder.build();

                    return chain.proceed(request);
                });
            } else {

                String finalAccess_token = access_token;
                httpClient.addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder requestBuilder = null;
                    if (ESPApplication.getInstance() != null) {
                        requestBuilder = original.newBuilder()
                                .header("locale", getLanguageSimpleContext(bContext))
                                .header("Authorization", "bearer " + finalAccess_token);


                    }
                    Request request = null;
                    if (requestBuilder != null)
                        request = requestBuilder.build();

                    return chain.proceed(request);
                });
            }


            httpClient.connectTimeout(5, TimeUnit.MINUTES);
            httpClient.readTimeout(5, TimeUnit.MINUTES);
            httpClient.writeTimeout(5, TimeUnit.MINUTES);


            Gson gson = new GsonBuilder()
                    .setLenient()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                    .create();

            /* retrofit builder and call web service*/
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.base_url)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();

            /* APIs Mapping respective Object*/
            APIs apis = retrofit.create(APIs.class);
            return apis;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }


    public void setBadge(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);
        context.sendBroadcast(intent);
    }

    private String getLauncherClassName(Context context) {

        PackageManager pm = context.getPackageManager();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }


    public void setToolbarHeight(Toolbar toolbar, boolean isShowCurve) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) toolbar.getLayoutParams();

        if (isShowCurve) {
            toolbar.setBackgroundResource(R.drawable.draw_toolbar);
            layoutParams.height = 155;
        } else {
            toolbar.setBackgroundResource(R.drawable.draw_bg_simple_green);
            layoutParams.height = 135;
        }
        toolbar.setLayoutParams(layoutParams);
    }

    public void setLayoutToolbarHeight(RelativeLayout rltoolbar, boolean isShowCurve) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) rltoolbar.getLayoutParams();
        if (isShowCurve) {
            rltoolbar.setBackgroundResource(R.drawable.draw_toolbar);
            layoutParams.height = 175;
        } else {
            rltoolbar.setBackgroundResource(R.drawable.draw_bg_simple_green);
            layoutParams.height = 155;
        }
        rltoolbar.setLayoutParams(layoutParams);
    }

    private DecimalFormat format = new DecimalFormat("#.##");
    private long MB = 1024 * 1024;
    private long KB = 1024;

    public String getAttachmentFileSize(File file) {

        if (!file.isFile()) {
            throw new IllegalArgumentException("Expected a file");
        }
        final double length = file.length();

        if (length > MB) {
            return format.format(length / MB) + " MB";
        }
        if (length > KB) {
            return format.format(length / KB) + " KB";
        }
        return format.format(length) + " B";
    }

    public Spannable getSearchedTextHighlight(String searchItem, String fullItem, Context context) {

        //searchItem = ReplaceSpeicalChars(searchItem, "");

        if (fullItem.toLowerCase().contains(searchItem.toLowerCase())) {
            Spannable spanText = Spannable.Factory.getInstance().newSpannable(fullItem);

            /*Matcher matcher = Pattern.compile(searchItem.toLowerCase())
                    .matcher(fullItem.toLowerCase());*/
            Matcher matcher = Pattern.compile(searchItem.toLowerCase()).matcher(fullItem.toLowerCase());


            while (matcher.find()) {
                try {

                    ColorStateList orangeColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{ContextCompat.getColor(context,R.color.green)});
                    TextAppearanceSpan highlightSpan = new TextAppearanceSpan(null, Typeface.NORMAL, -1, orangeColor, null);
                    spanText.setSpan(highlightSpan, matcher.start(), matcher.start() + searchItem.length(), Spannable.SPAN_MARK_MARK);

                } catch (Exception e) {
                    Shared.getInstance().errorLogWrite("TAG", e.getMessage());
                }
            }
            //SPAN_EXCLUSIVE_EXCLUSIVE
            return spanText;
        }
        return new SpannableString(fullItem);

    }

    public DynamicFormSectionFieldDAO populateCurrencyByObject(DynamicFormSectionFieldDAO fieldDAO) {
        //DynamicFormSectionFieldDAO fieldDAO = new DynamicFormSectionFieldDAO();
        String getValue = fieldDAO.getValue();
        if (getValue != null && getValue.length() > 0) {

            String currency_val = "";

            String[] currency = getValue.split("\\:");
            CustomLogs.displayLogs(TAG + " currency.length: " + currency.length);

            if (currency.length == 1)
                return fieldDAO;

            if (currency.length > 0) {
                if (currency[currency.length - 1] != null) {
                    currency_val = currency[currency.length - 1];
                    // fieldDAO.setValue(currency_val);
                    fieldDAO.setSelectedCurrencySymbol(currency_val);
                    CustomLogs.displayLogs(TAG + " currency_val: " + currency_val);
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

        }
        return fieldDAO;
    }

    public String populateLookupValues(String value, String actualResponseJson) {
        StringBuilder sb = new StringBuilder();
        DynamicResponseDAO response = new Gson().fromJson(actualResponseJson, DynamicResponseDAO.class);

        for (int i = 0; i < response.getForm().getSections().size(); i++) {

            if (response.getForm().getSections().get(i).getFields() != null && response.getForm().getSections().get(i).getFields().size() > 0) {

                for (int j = 0; j < response.getForm().getSections().get(i).getFields().size(); j++) {
                    if (response.getForm().getSections().get(i).getFields().get(j).getLookupValues() != null && response.getForm().getSections().get(i).getFields().get(j).getLookupValues().size() > 0) {

                        List<String> myList = new ArrayList<String>(Arrays.asList(value.split(",")));
                        for (DynamicFormSectionFieldLookupValuesDAO lookup : response.getForm().getSections().get(i).getFields().get(j).getLookupValues()) {


                            for (int k = 0; k < myList.size(); k++) {
                                Integer value_ = 0;
                                try {
                                    value_ = Integer.parseInt(myList.get(k));
                                } catch (Exception e) {
                                    //e.printStackTrace();
                                }
                                if (lookup.getId() == value_) {
                                    //   lookup.setSelected(true);
                                    if (sb.length() > 0)
                                        sb.append(", ");
                                    sb.append(lookup.getLabel());

                                }
                            }

                        }


                    }
                }
            }
        }
        return sb.toString();
    }

    public String populateStageLookupValues(String value, String actualResponseJson) {
        StringBuilder sb = new StringBuilder();
        DynamicResponseDAO response = new Gson().fromJson(actualResponseJson, DynamicResponseDAO.class);
        for (int ss = 0; ss < response.getStages().size(); ss++)  // stage sections
        {
            List<DynamicStagesCriteriaListDAO> CriteriaList = response.getStages().get(ss).getCriteriaList();

            for (int jj = 0; jj < CriteriaList.size(); jj++) {

                List<DynamicFormSectionDAO> sections = CriteriaList.get(jj).getForm().getSections();

                for (int kk = 0; kk < sections.size(); kk++) {
                    List<DynamicFormSectionFieldDAO> fields = sections.get(kk).getFields();

                    for (int ll = 0; ll < fields.size(); ll++) {
                        List<DynamicFormSectionFieldLookupValuesDAO> lookupValues = fields.get(ll).getLookupValues();

                        List<String> myList = new ArrayList<String>(Arrays.asList(value.split(",")));
                        for (DynamicFormSectionFieldLookupValuesDAO lookup : lookupValues) {


                            for (int k = 0; k < myList.size(); k++) {
                                Integer value_ = 0;
                                try {
                                    value_ = Integer.parseInt(myList.get(k));
                                } catch (Exception e) {
                                    // e.printStackTrace();
                                }
                                if (lookup.getId() == value_) {
                                    //   lookup.setSelected(true);
                                    if (sb.length() > 0)
                                        sb.append(", ");
                                    sb.append(lookup.getLabel());

                                }
                            }

                        }

                    }
                }

            }
        } // stage sections
        return sb.toString();
    }

    public String populateLookupValuesForProfileSections(String value, DynamicFormSectionFieldDAO field) {
        StringBuilder sb = new StringBuilder();

        if (field.getLookupValues() != null && field.getLookupValues().size() > 0) {

            List<String> myList = new ArrayList<String>(Arrays.asList(value.split(",")));
            for (DynamicFormSectionFieldLookupValuesDAO lookup : field.getLookupValues()) {


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

            }


        }


        return sb.toString();
    }

    public void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public List<DynamicFormSectionFieldsCardsDAO> addData(List<DynamicFormSectionFieldsCardsDAO> fieldsCardsList, DynamicFormSectionFieldsCardsDAO cardCopy) {
        fieldsCardsList.add(cardCopy);
        return fieldsCardsList;
    }

    public void saveLookUpItems(int id, List<LookUpDAO> lookupitemArray) {
        lookUpItemHashMap.put(id, lookupitemArray);
    }

    public List<LookUpDAO> getLookUpItems(int id) {
        return lookUpItemHashMap.get(id);
    }


}

