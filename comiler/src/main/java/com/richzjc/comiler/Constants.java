package com.richzjc.comiler;

public class Constants {
    // Activity全类名
    public static final String ACTIVITY = "android.app.Activity";
    public static final String FRAGMENT = "androidx.fragment.app.Fragment";
    public static final String IGETINTENT = "com.richzjc.anotation_api.IGetBundle";
    static final String BASE_PACKAGE = "com.richzjc.anotation_api";
    // 获取参数，加载接口
    public static final String PARAMETER_LOAD = BASE_PACKAGE + ".ParameterLoad";

    // 获取参数，方法名
    public static final String PARAMETER_NAMR = "target";
    // 获取参数，参数名
    public static final String PARAMETER_METHOD_NAME = "loadParameter";

    // APT生成的获取参数类文件名
    public static final String PARAMETER_FILE_NAME = "$$Parameter";

    // String全类名
    public static final String STRING = "java.lang.String";
    public static final String CHARSEQUENCE = "java.lang.CharSequence";
    public static final String BUNDLE = "android.os.Bundle";
    public static final String STRING_ARRAY = "java.lang.String[]";

}
