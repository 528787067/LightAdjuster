package com.x8.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.x8.utils.PackInfo;

public class AboutActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ((TextView)this.findViewById(R.id.about_text)).setText(String.format(getString(R.string.about_text), PackInfo.getVersionName(getApplicationContext())));
    }
}
