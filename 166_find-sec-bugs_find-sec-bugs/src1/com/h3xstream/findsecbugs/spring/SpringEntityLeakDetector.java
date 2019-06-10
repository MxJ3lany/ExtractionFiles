/**
 * Find Security Bugs
 * Copyright (c) Philippe Arteau, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.h3xstream.findsecbugs.spring;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.ba.ClassContext;
import org.apache.bcel.Repository;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Detects Persistent Objects leak and mass updation
 *
 * @author Karan Bansal (Github:karanb192)
 */
public class SpringEntityLeakDetector implements Detector {
	private static final String SPRING_ENTITY_LEAK_TYPE = "SPRING_ENTITY_LEAK";
	private static final List<String> REQUEST_MAPPING_ANNOTATION_TYPES = Arrays.asList(
			"Lorg/springframework/web/bind/annotation/RequestMapping;",
			"Lorg/springframework/web/bind/annotation/GetMapping;",
			"Lorg/springframework/web/bind/annotation/PostMapping;",
			"Lorg/springframework/web/bind/annotation/PutMapping;",
			"Lorg/springframework/web/bind/annotation/DeleteMapping;",
			"Lorg/springframework/web/bind/annotation/PatchMapping;");

	private static final List<String> ENTITY_ANNOTATION_TYPES = Arrays.asList(
			"Ljavax/persistence/Entity;",
			"Ljavax/jdo/spi/PersistenceCapable;",
			"Lorg/springframework/data/mongodb/core/mapping/Document;");

	private BugReporter reporter;

	public SpringEntityLeakDetector(BugReporter bugReporter) {
		this.reporter = bugReporter;
	}

	@Override
	public void visitClassContext(ClassContext classContext) {
		JavaClass clazz = classContext.getJavaClass();

		if (hasRequestMapping(clazz)) {
			Method[] methods = clazz.getMethods();
			for (Method m: methods) {
				analyzeMethod(m, classContext);
			}
		}
	}

	private boolean hasRequestMapping(JavaClass clazz) {
		Method[] methods = clazz.getMethods();
		for (Method m: methods) {
			AnnotationEntry[] annotations = m.getAnnotationEntries();
			m.getReturnType();

			for (AnnotationEntry ae: annotations) {
				if (REQUEST_MAPPING_ANNOTATION_TYPES.contains(ae.getAnnotationType())) {
					return true;
				}
			}
		}
		return false;
	}

	private List<String> getAnnotationList(JavaClass javaClass) {
		ArrayList<String> annotations = new ArrayList<>();
		for (AnnotationEntry annotationEntry: javaClass.getAnnotationEntries()) {
			annotations.add(annotationEntry.getAnnotationType());
		}
		try {
			for (JavaClass subclass: javaClass.getSuperClasses()) {
				annotations.addAll(getAnnotationList(subclass));
			}
		} catch (Exception e) {
		}

		return annotations;
	}
	private void analyzeMethod(Method m, ClassContext classContext) {
		JavaClass clazz = classContext.getJavaClass();
		MethodGen methodGen = classContext.getMethodGen(m);

		List<String> annotations = new ArrayList<>();
		List<String> classesToInspect = new ArrayList<>(Arrays.asList(methodGen.getReturnType().toString()));
		for (Type type: methodGen.getArgumentTypes())
			classesToInspect.add(type.toString());


		for (String classToInspect : classesToInspect) {
			try {
				JavaClass javaClass = Repository.lookupClass(classToInspect);
				annotations.addAll(getAnnotationList(javaClass));
			} catch (Exception e) {
			}
		}

		for (String annotation: annotations) {
			if (ENTITY_ANNOTATION_TYPES.contains(annotation)) {
				BugInstance bug = new BugInstance(this, SPRING_ENTITY_LEAK_TYPE, Priorities.NORMAL_PRIORITY);
				bug.addClassAndMethod(clazz, m);
				reporter.reportBug(bug);
				break;
			}
		}
	}

	@Override
	public void report() {

	}
}
