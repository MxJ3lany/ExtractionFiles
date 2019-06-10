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

import android.view.View;

/**
 * 皮肤属性。
 */
public class SkinAttr {

    /**
     * 资源名。
     */
    String resName;

    /**
     * 属性类型。
     */
    SkinAttrType attrType;

    public SkinAttr(SkinAttrType attrType, String resName) {
        this.resName = resName;
        this.attrType = attrType;
    }

    /**
     * 把皮肤的属性应用到View上。
     */
    public void apply(View view) {
        attrType.apply(view, resName);
    }
}
