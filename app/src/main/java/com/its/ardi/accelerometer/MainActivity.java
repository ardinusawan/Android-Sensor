package com.its.ardi.accelerometer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.core.Instance;
import weka.core.Instances;


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
    public int status_act = 0;
    LocationService myService;
    static boolean status;
    LocationManager locationManager;
    static TextView dist, time, speed;
    static long startTime, endTime;
    ImageView image;
    static ProgressDialog locate;
    static int p = 0;
<<<<<<< HEAD
    public static String loc;

=======
    BroadcastReceiver br = new AutoSMSActivity();
>>>>>>> bf63c83ea5a42a200670ceb2e4c8c0e136454ef5

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        mySensorAcc = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mySensorLight = SM.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        SM.registerListener(this, mySensorAcc, SensorManager.SENSOR_DELAY_NORMAL);
        SM.registerListener(this, mySensorLight, SensorManager.SENSOR_DELAY_NORMAL);

        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);

        dist = (TextView) findViewById(R.id.distancetext);
        time = (TextView) findViewById(R.id.timetext);
        speed = (TextView) findViewById(R.id.speedtext);

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
                    checkGps();
                    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                        return;
                    }


                    if (status == false)
                        //Here, the Location Service gets bound and the GPS Speedometer gets Active.
                        bindService();
                    locate = new ProgressDialog(MainActivity.this);
                    locate.setIndeterminate(true);
                    locate.setCancelable(false);
                    locate.setMessage("Getting Location...");
                    locate.show();
                }
            }
        });

        try {
            KNN();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //eksekusi saat klik button stop
        stopButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                flag = false;
                start.setText("0");
                stop.setText("1");
                stopRecord = true;
                startRecord = false;
                if (status == true)
                    unbindService();
                p = 0;

            }
        });

    }


    void checkGps() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {


            showGPSDisabledAlertToUser();
        }
    }

    //This method configures the Alert Dialog box.
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Enable GPS to use application")
                .setCancelable(false)
                .setPositiveButton("Enable GPS",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private ServiceConnection sc = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            myService = binder.getService();
            status = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            status = false;
        }
    };

    void bindService() {
        if (status == true)
            return;
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        bindService(i, sc, BIND_AUTO_CREATE);
        status = true;
        startTime = System.currentTimeMillis();
    }

    void unbindService() {
        if (status == false)
            return;
        Intent i = new Intent(getApplicationContext(), LocationService.class);
        unbindService(sc);
        status = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (status == true)
            unbindService();
    }

    @Override
    public void onBackPressed() {
        if (status == false)
            super.onBackPressed();
        else
            moveTaskToBack(true);
    }
    public static BufferedReader readDataFile(String filename) {
        BufferedReader inputReader = null;

        try {
            inputReader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException ex) {
            System.err.println("File not found: " + filename);
        }

        return inputReader;
    }
    public  void KNN() throws Exception {
        BufferedReader datafile = readDataFile(sdcard+"/data_latih.data");

        Instances data = new Instances(datafile);
        data.setClassIndex(data.numAttributes() - 1);

        //do not use first and second
        Instance first = data.instance(0);
        data.delete(0);

        Classifier ibk = new IBk();
        ibk.buildClassifier(data);

        double class1 = ibk.classifyInstance(first);
        System.out.println("first: " + class1);
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onSensorChanged(SensorEvent event) {
        String nama = sdcard+"/datasensor.csv";
        File namafile = new File(nama);
        simpanDi.setText(nama);

        SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm:ss");
        String currentDateandTime1 = sdf1.format(new Date());
        String currentDateandTime2 = sdf2.format(new Date());
        String res="";


        if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){
            if(event.values[0]>=1){
                textLIGHT_available.setText("Activated");
                active = true;
            }
            else if(event.values[0]<1){
                textLIGHT_available.setText("Not Activated");
                active = false;
            }
            textLIGHT_reading.setText("LIGHT: " + event.values[0]);
        }


        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            xText.setText("X: " + event.values[0]);
            yText.setText("Y: " + event.values[1]);
            zText.setText("Z: " + event.values[2]);
            res=String.valueOf(currentDateandTime1+"#"+currentDateandTime2+"#"+event.values[0])+"#"+String.valueOf(event.values[1])+"#"+String.valueOf(event.values[2]);

            if (event.values[1] > 0 && event.values[2] > 0 && LocationService.speed < 20) {
                res = res + "#Jalan";
                if(status_act ==1 ) {
                    unregisterReceiver(br);
                    status_act = 0;
                }
                //
            }
            else if((event.values[1]<0 && event.values[2]<0) || LocationService.speed > 20){
                res = res + "#Naik Motor";
                if(status_act == 0){
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("android.provider.Telephony.SMS_RECEIVED");
                    registerReceiver(br, filter);
                    status_act = 1;
                }
            }

            else {
                res = res + "#_";
                if(status_act ==1 ){
                    unregisterReceiver(br);
                    status_act = 0;
                }
            }
            try {

                if(startRecord&&active&&flag){
                    if(writer==null){
                        writer = new CSVWriter(new FileWriter(namafile,true), ',');
                        String resStart = "Tanggal#Jam#X-axis#Y-axis#Z-axis#Aktifitas";
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
