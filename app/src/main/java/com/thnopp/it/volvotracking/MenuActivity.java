package com.thnopp.it.volvotracking;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by THLT88 on 10/20/2017.
 */

public class MenuActivity extends Activity {

    TextView lbltype, lbluser ;

    Button scan, trip, logout, trip_p,upload,fuel,wi ;

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

                Intent intent = new Intent(getApplicationContext(), TripPendingActivity.class);
                startActivity(intent);

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


    @Override
    public void onBackPressed() {
    }

}
