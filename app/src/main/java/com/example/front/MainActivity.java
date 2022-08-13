package com.example.front;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapView;

public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    String API_Key = "l7xxbf43a180f6e14ab69c7c179238f21d85";

    // T Map View
    TMapView tMapView = null;

    // T Map GPS
    TMapGpsManager tMapGPS = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // T Map View
        tMapView = new TMapView(this);

        // API Key
        tMapView.setSKTMapApiKey(API_Key);

        //초기 설정
        //줌 깊이
        tMapView.setZoomLevel(16);
        //현위치 아이콘 표시
        tMapView.setIconVisibility(true);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        // T Map View Using Linear Layout
        RelativeLayout RelativeLayoutTmap = (RelativeLayout)findViewById(R.id.map_view);
        RelativeLayoutTmap.addView(tMapView);

        //GPS 요청 권한 없으면 안됨
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // GPS using T Map
        tMapGPS = new TMapGpsManager(this);

        // 초기 설정
        tMapGPS.setMinTime(1000);
        tMapGPS.setMinDistance(10);

        //네트워크 기반으로 위치 제공(실제휴대폰)
        //tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
        //gps 기반으로 위치 제공(에뮬레이터에서씀)
        tMapGPS.setProvider(tMapGPS.GPS_PROVIDER);
        tMapGPS.OpenGps();

        ImageButton CurrentLocation = (ImageButton)findViewById(R.id.CurrentLocate);
        CurrentLocation.bringToFront();
        CurrentLocation.setBackgroundResource(R.drawable.cl);
        CurrentLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                tMapView.setTrackingMode(true);
                tMapView.setSightVisible(true);
            }
        });
    }


    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
    }
}