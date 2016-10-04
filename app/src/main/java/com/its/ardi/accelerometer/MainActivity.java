package com.its.ardi.accelerometer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.FileNameMap;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity extends Activity implements SensorEventListener  {

    private TextView xText, yText, zText, start,stop, simpanDi,textLIGHT_available,textLIGHT_reading;
    private Sensor mySensorAcc,mySensorLight;
    private SensorManager SM;
    String sdcard = Environment.getExternalStorageDirectory().getPath();
    private Button startButton,stopButton;
    private static final String TAG = "MyActivity";
    private boolean stopRecord = false;
    private boolean startRecord = false;
    private boolean active = false;
    private CSVWriter writer = null;
    private boolean flag = false;
    private Button Display;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        mySensorAcc = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mySensorLight = SM.getDefaultSensor(Sensor.TYPE_LIGHT);
        SM.registerListener(this, mySensorAcc, SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(this, mySensorLight, SensorManager.SENSOR_DELAY_NORMAL);

        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);
        startButton = (Button)findViewById(R.id.startButton);
        stopButton = (Button)findViewById(R.id.stopButton);
        Display = (Button) findViewById(R.id.display);
        Display.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick (View view){
                Intent display_intent = new Intent(getApplicationContext(), DisplayActivity.class);
                startActivity(display_intent);
                finish();
            }
        });
        start = (TextView)findViewById(R.id.start);
        stop = (TextView)findViewById(R.id.stop);
        simpanDi = (TextView)findViewById(R.id.simpanDi);
        textLIGHT_available
                = (TextView)findViewById(R.id.LIGHT_available);
        textLIGHT_reading
                = (TextView)findViewById(R.id.LIGHT_reading);

        startButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(isStoragePermissionGranted()) {
                    flag = true;
                    startRecord = true;
                    start.setText("1");
                    stop.setText("0");
                    stopRecord = false;
                }
            }
        });

        //eksekusi saat klik button stop
        stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flag = false;
                start.setText("0");
                stop.setText("1");
                stopRecord = true;
                startRecord = false;

            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xText.setText("X: " + event.values[0]);
        yText.setText("Y: " + event.values[1]);
        zText.setText("Z: " + event.values[2]);


        if(event.sensor.getType() == Sensor.TYPE_LIGHT){
            if(event.values[0]<=30.0){
                textLIGHT_available.setText("Activated");
                active = true;
            }
            else if(event.values[0]>30.0){
                textLIGHT_available.setText("Not Activated");
                active = false;
            }
            textLIGHT_reading.setText("LIGHT: " + event.values[0]);
        }
            String nama = sdcard+"/datasensor.csv";
            File namafile = new File(nama);
            simpanDi.setText(nama);

            SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
            String currentDateandTime1 = sdf1.format(new Date());
            String currentDateandTime2 = sdf2.format(new Date());

            String res=String.valueOf(currentDateandTime1+"#"+currentDateandTime2+"#"+event.values[0])+"#"+String.valueOf(event.values[1])+"#"+String.valueOf(event.values[2]);

            Log.d("test", res);
            try {

                if(startRecord&&active&&flag){
                    if(writer==null){
                        writer = new CSVWriter(new FileWriter(namafile,true), ',');
                        String resStart = "Tanggal#Jam#X-axis#Y-axis#Z-axis";
                        String[] entriesStart = resStart.split("#"); // array of your values
                        writer.writeNext(entriesStart);
                    }

                    String[] entries = res.split("#"); // array of your values
                    writer.writeNext(entries);
                }
                else if(stopRecord&&!active){

                    if(writer!=null){

                        writer.close();
                        writer = null;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
        }
    }







}
