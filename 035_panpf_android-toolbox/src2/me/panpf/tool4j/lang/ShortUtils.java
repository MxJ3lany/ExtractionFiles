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

package me.panpf.tool4j.lang;

/**
 * Short相关的工具函数
 */
public class ShortUtils {
    /**
     * 将字节数组data转换成一个短整型数据
     *
     * @param data 源数组
     * @return 一个短整型数据
     */
    public static short valueOf(byte[] data) {
        short res = 0;
        if (data.length >= 2) {
            res = (short) ((data[0] << 8) + (data[1] & 0xFF));
        } else if (data.length == 1) {
            res = (short) data[0];
        }
        return res;
    }
}
