package com.example.roomgan;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.regex.Pattern;

public class Register extends AppCompatActivity implements Listener{

    private final String PASSWORDTEXT = "Your password is not accomplishing enough conditions ";
    private final String REGEXP = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\-\\._$@$!%*?&])([A-Za-z\\d$@$!%*?&]|[^ ]){8,15}$";
    private TextView username;
    private TextView mail;
    private TextView password1;
    private TextView password2;
    private TextView alertText;
    private Button registerButton;
    private boolean password1Visible = false;
    private boolean password2Visible = false;
    private int level=0;
    private TextView  uppercaseText,lowercaseText,numberText, specialCharacterText,level_security;
    private CheckBox uppercaseCheck, lowercaseCheck, numberCheck, specialCharacterCheck;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        username = findViewById(R.id.registerUsername);
        mail = findViewById(R.id.registerMail);
        password1 = findViewById(R.id.registerPassword);
        password2 = findViewById(R.id.registerpassword2);
        alertText = findViewById(R.id.wrongPassword);
        registerButton = findViewById(R.id.registerButton);
        uppercaseText= findViewById(R.id.uppercaseText);
        lowercaseText=findViewById(R.id.lowercaseText);
        numberText=findViewById(R.id.numberText);
        specialCharacterText=findViewById(R.id.specialCharacterText);
        uppercaseCheck=findViewById(R.id.uppercaseCheck);
        lowercaseCheck=findViewById(R.id.lowercaseCheck);
        numberCheck=findViewById(R.id.numberCheck);
        specialCharacterCheck=findViewById(R.id.specialCharacterCheck);
        password1.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (password1.getRight() - password1.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showHidePassword1();
                        return true;
                    }
                }

                return false;
            }
        });

        password1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                level=0;
                if(password1.getText().toString().matches(".*[a-z].*")){
                    lowercaseText.setTextColor(Color.parseColor("#008000"));
                    lowercaseCheck.setChecked(true);
                    level++;
                }else{
                    lowercaseText.setTextColor(Color.parseColor("#000000"));
                    lowercaseCheck.setChecked(false);
                }
                if(password1.getText().toString().matches(".*[A-Z].*")) {
                    uppercaseText.setTextColor(Color.parseColor("#008000"));
                    uppercaseCheck.setChecked(true);
                    level++;
                }else{
                    uppercaseText.setTextColor(Color.parseColor("#000000"));
                    uppercaseCheck.setChecked(false);
                }
                if(password1.getText().toString().matches(".*\\d.*")) {
                    numberText.setTextColor(Color.parseColor("#008000"));
                    numberCheck.setChecked(true);
                    level++;
                }else{
                    numberText.setTextColor(Color.parseColor("#000000"));
                    numberCheck.setChecked(false);
                }
                if(password1.getText().toString().matches(".*[\\-\\._$@$!%*?&].*")) {
                    specialCharacterText.setTextColor(Color.parseColor("#008000"));
                    specialCharacterCheck.setChecked(true);
                    level++;
                }else{
                    specialCharacterText.setTextColor(Color.parseColor("#000000"));
                    specialCharacterCheck.setChecked(false);
                }

            }
        });

        password2.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (password2.getRight() - password2.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showHidePassword2();
                        return true;
                    }
                }
                return false;
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });


    }

    private void register(){
        if(password1.getText().toString().trim().equals(password2.getText().toString().trim())){
            if(level >= 3){

                JSONObject data = new JSONObject();
                try {
                    data.put("username", username.getText().toString().trim());
                    data.put("mail", mail.getText().toString().trim());
                    data.put("password", password1.getText().toString().trim());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                new ServerConnection().post(Register.this, data, "/register", null);

                //Toast.makeText(this, "Verification email sended", Toast.LENGTH_LONG).show();

            }else{
                alertText.setText(getResources().getString(R.string.passwordSecurity));
                alertText.setVisibility(View.VISIBLE);
            }

        } else{
            password1.setText("");
            password2.setText("");
            alertText.setText(getResources().getString(R.string.passwordDifferent));
            alertText.setVisibility(View.VISIBLE);
        }
    }

    private void showHidePassword1(){
        if(password1Visible){
            password1.setTransformationMethod(PasswordTransformationMethod.getInstance());

            password1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white, 0, R.drawable.ic_visible_white, 0);
            password1Visible = false;
        } else{
            password1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            password1.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white, 0, R.drawable.ic_novisible_white, 0);
            password1Visible = true;
        }
    }

    private void showHidePassword2(){
        if(password2Visible){
            password2.setTransformationMethod(PasswordTransformationMethod.getInstance());

            password2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white, 0, R.drawable.ic_visible_white, 0);
            password2Visible = false;
        } else{
            password2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            password2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white, 0, R.drawable.ic_novisible_white, 0);
            password2Visible = true;
        }
    }

    @Override
    public void receiveMessage(JSONObject data) {
        try{
            switch (data.getInt("status")){
                case 0:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Register.this, getResources().getString(R.string.verificationEmail), Toast.LENGTH_LONG).show();
                            onBackPressed();
                        }
                    });

                    break;
                case 1:
                    alertText.setText(getResources().getString(R.string.usernameUsed));
                    alertText.setVisibility(View.VISIBLE);
                    break;

            }
        } catch (JSONException e){
            e.printStackTrace();
        }

    }
}