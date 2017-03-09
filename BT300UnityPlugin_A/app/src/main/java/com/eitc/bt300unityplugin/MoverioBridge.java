package com.eitc.bt300unityplugin;

/**
 * Created by EITC November 2016
 */


//Import the necessary Sensor Packages Here.


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

//Import additional packages including the Moverio SDK.

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.epson.moverio.btcontrol.*;
import com.unity3d.player.UnityPlayerActivity;


public class MoverioBridge extends UnityPlayerActivity implements SensorEventListener{

    //Get the necessary sensor values in order to use the Epson Defined
    //sensor types.

    public final int SENSOR_TYPE_HEADSET_TAP = 8193;
    public final int SENSOR_TYPE_CONTROLLER_ACCELEROMETER = 1048577;
    public final int SENSOR_TYPE_CONTROLLER_MAGNETIC_FIELD = 1048578;
    public final int SENSOR_TYPE_CONTROLLER_GYROSCOPE = 1048580;
    public final int SENSOR_TYPE_CONTROLLER_ROTATION_VECTOR = 1048587;

    public final int CUSTOM_SENSOR_COUNT = 9;


    //set up values to simplify sensor data.

    public final int TYPE_HEADSET_ACCELEROMETER = 0;
    public final int TYPE_CONTROLLER_ACCELEROMETER = 1;
    public final int TYPE_CONTROLLER_MAGNETIC_FIELD = 2;
    public final int TYPE_CONTROLLER_GYROSCOPE = 3;
    public final int TYPE_CONTROLLER_ROTATION_VECTOR = 4;
    public final int TYPE_HEADSET_TAP = 5;

    //Variables to handle the various sensors and display parameters.

    private DisplayControl mDisplayControl = null;
    //Sensor Test
    private SensorManager sensorManager;
    private Sensor[]  sensorArray = null;

    private boolean isMoverioDevice = false;

    private static MoverioBridge mInstance = null;
    private float[][] sensorData = null;
    private static int tapCount = 0;
    private static long startTime = 0;

    public static MoverioBridge instance(){
        if(mInstance == null){
            Looper.prepare();
            mInstance = new MoverioBridge();
        }
        return mInstance;
    }

    public MoverioBridge(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mDisplayControl = new DisplayControl(this);

        //Create a Sensor Array to handle the various Epson defined sensor types.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorArray = new Sensor[CUSTOM_SENSOR_COUNT];
        sensorData = new float[CUSTOM_SENSOR_COUNT][3];

        sensorArray[TYPE_HEADSET_ACCELEROMETER] = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        //Configure the controller sensors.
        sensorArray[TYPE_CONTROLLER_ACCELEROMETER] = sensorManager.getDefaultSensor(SENSOR_TYPE_CONTROLLER_ACCELEROMETER);
        sensorArray[TYPE_CONTROLLER_MAGNETIC_FIELD] = sensorManager.getDefaultSensor(SENSOR_TYPE_CONTROLLER_MAGNETIC_FIELD);
        sensorArray[TYPE_CONTROLLER_GYROSCOPE] = sensorManager.getDefaultSensor(SENSOR_TYPE_CONTROLLER_GYROSCOPE);
        sensorArray[TYPE_CONTROLLER_ROTATION_VECTOR] = sensorManager.getDefaultSensor(SENSOR_TYPE_CONTROLLER_ROTATION_VECTOR);


        //Configure Headset Tap
        sensorArray[TYPE_HEADSET_TAP] = sensorManager.getDefaultSensor(SENSOR_TYPE_HEADSET_TAP);

    }

    public void onStop(){
        if(isMoverioDevice){
            mDisplayControl.setBacklight(20);
            mDisplayControl.setMode(DisplayControl.DISPLAY_MODE_2D, false);

        }
        //sensorArray = null;
        //sensorData = null;

        super.onStop();
    }


    //a function to make sure this works with a Moverio Device.
    public void SetMoverioDevice(){
        isMoverioDevice = true;
    }

    //a function to let developers set 2D/3D Mode
    public void SetDisplay3D(boolean on){
        if(on){
            mDisplayControl.setMode(DisplayControl.DISPLAY_MODE_3D, true);
        }else{
            mDisplayControl.setMode(DisplayControl.DISPLAY_MODE_2D, false);
        }
    }

    //A function to let developers set the screen brightness
    public String SetDisplayBrightness(int brightness){
        String msg = "";
        if(brightness > -1 && brightness < 21){
            mDisplayControl.setBacklight(brightness);
            msg = "SUCCESS: SetBackLight: "+ String.valueOf(brightness);
        }else{
            msg = "ERROR: setBackLight value out of range. Must be 0-20";
        }
        return msg;
    }

    //function that will allow users to get sensor data from the controller.
    public float[] GetSensorData( int sensorType ){

        return sensorData[sensorType];
    }

    public boolean GetHeadsetTap(){
        if ( (int)sensorData[TYPE_HEADSET_TAP][0] == 1)
            return true;
        else
            return false;
    }

    public int GetHeadsetTapCount(){
        return tapCount;
    }


    public int GetDisplayBrightness(){
        int i = mDisplayControl.getBacklight();
        return i;
    }


    //function to let developers Mute the Display.
    public void MuteDisplay(boolean on){
        mDisplayControl.setMute(on);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy){

        //if your sensor accuracy changes plus put in code/methods
        //to fix that here.

    }

    //onSensorChanged will actually the read/send the sensor data when
    //needed. 
    @Override
    public final void onSensorChanged(SensorEvent event){
        long currentTime;

        if ( (int)sensorData[TYPE_HEADSET_TAP][0] == 1 ) { //Headset Tapped
            currentTime = System.currentTimeMillis();
            if ( (currentTime - startTime) > 2000L) { //2 secons delay to keep tap event data
                sensorData[TYPE_HEADSET_TAP][0] = 0;
            }
        }

        //float values that will actually hold the numerical data from sensors.

        //if statement to get the data from the accelerometer.

        switch ( event.sensor.getType() ){
            case Sensor.TYPE_ACCELEROMETER:
                sensorData[TYPE_HEADSET_ACCELEROMETER] = event.values;
                break;

            case SENSOR_TYPE_CONTROLLER_ACCELEROMETER:
                sensorData[TYPE_CONTROLLER_ACCELEROMETER] = event.values;
                break;

            case SENSOR_TYPE_CONTROLLER_MAGNETIC_FIELD:
                sensorData[TYPE_CONTROLLER_MAGNETIC_FIELD] = event.values;
                break;

            case SENSOR_TYPE_CONTROLLER_GYROSCOPE:
                sensorData[TYPE_CONTROLLER_GYROSCOPE] = event.values;
                break;

            case SENSOR_TYPE_CONTROLLER_ROTATION_VECTOR:
                sensorData[TYPE_CONTROLLER_ROTATION_VECTOR] = event.values;
                break;

            case SENSOR_TYPE_HEADSET_TAP:
                //don't input event.values[0] into sensorData[TYPE_HEADSET_TAP][0]
                // it may not be value 2
                sensorData[TYPE_HEADSET_TAP][0] = 1;
                tapCount++;
                startTime = System.currentTimeMillis();
                break;
            

        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        for (int i = 0; i< CUSTOM_SENSOR_COUNT; i++) {
            sensorManager.registerListener(this, sensorArray[i], SensorManager.SENSOR_DELAY_NORMAL);

        }
        //check to see if value is null.
    }

    //OnPause method to unregister the sensor managers with a listener.

    @Override
    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

}

