/*
 * Copyright (C) 2019 The JackKnife Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lwh.jackknife.multiproxy.writer;

import com.lwh.jackknife.multiproxy.annotation.Difference;
import com.lwh.jackknife.multiproxy.annotation.DifferenceInterface;
import com.lwh.jackknife.multiproxy.annotation.Proxy;
import com.lwh.jackknife.multiproxy.annotation.Wrapper;
import com.lwh.jackknife.multiproxy.interfaces.DecoratorFactory;
import com.lwh.jackknife.multiproxy.interfaces.IDifference;
import com.lwh.jackknife.multiproxy.MultiProxy;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class JavaWriter implements AbstractWriter {

    private ProcessingEnvironment mProcessingEnv;

    private Messager mMessager;

    private Filer mFiler;

    public JavaWriter(ProcessingEnvironment env) {
        this.mProcessingEnv = env;
        this.mMessager = env.getMessager();
        this.mFiler = mProcessingEnv.getFiler();
    }

    @Override
    public void generate(Map<String, List<Element>> map) {
        for (Map.Entry<String, List<Element>> entry : map.entrySet()) {
            List<Element> elements = entry.getValue();
            for (Element element : elements) {
                Difference difference = element.getAnnotation(Difference.class);
                Wrapper wrapper = element.getAnnotation(Wrapper.class);
                if (difference != null) {
                    handleAnnotation(difference, element);
                }
                if (wrapper != null) {
                    handleAnnotation(wrapper, element);
                }
            }
        }
    }

    private void handleAnnotation(Difference difference, Element element) {
        String proxyName = difference.proxyName();
        TypeElement typeElement = (TypeElement) element;
        TypeVariableName c = TypeVariableName.get("C", IDifference.class);
        TypeVariableName d = TypeVariableName.get("D", IDifference.class);
        MethodSpec.Builder newDecoratorMtdBuilder = MethodSpec.methodBuilder("newDecorator");
        newDecoratorMtdBuilder.addModifiers(Modifier.PUBLIC);
        newDecoratorMtdBuilder.addTypeVariable(c);
        newDecoratorMtdBuilder.addTypeVariable(d);
        newDecoratorMtdBuilder.addParameter(c, "component");
        newDecoratorMtdBuilder.addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), c), "componentClazz");
        MethodSpec.Builder getDecoratorClassMtdBuilder = MethodSpec.methodBuilder("getDecoratorClass");
        getDecoratorClassMtdBuilder.addModifiers(Modifier.PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(IDifference.class)));
        boolean needReturnNull = false;
        List<? extends AnnotationMirror> annotationMirrors = typeElement.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            Element ae = annotationType.asElement();
            Proxy proxy = ae.getAnnotation(Proxy.class);
            String s = ae.getSimpleName().toString();
            if (proxy == null) {
                continue;
            }
            if (proxyName.equalsIgnoreCase(s)) {
                List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
                for (TypeMirror typeMirror : interfaces) {
                    Types types = mProcessingEnv.getTypeUtils();
                    Element e = types.asElement(typeMirror);
                    DifferenceInterface differenceInterface = e.getAnnotation(DifferenceInterface.class);
                    if (differenceInterface != null) {
                        newDecoratorMtdBuilder.addCode("try {\n  $T constructor = getDecoratorClass().getConstructor(componentClazz);\n", Constructor.class);
                        newDecoratorMtdBuilder.addStatement("  constructor.setAccessible(true)");
                        newDecoratorMtdBuilder.addStatement("  return (D) constructor.newInstance(component)");
                        newDecoratorMtdBuilder.addCode("} catch($T e) {\n  e.printStackTrace();\n}\n", Exception.class);
                        getDecoratorClassMtdBuilder.addStatement("return $T.class", ClassName
                                .bestGuess(differenceInterface.packageName()+"."+differenceInterface.moduleName() + s));
                        needReturnNull = true;
                    }
                }
            }
        }
        if (!needReturnNull) {
            getDecoratorClassMtdBuilder.addStatement("return null");
        }
        newDecoratorMtdBuilder.addStatement("return null");
        newDecoratorMtdBuilder.returns(d);
        String packageName = MultiProxy.getPackageName(mProcessingEnv, element);
        String className = typeElement.getSimpleName().toString();
        className += "$Factory";
        TypeSpec typeSpec = TypeSpec.classBuilder(className)
                .addSuperinterface(DecoratorFactory.class)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(newDecoratorMtdBuilder.build())
                .addMethod(getDecoratorClassMtdBuilder.build())
                .build();
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                .addFileComment("These codes are generated by JackKnife automatically. Do not modify!")
                .build();
        try {
            javaFile.writeTo(mProcessingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAnnotation(Wrapper wrapper, Element element) {
        String proxyName = wrapper.proxyName();
        Types types = mProcessingEnv.getTypeUtils();
        TypeElement typeElement = (TypeElement) element;
        List<? extends AnnotationMirror> annotationMirrors = typeElement.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            Element ae = annotationType.asElement();
            Proxy proxy = ae.getAnnotation(Proxy.class);
            if (proxy == null) {
                continue;
            }
            List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
            for (TypeMirror typeMirror:interfaces) {
                Element interfaceElement = types.asElement(typeMirror);
                DifferenceInterface differenceInterface = interfaceElement.getAnnotation(DifferenceInterface.class);
                if (differenceInterface != null) {
                    String packageName = differenceInterface.packageName();
                    String moduleName = differenceInterface.moduleName();
                    String s = ae.getSimpleName().toString();
                    if (s.equalsIgnoreCase(proxyName)) {
                        MethodSpec methodSpec = MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(ClassName.bestGuess(interfaceElement.toString()), "base")
                                .addStatement("super(base)")
                                .build();
                        TypeSpec typeSpec = TypeSpec.classBuilder(moduleName + s)
                                .addModifiers(Modifier.PUBLIC)
                                .superclass(ClassName.bestGuess(typeElement.toString()))
                                .addMethod(methodSpec)
                                .build();
                        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
                                .addFileComment("These codes are generated by JackKnife automatically. Do not modify!")
                                .build();
                        try {
                            javaFile.writeTo(mProcessingEnv.getFiler());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
