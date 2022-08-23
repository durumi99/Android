package com.example.front;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Location;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    String API_Key = "l7xxbf43a180f6e14ab69c7c179238f21d85";

    // T Map View
    TMapView tMapView = null;

    // T Map GPS
    TMapGpsManager tMapGPS = null;
    EditText keywordView;

    double latitude;
    double longitude;
    double minRadius = 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        keywordView = (EditText) findViewById(R.id.searchBar);

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

        TMapPoint tpoint = tMapView.getLocationPoint();
        latitude = tpoint.getLatitude();
        longitude = tpoint.getLongitude();
        //네트워크 기반으로 위치 제공(실제휴대폰)
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
        //gps 기반으로 위치 제공(에뮬레이터에서씀)
        //tMapGPS.setProvider(tMapGPS.GPS_PROVIDER);
        tMapGPS.OpenGps();
        ImageButton optionButton = (ImageButton)findViewById(R.id.optionButton);
        optionButton.bringToFront();

        //searchBar 객체 생성 & 최상단으로 가져오기
        EditText searchBar = (EditText) findViewById(R.id.searchBar);
        searchBar.bringToFront();

        //searchbarLayout 객체 생성 & 최상단으로 가져오기
        FrameLayout searchbarLayout = (FrameLayout) findViewById(R.id.searchbarLayout);
        searchbarLayout.bringToFront();

        //현위치로 돌아오는 버튼 객체 생성 & 클릭 이벤트
        ImageButton CurrentLocation = (ImageButton)findViewById(R.id.CurrentLocate);
        CurrentLocation.bringToFront();
        CurrentLocation.setBackgroundResource(R.drawable.ic_gps);
        CurrentLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                tMapView.setTrackingMode(true);
                tMapView.setSightVisible(true);
            }
        });

        //검색 수행하는 버튼 객체 생성 & 클릭 이벤트
        Button btn = (Button) findViewById(R.id.search_button1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search();
            }
        });

        //출발지 선택시 searchbar 나타내며 수행할 것
        TextInputEditText start_edit = (TextInputEditText) findViewById(R.id.edit_start);
        SlidingUpPanelLayout slidingView = (SlidingUpPanelLayout) findViewById(R.id.slidingView);
        int Height = slidingView.getPanelHeight();
         start_edit.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view , MotionEvent event){

                slidingView.setPanelHeight(0);
                CurrentLocation.setVisibility(View.GONE);
                optionButton.setVisibility(View.GONE);
                searchbarLayout.setVisibility(View.VISIBLE);
                return true;
            }
        });

         //searchbar에서 MAIN으로 돌아오는 BACKTO MAIN 객체 생성 & 클릭 이벤트
        Button backToMain = (Button) findViewById(R.id.backToMain);
        backToMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                slidingView.setPanelHeight(Height);
                CurrentLocation.setVisibility(View.VISIBLE);
                optionButton.setVisibility(View.VISIBLE);
                searchbarLayout.setVisibility(View.INVISIBLE);
            }
        });

        searchBar.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode,KeyEvent event){
                switch(keyCode){
                    case KeyEvent.KEYCODE_ENTER:
                        search();
                        break;
                    case KeyEvent.KEYCODE_DEL:
                        int length = searchBar.getText().length();
                        if (length > 0) {
                            searchBar.getText().delete(length -1, length);
                        }
                        break;
                }
                return true;
            }
        });
        tMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressUpEvent(ArrayList markerlist,ArrayList poilist, TMapPoint point, PointF pointf) {
                TMapMarkerItem tItem = new TMapMarkerItem();
                tItem.setTMapPoint(point);
                tItem.setName("위치");
                tItem.setVisible(TMapMarkerItem.VISIBLE);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_icon);
                tItem.setIcon(bitmap);
                tMapView.addMarkerItem("marker",tItem);
                TMapData tmapdata = new TMapData();
                tmapdata.convertGpsToAddress(point.getLatitude(), point.getLongitude(),
                        new TMapData.ConvertGPSToAddressListenerCallback() {
                            @Override
                            public void onConvertToGPSToAddress(String address) {
//                            tmapdata.findAddressPOI(address, new TMapData.FindAddressPOIListenerCallback() {
//                                @Override
//                                public void onFindAddressPOI(ArrayList poiItem) {
//                                    for(int i = 0 ; i < poiItem.size(); i++){
//                                        TMapPOIItem  item = (TMapPOIItem) poiItem.get(i);
//                                        Log.d("POI Name: ", item.getPOIName().toString() + ", " +
//                                                "Address: " + item.getPOIAddress().replace("null", "")  + ", " +
//                                                "Point: " + item.getPOIPoint().toString());
//                                    }
//                                }
//                            });

                                TextInputLayout editTextHint = (TextInputLayout) findViewById(R.id.edit_start_hint);
                                editTextHint.setHint(null);
                                EditText editText = (EditText) findViewById(R.id.edit_start);
                                editText.setText(address);
                            }
                        });

                return false;
            }

            @Override
            public boolean onPressEvent(ArrayList markerlist,ArrayList poilist, TMapPoint point, PointF pointf) {
                TMapMarkerItem tItem = new TMapMarkerItem();
                tItem.setTMapPoint(point);
                tItem.setName("위치");
                tItem.setVisible(TMapMarkerItem.VISIBLE);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_icon);
                tItem.setIcon(bitmap);
                tMapView.addMarkerItem("marker",tItem);
                TMapData tmapdata = new TMapData();
                tmapdata.convertGpsToAddress(point.getLatitude(), point.getLongitude(),
                        new TMapData.ConvertGPSToAddressListenerCallback() {
                            @Override
                            public void onConvertToGPSToAddress(String address) {
                                TextInputLayout editTextHint = (TextInputLayout) findViewById(R.id.edit_start_hint);
                                editTextHint.setHint(null);
                                EditText editText = (EditText) findViewById(R.id.edit_start);
                                editText.setText(address);
                            }
                        });

                return false;
            }
        });
        tMapView.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressUpEvent(ArrayList markerlist,ArrayList poilist, TMapPoint point, PointF pointf) {
                TMapMarkerItem tItem = new TMapMarkerItem();
                tItem.setTMapPoint(point);
                tItem.setName("위치");
                tItem.setVisible(TMapMarkerItem.VISIBLE);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_icon);
                tItem.setIcon(bitmap);
                tMapView.addMarkerItem("marker",tItem);
                TMapData tmapdata = new TMapData();
                tmapdata.convertGpsToAddress(point.getLatitude(), point.getLongitude(),
                        new TMapData.ConvertGPSToAddressListenerCallback() {
                            @Override
                            public void onConvertToGPSToAddress(String address) {
//                            tmapdata.findAddressPOI(address, new TMapData.FindAddressPOIListenerCallback() {
//                                @Override
//                                public void onFindAddressPOI(ArrayList poiItem) {
//                                    for(int i = 0 ; i < poiItem.size(); i++){
//                                        TMapPOIItem  item = (TMapPOIItem) poiItem.get(i);
//                                        Log.d("POI Name: ", item.getPOIName().toString() + ", " +
//                                                "Address: " + item.getPOIAddress().replace("null", "")  + ", " +
//                                                "Point: " + item.getPOIPoint().toString());
//                                    }
//                                }
//                            });

                                EditText editText = (EditText) findViewById(R.id.edit_start);
                                Log.d("Start ",editText.toString());
                                if(editText.getText().toString().equals("") || editText.getText().toString() == null) {
                                    TextInputLayout editTextHint = (TextInputLayout) findViewById(R.id.edit_start_hint);
                                    editTextHint.setHint(null);
                                    editText.setText(address);
                                }
                                else{
                                    editText = (EditText) findViewById(R.id.edit_end);
                                    TextInputLayout editTextHint = (TextInputLayout) findViewById(R.id.edit_end_hint);
                                    editTextHint.setHint(null);
                                    editText.setText(address);
                                }
                            }
                        });

                return false;
            }

            @Override
            public boolean onPressEvent(ArrayList markerlist,ArrayList poilist, TMapPoint point, PointF pointf) {
//                TMapMarkerItem tItem = new TMapMarkerItem();
//                tItem.setTMapPoint(point);
//                tItem.setName("위치");
//                tItem.setVisible(TMapMarkerItem.VISIBLE);
//                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_icon);
//                tItem.setIcon(bitmap);
//                tMapView.addMarkerItem("marker",tItem);
//                TMapData tmapdata = new TMapData();
//                tmapdata.convertGpsToAddress(point.getLatitude(), point.getLongitude(),
//                        new TMapData.ConvertGPSToAddressListenerCallback() {
//                            @Override
//                            public void onConvertToGPSToAddress(String address) {
//                                EditText editText = (EditText) findViewById(R.id.edit_start);
//                                Log.d("Start ",editText.toString());
//                                if(editText.getText().toString().equals("") || editText.getText().toString() == null) {
//                                    TextInputLayout editTextHint = (TextInputLayout) findViewById(R.id.edit_start_hint);
//                                    editTextHint.setHint(null);
//                                    editText.setText(address);
//                                }
//                                else{
//                                    editText = (EditText) findViewById(R.id.edit_end);
//                                    TextInputLayout editTextHint = (TextInputLayout) findViewById(R.id.edit_end_hint);
//                                    editTextHint.setHint(null);
//                                    editText.setText(address);
//                                }
//                            }
//                        });

                return false;
            }
        });
    }


    private void search(){
        TMapData tmapdata = new TMapData();
        String keyword = keywordView.getText().toString();


        tmapdata.findAllPOI(keyword, new TMapData.FindAllPOIListenerCallback() {
            @Override
            public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
                tMapView.removeAllMarkerItem(); //찍은 마커들 제거
                for(int i = 0; i < poiItem.size(); i++) {
                    TMapPOIItem item = (TMapPOIItem) poiItem.get(i);
                    TMapPoint point = new TMapPoint(latitude, longitude);
                    /*double Distance = item.getDistance(point);
                    System.out.println(Distance);
                    if (Distance < minRadius) {
                        continue;
                    }*/

                    //poi 받아서 marker 추가
                    for (TMapPOIItem poi : poiItem) {
                            addMarker(poi);

                    }
                    //로그찍어보기위함
                    Log.d("POI Name: ", item.getPOIName().toString() + ", " +
                            "Address: " + item.getPOIAddress().replace("null", "")  + ", " +
                            "Point: " + item.getPOIPoint().toString());
                }
                poiItem.clear(); //마커 담긴 arraylist 초기화
            }
        });
    }
    public void addMarker(TMapPOIItem poi) {
        //point 객체
        TMapMarkerItem item = new TMapMarkerItem();
        item.setTMapPoint(poi.getPOIPoint());



        item.setTMapPoint(item.getTMapPoint()); //마커 위치
        item.setName(poi.getPOIName()); //마커 이름
        item.setCalloutTitle(poi.getPOIName()); //main message
        item.setCalloutSubTitle(poi.getPOIContent());//sub message
        item.setVisible(TMapMarkerItem.VISIBLE);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_icon);//비트맵을 marker 아이콘 설정
        item.setIcon(bitmap);// 아이콘 적용
        item.setCanShowCallout(true);

        tMapView.addMarkerItem(poi.getPOIID(),item);
    }

    public void popClick(View view){
        PopupMenu popM = new PopupMenu(this,view);
        popM.getMenuInflater().inflate(R.menu.main_menu,popM.getMenu());
        popM.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent;
                switch (item.getItemId()){
                    case R.id.menu1:
                        //Toast.makeText(getApplicationContext(),"Select Menu1",Toast.LENGTH_SHORT).show();
                        intent = new Intent(MainActivity.this,NoticeActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.menu2:
                        //Toast.makeText(getApplicationContext(),"Select Menu2",Toast.LENGTH_SHORT).show();
                        intent = new Intent(MainActivity.this,FAQActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.menu3:
                        //Toast.makeText(getApplicationContext(),"Select Menu3",Toast.LENGTH_SHORT).show();
                        intent = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.menu4:
                        //Toast.makeText(getApplicationContext(),"Select Menu4",Toast.LENGTH_SHORT).show();
                        intent = new Intent(MainActivity.this,ReportActivity.class);
                        startActivity(intent);
                        return true;

                    default:
                        return false;
                }
            }
        });
        popM.show();
    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
//
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        super.onOptionsItemSelected(item);
//        Toast toast = Toast.makeText(getApplicationContext(),"", Toast.LENGTH_LONG);
//
//        switch(item.getItemId())
//        {
//            case R.id.menu1:
//                toast.setText("Select Menu1");
//                break;
//            case R.id.menu2:
//                toast.setText("Select Menu2");
//                break;
//            case R.id.menu3:
//                toast.setText("Select Menu3");
//                break;
//        }
//
//        toast.show();
//
//        return super.onOptionsItemSelected(item);
//
//    }

    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
    }
}