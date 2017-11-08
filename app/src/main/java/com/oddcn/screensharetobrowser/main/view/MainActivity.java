package com.oddcn.screensharetobrowser.main.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.oddcn.screensharetobrowser.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_fragment_container, new MainFragment())
                .commit();
    }
}
