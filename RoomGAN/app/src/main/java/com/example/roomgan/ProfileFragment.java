package com.example.roomgan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Pattern;

public class ProfileFragment extends Fragment implements Listener{
    private final String REGEXP = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\-\\._$@$!%*?&])([A-Za-z\\d$@$!%*?&]|[^ ]){8,15}$";

    private View view;

    private Context context;
    private Activity activity;

    private String mUsername;
    private String mMail;
    private String mPasswordHash;
    private String mPassword;
    private TextView username;
    private TextView mail;
    private TextView currentPassword;
    private TextView password1;
    private TextView password2;
    private TextView wrongPassword;
    private boolean passwordVisibleC;
    private boolean passwordVisible1;
    private boolean passwordVisible2;
    private int level=0;
    private TextView  uppercaseText,lowercaseText,numberText, specialCharacterText,level_security;
    private CheckBox uppercaseCheck, lowercaseCheck, numberCheck, specialCharacterCheck;

    public static ProfileFragment newInstance(){
        ProfileFragment profileFragment = new ProfileFragment();
        return profileFragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = getActivity();
        context = activity.getApplicationContext();

        mUsername = ((GlobalState) activity.getApplication()).getUsername();
        mPassword = ((GlobalState) activity.getApplication()).getPassword();

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        username = view.findViewById(R.id.username);
        username.setText(mUsername);
        mail = view.findViewById(R.id.mail);
        currentPassword = view.findViewById(R.id.currentPassword);
        wrongPassword = view.findViewById(R.id.wrongPassword);
        password1 = view.findViewById(R.id.password1);
        password2 = view.findViewById(R.id.password2);
        uppercaseText= view.findViewById(R.id.uppercaseText);
        lowercaseText=view.findViewById(R.id.lowercaseText);
        numberText=view.findViewById(R.id.numberText);
        specialCharacterText=view.findViewById(R.id.specialCharacterText);
        uppercaseCheck=view.findViewById(R.id.uppercaseCheck);
        lowercaseCheck=view.findViewById(R.id.lowercaseCheck);
        numberCheck=view.findViewById(R.id.numberCheck);
        specialCharacterCheck=view.findViewById(R.id.specialCharacterCheck);

        currentPassword.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (currentPassword.getRight() - currentPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showHidePassword((TextView)v, "C");
                        return true;
                    }
                }
                return false;
            }
        });

        password1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (password1.getRight() - password1.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showHidePassword((TextView)v, "1");
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
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (password2.getRight() - password2.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        showHidePassword((TextView)v, "2");
                        return true;
                    }
                }
                return false;
            }
        });

        view.findViewById(R.id.deleteAccountButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount(v);
            }
        });

        view.findViewById(R.id.changePasswordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout changePasswordLayout = view.findViewById(R.id.changePasswordLayout);
                int visibility = changePasswordLayout.getVisibility();
                visibility = visibility==View.VISIBLE?View.GONE:View.VISIBLE;
                currentPassword.setText("");
                password1.setText("");
                password2.setText("");
                changePasswordLayout.setVisibility(visibility);
            }
        });

        view.findViewById(R.id.updatePasswordButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword(v);
            }
        });

        getProfileInfo();

        return view;

    }

    private void getProfileInfo(){
        new ServerConnection().get(this, "profile", null,
                ((GlobalState) activity.getApplication()).getToken());
    }


    @Override
    public void receiveMessage(JSONObject data) {
        try {
            switch (data.getString("method")){
                case "profileInfo":
                    switch (data.getInt("status")){
                        case 0:
                            JSONObject profileInfo = data.getJSONObject("data");
                            mMail = profileInfo.getString("mail");
                            mPasswordHash = profileInfo.getString("password");
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mail.setText(mMail);
                                }
                            });
                            break;
                    }
                    break;
                case "updatePassword":
                    switch (data.getInt("status")){
                        case 0:
                            Log.i("prueba", "Dentro de status 0");
                            ((GlobalState)activity.getApplication()).setPassword(mPassword);
                            SharedPreferences sharedPreferences = activity.getSharedPreferences("login", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("password", mPassword);
                            editor.apply();
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, getResources().getString(R.string.updatedPassword), Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                        case 1:
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, "Server internal error. The password has not beed updated",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                    }
                    break;
                case "delete":
                    switch (data.getInt("status")){
                        case 0:
                            new Thread(new Runnable() {
                                private Activity activity;
                                private ProfileFragment profileFragment;

                                public Runnable init(Activity activity, ProfileFragment profileFragment){
                                    this.activity = activity;
                                    this.profileFragment = profileFragment;
                                    return this;
                                }

                                @Override
                                public void run() {
                                    SharedPreferences sharedPreferences = activity.getSharedPreferences("login", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.clear();
                                    editor.commit();
                                    profileFragment.openLogin();
                                }
                            }.init(activity, ProfileFragment.this)).start();

                            break;
                        case 1:
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, "Server internal error. The account has not been deleted",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                            break;
                    }
                    break;


            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void openLogin(){
        startActivity(new Intent(activity, Login.class));
        activity.finishAffinity();
        activity.finish();
    }

    private void showHidePassword(TextView password, String passwordTag){
        boolean visible = false;
        switch (passwordTag){
            case "C":
                visible = passwordVisibleC;
                passwordVisibleC = !passwordVisibleC;
                break;
            case "1":
                visible = passwordVisible1;
                passwordVisible1 = !passwordVisible1;
                break;
            case "2":
                visible = passwordVisible2;
                passwordVisible2 = !passwordVisible2;
                break;
        }

        if(visible){
            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
            password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white, 0, R.drawable.ic_visible_white, 0);
        } else{
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            password.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_white, 0, R.drawable.ic_novisible_white, 0);
        }
    }



    public void updatePassword(View v){
        if(currentPassword.getText().toString().equals(mPassword)){
            if(password1.getText().toString().equals(password2.getText().toString())){
                if(level >= 3){
                    mPassword = password1.getText().toString();
                    JSONObject data = new JSONObject();
                    try {
                        data.put("username", mUsername);
                        data.put("password", mPassword);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    new ServerConnection().post(ProfileFragment.this, data, "update/password",
                            ((GlobalState)activity.getApplication()).getToken());
                    view.findViewById(R.id.changePasswordLayout).setVisibility(View.GONE);
                    currentPassword.setText("");
                    password1.setText("");
                    password2.setText("");
                    wrongPassword.setVisibility(View.GONE);
                }else{
                    wrongPassword.setText(getResources().getString(R.string.passwordSecurity));
                    wrongPassword.setVisibility(View.VISIBLE);
                }
            } else {
                wrongPassword.setText(getResources().getString(R.string.passwordDifferent));
                wrongPassword.setVisibility(View.VISIBLE);
            }
        }else {
            wrongPassword.setText(activity.getResources().getString(R.string.wrongCurrentPassword));
            wrongPassword.setVisibility(View.VISIBLE);
        }
    }

    public void deleteAccount(View v){
        new AlertDialog.Builder(activity).setTitle(getResources().getString(R.string.deleteAccount))
                .setMessage(getResources().getString(R.string.alertDeleteMessage))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new ServerConnection().get(ProfileFragment.this, "delete", null,
                                ((GlobalState) activity.getApplication()).getToken());
                    }
                }).setNegativeButton(android.R.string.cancel,null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}