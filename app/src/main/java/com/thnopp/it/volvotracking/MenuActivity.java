package com.thnopp.it.volvotracking;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by THLT88 on 10/20/2017.
 */

public class MenuActivity extends Activity {

    TextView lbltype, lbluser ;

    Button scan, trip, logout, trip_p,upload,fuel,wi ;

    Long id;

    DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);


        SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
        String username = prefs.getString("username","");
        Integer client = prefs.getInt("client",0);
        String conf = prefs.getString("conf","");
        Global.user = username;
        Global.client = client;
        Global.conf = conf;

        db = DatabaseHelper.getInstance(getApplicationContext());

       // db.delete_unmatch_user(username);

        //lst = db.getAllVIN();

        //assign value into label
        Vinmaster v = db.findAvaialble_Order();
        if (v != null){
            id = v.getId();
        }else{
            id = 0L;
        }
        lbluser = (TextView)findViewById(R.id.lbluser);
        lbluser.setText(Global.user);

        scan = (Button)findViewById(R.id.buttonReceive);

        int chkDepart = db.chkPendingPOD_Incomplete();
        if (chkDepart > 0){
            lbluser.setText(Global.user + "/ กำลังเดินทาง");
            scan.setVisibility(View.INVISIBLE);
        }else{
            scan.setVisibility(View.VISIBLE);
        }
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*
                check pending order(order departdt or arrivaldt = null)_
                if found, goto rceive edit
                if not found go to receive new
                 */
                int chkpod = db.chkPendingPOD();
                if (chkpod ==0){
                    //new data
                    Intent intent = new Intent(getApplicationContext(), ReceiveNewActivity.class);
                    startActivity(intent);
                }else{
                    //no new data
                    Intent intent = new Intent(getApplicationContext(), ReceiveEditActivity.class);
                    startActivity(intent);
                }


            }
        });
        fuel = (Button)findViewById(R.id.buttonFuel);

        fuel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), TripdetailActivity.class);
                startActivity(intent);

            }
        });

        trip = (Button)findViewById(R.id.buttonQC);

        trip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), CaptureActivity.class);
                startActivity(intent);

            }
        });

        trip_p = (Button)findViewById(R.id.buttonUpdate);

        trip_p.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new chk_holding_status().execute();
               /* Intent intent = new Intent(getApplicationContext(), TripPendingActivity.class);
                startActivity(intent);*/

            }
        });

        wi = (Button)findViewById(R.id.buttonWI);

        wi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), MenuWIActivity.class);
                startActivity(intent);

            }
        });

        logout = (Button)findViewById(R.id.buttonLogout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Global.user=null;

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);

            }
        });
        upload = (Button) findViewById(R.id.buttonUpload);
        upload.setText("เชื่อมต่อกับ Server");


        upload.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View view) {
                  //sending data
                  if (isMyServiceRunning(ScheduleService.class)==false)
                      startService(new Intent(getApplicationContext(), ScheduleService.class));

                  if (isMyServiceRunning(ScheduleService_long.class)==false)
                      startService(new Intent(getApplicationContext(), ScheduleService_long.class));
                                      }
                                  }

        );


        if (isMyServiceRunning(ScheduleService.class)==false)
            startService(new Intent(this, ScheduleService.class));

        if (isMyServiceRunning(ScheduleService_long.class)==false)
            startService(new Intent(this, ScheduleService_long.class));
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

    private class chk_holding_status extends AsyncTask<Void,Integer, String> {
        @Override
        protected  void onPreExecute(){

            super.onPreExecute();

        }
        @Override
        protected String doInBackground(Void... params) {
            return uploadProcess();

        }

        @SuppressWarnings("deprecation")
        private String uploadProcess(){
            String responseString = null;
            // add data to table via json

            RequestQueue queue = Volley.newRequestQueue(MenuActivity.this);
            DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            StringRequest request = new StringRequest(Request.Method.POST, Config.CHK_HOLD, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    //   Toast.makeText(FModelActivity.this, "my success"+response, Toast.LENGTH_LONG).show();
                   // Log.i("My success",""+response);

                    if (response.contains("OK") ){
                        Intent intent = new Intent(getApplicationContext(), TripPendingActivity.class);
                        startActivity(intent);
                    } else  if (response.contains("HOLD")){
                        Toast.makeText(getBaseContext(), "This job " + id + " was HOLD!!!", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getBaseContext(), "data error " + response, Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(MenuActivity.this, "my error :"+error, Toast.LENGTH_LONG).show();
                   // Log.i("My error",""+error);
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Config.HEAD_KEY, Config.HEAD_VALUE);
                    return params;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    SharedPreferences prefs = getSharedPreferences("UserData", MODE_PRIVATE);
                    String username = prefs.getString("username","");

                    Map<String,String> map = new HashMap<String, String>();
                    map.put("id",String.valueOf(id));
                    map.put("user",username);
                    return map;
                }
            };
            request.setRetryPolicy(retryPolicy);
            queue.add(request);

            return  responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.e(TAG, "Response from server: " + result);

            super.onPostExecute(result);

        }

    }
    public class chk_holding_statusd extends AsyncTask<Void, Void, Void> {
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
                //result = rs.chkJobHeader(HRef);

            }catch (Exception ex){
//                Toast.makeText(getBaseContext(),ex.getMessage(),Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result2) {
            super.onPostExecute(result2);


            if (result== null){
                Toast.makeText(getBaseContext(),result,Toast.LENGTH_SHORT).show();
            }else{

                if (result.contains("OK") ){
                    Intent intent = new Intent(getApplicationContext(), TripPendingActivity.class);
                    startActivity(intent);
                } else  if (result.contains("HOLD")){
                    Toast.makeText(getBaseContext(), "This job " + id + " was HOLD!!!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getBaseContext(), "data error " + result, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
    }

}
