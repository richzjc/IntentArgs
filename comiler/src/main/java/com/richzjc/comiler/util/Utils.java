package com.richzjc.comiler.util;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class Utils {
    public static boolean checkIsSerializable(TypeMirror mirror, Elements elementUtils, Types typeUtils){
        TypeElement activityType = elementUtils.getTypeElement("java.io.Serializable");
        return typeUtils.isSubtype(mirror, activityType.asType());
    }

    public static boolean checkIsParcelable(TypeMirror mirror, Elements elementUtils, Types typeUtils){
        TypeElement activityType = elementUtils.getTypeElement("android.os.Parcelable");
        return typeUtils.isSubtype(mirror, activityType.asType());
    }

    public static boolean checkIsList(TypeMirror mirror){
        String typeMirrorValue = mirror.toString();
        return typeMirrorValue.startsWith("java.util.List") || typeMirrorValue.startsWith("java.util.ArrayList");
    }

    public static boolean isParcelableArray(TypeMirror typeMirror, Elements elementUtils, Types typeUtils) {
        TypeElement activityType = elementUtils.getTypeElement("android.os.Parcelable");
        String typeMirrorValue = typeMirror.toString();
        if(typeMirrorValue.contains("[")){
            int index = typeMirrorValue.indexOf("[");
            String typeValue = typeMirrorValue.substring(0, index);
            TypeElement typeElement = elementUtils.getTypeElement(typeValue);
            return typeUtils.isSubtype(typeElement.asType(), activityType.asType());
        }else{
            return false;
        }
    }

    public static String getArrayType(TypeMirror typeMirror) {
        String typeMirrorValue = typeMirror.toString();
        if(typeMirrorValue.contains("[")){
            int index = typeMirrorValue.indexOf("[");
            String typeValue = typeMirrorValue.substring(0, index);
            return typeValue;
        }else{
            return "";
        }
    }
}
