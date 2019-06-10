package com.lwh.jackknife.ioc2.util;

import com.lwh.jackknife.ioc2.Context;
import com.lwh.jackknife.ioc2.match.Matcher;

import javax.lang.model.element.Element;

public interface Coder {

    void code(Matcher matcher, Element element);
    void write(Context context, Element element);
}
