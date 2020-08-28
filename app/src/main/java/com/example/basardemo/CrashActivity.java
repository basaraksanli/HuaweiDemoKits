package com.example.basardemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.huawei.agconnect.crash.AGConnectCrash;

public class CrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        Button btn_crash = findViewById(R.id.btn_crash);
        btn_crash.setOnClickListener(view -> AGConnectCrash.getInstance().testIt(CrashActivity.this));

        findViewById(R.id.enable_crash_off).setOnClickListener(v -> AGConnectCrash.getInstance().enableCrashCollection(false));

        findViewById(R.id.enable_crash_on).setOnClickListener(v -> AGConnectCrash.getInstance().enableCrashCollection(true));

        findViewById(R.id.CustomReport).setOnClickListener(v -> {
            AGConnectCrash.getInstance().setUserId("testuser");
            AGConnectCrash.getInstance().log(Log.DEBUG, "set debug log.");
            AGConnectCrash.getInstance().log(Log.INFO, "set info log.");
            AGConnectCrash.getInstance().log(Log.WARN, "set warning log.");
            AGConnectCrash.getInstance().log(Log.ERROR, "set error log.");
            AGConnectCrash.getInstance().setCustomKey("stringKey", "Hello world");
            AGConnectCrash.getInstance().setCustomKey("booleanKey", false);
            AGConnectCrash.getInstance().setCustomKey("doubleKey", 1.1);
            AGConnectCrash.getInstance().setCustomKey("floatKey", 1.1f);
            AGConnectCrash.getInstance().setCustomKey("intKey", 0);
            AGConnectCrash.getInstance().setCustomKey("longKey", 11L);

        });


    }
}