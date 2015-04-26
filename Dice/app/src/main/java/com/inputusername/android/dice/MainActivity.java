package com.inputusername.android.dice;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import java.util.Random;


public class MainActivity extends Activity {

    /* Dice image code */
    private ImageView imageView;
    private final int[] IMAGE_IDS = {
            R.drawable.dice_1,
            R.drawable.dice_2,
            R.drawable.dice_3,
            R.drawable.dice_4,
            R.drawable.dice_5,
            R.drawable.dice_6
    };

    /* Dice logic code */
    private int sensitivity = 10;
    private boolean throwing = false;
    private int counter = 0;
    private int updateSpeed = 125;
    private int counterLimit = 5 * 1000 / updateSpeed; //roll for 10 seconds
    private Random rand = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Dice throw logic */
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        imageView = (ImageView)findViewById(R.id.imageView_dice);

        randomize();
    }

    private void randomize() {
        int random = rand.nextInt(6);
        imageView.setImageResource(IMAGE_IDS[random]);
    }

    /* Timer code */
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            counter++;
            randomize();

            if (counter >= counterLimit) {
                throwing = false;
                counter = 0;
                timerHandler.removeCallbacks(this);
            }

            if (throwing) {
                timerHandler.postDelayed(this, updateSpeed);
            }
        }
    };

    /* Accelerometer code */
    private SensorManager mSensorManager;
    private float mAccel; // acceleration apart from gravity
    private float mAccelCurrent; // current acceleration including gravity
    private float mAccelLast; // last acceleration including gravity

    private final SensorEventListener mSensorListener = new SensorEventListener() {

        public void onSensorChanged(SensorEvent se) {
            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = android.util.FloatMath.sqrt(x*x + y*y + z*z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            if (mAccel > sensitivity) {
                if (!throwing) {
                    throwing = true;
                    timerHandler.post(timerRunnable);
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mSensorManager.unregisterListener(mSensorListener);
        super.onPause();
    }

}
