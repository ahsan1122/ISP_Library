package com.esp.library.utilities.common;

import android.os.Environment;

import java.util.ArrayList;

public class Constants {
    public static final String FOLDER_PATH = Environment.getExternalStorageDirectory().getPath();
    public static final String FOLDER_NAME = "ESP";
    public static final int CAPTURE_IMAGE = 100;
    public static final int CROP_IMAGE = 200;
    public static final String TEMP_SCAN = "ProfilePhoto.jpg";
    public static final String TEMP_SCANNED_SCAN = "SCANNED.jpg";
    public static final int NO_OF_INTRO = 3;
    public static final String MESSAGE_ID_SUCCESS = "001";
    public static final String MESSAGE_ID_UNSUCCESS = "002";
    public static String LOAD_VIEW = "p";
    static ArrayList<Integer> listIdontWantAnymore = new ArrayList<Integer>();
    public static final int PROFILE_PHOTO_SIZE = 100;
    public static boolean WRITE_LOG = true;
    public static String base_url="";
    public static String base_url_api = "/webapi/";
    public static boolean isApplicationChagned = false;


}
