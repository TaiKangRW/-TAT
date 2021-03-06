package com.example.myapplication;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.MyLocationStyle;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DrawerLayout drawer_layout;
    private FrameLayout fly_content;
    private View topbar;
    private Button btn_right;
    private RightFragment fg_right_menu;
    private FragmentManager fManager;
    private MapView mMapView = null;
    private AMap aMap = null;
    private MyLocationStyle myLocationStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fManager = getSupportFragmentManager();
        fg_right_menu = (RightFragment) fManager.findFragmentById(R.id.fg_right_menu);
        mMapView = (MapView) findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        if (aMap == null) {
            aMap = mMapView.getMap();
        }
        initViews();
    }

    private void initViews() {
        drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
        fly_content = (FrameLayout) findViewById(R.id.fly_content);
        topbar = findViewById(R.id.topbar);
        btn_right = (Button) topbar.findViewById(R.id.btn_right);
        btn_right.setOnClickListener(this);
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.END);
        drawer_layout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View view, float v) {

            }

            @Override
            public void onDrawerOpened(View view) {

            }

            @Override
            public void onDrawerClosed(View view) {
                drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.END);
            }

            @Override
            public void onDrawerStateChanged(int i) {

            }
        });
        fg_right_menu.setDrawerLayout(drawer_layout);

        myLocationStyle = new MyLocationStyle();
        myLocationStyle.interval(2000);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setMyLocationEnabled(true);
    }

    @Override
    public void onClick(View v) {
        drawer_layout.openDrawer(Gravity.RIGHT);
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.END);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}
