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

package com.lwh.jackknife.xskin.attr;

import android.content.Context;
import android.util.AttributeSet;

import com.lwh.jackknife.xskin.constant.SkinConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 皮肤属性工具类。
 */
public class SkinAttrSupport {

    /**
     * 从xml的属性集合中获取皮肤相关的属性。
     */
    public static List<SkinAttr> getSkinAttrs(AttributeSet attrs, Context context) {
        List<SkinAttr> skinAttrs = new ArrayList<>();
        SkinAttr skinAttr;
        for (int i = 0; i < attrs.getAttributeCount(); i++) {
            String attrName = attrs.getAttributeName(i);
            String attrValue = attrs.getAttributeValue(i);
            SkinAttrType attrType = getSupprotAttrType(attrName);
            if (attrType == null) continue;
            if (attrValue.startsWith("@")) {
                int id = Integer.parseInt(attrValue.substring(1));
                // 获取资源id的实体名称
                String entryName = context.getResources().getResourceEntryName(id);
                if (entryName.startsWith(SkinConfig.ATTR_PREFIX)) {
                    skinAttr = new SkinAttr(attrType, entryName);
                    skinAttrs.add(skinAttr);
                }
            }
        }
        return skinAttrs;

    }

    private static SkinAttrType getSupprotAttrType(String attrName) {
        for (SkinAttrType attrType : SkinAttrType.values()) {
            if (attrType.getAttrType().equals(attrName))
                return attrType;
        }
        return null;
    }
}
