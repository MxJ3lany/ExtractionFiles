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

package com.lwh.jackknife.multiproxy.apt;

import com.google.auto.service.AutoService;
import com.lwh.jackknife.multiproxy.annotation.handler.AnnotationHandler;
import com.lwh.jackknife.multiproxy.annotation.handler.DifferenceHandler;
import com.lwh.jackknife.multiproxy.annotation.handler.WrapperHandler;
import com.lwh.jackknife.multiproxy.writer.JavaWriter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

@AutoService(Processor.class)
@SupportedAnnotationTypes(
        {
                "com.lwh.jackknife.multiproxy.annotation.Difference",
                "com.lwh.jackknife.multiproxy.annotation.Wrapper"
        })
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class DecorateProcessor extends AbstractProcessor {

    private List<AnnotationHandler> mHandlers = new ArrayList<>();
    private JavaWriter mWriter;
    private Map<String, List<Element>> mElementsMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        registerHandler(new DifferenceHandler());
        registerHandler(new WrapperHandler());
        mWriter = new JavaWriter(processingEnv);
    }

    protected void registerHandler(AnnotationHandler handler) {
        mHandlers.add(handler);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (AnnotationHandler handler : mHandlers) {
            handler.attachProcessingEnvironment(processingEnv);
            mElementsMap.putAll(handler.handleAnnotation(roundEnv));
        }
        mWriter.generate(mElementsMap);
        return true;    //处理完成了，return true就好
    }
}