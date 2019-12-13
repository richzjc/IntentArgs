package com.richzjc.intentargs;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.richzjc.annotation.Parameter;
import com.richzjc.anotation_api.manager.ParameterManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Parameter(name = "rich")
    List<TestEntity> sarr;

    @Parameter(name = "rich")
    List<String> sarr1;

    @Parameter(name = "rich")
    List<Integer> sarr2;

    @Parameter(name = "rich")
    List<CharSequence> sarr3;

    @Parameter(name = "rich")
    TestEntity[] sarr4;

    @Parameter(name = "rich")
    String[] sarr5;

    @Parameter(name = "rich")
    boolean[] sarr6;

    @Parameter(name = "rich")
    int[] sarr7;

    @Parameter(name = "rich")
    short[] sarr8;

    @Parameter(name = "rich")
    float[] sarr9;

    @Parameter(name = "rich")
    String sarr10;

    @Parameter(name = "rich")
    int sarr11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        ParameterManager.getInstance().loadParameter(this);
    }
}
