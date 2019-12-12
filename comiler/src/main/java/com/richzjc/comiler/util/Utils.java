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

}
