/*
 * Copyright (C) 2017 Peng fei Pan <sky@panpf.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.panpf.tool4j.util;

/**
 * 字符串验证模式
 */
public enum StringVerifyMode {
    /**
     * 包含标记
     */
    CONTAIN_KEYWORDS,
    /**
     * 不包含标记
     */
    NOT_CONTAIN_KEYWORDS,
    /**
     * 等于标记
     */
    EQUAL_KEYWORDS,
    /**
     * 不等于标记
     */
    NOT_EQUAL_KEYWORDS,
    /**
     * 以标记结尾
     */
    ENDS_WITH_KEYWORDS,
    /**
     * 不以标记结尾
     */
    NOT_ENDS_WITH_KEYWORDS,
    /**
     * 以标记开头
     */
    STARTS_WIT_KEYWORDS,
    /**
     * 不以标记开头
     */
    NOT_STARTS_WIT_KEYWORDS;
}