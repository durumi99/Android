package com.example.front;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText email, pass;
    Button submit;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        submit = findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = email.getText().toString();
                String password = pass.getText().toString();
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
        //toggle
        TextView signUp = (TextView) findViewById(R.id.signup);
        TextView logIn = (TextView) findViewById(R.id.login);
        Button submit = (Button) findViewById(R.id.submit);
        View signUpUnderLine = (View) findViewById(R.id.signupunderline);
        View logInUnderLine = (View) findViewById(R.id.loginunderline);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp.setTextColor(getResources().getColorStateList(R.color.black));
                logIn.setTextColor(getResources().getColorStateList(R.color.gray));
                submit.setText("Sign Up");
                signUpUnderLine.setVisibility(View.VISIBLE);
                logInUnderLine.setVisibility(View.GONE);
            }
        });
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp.setTextColor(getResources().getColorStateList(R.color.gray));
                logIn.setTextColor(getResources().getColorStateList(R.color.black));
                submit.setText("Next");
                signUpUnderLine.setVisibility(View.GONE);
                logInUnderLine.setVisibility(View.VISIBLE);
            }
        });
    }

}
