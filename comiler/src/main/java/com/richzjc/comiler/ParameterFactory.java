package com.richzjc.comiler;

import com.richzjc.annotation.Parameter;
import com.richzjc.comiler.util.EmptyUtils;
import com.richzjc.comiler.util.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public class ParameterFactory {

    private Elements elementUtils;
    private Types typeUtils;
    // MainActivity t = (MainActivity) target;
    private static final String CONTENT = "$T t = ($T)target";

    // 方法体构建
    private MethodSpec.Builder methodBuidler;

    // Messager用来报告错误，警告和其他提示信息
    private Messager messager;

    // 类名，如：MainActivity
    private ClassName className;

    private ParameterFactory(Builder builder) {
        this.messager = builder.messager;
        this.className = builder.className;
        this.typeUtils = builder.typeUtils;
        this.elementUtils = builder.elementUtils;

        // 通过方法参数体构建方法体：public void loadParameter(Object target) {
        methodBuidler = MethodSpec.methodBuilder(Constants.PARAMETER_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(builder.parameterSpec);
    }

    /**
     * 添加方法体内容的第一行（MainActivity t = (MainActivity) target;）
     */
    public void addFirstStatement() {
        // 方法内容：MainActivity t = (MainActivity) target;
        methodBuidler.addStatement(CONTENT, className, className);
    }

    public MethodSpec build() {
        return methodBuidler.build();
    }

    /**
     * 构建方体内容，如：t.s = t.getIntent.getStringExtra("s");
     *
     * @param element 被注解的属性元素
     */
    public void buildStatement(Element element) {
        TypeMirror typeMirror = element.asType();
        int type = typeMirror.getKind().ordinal();
        String fieldName = element.getSimpleName().toString();
        String annotationValue = element.getAnnotation(Parameter.class).name();
        annotationValue = EmptyUtils.isEmpty(annotationValue) ? fieldName : annotationValue;
        String finalValue = "t." + fieldName;
        String methodContent = getMethodContent(element);
        messager.printMessage(Diagnostic.Kind.NOTE, "type = " + type + "; typeMIrror  = " + typeMirror.toString() + ";  methodContent = " + methodContent);

        // TypeKind 枚举类型不包含String
        if (type == TypeKind.INT.ordinal()) {
            methodContent += "getIntExtra($S, " + finalValue + ")";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (type == TypeKind.BOOLEAN.ordinal()) {
            methodContent += "getBooleanExtra($S, " + finalValue + ")";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (type == TypeKind.BYTE.ordinal()) {
            methodContent += "getByte($S, " + finalValue + ")";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (type == TypeKind.SHORT.ordinal()) {
            methodContent += "getShort($S, " + finalValue + ")";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (type == TypeKind.LONG.ordinal()) {
            methodContent += "getLongExtra($S, " + finalValue + ")";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (type == TypeKind.CHAR.ordinal()) {
            methodContent += "getChar($S, " + finalValue + ")";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (type == TypeKind.FLOAT.ordinal()) {
            methodContent += "getFloat($S, " + finalValue + ")";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (type == TypeKind.ARRAY.ordinal()) {
           parseArray(typeMirror, annotationValue, methodContent, finalValue);
        } else if (typeMirror.toString().equalsIgnoreCase(Constants.STRING)) {
            methodContent += "getString($S, " + finalValue + ")";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (typeMirror.toString().equalsIgnoreCase(Constants.CHARSEQUENCE)) {
            methodContent += "getCharSequence($S, " + finalValue + ")";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (typeMirror.toString().equalsIgnoreCase(Constants.BUNDLE)) {
            methodContent += "getBundle($S)";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (type == TypeKind.DECLARED.ordinal()) {
            getDeclaredContent(element, methodContent, annotationValue, finalValue);
        }
    }

    private void addMethod(TypeMirror typeMirror, String annotationValue, String methodContent, String finalValue) {
        methodContent = finalValue + " = " + methodContent;
        // 健壮代码
        if (methodContent.endsWith(")")) {
            // 添加最终拼接方法内容语句
            methodBuidler.addStatement(methodContent, annotationValue);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "目前暂支持类型不包括" + typeMirror.toString());
        }
    }

    private String getDeclaredContent(Element element, String methodContent, String annotationValue, String finalValue) {
        TypeMirror typeMirror = element.asType();
        if (Utils.checkIsList(typeMirror)) {
            parsedList(typeMirror, methodContent, finalValue, annotationValue);
        } else if(Utils.checkIsParcelable(typeMirror, elementUtils, typeUtils)){
            methodContent += "getParcelable($S)";
            TypeElement typeElement = elementUtils.getTypeElement(typeMirror.toString());
            methodContent = "($T)" + methodContent;
            methodContent = finalValue + " = " + methodContent;
            methodBuidler.addStatement(methodContent, ClassName.get(typeElement), annotationValue);
        }else if (Utils.checkIsSerializable(typeMirror, elementUtils, typeUtils)) {
            methodContent += "getSerializable($S)";
            TypeElement typeElement = elementUtils.getTypeElement(typeMirror.toString());
            methodContent = "($T)" + methodContent;
            methodContent = finalValue + " = " + methodContent;
            methodBuidler.addStatement(methodContent, ClassName.get(typeElement), annotationValue);

        }
        return methodContent;
    }

    private void parsedList(TypeMirror mirror, String methodContent, String  finalValue, String annotationValue){
        String value = mirror.toString();
        if(value.contains("<") && value.contains(">")){
            String typeStr = value.substring(value.indexOf("<") + 1, value.lastIndexOf(">"));
            // TypeKind 枚举类型不包含String
            Element element = elementUtils.getTypeElement(typeStr);
            TypeMirror typeMirror = element.asType();
            int type = typeMirror.getKind().ordinal();
            if (type == TypeKind.INT.ordinal() || typeMirror.toString().equalsIgnoreCase("java.lang.Integer")) {
                methodContent += "getIntegerArrayList($S)";
                addMethod(typeMirror, annotationValue, methodContent, finalValue);
            }else if (typeMirror.toString().equalsIgnoreCase(Constants.STRING)) {
                methodContent += "getStringArrayList($S)";
                addMethod(typeMirror, annotationValue, methodContent, finalValue);
            } else if (typeMirror.toString().equalsIgnoreCase(Constants.CHARSEQUENCE)) {
                methodContent += "getCharSequenceArrayList($S)";
                addMethod(typeMirror, annotationValue, methodContent, finalValue);
            } else if (type == TypeKind.DECLARED.ordinal()) {
                methodContent += "getParcelableArrayList($S)";
                methodContent = finalValue + " = " + methodContent;
                methodBuidler.addStatement(methodContent, annotationValue);
            }
        }else{
            methodContent += "getParcelableArrayList($S)";
            methodContent = finalValue + " = " + methodContent;
            methodBuidler.addStatement(methodContent, annotationValue);
        }
    }

    private String getMethodContent(Element element) {
        TypeElement typeElement = (TypeElement) element.getEnclosingElement();
        StringBuilder builder = new StringBuilder();
        builder.append("t.");
        TypeElement activityType = elementUtils.getTypeElement(Constants.ACTIVITY);
        TypeElement fragmentType = elementUtils.getTypeElement(Constants.FRAGMENT);
        TypeElement iGetIntentType = elementUtils.getTypeElement(Constants.IGETINTENT);
        if (!typeUtils.isSubtype(typeElement.asType(), activityType.asType())
                && !typeUtils.isSubtype(typeElement.asType(), fragmentType.asType())
                && !typeUtils.isAssignable(typeElement.asType(), iGetIntentType.asType())) {
            throw new RuntimeException("@Parameter注解目前仅限用于Activity类和Fragment类, 以及实现了IGetIntent接口的类上面");
        }

        if (typeUtils.isSubtype(typeElement.asType(), activityType.asType())) {
            builder.append("getIntent().getExtras().");
        } else if (typeUtils.isSubtype(typeElement.asType(), fragmentType.asType())) {
            builder.append("getArguments().");
        } else if (typeUtils.isAssignable(typeElement.asType(), iGetIntentType.asType())) {
            builder.append("getBundle().");
        }
        return builder.toString();
    }

    private String parseArray(TypeMirror typeMirror, String annotationValue, String methodContent, String finalValue) {
        if (typeMirror.toString().equalsIgnoreCase(Constants.STRING_ARRAY)) {
            methodContent += "getStringArray($S)";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (typeMirror.toString().equalsIgnoreCase("int[]")) {
            methodContent += "getIntArray($S)";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (typeMirror.toString().equalsIgnoreCase("float[]")) {
            methodContent += "getFloatArray($S)";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (typeMirror.toString().equalsIgnoreCase("char[]")) {
            methodContent += "getCharArray($S)";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (typeMirror.toString().equalsIgnoreCase("double[]")) {
            methodContent += "getDoubleArray($S)";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (typeMirror.toString().equalsIgnoreCase("byte[]")) {
            methodContent += "getByteArray($S)";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (typeMirror.toString().equalsIgnoreCase("short[]")) {
            methodContent += "getShortArray($S)";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (typeMirror.toString().equalsIgnoreCase("boolean[]")) {
            methodContent += "getBooleanArray($S)";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (typeMirror.toString().equalsIgnoreCase("java.lang.CharSequence[]")) {
            methodContent += "getCharSequenceArray($S)";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if (typeMirror.toString().equalsIgnoreCase("long[]")) {
            methodContent += "getLongArray($S)";
            addMethod(typeMirror, annotationValue, methodContent, finalValue);
        } else if(Utils.isParcelableArray(typeMirror, elementUtils, typeUtils)){
            methodContent += "getParcelableArray($S)";
            TypeElement typeElement = elementUtils.getTypeElement(Utils.getArrayType(typeMirror));
            methodContent = "($T[])" + methodContent;
            methodContent = finalValue + " = " + methodContent;
            messager.printMessage(Diagnostic.Kind.NOTE, "fdasklfjaklfd>>>" + methodContent);
            methodBuidler.addStatement(methodContent, ClassName.get(typeElement), annotationValue);
        }
        return methodContent;
    }

    public static class Builder {

        // Messager用来报告错误，警告和其他提示信息
        private Messager messager;

        // 类名，如：MainActivity
        private ClassName className;
        private Elements elementUtils;
        private Types typeUtils;

        // 方法参数体
        private ParameterSpec parameterSpec;

        public Builder(ParameterSpec parameterSpec) {
            this.parameterSpec = parameterSpec;
        }

        public Builder setMessager(Messager messager) {
            this.messager = messager;
            return this;
        }

        public Builder setClassName(ClassName className) {
            this.className = className;
            return this;
        }

        public ParameterFactory build() {
            if (parameterSpec == null) {
                throw new IllegalArgumentException("parameterSpec方法参数体为空");
            }

            if (className == null) {
                throw new IllegalArgumentException("方法内容中的className为空");
            }

            if (messager == null) {
                throw new IllegalArgumentException("messager为空，Messager用来报告错误、警告和其他提示信息");
            }

            return new ParameterFactory(this);
        }

        public Builder setElementsUtils(Elements elementUtils) {
            this.elementUtils = elementUtils;
            return this;
        }

        public Builder setTypeUtils(Types typeUtils) {
            this.typeUtils = typeUtils;
            return this;
        }
    }
}
