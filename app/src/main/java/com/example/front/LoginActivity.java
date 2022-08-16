package com.example.front;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText edt1, edt2;
    Button btn1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edt1 = findViewById(R.id.edt1);
        edt2 = findViewById(R.id.edt2);
        btn1 = findViewById(R.id.btn1);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edt1.getText().toString();
                String password = edt2.getText().toString();
                if(name.equals("") || password.equals("")){
                    Toast.makeText(getApplicationContext(),"사용자 이름과 비밀번호를 입력하세요",Toast.LENGTH_SHORT).show();
                }
                else{
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    intent.putExtra("name",name);
                    startActivityForResult(intent,1);
                }
            }
        });
    }
}
