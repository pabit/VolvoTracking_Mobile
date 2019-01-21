package com.thnopp.it.volvotracking;

/**
 * Created by CEVAUser on 5/27/2017.
 */


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;


public class TripdetailActivity extends Activity {

    private TextView lbldest,  lbldistance, lblfuel, lblsource;
    private ImageView img1;


    DatabaseHelper db;


    private Button btnComplete;

    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_detail);

       // db = DatabaseHelper.getInstance(getApplicationContext());
        db = DatabaseHelper.getInstance(getApplicationContext());
        lblsource = (TextView)findViewById(R.id.lblsource);
        lbldest = (TextView)findViewById(R.id.lbldest);
        lbldistance = (TextView)findViewById(R.id.lbldestance);
        lblfuel = (TextView)findViewById(R.id.lblfuel);

        btnComplete = (Button) findViewById(R.id.btnBack);
        final double slat, slon,dlat,dlon;
        /*
        load data from local server

         */

        Vinmaster v = db.findAvaialble_Order();
        if (v==null){
            Toast.makeText(getBaseContext(),"ยังไม่มี Trip ในมือถือ",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(), ReceiveNewActivity.class);
            startActivity(intent);
        }else{
            Global.t_delid = v.getId();
            lblsource.setText(v.getSource());
            lbldest.setText(v.getDealer_name());
            lbldistance.setText(v.getRef());
            lblfuel.setText(String.format("%.2f", Double.parseDouble(v.getEngine())));
            slat = v.getSlat();
            slon = v.getSlon();
            dlat = v.getDlat();
            dlon = v.getDlon();

            Button cmdMapR = (Button) findViewById(R.id.CmdMapR);
            cmdMapR.setText("Map ต้นทาง");
            cmdMapR.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String s = String.valueOf(slat) + "," +
                            String.valueOf(slon)+  "(Start)";
                    Uri u = Uri.parse("geo:0,0?q=" + s);
                    showMap(u);

                }
            });

            Button cmdMapD = (Button) findViewById(R.id.CmdMapD);
            cmdMapD.setText("Map ปลายทาง");
            cmdMapD.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String s = String.valueOf(dlat) + "," +
                            String.valueOf(dlon)+ "(Stop)";
                    Uri u = Uri.parse("geo:0,0?q=" + s);
                    showMap(u);

                }
            });

            Button cmdMapRo = (Button) findViewById(R.id.CmdMapRoute);
            cmdMapRo.setText("เส้นทาง");
            cmdMapRo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    String latitude1 = String.valueOf(slat);
                    String longitude1 = String.valueOf(slon);
                    String latitude2 = String.valueOf(dlat);
                    String longitude2 = String.valueOf(dlon);

                           /* String r = "http://maps.google.com/maps?f=d&hl=en&saddr="+latitude1+","+longitude1+"&daddr="+latitude2+","+longitude2;

                            Uri uri = Uri.parse("geo:" + latitude1 + "," + longitude1 + "?q=" + Uri.encode(latitude2 + "," + longitude2 + "(route)"));

                            Uri u = Uri.parse("geo:" + r);
                            showMap(uri);*/

                    String uri1 = "http://maps.google.com/maps?f=d&hl=en&saddr="+latitude1+","+longitude1+"&daddr="+latitude2+","+longitude2;
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri1));
                    startActivity(Intent.createChooser(intent, "Select an application"));

                }
            });


        }



        if (Global.user == null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

        }else{

            btnComplete.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent i = new Intent(TripdetailActivity.this, MenuActivity.class);
                    startActivity(i);
                }
            });

            btnComplete.setVisibility(View.VISIBLE);

        }



    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    @Override
    public void onBackPressed() {
    }










}
