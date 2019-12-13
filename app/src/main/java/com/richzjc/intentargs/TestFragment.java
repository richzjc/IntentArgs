package com.richzjc.intentargs;

import androidx.fragment.app.Fragment;

import com.richzjc.annotation.Parameter;

import java.util.List;

public class TestFragment extends Fragment {

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
    byte[] sarr10;

    @Parameter(name = "rich")
    byte sarr11;

}
