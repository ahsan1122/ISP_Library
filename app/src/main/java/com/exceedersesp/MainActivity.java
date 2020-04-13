package com.exceedersesp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.esp.library.exceedersesp.BaseActivity;
import com.esp.library.exceedersesp.ESPApplication;
import com.esp.library.exceedersesp.controllers.applications.ApplicationActivityTabs;
import com.esp.library.utilities.common.Constants;
import com.esp.library.utilities.common.SharedPreference;

import utilities.model.Labels;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



       /* setInstance();
        setLabels();*/

        /*FragmentManager supportFragmentManager = getSupportFragmentManager();
        ApplicationActivityTabs submit_request = ApplicationActivityTabs.Companion.newInstance();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.request_fragment, submit_request);
        fragmentTransaction.commit();*/

        /*try {
            Intent myIntent = new Intent(this,Class.forName("com.esp.library.exceedersesp.controllers.applications.AddApplicationsActivity"));
            startActivity(myIntent );
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }*/


        try {
            Intent myIntent = new Intent(this, Class.forName("com.esp.library.exceedersesp.controllers.SplashScreenActivity"));
            startActivity(myIntent);
            finish();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    private void setInstance() {
        Constants.base_url = "https://qaesp.azurewebsites.net/webapi/";
        ESPApplication.getInstance().setComponent(true);
        ESPApplication.getInstance().setAccess_token("UtJUtpDrpg6LMP1FBOGBBjmlNstPZz5cJm1UmpO88csC8cfszvnK5kZJNX60lVdNi75RdQzwc8Yf4yCKH-lm4eOKpBM314Xjp4KIXDVs6DqCa2fy-xO19qxfKhQ8FCyDFJIirjowQ18_VfoT3ydM6vXLwdp_qT8kjP1VgTxwzh85UzPdVOqJj4W0D_qF4GGWV8H8zxOI45hX_ViqmyCN5nwE-b_mkQAXtKSVCcSjKVxay189WTI_Yh0JaaBdgIUfcQ-JoXp8Mx_d82kn9MLR5hqWej2hrgQnWUHX2tlsajVYa5aeQSRQHx6oQM98TkS_CunwKNCUd-wmven1IqrdawnYoEi3ilL6hktC0NNnA4UXbqeXYG-BLiBDLgzCnIImkrIy9EZ6ub8a8wInZRCEf86vMTBNEL6KJr83ZCpSjLUHMRGDO3mPzf6ys6BkCQkYuctxNjeP2OMl2xR_i5NbCtyW25OOKfgIHpd1Qm7BPZCjD57K42uokSjAgcQAfP5Idps6A2PSxL8OHC8SJyATb6JUEg29-jRBJOioPxHd3sLKvRhE8MMyNonmijhGpT18NzjPmq27XRLR4Vi8ZJ0E9gu2RiBWu73TEtal8--rl8LXTQh1aVl2tTa_MESoAmqMNKJ-BkCJNFOXI0ZdWzM4c4WE0mVhCbvCMM0PjjDOsbev1fKu2_4yMs3axqpNk0QY2zRnosZ01dQN-D9OyM6a61R8jQTGV7xIY6FbGEt4nL2Rd_k0r3FkdedGYIPDsH9jZf2j0mnEq-Ek_Rarowny3pSTkwJSzvGmwgjRXLlZO1cp2tWUYhEe5e9j-q1z3QRpya-d5JWucg5w6ThQwyoXaKaffz_XqkKnvlPfo9zHXtFPztBzmHOSghRI1uhog8bhZ33RU71soBDCzligfFWgSnl6V4p7_JdrjCTB1goXpOZsUJSsGbJPgWKk64ARyiwGFDtTBPm5o7gchkZbxF8FF1MfX95IFjHSrotGfRgVSOilB6PGKOmu8PkVvWlM2bpAF8kM5Vq4s8-cLk0fQCjx-UcAKsvsmdyKlPXvXEchpf-JgSj0uw6cN_sdj0JV5bnpBjMCt1cUwKnASRWoqeUxXmaHKhZy1ecs3SCj6dZVX0MwGN3WDKLT8yA3qgd5ou0ZdzztRZKcRlpzUpFbLe_ljKVAu-a9dzX4gyBQMFQfWiDnDV29W3-J3iJRiTbZ1SE6BKXaM6VX6gcj_p4II5raByagDI-O8-wdqObJD1L4hcv4l9yW34OfrI1B1_L8hzIq");

    }

    private void setLabels() {
        Labels labels = new Labels();
        labels.setApplication(getString(R.string.application));
        labels.setApplications(getString(R.string.applications));
        labels.setApplicant(getString(R.string.applicant));
        labels.setApplicants(getString(R.string.applicants));
        labels.setDefinition(getString(R.string.definition));
        labels.setDefinitions(getString(R.string.definitions));

        SharedPreference pref = new SharedPreference(this);
        pref.savelabels(labels);
    }
}
