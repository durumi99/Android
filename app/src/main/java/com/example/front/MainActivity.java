package com.example.front;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    String API_Key = "l7xxbf43a180f6e14ab69c7c179238f21d85";

    // T Map View
    TMapView tMapView = null;

    // T Map GPS
    TMapGpsManager tMapGPS = null;
    EditText keywordView_start;
    EditText keywordView_end;
    double latitude;
    double longitude;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        keywordView_start = (EditText) findViewById(R.id.searchBar_start);
        keywordView_end = (EditText) findViewById(R.id.searchBar_end);
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
//        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
        //gps 기반으로 위치 제공(에뮬레이터에서씀)
        tMapGPS.setProvider(tMapGPS.GPS_PROVIDER);
        tMapGPS.OpenGps();
        tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_currenticon);
        tMapView.setIcon(bitmap);

        ImageButton optionButton = (ImageButton)findViewById(R.id.optionButton);
        optionButton.bringToFront();

        //searchBar 객체 생성 & 최상단으로 가져오기
        EditText searchBar = (EditText) findViewById(R.id.searchBar_start);
        searchBar.bringToFront();
        EditText searchBar_end = (EditText) findViewById(R.id.searchBar_end);
        searchBar_end.bringToFront();
        //searchbarLayout 객체 생성 & 최상단으로 가져오기
        FrameLayout searchbarLayout = (FrameLayout) findViewById(R.id.searchbarLayout_start);
        searchbarLayout.bringToFront();
        FrameLayout searchbarLayout_end = (FrameLayout) findViewById(R.id.searchbarLayout_end);
        searchbarLayout_end.bringToFront();

        //현위치로 돌아오는 버튼 객체 생성 & 클릭 이벤트
        ImageButton CurrentLocation = (ImageButton)findViewById(R.id.CurrentLocate);
        ImageView CurrentLocationBackground = (ImageView)findViewById(R.id.CurrentLocateBackground);
        CurrentLocationBackground.bringToFront();
        CurrentLocation.bringToFront();
        CurrentLocation.setBackgroundResource(R.drawable.ic_gps);
        CurrentLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                tMapView.setTrackingMode(true);
                tMapView.setSightVisible(true);
            }
        });

        //여기 밑으로 시작경로랑 도착경로 searchbar 두개인데 코드 복붙이라 간결하게해야함..

        //출발지 선택시 searchbar 나타내며 수행할 것
        EditText start_edit = (EditText) findViewById(R.id.edit_start);
        start_edit.bringToFront();
        SlidingUpPanelLayout slidingView = (SlidingUpPanelLayout) findViewById(R.id.slidingView);
        int Height = slidingView.getPanelHeight();
        start_edit.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view , MotionEvent event){

                hide(slidingView);
                return true;
            }
        });

        Button backToMain = (Button) findViewById(R.id.backToMain_start);
        backToMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                nonhide(slidingView);
            }
        });
        //searchbar에서 MAIN으로 돌아오는 BACKTO MAIN 객체 생성 & 클릭 이벤트
        searchBar.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode,KeyEvent event){
                switch(keyCode){
                    case KeyEvent.KEYCODE_ENTER:
                        search(1);
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

        //도착지 선택시 searchbar 나타내며 수행할 것
        EditText edit_end = (EditText) findViewById(R.id.edit_end);
        int Height_end = slidingView.getPanelHeight();
        edit_end.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view , MotionEvent event){
                hide(slidingView);

                return true;
            }
        });
        //searchbar에서 MAIN으로 돌아오는 BACKTO MAIN 객체 생성 & 클릭 이벤트
        Button backToMain_end = (Button) findViewById(R.id.backToMain_end);
        backToMain_end.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                nonhide(slidingView);

            }
        });
        searchBar_end.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v, int keyCode,KeyEvent event){
                switch(keyCode){
                    case KeyEvent.KEYCODE_ENTER:
                        search(2);
                        break;
                    case KeyEvent.KEYCODE_DEL:
                        int length = searchBar_end.getText().length();
                        if (length > 0) {
                            searchBar_end.getText().delete(length -1, length);
                        }
                        break;
                }
                return true;
            }
        });

        tMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {

            @Override
            public void onLongPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint point) {
                TMapMarkerItem tItem = new TMapMarkerItem();
                tItem.setTMapPoint(point);
                tItem.setName("위치");
                tItem.setVisible(TMapMarkerItem.VISIBLE);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_icon);
                tItem.setIcon(bitmap);
                tMapView.addMarkerItem("marker",tItem);
                TMapData tmapdata = new TMapData();
                // point.getLatitude(), point.getLongitude() 각각 위 경도,
                tmapdata.convertGpsToAddress(point.getLatitude(), point.getLongitude(),
                        new TMapData.ConvertGPSToAddressListenerCallback() {
                            @Override
                            public void onConvertToGPSToAddress(String address) {

                                EditText editText = (EditText) findViewById(R.id.edit_start);
                                Log.d("Start ",editText.toString());
                                if(editText.getText().toString().equals("") || editText.getText().toString() == null) {
                                    EditText editTextHint = (EditText) findViewById(R.id.edit_start);
                                    editTextHint.setHint(null);
                                    editText.setText(address);
                                }
                                else{
                                    editText = (EditText) findViewById(R.id.edit_end);
                                    EditText editTextHint = (EditText) findViewById(R.id.edit_end);
                                    editTextHint.setHint(null);
                                    editText.setText(address);
                                }
                            }
                        });
            }

        });
    }

    private void hide(SlidingUpPanelLayout slidingView){
        ImageButton CurrentLocation = (ImageButton)findViewById(R.id.CurrentLocate);
        ImageButton optionButton = (ImageButton)findViewById(R.id.optionButton);
        FrameLayout searchbarLayout = (FrameLayout) findViewById(R.id.searchbarLayout_start);
        ImageView CurrentLocationBackground = (ImageView)findViewById(R.id.CurrentLocateBackground);
        CurrentLocationBackground.setVisibility(View.GONE);
        slidingView.setPanelHeight(0);
        CurrentLocation.setVisibility(View.GONE);
        optionButton.setVisibility(View.GONE);
        searchbarLayout.setVisibility(View.VISIBLE);

    }
    private void nonhide(SlidingUpPanelLayout slidingView){
        ImageButton CurrentLocation = (ImageButton)findViewById(R.id.CurrentLocate);
        ImageButton optionButton = (ImageButton)findViewById(R.id.optionButton);
        FrameLayout searchbarLayout = (FrameLayout) findViewById(R.id.searchbarLayout_start);
        ImageView CurrentLocationBackground = (ImageView)findViewById(R.id.CurrentLocateBackground);
        CurrentLocationBackground.setVisibility(View.VISIBLE);
        slidingView.setPanelHeight(340);
        CurrentLocation.setVisibility(View.VISIBLE);
        optionButton.setVisibility(View.VISIBLE);
        searchbarLayout.setVisibility(View.GONE);
    }
    private void search(int index){
        TMapPoint tpoint = tMapView.getLocationPoint();
        latitude = tpoint.getLatitude();
        longitude = tpoint.getLongitude();
        TMapData tmapdata = new TMapData();
        String keyword;
        if(index==1)
            keyword = keywordView_start.getText().toString();
        else
            keyword = keywordView_end.getText().toString();
        TMapView tmapView = new TMapView(this);
        tmapView.setCenterPoint(longitude, latitude, false);
        tmapView.setLocationPoint(longitude, latitude);

        tmapdata.findAroundNamePOI(tpoint, keyword, new TMapData.FindAroundNamePOIListenerCallback()
        {
            @Override
            public void onFindAroundNamePOI(final ArrayList<TMapPOIItem> poiItem) {
                tMapView.removeAllMarkerItem();
                for(int i = 0; i < poiItem.size(); i++) {
                    TMapPOIItem item = poiItem.get(i);
                    for (TMapPOIItem poi : poiItem) {
                        addMarker(poi);

                    }
                    Log.d("POI Name: ", item.getPOIName().toString() + ", " +
                            "Address: " + item.getPOIAddress().replace("null", "")  + ", " +
                            "Point: " + item.getPOIPoint().toString());
                }
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
                        //Toast.makeText(getApplicationContext(),"Select Menu2",Toast.LENGTH_SHORT).show();
                        intent = new Intent(MainActivity.this,FAQActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.menu2:
                        //Toast.makeText(getApplicationContext(),"Select Menu3",Toast.LENGTH_SHORT).show();
                        intent = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.menu3:
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