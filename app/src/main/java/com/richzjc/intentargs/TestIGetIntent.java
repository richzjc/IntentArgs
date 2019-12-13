package com.richzjc.intentargs;

import android.content.Intent;
import android.os.Bundle;

import com.richzjc.annotation.Parameter;
import com.richzjc.anotation_api.IGetBundle;
import java.util.List;

public class TestIGetIntent implements IGetBundle {

    @Override
    public Bundle getBundle() {
        return null;
    }

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


}
