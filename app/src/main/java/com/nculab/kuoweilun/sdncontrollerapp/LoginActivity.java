package com.nculab.kuoweilun.sdncontrollerapp;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nculab.kuoweilun.sdncontrollerapp.controller.ControllerURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private Button button_login;
    private String urlstr = "";
    private TextInputLayout textInputLayout_account;
    private TextInputLayout textInputLayout_password;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_login);
        Bundle bundle = this.getIntent().getExtras();
        urlstr = bundle.getString("urlstr");
        initView();
        setListeners();
    }

    private void initView() {
        button_login = (Button) findViewById(R.id.button_login);
        textInputLayout_account = (TextInputLayout) findViewById(R.id.account);
        textInputLayout_password = (TextInputLayout) findViewById(R.id.password);
    }

    private void setListeners() {
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String account = textInputLayout_account.getEditText().getText().toString();
                        String password = textInputLayout_password.getEditText().getText().toString();

                        textInputLayout_account.setErrorEnabled(false);
                        textInputLayout_password.setErrorEnabled(false);
                        ControllerURLConnection controllerURLConnection = new ControllerURLConnection(urlstr, null);
                        JSONObject input = new JSONObject();
                        try {
                            input.put("username", account);
                            input.put("password", password);
                            JSONArray output = controllerURLConnection.login(input);
                            String ssid = output.getString(0);
                            new AppFile(getApplicationContext()).saveSSID(ssid);
                            Log.d("ssid: ", ssid);
                            finish();
                        } catch (IOException e) {
                            e.printStackTrace();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "登入失敗", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "登入失敗", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

}
