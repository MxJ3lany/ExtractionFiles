/*
 * Copyright (C) 2018 The JackKnife Open Source Project
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

package com.lwh.jackknife.ioc2.writer;

import com.lwh.jackknife.ioc2.match.ActivityMatcher;
import com.lwh.jackknife.ioc2.util.Coder;
import com.lwh.jackknife.ioc2.match.FragmentMatcher;
import com.lwh.jackknife.ioc2.util.IOCCoder;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class JavaWriter implements AdapterWriter {
	
	protected ProcessingEnvironment mProcessingEnv;
	protected Filer mFiler;
	
	public JavaWriter(ProcessingEnvironment env) {
		this.mProcessingEnv = env;
		this.mFiler = env.getFiler();
	}

	@Override
	public void generate(Map<String, List<Element>> map) {
		Iterator<Entry<String, List<Element>>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, List<Element>> entry = iterator.next();
			List<Element> cacheElements = entry.getValue();
			if (cacheElements == null || cacheElements.size() == 0) {
				continue;
			}
			for (Element element : cacheElements) {
				TypeElement typeElement = (TypeElement) element.getEnclosingElement();
				TypeMirror typeMirror = typeElement.getSuperclass();
				String className = typeMirror.toString();
				Coder coder = new IOCCoder(className);
				coder.code(new ActivityMatcher(mProcessingEnv, mFiler), element);
				coder.code(new FragmentMatcher(mProcessingEnv, mFiler), element);
			}
		}
	}
}