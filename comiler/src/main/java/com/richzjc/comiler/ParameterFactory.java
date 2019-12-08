package com.richzjc.comiler;

import com.richzjc.annotation.Parameter;
import com.richzjc.comiler.util.EmptyUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class ParameterFactory {

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
        // 遍历注解的属性节点 生成函数体
        TypeMirror typeMirror = element.asType();
        // 获取 TypeKind 枚举类型的序列号
        int type = typeMirror.getKind().ordinal();
        // 获取属性名
        String fieldName = element.getSimpleName().toString();
        // 获取注解的值
        String annotationValue = element.getAnnotation(Parameter.class).name();
        // 判断注解的值为空的情况下的处理（注解中有name值就用注解值）
        annotationValue = EmptyUtils.isEmpty(annotationValue) ? fieldName : annotationValue;
        // 最终拼接的前缀：
        String finalValue = "t." + fieldName;
        // t.s = t.getIntent().
        String methodContent = finalValue + " = t.getIntent().";
        messager.printMessage(Diagnostic.Kind.NOTE, "如何查看日志" + type);
        // TypeKind 枚举类型不包含String
        if (type == TypeKind.INT.ordinal()) {
            methodContent += "getIntExtra($S, " + finalValue + ")";
        } else if (type == TypeKind.BOOLEAN.ordinal()) {
            methodContent += "getBooleanExtra($S, " + finalValue + ")";
        } else if (type == TypeKind.BYTE.ordinal()) {
            methodContent += "getByteExtra($S, " + finalValue + ")";
        } else if (type == TypeKind.SHORT.ordinal()) {
            methodContent += "getShortExtra($S, " + finalValue + ")";
        } else if (type == TypeKind.LONG.ordinal()) {
            methodContent += "getLongExtra($S, " + finalValue + ")";
        } else if (type == TypeKind.CHAR.ordinal()) {
            methodContent += "getCharExtra($S, " + finalValue + ")";
        } else if (type == TypeKind.FLOAT.ordinal()) {
            methodContent += "getFloatExtra($S, " + finalValue + ")";
        } else if (type == TypeKind.ARRAY.ordinal()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "type = " + type + "; typeMIrror  = " + typeMirror.toString());
            methodContent = parseArray(typeMirror, methodContent);
        } else if (typeMirror.toString().equalsIgnoreCase(Constants.STRING)) {
            methodContent += "getStringExtra($S)";
        }

        // 健壮代码
        if (methodContent.endsWith(")")) {
            // 添加最终拼接方法内容语句
            methodBuidler.addStatement(methodContent, annotationValue);
        } else {
            messager.printMessage(Diagnostic.Kind.ERROR, "目前暂支持类型不包括" + typeMirror.toString());
        }
    }

    private String parseArray(TypeMirror typeMirror, String methodContent) {
        if (typeMirror.toString().equalsIgnoreCase(Constants.STRING_ARRAY)) {
            methodContent += "getStringArrayExtra($S)";
        }else if(typeMirror.toString().equalsIgnoreCase("int[]")){
            methodContent += "getIntArrayExtra($S)";
        }else if(typeMirror.toString().equalsIgnoreCase("float[]")){
            methodContent += "getFloatArrayExtra($S)";
        }else if(typeMirror.toString().equalsIgnoreCase("char[]")){
            methodContent += "getCharArrayExtra($S)";
        }else if(typeMirror.toString().equalsIgnoreCase("double[]")){
            methodContent += "getDoubleArrayExtra($S)";
        }else if(typeMirror.toString().equalsIgnoreCase("byte[]")){
            methodContent += "getByteArrayExtra($S)";
        }else if(typeMirror.toString().equalsIgnoreCase("short[]")){
            methodContent += "getShortArrayExtra($S)";
        }else if(typeMirror.toString().equalsIgnoreCase("boolean[]")){
            methodContent += "getBooleanArrayExtra($S)";
        }
        return methodContent;
    }

    public static class Builder {

        // Messager用来报告错误，警告和其他提示信息
        private Messager messager;

        // 类名，如：MainActivity
        private ClassName className;

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
    }
}
