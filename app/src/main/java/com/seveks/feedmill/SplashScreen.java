package com.seveks.feedmill;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    101);
        } else {
            startMainActivity();
        }

    }

    public void startMainActivity() {
        String pathName = Environment.getExternalStorageDirectory()+"/seveks_splashscreen.jpg";
        File file = new File(pathName);
        if(file.exists()) {
            Drawable d = Drawable.createFromPath(pathName);
            ((ImageView) findViewById(R.id.splashscreen)).setImageDrawable(d);
        } else {
            ((ImageView) findViewById(R.id.splashscreen)).setImageDrawable(getResources().getDrawable(R.drawable.seveks_splashscreen));
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                finish();
            }
        }, 1500);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startMainActivity();
                } else {
                    onBackPressed();
                }
                return;
            }
        }
    }
}
