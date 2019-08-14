package com.thnopp.it.volvotracking;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by THLT88 on 3/14/2018.
 */

public class ScheduleService extends Service implements LocationListener {
    // constant
    // old public static final long NOTIFY_INTERVAL = 10 * 1000; // 10 seconds

    public static final long NOTIFY_INTERVAL = 1 * 60 * 1000; // 1 minutes

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    String param, status;
    DatabaseHelper db;
    List<Scanvin> lst_pod_del;
    List<Vinmaster> lst_pod, lst_pod_user;
    List<Dealerinst> lst_inst;
    //for location tracking
    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude, longitude;
    LocationManager locationManager;
    Location location;
    private Handler mHandler1 = new Handler();
    private Timer mTimer1 = null;
    long notify_interval = 1000;
    public static String str_receiver = "servicetutorial.service.receiver";
    Intent intent;

    //end for location tracking
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }




    @Override
    public void onCreate() {
        // cancel if already existed
        if (mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }

    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                    String username = prefs.getString("username","");
                    String password = prefs.getString("password","");
                    String lat  = prefs.getString("lat","");
                    String lon  = prefs.getString("long","");
                    Global.user = username;
                    Global.conf = password;
                    /*
                    * 1. send update time depart
                    * 2. send update time arrival + status U
                    * 3. send path to rest
                    * 2.
                    * */
                    //do here
                    status = "A";
                    param = username;



                    //open database -> gen data to lst
                    db = new DatabaseHelper(getApplicationContext());

                    lst_pod = db.getPOD();
                    if (!lst_pod.isEmpty()){
                        new ScheduleService.update_pod().execute();
                    //    startService(new Intent(getApplicationContext(), LocationService.class));

                    }
                    //update detail
                    lst_pod_del = db.getPOD_Det();
                    if (!lst_pod_del.isEmpty()){
                        //upload picture here
                        new ScheduleService.update_pod_det().execute();

                    }

                    List<Wi> lst_wi = db.getAllWIUnread();
                    if (!isEmpty(lst_wi)){
                        for(Wi w: lst_wi){
                            db.updateWIStatusShow(w.getId());
                          //  showNotification_WI(w.getId(),w.getWicd(),w.getPath(),count); count++;
                        }
                    }

                    db.DeleteVIN_Old();
                    db.closeDB();

                    //clear old file older than 4 day
                    delOldFile();

                }

            });
        }

        private String getDateTime() {
            // get date time in custom format
            SimpleDateFormat sdf = new SimpleDateFormat("[yyyy/MM/dd - HH:mm:ss]");
            return sdf.format(new Date());
        }

        private void delOldFile(){
            int x = 4;
            File folder = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES),  Config.IMAGE_DIRECTORY_NAME); //This is just to cast to a File type since you pass it as a String
            File[] filesInFolder = folder.listFiles(); // This returns all the folders and files in your path
            if (isEmpty(filesInFolder)){

            }else{
                try{
                    for (File file : filesInFolder) { //For each of the entries do:
                        if (!file.isDirectory()) { //check that it's not a dir
                            long diff = new Date().getTime() - file.lastModified();

                            if (diff > x * 24 * 60 * 60 * 1000) {
                                file.delete();
                            }
                        }
                    }
                } catch (Exception e){
                    Log.e("", "Delete Old File", e);
                }
            }

        }

        private boolean isMyServiceRunning(Class<?> serviceClass) {
            ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
            return false;
        }

    }







    public class update_pod extends AsyncTask<Void, Void, Void> {
        RestService rs;
        String result;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rs = new RestService(getBaseContext(),"","",Global.user,Global.conf);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try{
              //  rs.Connect();
                //result = rs.GetJsonString("FixedAsset");
                SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                String username = prefs.getString("username","");

                result = rs.PostPOD_F(lst_pod, username);

            }catch (Exception ex){
//                Toast.makeText(getBaseContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result2) {
            super.onPostExecute(result2);
                   Toast.makeText(getBaseContext(),result, Toast.LENGTH_SHORT).show();
          /*  if (result== null){
              //  Toast.makeText(getBaseContext(),"Can't post the POD Data",Toast.LENGTH_LONG).show();
            }*/
            }
        }

    public class update_pod_det extends AsyncTask<Void, Void, Void> {
        RestService rs;
        String result;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rs = new RestService(getBaseContext(),"","",Global.user,Global.conf);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try{
                //  rs.Connect();
                //result = rs.GetJsonString("FixedAsset");
                result = rs.PostDataL(lst_pod_del);

            }catch (Exception ex){
//                Toast.makeText(getBaseContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result2) {
            super.onPostExecute(result2);
          //  Toast.makeText(getBaseContext(),result, Toast.LENGTH_SHORT).show();
          /*  if (result== null){
              //  Toast.makeText(getBaseContext(),"Can't post the POD Data",Toast.LENGTH_LONG).show();
            }*/
        }
    }

    public class delete_pod_user extends AsyncTask<Void, Void, Void> {
        RestService rs;
        String result;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rs = new RestService(getBaseContext(),"","","","");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try{
                // rs.Connect();
                //result = rs.GetJsonString("FixedAsset");
                result = rs.DelPOD(lst_pod_user);

            }catch (Exception ex){
//                Toast.makeText(getBaseContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result2) {
            super.onPostExecute(result2);
            //       Toast.makeText(getBaseContext(),result,Toast.LENGTH_LONG).show();
            if (result== null){
                //  Toast.makeText(getBaseContext(),"Can't delete the POD Data",Toast.LENGTH_LONG).show();
            }
        }
    }

    public static boolean isEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }

        if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        }
        if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return ((Map) obj).isEmpty();
        }

        // else
        return false;
    }

    public class GetWIMaster extends AsyncTask<Void, Void, Void> {
        RestService rs;
        String result;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rs = new RestService(getBaseContext(),"","","","");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try{
                //  rs.Connect();
                //result = rs.GetJsonString("FixedAsset");
                result = rs.GetJsonString(param,"WI");
            }catch (Exception ex){
//                Toast.makeText(getBaseContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result2) {
            super.onPostExecute(result2);
            //       Toast.makeText(getBaseContext(),result,Toast.LENGTH_LONG).show();
            if (result== null){
                // not found any order
                // Toast.makeText(getBaseContext(),"Can't load the Master Data",Toast.LENGTH_LONG).show();
            }else {
                if (status.equals("A")) {
                    // get data to buildings
                    try {
                        // String id, assetid, desc, branch, building, department, floor, room, location;
                        String id;
                        String wicd;
                        String widesc;
                        String path;
                        String lastupdate;

                        JSONArray jsonarray = new JSONArray(result);
                        for (int i = 0; i < jsonarray.length(); i++) {
                            JSONObject jsonobject = jsonarray.getJSONObject(i);
                            id = jsonobject.getString("id");
                            wicd = jsonobject.getString("wicd");
                            widesc = jsonobject.getString("widesc");
                            path = jsonobject.getString("path");
                            lastupdate = jsonobject.getString("lastupdate");


                            Wi d = new Wi();
                            d.setId(id);
                            d.setWicd(wicd);
                            d.setWi_desc(widesc);
                            d.setPath(path);
                            d.setLastupdate(lastupdate);

                            db.InsertWI(d);

                        }
                    } catch (JSONException e) {
                        Log.e("", "unexpected JSON exception", e);
                        //  Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                    // Toast.makeText(getBaseContext(), "Asset Load Complete", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
