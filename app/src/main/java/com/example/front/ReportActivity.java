package com.example.front;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportActivity  extends AppCompatActivity {
    private static final String TAG = "WHEEL SAFE";

    Uri uri;
    Button btn_photo,btn_photo_gal;
    ImageView iv_photo;

    final static int TAKE_PICTURE = 1;

    String mCurrentPhotoPath;
    final static int REQUEST_TAKE_PHOTO = 1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        iv_photo = findViewById(R.id.iv_photo);
        btn_photo = findViewById(R.id.btn_photo);
        btn_photo_gal = findViewById(R.id.btn_photo_gal);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "권한 설정 완료");
            }
            else {
                Log.d(TAG, "권한 설정 요청");
                ActivityCompat.requestPermissions(ReportActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_photo:
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, TAKE_PICTURE);
                        break;
                }
            }
        });
        btn_photo_gal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_photo_gal:
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityResult.launch(intent);
                        break;
                }
            }
        });

        Button submitButton = (Button) findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkInput() == false){
                    return;
                }
                reportResponse();
                removeWholeText();
                Intent intent = new Intent(ReportActivity.this, PopupActivity.class);
                startActivity(intent);
            }
        });

    }

    public void reportResponse() {
        EditText titleText = (EditText) findViewById(R.id.place);
        EditText contentText = (EditText) findViewById(R.id.explain);
        EditText addressText = (EditText) findViewById(R.id.address);
        ImageView imgView = (ImageView) findViewById(R.id.iv_photo);
        String title = titleText.getText().toString().trim();
        String content = contentText.getText().toString().trim();
        String address = addressText.getText().toString().trim();
        String image = imgView.toString().trim();

//        File file = new File(imgView);
//        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
//        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), requestFile);
//        RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), idx);
//

        //loginRequest에 사용자가 입력한 email과 pw를 저장
        ReportRequest reportRequest = new ReportRequest(title,content,address,image);

        //retrofit 생성
        RetrofitClient retrofitClient = RetrofitClient.getInstance();
        initReportApi initReportApi = RetrofitClient.getReportInterface();
        Call<String> call = initReportApi.getReportResponse(reportRequest);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d("isSuccessful", String.valueOf(response.isSuccessful()));
                Log.d("body", String.valueOf(response.body()));
                Log.d("response", String.valueOf(response.code()));
                //통신 성공
                if (response.isSuccessful() && response.body() != null) {
                        ;
                }
                else {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
                builder.setTitle("알림")
                        .setMessage("전송 실패.\n")
                        .setPositiveButton("확인", null)
                        .create()
                        .show();
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReportActivity.this);
                t.printStackTrace();
                builder.setTitle("알림")
                        .setMessage("예기치 못한 오류가 발생하였습니다.\n 고객센터에 문의바랍니다.")
                        .setPositiveButton("확인", null)
                        .create()
                        .show();
            }
        });
    }


    public boolean checkInput(){
        EditText placeText = (EditText) findViewById(R.id.place);
        EditText addressText = (EditText) findViewById(R.id.address);
        EditText explainText = (EditText) findViewById(R.id.explain);
        boolean check = true;
        if(placeText.getText().toString().equals("") || placeText.getText().toString() == null){
            placeText.setBackgroundResource(R.drawable.red_edittext);
            check = false;
        }
        if(addressText.getText().toString().equals("") || addressText.getText().toString() == null){
            addressText.setBackgroundResource(R.drawable.red_edittext);
            check = false;
        }
        if(explainText.getText().toString().equals("") || explainText.getText().toString() == null){
            explainText.setBackgroundResource(R.drawable.red_edittext);
            check = false;
        }
        return check;
    }
    public void removeWholeText() {
        EditText placeText = (EditText) findViewById(R.id.place);
        EditText addressText = (EditText) findViewById(R.id.address);
        EditText explainText = (EditText) findViewById(R.id.explain);
        ImageView image = (ImageView) findViewById(R.id.iv_photo);
        placeText.setText(null);
        addressText.setText(null);
        explainText.setText(null);
        image.setImageResource(0);
    }

    ActivityResultLauncher<Intent> startActivityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>(){
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode() == RESULT_OK && result.getData() != null){
                        uri = result.getData().getData();
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                            iv_photo.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult");
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED ) {
            Log.d(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == RESULT_OK && intent.hasExtra("data")) {
                    Bitmap bitmap = (Bitmap) intent.getExtras().get("data");
                    if (bitmap != null) {
                        iv_photo.setImageBitmap(bitmap);
                    }

                }
                break;
        }
    }
}
