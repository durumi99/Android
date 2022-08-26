package com.example.front;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.poi_item.TMapPOIItem;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;


public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    String API_Key = "l7xxbf43a180f6e14ab69c7c179238f21d85";

    // T Map View
    TMapView tMapView = null;

    ArrayList<HashMap<String,String>> listviewlist=  new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String,String>> listviewresult=  new ArrayList<HashMap<String, String>>();;

    ArrayList<String> listviewtmp;

    SimpleAdapter Adapter;
    // T Map GPS
    TMapGpsManager tMapGPS = null;
    EditText keywordView_start;
    EditText keywordView_end;
    double latitude;
    double longitude;
    double startLatitude, startLongitude, destLatitude, destLongitude;
    boolean isStart = false;
    boolean editState = false;
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
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
        //gps 기반으로 위치 제공(에뮬레이터에서씀)
        //tMapGPS.setProvider(tMapGPS.GPS_PROVIDER);

        tMapGPS.OpenGps();
        tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_currenticon);
        tMapView.setIcon(bitmap);

        // 옵션메뉴
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

        //리스트뷰객체
        ListView list = (ListView)findViewById(R.id.SearchListListView);

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
        start_edit.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view , MotionEvent event){

                editState = false;
                isStart = true;
                hide(slidingView,0);
                return true;
            }
        });

        Button backToMain_start = (Button) findViewById(R.id.backToMain_start);
        backToMain_start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                editState = false;
                nonhide(slidingView,0);
                bindList(1);
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
                        list.setVisibility(View.INVISIBLE);
                        int length = searchBar.getText().length();
                        if (length > 0) {
                            searchBar.getText().delete(length -1, length);
                        }
                        break;
                    case KeyEvent.KEYCODE_BACK:
                        list.setVisibility(View.VISIBLE);
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
                editState = false;
                isStart = false;
                hide(slidingView,1);
                return true;
            }
        });
        //searchbar에서 MAIN으로 돌아오는 BACKTO MAIN 객체 생성 & 클릭 이벤트
        Button backToMain_end = (Button) findViewById(R.id.backToMain_end);
        backToMain_end.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                editState = false;
                nonhide(slidingView,2);

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

        Button backToMain = (Button) findViewById(R.id.backToMain);
        backToMain.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                ConstraintLayout navigation = (ConstraintLayout) findViewById(R.id.Navigation);
                navigation.setVisibility(View.GONE);
                editState = false;
                nonhide(slidingView,2);
            }
        });

        //지도 눌렀을때 주소 읽어옴
        tMapView.setOnLongClickListenerCallback(new TMapView.OnLongClickListenerCallback() {

            @Override
            public void onLongPressEvent(ArrayList<TMapMarkerItem> arrayList, ArrayList<TMapPOIItem> arrayList1, TMapPoint point) {
                Log.d("editState : ",String.valueOf(editState));
                Log.d("isStart : ",String.valueOf(isStart));
                if(editState == false)
                    return;
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

//                                Log.d("Start ",editText.toString());
//                                if(editText.getText().toString().equals("") || editText.getText().toString() == null) {
                                if(isStart == true){
                                    Log.d("tag","start");
                                    Log.d("address : ",address);
//                                    EditText editStart = (EditText) findViewById(R.id.edit_start);
                                    EditText searchBarStart = (EditText) findViewById(R.id.searchBar_start);
//                                    editStart.setHint(null);
//                                    editStart.setText(address);
                                    searchBarStart.setText(address);
//                                    Log.d("editStart : ",editStart.getText().toString());
                                    Log.d("searchBarStart : ",searchBarStart.getText().toString());
                                    startLatitude = point.getLatitude();
                                    startLongitude =  point.getLongitude();
                                }
                                else{
                                    Log.d("tag","end");
                                    Log.d("address : ",address);
//                                    EditText editEnd = (EditText) findViewById(R.id.edit_end);
                                    EditText searchBarEnd = (EditText) findViewById(R.id.searchBar_end);
//                                    editEnd.setHint(null);
//                                    editEnd.setText(address);
                                    searchBarEnd.setText(address);
//                                    Log.d("editEnd : ",editEnd.getText().toString());
                                    Log.d("searchBarEnd : ",searchBarEnd.getText().toString());
                                    destLatitude = point.getLatitude();
                                    destLongitude =  point.getLongitude();
                                }
                            }
                        });
            }

        });

        //현재 위치를 시작 주소로
        setStartCurrent();
    }

    private void hide(SlidingUpPanelLayout slidingView,int check){ // 0 : start, 1 : end ,2 : map button
        LinearLayout dragview1 = (LinearLayout)findViewById(R.id.dragview1);
        LinearLayout dragview2 = (LinearLayout)findViewById(R.id.dragview2);
        ListView HistoryListView = (ListView)findViewById(R.id.HistoryListView);
        slidingView.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        Log.d("sliding view : ",String.valueOf(slidingView.getPanelState()));
        ImageButton CurrentLocation = (ImageButton)findViewById(R.id.CurrentLocate);
        ImageButton optionButton = (ImageButton)findViewById(R.id.optionButton);
        FrameLayout searchbarLayoutStart = (FrameLayout) findViewById(R.id.searchbarLayout_start);
        FrameLayout searchbarLayoutEnd = (FrameLayout) findViewById(R.id.searchbarLayout_end);
        ImageView CurrentLocationBackground = (ImageView)findViewById(R.id.CurrentLocateBackground);
        ListView list = (ListView)findViewById(R.id.SearchListListView);
        list.setVisibility(View.VISIBLE);
        CurrentLocationBackground.setVisibility(View.GONE);

        CurrentLocation.setVisibility(View.GONE);
        optionButton.setVisibility(View.GONE);

        if(check == 0) {
            EditText searchBarStart = (EditText) findViewById(R.id.searchBar_start);
            EditText editStart = (EditText) findViewById(R.id.edit_start);
            searchBarStart.setText(editStart.getText().toString());
            searchbarLayoutStart.setVisibility(View.VISIBLE);
            dragview1.setVisibility(View.GONE);
            dragview2.setVisibility(View.GONE);
            HistoryListView.setVisibility(View.GONE);
            slidingView.setPanelHeight(0);
        }
        else {
            EditText searchBarEnd = (EditText) findViewById(R.id.searchBar_end);
            EditText editEnd = (EditText) findViewById(R.id.edit_end);
            searchBarEnd.setText(editEnd.getText().toString());
            searchbarLayoutEnd.setVisibility(View.VISIBLE);
            if(check == 2) {
                Button confirmButton = (Button) findViewById(R.id.confirm_button);
                confirmButton.setVisibility(View.VISIBLE);
            }
            slidingView.setPanelHeight(0);
        }

    }

    private void bindList(int delete) {
        if(delete==1) {
            listviewlist.clear();
            return;
        }
        Adapter = new SimpleAdapter(this,listviewlist,android.R.layout.simple_list_item_2,new String[] {"item1","item2"},new int[] {android.R.id.text1, android.R.id.text2});

        Adapter.notifyDataSetChanged();
        ListView list = (ListView)findViewById(R.id.SearchListListView);
        list.bringToFront();

        list.setAdapter(Adapter);
        list.setVisibility(View.VISIBLE);

    }
    private void nonhide(SlidingUpPanelLayout slidingView,int check){ // 0 : start, 1 : end, 2 : map button

        ListView HistoryListView = (ListView)findViewById(R.id.HistoryListView);
        LinearLayout dragview1 = (LinearLayout)findViewById(R.id.dragview1);
        LinearLayout dragview2 = (LinearLayout)findViewById(R.id.dragview2);
        ImageButton CurrentLocation = (ImageButton)findViewById(R.id.CurrentLocate);
        ImageButton optionButton = (ImageButton)findViewById(R.id.optionButton);
        FrameLayout searchbarLayoutStart = (FrameLayout) findViewById(R.id.searchbarLayout_start);
        FrameLayout searchbarLayoutEnd = (FrameLayout) findViewById(R.id.searchbarLayout_end);
        ImageView CurrentLocationBackground = (ImageView)findViewById(R.id.CurrentLocateBackground);
        ListView list = (ListView)findViewById(R.id.SearchListListView);
        list.setVisibility(View.INVISIBLE);
        CurrentLocationBackground.setVisibility(View.VISIBLE);
        slidingView.setPanelHeight(340);
        CurrentLocation.setVisibility(View.VISIBLE);
        optionButton.setVisibility(View.VISIBLE);

        if(check == 0) {
            EditText searchBarStart = (EditText) findViewById(R.id.searchBar_start);
            EditText editStart = (EditText) findViewById(R.id.edit_start);
            Log.v("searchBarStart : ",searchBarStart.getText().toString());
            editStart.setText(searchBarStart.getText().toString());
            searchbarLayoutStart.setVisibility(View.GONE);
            dragview1.setVisibility(View.VISIBLE);
            dragview2.setVisibility(View.VISIBLE);
            HistoryListView.setVisibility(View.VISIBLE);
        }
        else if(check == 1) {
            EditText searchBarEnd = (EditText) findViewById(R.id.searchBar_end);
            EditText editEnd = (EditText) findViewById(R.id.edit_end);
            editEnd.setText(searchBarEnd.getText().toString());
            searchbarLayoutEnd.setVisibility(View.GONE);
        }
        else{
            TextView searchBarStart = (TextView) findViewById(R.id.searchBar_start2);
            EditText editStart = (EditText) findViewById(R.id.edit_start);
            editStart.setText(searchBarStart.getText().toString());

            TextView searchBarEnd = (TextView) findViewById(R.id.searchBar_end2);
            EditText editEnd = (EditText) findViewById(R.id.edit_end);
            editEnd.setText(searchBarEnd.getText().toString());

            FrameLayout searchbarLayout = (FrameLayout) findViewById(R.id.searchbarLayout);
            searchbarLayout.setVisibility(View.GONE);
        }
        //확인 버튼
        Button confirmButton = (Button)findViewById(R.id.confirm_button);
        confirmButton.setVisibility(View.GONE);
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
        listviewtmp = new ArrayList<>();
        listviewlist.clear();

        tmapdata.findAroundNamePOI(tpoint, keyword, new TMapData.FindAroundNamePOIListenerCallback()
        {
            @Override
            public void onFindAroundNamePOI(final ArrayList<TMapPOIItem> poiItem) {
                if (poiItem == null) return;
                tMapView.removeAllMarkerItem();
                for(int i = 0; i < poiItem.size(); i++) {
                    TMapPOIItem item = poiItem.get(i);
                    for (TMapPOIItem poi : poiItem) {

                        //System.out.println(itemlist.containsValue(item.getPOIName().toString()));
                        if(!listviewtmp.contains(item.getPOIName().toString())) {
                            HashMap<String,String> itemlist = new HashMap<String, String>();

                            System.out.println(item.name);
                            listviewtmp.add(item.getPOIName().toString());
                            itemlist.put("item1", item.getPOIName().toString());
                            itemlist.put("item2", String.valueOf(Math.round(item.getDistance(tpoint)))+"m");

                            listviewlist.add(itemlist);
                        }

                        addMarker(poi);
                    }

                    Collections.sort(listviewlist, new Comparator<HashMap<String, String>>() {
                        @Override
                        public int compare(HashMap<String, String> o1, HashMap<String, String> o2) {
                            String name1 = (String) o1.get("item2");
                            String name2 = (String) o2.get("item2");
                            if(name1.length()>name2.length()) {
                                return name2.compareTo(name1);
                            }
                            else{
                                return name1.compareTo(name2);
                            }
                        }
                    });

                    /*Log.d("POI Name: ", item.getPOIName().toString() + ", " +
                            "Address: " + item.getPOIAddress().replace("null", "")  + ", " +
                            "Point: " + item.getPOIPoint().toString());*/
                }

            }

        });



        bindList(0);

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
                        intent = new Intent(MainActivity.this,FAQActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.menu2:
                        intent = new Intent(MainActivity.this,LoginActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.menu3:
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

    public void confirmClick(View view){
        editState = false;
        FrameLayout searchbarLayoutStart = (FrameLayout) findViewById(R.id.searchbarLayout_start);
        FrameLayout searchbarLayoutEnd = (FrameLayout) findViewById(R.id.searchbarLayout_end);
        FrameLayout searchbarLayout = (FrameLayout) findViewById(R.id.searchbarLayout);
        searchbarLayoutStart.setVisibility(View.GONE);
        searchbarLayoutEnd.setVisibility(View.GONE);

        EditText edit_start= (EditText) findViewById(R.id.edit_start);
        TextView searchBar_start2 = (TextView) findViewById(R.id.searchBar_start2);
        EditText searchBar_end = (EditText) findViewById(R.id.searchBar_end);
        TextView searchBar_end2 = (TextView) findViewById(R.id.searchBar_end2);
        View divider = (View) findViewById(R.id.divider);

        searchBar_start2.setText(edit_start.getText().toString());
        searchBar_end2.setText(searchBar_end.getText().toString());
        searchBar_start2.bringToFront();
        divider.bringToFront();
        searchBar_end2.bringToFront();
        searchbarLayout.bringToFront();


        searchbarLayout.setVisibility(View.VISIBLE);


        ConstraintLayout navigation = (ConstraintLayout) findViewById(R.id.Navigation);
        navigation.setVisibility(View.VISIBLE);
        navigation.bringToFront();
        drawRoute();
    }
    private void drawRoute(){
        TextView searchBar_start = (TextView) findViewById(R.id.searchBar_start2);
        TextView searchBar_end = (TextView) findViewById(R.id.searchBar_end2);
        String startAddress = searchBar_start.getText().toString();
        String destAddress = searchBar_end.getText().toString();
        Log.d("시작 주소 : ",startAddress);
        Log.d("도착 주소 : ",destAddress);
        TMapData tmapdata = new TMapData();
        TMapPoint startpoint = new TMapPoint(startLatitude, startLongitude);
        TMapPoint destpoint = new TMapPoint(destLatitude, destLongitude);
        TMapMarkerItem startItem = new TMapMarkerItem();
        TMapMarkerItem destItem = new TMapMarkerItem();

        startItem.setTMapPoint(startpoint);
        destItem.setTMapPoint(destpoint);
        startItem.setName("출발지");
        destItem.setName("목적지");
        startItem.setVisible(TMapMarkerItem.VISIBLE);
        destItem.setVisible(TMapMarkerItem.VISIBLE);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_current);
        startItem.setIcon(bitmap);
        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ic_icon);
        destItem.setIcon(bitmap);

        TMapView tmapview = new TMapView(this);
        tmapview.addMarkerItem("출발지",startItem);
        tmapview.addMarkerItem("목적지",destItem);
    }
    public void switchClick(View view){
        TextView searchBar_start = (TextView) findViewById(R.id.searchBar_start2);
        TextView searchBar_end = (TextView) findViewById(R.id.searchBar_end2);
        String tmp = searchBar_start.getText().toString();
        searchBar_start.setText(searchBar_end.getText().toString());
        searchBar_end.setText(tmp);
    }

    public void mapClick(View view){
        SlidingUpPanelLayout slidingView = (SlidingUpPanelLayout) findViewById(R.id.slidingView);
        editState = true;
        isStart = false;
        hide(slidingView,2);
        slidingView.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    public void setStartCurrent(){
        TMapPoint tpoint = tMapView.getLocationPoint();
        EditText start_edit = (EditText) findViewById(R.id.edit_start);
        EditText searchBar_start = (EditText) findViewById(R.id.searchBar_start);
        TextView searchBar_start2 = (TextView) findViewById(R.id.searchBar_start2);
        TMapData tmapdata = new TMapData();
        tmapdata.convertGpsToAddress(tpoint.getLatitude(), tpoint.getLongitude(),
                new TMapData.ConvertGPSToAddressListenerCallback() {
                    @Override
                    public void onConvertToGPSToAddress(String strAddress) {
                        start_edit.setText(strAddress);
                        searchBar_start.setText(strAddress);
                        searchBar_start2.setText(strAddress);
                    }
                });
    }
    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
        setStartCurrent();
    }
}