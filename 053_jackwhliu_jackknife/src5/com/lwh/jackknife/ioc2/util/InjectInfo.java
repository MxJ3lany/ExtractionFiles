package com.lwh.jackknife.ioc2.util;

import com.lwh.jackknife.ioc2.ViewInjector;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

public class InjectInfo {

	public String packageName;
	public String className;
	public String newClassName;

	public InjectInfo(String packageName, String className) {
		this.packageName = packageName;
		this.className = className;
		this.newClassName = className + ViewInjector.SUFFIX;
	}

	public static String getPackageName(ProcessingEnvironment env, Element element) {
		return env.getElementUtils().getPackageOf(element).getQualifiedName().toString();
	}

	public static InjectInfo createInjectInfo(ProcessingEnvironment env, Element element) {
		TypeElement typeElement = (TypeElement) element.getEnclosingElement();
		String packageName = getPackageName(env, element);
		String className = typeElement.getSimpleName().toString();
		return new InjectInfo(packageName, className);
	}
}