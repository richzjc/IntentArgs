package com.richzjc.comiler;

import com.google.auto.service.AutoService;
import com.richzjc.annotation.Parameter;
import com.richzjc.comiler.util.EmptyUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class ParameterProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Messager messager;
    private Filer filer;
    private Types typeUtils;

    // 临时map存储，用来存放被@Parameter注解的属性集合，生成类文件时遍历
    // key:类节点, value:被@Parameter注解的属性集合
    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(Parameter.class.getName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!EmptyUtils.isEmpty(annotations)) {
            try {
                Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Parameter.class);
                valueOfParameterMap(elements);
                createParameterFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }


    /**
     * 赋值临时map存储，用来存放被@Parameter注解的属性集合，生成类文件时遍历
     *
     * @param elements 被 @Parameter 注解的 元素集合
     */
    private void valueOfParameterMap(Set<? extends Element> elements) {
        for (Element element : elements) {
            // 注解在属性之上，属性节点父节点是类节点
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            // 如果map集合中的key：类节点存在，直接添加属性
            if (tempParameterMap.containsKey(enclosingElement)) {
                tempParameterMap.get(enclosingElement).add(element);
            } else {
                List<Element> fields = new ArrayList<>();
                fields.add(element);
                tempParameterMap.put(enclosingElement, fields);
            }
        }
    }


    private void createParameterFile() throws IOException {
        // 判断是否有需要生成的类文件
        if (EmptyUtils.isEmpty(tempParameterMap)) return;
        // 通过Element工具类，获取Parameter类型
        TypeElement activityType = elementUtils.getTypeElement(Constants.ACTIVITY);
        TypeElement fragmentType = elementUtils.getTypeElement(Constants.FRAGMENT);
        TypeElement iGetIntentType = elementUtils.getTypeElement(Constants.IGETINTENT);
        TypeElement parameterType = elementUtils.getTypeElement(Constants.PARAMETER_LOAD);

        // 参数体配置(Object target)
        ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, Constants.PARAMETER_NAMR).build();
        for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {
            // Map集合中的key是类名，如：MainActivity
            TypeElement typeElement = entry.getKey();
            // 如果类名的类型和Activity类型不匹配
            if (!typeUtils.isSubtype(typeElement.asType(), activityType.asType())
                    && !typeUtils.isSubtype(typeElement.asType(), fragmentType.asType())
                    && !typeUtils.isAssignable(typeElement.asType(), iGetIntentType.asType())) {
                throw new RuntimeException("@Parameter注解目前仅限用于Activity类和Fragment类, 以及实现了IGetIntent接口的类上面");
            }

            // 获取类名
            ClassName className = ClassName.get(typeElement);
            // 方法体内容构建
            ParameterFactory factory = new ParameterFactory.Builder(parameterSpec)
                    .setMessager(messager)
                    .setElementsUtils(elementUtils)
                    .setTypeUtils(typeUtils)
                    .setClassName(className)
                    .build();

            // 添加方法体内容的第一行
            factory.addFirstStatement();

            // 遍历类里面所有属性
            for (Element fieldElement : entry.getValue()) {
                factory.buildStatement(fieldElement);
            }

            // 最终生成的类文件名（类名$$Parameter）
            String finalClassName = typeElement.getSimpleName() + Constants.PARAMETER_FILE_NAME;
            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成获取参数类文件：" +
                    className.packageName() + "." + finalClassName);

            // MainActivity$$Parameter
            JavaFile.builder(className.packageName(), // 包名
                    TypeSpec.classBuilder(finalClassName) // 类名
                            .addSuperinterface(ClassName.get(parameterType)) // 实现ParameterLoad接口
                            .addModifiers(Modifier.PUBLIC) // public修饰符
                            .addMethod(factory.build()) // 方法的构建（方法参数 + 方法体）
                            .build()) // 类构建完成
                    .build() // JavaFile构建完成
                    .writeTo(filer); // 文件生成器开始生成类文件
        }
    }

}
