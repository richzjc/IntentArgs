package com.richzjc.intentargs;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.richzjc.annotation.Parameter;
import com.richzjc.anotation_api.manager.ParameterManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Parameter(name = "rich")
    ArrayList<String> sarr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        ParameterManager.getInstance().loadParameter(this);
    }
}
