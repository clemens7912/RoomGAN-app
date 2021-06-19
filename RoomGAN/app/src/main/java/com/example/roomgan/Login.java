package com.example.roomgan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class Login extends AppCompatActivity implements Listener{

    private Button enter;
    private TextView register;
    private TextView username;
    private TextView password;
    private TextView errorText;
    private boolean passwordVisible = false;
    private SharedPreferences sharedPreferences;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        getDelegate().applyDayNight();

        sharedPreferences = getSharedPreferences("login",Context.MODE_PRIVATE);
        boolean logged = sharedPreferences.getBoolean("logged", false);
        if(logged){
            GlobalState gb = (GlobalState) this.getApplication();
            gb.setUsername(sharedPreferences.getString("username", ""));
            gb.setPassword(sharedPreferences.getString("password", ""));
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        enter = findViewById(R.id.enter);
        errorText = findViewById(R.id.errorText);

        password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showHidePassword();
                        return true;
                    }
                }
                return false;
            }
        });

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject data = new JSONObject();
                try {
                    data.put("username", username.getText().toString().trim());
                    data.put("password", password.getText().toString().trim());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new ServerConnection().post(Login.this, data, "login", null);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent register = new Intent(Login.this, Register.class);
                startActivity(register);
            }
        });
    }

    private void showHidePassword(){
        if(passwordVisible){
            password.setTransformationMethod(PasswordTransformationMethod.getInstance());

            password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white, 0, R.drawable.ic_visible_white, 0);
            passwordVisible = false;
        } else{
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white, 0, R.drawable.ic_novisible_white, 0);
            passwordVisible = true;
        }
    }

    @Override
    public void receiveMessage(JSONObject data) {
        try {
            switch (data.getString("method")){
                case "login":
                    switch(data.getInt("status")){
                        case 0:
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("logged", true);
                            editor.putString("username", username.getText().toString().trim());
                            editor.putString("password", password.getText().toString().trim());
                            editor.apply();

                            GlobalState state = (GlobalState) Login.this.getApplication();
                            state.setUsername(username.getText().toString().trim());
                            state.setPassword(password.getText().toString().trim());
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                            break;
                        case 1:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    errorText.setText(getResources().getString(R.string.usernameError));
                                    errorText.setVisibility(View.VISIBLE);
                                }
                            });
                            break;
                        case 2:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    errorText.setText(getResources().getString(R.string.incorrectPassword));
                                    errorText.setVisibility(View.VISIBLE);
                                    password.setText("");
                                }
                            });
                            break;
                    }
                    break;
                case "recover":
                    switch (data.getInt("status")){
                        case 0:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Login.this, getResources().getString(R.string.newPassword), Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        case 1:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    errorText.setText(getResources().getString(R.string.usernameError));
                                    errorText.setVisibility(View.VISIBLE);
                                }
                            });
                            break;
                        default:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Login.this, "Internal server error", Toast.LENGTH_LONG).show();
                                }
                            });
                    }
                    break;


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void recoverPassword(View v){
        HashMap<String, String> params = new HashMap<>();
        params.put("username", username.getText().toString().trim());
        new ServerConnection().get(Login.this, "recover", params, null);
    }

}