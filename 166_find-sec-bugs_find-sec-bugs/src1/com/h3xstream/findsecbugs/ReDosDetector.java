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
package com.h3xstream.findsecbugs;

import com.h3xstream.findsecbugs.common.StackUtils;
import com.h3xstream.findsecbugs.common.matcher.InvokeMatcherBuilder;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.OpcodeStack;
import edu.umd.cs.findbugs.Priorities;
import edu.umd.cs.findbugs.bcel.OpcodeStackDetector;
import edu.umd.cs.findbugs.classfile.FieldDescriptor;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import org.apache.bcel.Const;

import static com.h3xstream.findsecbugs.common.matcher.InstructionDSL.invokeInstruction;

/**
 * This detector does minimal effort to find potential REDOS.
 * <p>
 * It will identify pattern similar to : <code>(( )+)+</code>
 * </p>
 * <p>
 * It will not identify pattern of equivalence (such as<code>(aa|a)</code>).
 * It is far more complex to identify.
 * </p>
 * <p>
 * For more advanced Regex analysis: <a href="http://code.google.com/p/saferegex/">Safe Regex</a>
 * </p>
 */
public class ReDosDetector extends OpcodeStackDetector {

    private static final String REDOS_TYPE = "REDOS";

    private static final char[] OPENING_CHAR = {'(', '['};

    private static final char[] CLOSING_CHAR = {')', ']'};

    private static final char[] PLUS_CHAR = {'+', '*', '?'};

    private static final InvokeMatcherBuilder PATTERN_COMPILE = invokeInstruction().atClass("java/util/regex/Pattern")
            .atMethod("compile").withArgs("(Ljava/lang/String;)Ljava/util/regex/Pattern;");
    private static final InvokeMatcherBuilder STRING_MATCHES = invokeInstruction().atClass("java/lang/String")
            .atMethod("matches").withArgs("(Ljava/lang/String;)Z");

    private BugReporter bugReporter;

    public ReDosDetector(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void sawOpcode(int seen) {
        //printOpCode(seen);
        if (seen == Const.INVOKESTATIC && PATTERN_COMPILE.matches(this)) {
            OpcodeStack.Item item = stack.getStackItem(0);
            if (!StackUtils.isVariableString(item)) {
                String value = (String) item.getConstant();
                analyseRegexString(value);
            }
        } else if (seen == Const.INVOKEVIRTUAL && STRING_MATCHES.matches(this)) {
            OpcodeStack.Item item = stack.getStackItem(0);
            if (!StackUtils.isVariableString(item)) {
                String value = (String) item.getConstant();
                analyseRegexString(value);
            }
        }
    }

    public void analyseRegexString(String regex) {
        if (regex.length() > 0) {
            recurAnalyseRegex(regex, regex.length() - 1, 0);
        }
    }

    private int recurAnalyseRegex(String regex, int startPosition, int level) {
//        print(level, "level = " + level);
        if (level == 2) {

            MethodDescriptor md = this.getMethodDescriptor();
            FieldDescriptor fd = this.getFieldDescriptor();

            BugInstance bug = new BugInstance(this, REDOS_TYPE, Priorities.NORMAL_PRIORITY) //
                    .addString(regex).addClass(this);
            if (md != null)
                bug.addMethod(md);
            if (fd != null)
                bug.addField(fd);

            try {
                bug.addSourceLine(this);
            } catch (IllegalStateException e) {
            }

            bugReporter.reportBug(bug);
            return 0;
        }


//        print(level, "Analysing " + regex.substring(0, startPosition + 1));

        boolean openingMode = false;

        for (int i = startPosition; i >= 0; i--) {
//            print(level, "[" + i + "] = '" + regex.charAt(i) + "'");

            if (isChar(regex, i, OPENING_CHAR)) {
//                print(level, "<<<<");
                return i;
            }

            if (isChar(regex, i, CLOSING_CHAR)) {
                int newLevel = level;
                if (i + 1 < regex.length() && isChar(regex, i + 1, PLUS_CHAR)) {
                    newLevel += 1;
                }
//                print(level, ">>>>");
                openingMode = true;
                i = recurAnalyseRegex(regex, i - 1, newLevel);
                if (i == -1) {
                    return 0;
                }
//                print(level, "Restarting at " + i);
            }
        }

//        print(level, "END!");

        return 0;
    }

    /**
     * @param value
     * @param position
     * @param charToTest
     * @return
     */
    private boolean isChar(String value, int position, char[] charToTest) {
        char actualChar = value.charAt(position);
        boolean oneCharFound = false;
        for (char ch : charToTest) {
            if (actualChar == ch) {
                oneCharFound = true;
                break;
            }
        }
        return oneCharFound && (position == 0 || value.charAt(position - 1) != '\\');
    }

    //Debug method

//    private void print(int level,Object obj) {
//        System.out.println(lvl(level) + "> "+ obj);
//    }
//
//    private String lvl(int level) {
//        StringBuilder str = new StringBuilder();
//        for(int i=0;i<level;i++) {
//            str.append("-\t");
//        }
//        return str.toString();
//    }

}
