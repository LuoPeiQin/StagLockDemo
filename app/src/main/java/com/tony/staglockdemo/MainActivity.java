package com.tony.staglockdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Dncp协议测试
    public void btnDncpTest(View view) {
        startActivity(new Intent(this, DncpTestActivity.class));
    }

    // Dscp协议测试
    public void btnDscpTest(View view) {
        startActivity(new Intent(this, DscpTestActivity.class));
    }
}
