package com.richzjc.comiler;

import com.richzjc.annotation.Parameter;
import com.richzjc.comiler.util.EmptyUtils;
import com.richzjc.comiler.util.Utils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
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
        String methodContent = getMethodContent(element, finalValue);
        messager.printMessage(Diagnostic.Kind.NOTE, "type = " + type + "; typeMIrror  = " + typeMirror.toString());

        // TypeKind 枚举类型不包含String
        if (type == TypeKind.INT.ordinal()) {
            methodContent += "getIntExtra($S, " + finalValue + ")";
        } else if (type == TypeKind.BOOLEAN.ordinal()) {
            methodContent += "getBooleanExtra($S, " + finalValue + ")";
        } else if (type == TypeKind.BYTE.ordinal()) {
            methodContent += "getByte($S, " + finalValue + ")";
        } else if (type == TypeKind.SHORT.ordinal()) {
            methodContent += "getShort($S, " + finalValue + ")";
        } else if (type == TypeKind.LONG.ordinal()) {
            methodContent += "getLongExtra($S, " + finalValue + ")";
        } else if (type == TypeKind.CHAR.ordinal()) {
            methodContent += "getChar($S, " + finalValue + ")";
        } else if (type == TypeKind.FLOAT.ordinal()) {
            methodContent += "getFloat($S, " + finalValue + ")";
        } else if (type == TypeKind.ARRAY.ordinal()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "type = " + type + "; typeMIrror  = " + typeMirror.toString());
            methodContent = parseArray(typeMirror, methodContent);
        } else if (typeMirror.toString().equalsIgnoreCase(Constants.STRING)) {
            methodContent += "getString($S, " + finalValue + ")";
        } else if (typeMirror.toString().equalsIgnoreCase(Constants.CHARSEQUENCE)) {
            methodContent += "getCharSequence($S, " + finalValue + ")";
        } else if (typeMirror.toString().equalsIgnoreCase(Constants.BUNDLE)) {
            methodContent += "getBundle($S)";
        } else if (type == TypeKind.DECLARED.ordinal()) {
            methodContent += getDeclaredContent(element, methodContent);
        }

        methodContent = finalValue + " = " + methodContent;

        // 健壮代码
        if (methodContent.endsWith(")")) {
            // 添加最终拼接方法内容语句
            methodBuidler.addStatement(methodContent, annotationValue);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "目前暂支持类型不包括" + typeMirror.toString());
        }
    }

    private String getDeclaredContent(Element element, String methodContent) {
        TypeMirror typeMirror = element.asType();
        messager.printMessage(Diagnostic.Kind.NOTE, element.getEnclosedElements().toString());
        if (Utils.checkIsList(typeMirror, elementUtils, typeUtils)) {
            messager.printMessage(Diagnostic.Kind.NOTE, element.getEnclosedElements().toString());
        } else if (Utils.checkIsSerializable(typeMirror, elementUtils, typeUtils)) {
            methodContent += "getSerializable($S)";
        }
        return methodContent;
    }

    private String getMethodContent(Element element, String finalValue) {
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

    private String parseArray(TypeMirror typeMirror, String methodContent) {
        if (typeMirror.toString().equalsIgnoreCase(Constants.STRING_ARRAY)) {
            methodContent += "getStringArray($S)";
        } else if (typeMirror.toString().equalsIgnoreCase("int[]")) {
            methodContent += "getIntArray($S)";
        } else if (typeMirror.toString().equalsIgnoreCase("float[]")) {
            methodContent += "getFloatArray($S)";
        } else if (typeMirror.toString().equalsIgnoreCase("char[]")) {
            methodContent += "getCharArray($S)";
        } else if (typeMirror.toString().equalsIgnoreCase("double[]")) {
            methodContent += "getDoubleArray($S)";
        } else if (typeMirror.toString().equalsIgnoreCase("byte[]")) {
            methodContent += "getByteArray($S)";
        } else if (typeMirror.toString().equalsIgnoreCase("short[]")) {
            methodContent += "getShortArray($S)";
        } else if (typeMirror.toString().equalsIgnoreCase("boolean[]")) {
            methodContent += "getBooleanArray($S)";
        } else if (typeMirror.toString().equalsIgnoreCase("java.lang.CharSequence[]")) {
            methodContent += "getCharSequenceArray($S)";
        } else if (typeMirror.toString().equalsIgnoreCase("long[]")) {
            methodContent += "getLongArray($S)";
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
