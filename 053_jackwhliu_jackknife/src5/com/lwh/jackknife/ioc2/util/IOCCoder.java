package com.lwh.jackknife.ioc2.util;

import com.lwh.jackknife.ioc2.Context;
import com.lwh.jackknife.ioc2.match.Matcher;

import javax.lang.model.element.Element;

public class IOCCoder implements Coder {

    private String mClassName;

    public IOCCoder(String className) {
        this.mClassName = className;
    }

    @Override
    public void code(Matcher matcher, Element element) {
        if (matcher.match(mClassName)) {
            write(matcher.getContext(), element);
        }
    }

    @Override
    public void write(Context context, Element element) {
        context.write(element);
    }
}
