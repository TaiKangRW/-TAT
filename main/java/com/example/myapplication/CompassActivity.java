package com.example.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class CompassActivity extends AppCompatActivity implements SensorEventListener{

    public static final String TAG = "CompassActivity";
    private static final int EXIT_TIME = 2000;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagneticField;
    private LocationManager mLocationManager;
    private String mLocationProvider;
    private float mCurrentDegree = 0f;
    private float[] mAccelerometerValues = new float[3];
    private float[] mMagneticFieldValues = new float[3];
    private float[] mValues = new float[3];
    private float[] mMatrix = new float[9];

    private long firstExitTime = 0L;

    private TextView mTvCoord;
    private LinearLayout mLlLocation;
    private TextView mTvAltitude;
    private ImageView mIvCompass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);
        initService();
        findViews();
    }

    private void findViews() {
        mIvCompass = (ImageView) findViewById(R.id.iv_compass);
        mTvCoord = (TextView) findViewById(R.id.tv_coord);
        mTvAltitude = (TextView) findViewById(R.id.tv_altitude);
        mLlLocation = (LinearLayout) findViewById(R.id.ll_location);
        mLlLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLocationService();
                updateLocationService();
            }
        });
    }

    private void initService() {
        initSensorService();
        initLocationService();
    }

    private void initSensorService() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private void initLocationService() {
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        mLocationProvider = mLocationManager.getBestProvider(criteria, true);// 获取条件最好的Provider,若没有权限，mLocationProvider 为null
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerService();
    }

    private void registerService() {
        registerSensorService();
        updateLocationService();
    }

    private void registerSensorService() {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagneticField, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void updateLocationService() {
        if (!checkLocationPermission()) {
            mTvCoord.setText(R.string.check_location_permission);
            return;
        }
        if (mLocationProvider != null) {
            updateLocation(mLocationManager.getLastKnownLocation(mLocationProvider));
            mLocationManager.requestLocationUpdates(mLocationProvider, 2000, 10, mLocationListener);
        }
        else {
            mTvCoord.setText(R.string.cannot_get_location);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregister();
    }

    private void unregister() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }

        if (mLocationManager != null) {
            if (!checkLocationPermission()) {
                return;
            }
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccelerometerValues = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mMagneticFieldValues = event.values;
        }
        SensorManager.getRotationMatrix(mMatrix, null, mAccelerometerValues, mMagneticFieldValues);
        SensorManager.getOrientation(mMatrix, mValues);
        float degree = (float) Math.toDegrees(mValues[0]);
        setImageAnimation(degree);
        mCurrentDegree = -degree;
    }

    private void setImageAnimation(float degree) {
        RotateAnimation ra = new RotateAnimation(mCurrentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        ra.setDuration(200);
        ra.setFillAfter(true);
        mIvCompass.startAnimation(ra);
    }

    private boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    private void updateLocation(Location location) {
        if (location == null) {
            mTvCoord.setText(getString(R.string.cannot_get_location));
            mTvAltitude.setVisibility(View.GONE);
        }
        else {
            mTvAltitude.setVisibility(View.VISIBLE);
            StringBuilder stringBuilder = new StringBuilder();
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            double altitude = location.getAltitude();
            if (latitude >= 0.0f) {
                stringBuilder.append(getString(R.string.location_north, latitude));
            }
            else {
                stringBuilder.append(getString(R.string.location_south, (-1.0 * latitude)));
            }
            stringBuilder.append("      ");
            if (longitude >= 0.0f) {
                stringBuilder.append(getString(R.string.location_east, longitude));
            }
            else {
                stringBuilder.append(getString(R.string.location_west, (-1.0 * longitude)));
            }
            mTvCoord.setText(getString(R.string.correct_coord, stringBuilder.toString()));
            mTvAltitude.setText(getString(R.string.correct_altitude, altitude));
        }
    }

    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (status != LocationProvider.OUT_OF_SERVICE) {
                if (!checkLocationPermission()) {
                    mTvCoord.setText(R.string.check_location_permission);
                    return;
                }
                updateLocation(mLocationManager.getLastKnownLocation(mLocationProvider));
            }
            else {
                mTvCoord.setText(R.string.check_location_permission);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onBackPressed() {
        long curTime = System.currentTimeMillis();
        if (curTime - firstExitTime < EXIT_TIME) {
            finish();
        }
        else {
            Toast.makeText(this, R.string.exit_toast, Toast.LENGTH_SHORT).show();
            firstExitTime = curTime;
        }
    }
}
