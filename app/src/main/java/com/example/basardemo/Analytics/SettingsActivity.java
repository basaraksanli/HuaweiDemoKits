package com.example.basardemo.Analytics;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.basardemo.R;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;

public class SettingsActivity extends AppCompatActivity {
    private Button btnSave;
    private EditText editFavorSport;
    private String strFavorSport;

    //Define a var for Analytics Instance
    HiAnalyticsInstance instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // Genarate Analytics Kit Instance
        instance = HiAnalytics.getInstance(this);

        btnSave = (Button) findViewById(R.id.save_setting_button);
        btnSave.setOnClickListener((View.OnClickListener) view -> {
            editFavorSport = (EditText) findViewById(R.id.edit_favoraite_sport);
            strFavorSport = editFavorSport.getText().toString().trim();
            // save favorite sport by user setUserProperty
            instance.setUserProfile("favor_sport", strFavorSport);
        });
    }
}