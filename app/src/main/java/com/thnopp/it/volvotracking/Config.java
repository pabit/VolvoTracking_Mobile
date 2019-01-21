package com.thnopp.it.volvotracking;

/**
 * Created by CEVAUser on 5/30/2017.
 */

public class Config {

    //rest java
    public static final String LOGIN_URL = "http://203.154.71.73:8080/volvo/api/login";
    public static final String LOAD_VIN_URL = "http://203.154.71.73:8080/volvo/api/vin";//  = "http://THLT5CG7312T5F/tms/loadvin.php";
    public static final String CHK_VIN = "http://203.154.71.73:8080/volvo/api/updatevin";
    public static final String CHK_VIN1 = "http://203.154.71.73:8080/volvo/api/updatevin1";

    // Directory name to store captured images and videos
    public static final String IMAGE_DIRECTORY_NAME = "Volvo Picture Upload";
    public static final String HEAD_KEY = "Authorization";
    public static final String HEAD_VALUE = "Basic Y2V2YTpWcjF0ZWFtIQ==";
    public static final String UPDATE_URL = "http://203.154.71.73/tms_volvo/setup/volvo_tms.apk";

    public static final String WI_URL = "http://203.154.71.73:8080/tr_tracking/api/wi";

    //php
    public static final String FILE_UPLOAD_URL = "http://203.154.71.73/tms_volvo/fileupload.php";
}