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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * <h2>Object工具类，提供一些有关Object的便捷方法</h2>
 * <p>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;(01)、将指定的Object序列化成字节：static byte[] objectToByte(Object object)
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;(02)、将指定的字节反序列化成一个Object：static Object byteToObject(byte[] bytes)
 */
public class ObjectUtils {

    /**
     * (01)、将指定的Object序列化成字节
     *
     * @param object 指定的Object
     * @return 经过序列化得到的字节数据
     * @throws java.io.IOException i/o异常
     */
    public static byte[] objectToByte(Object object) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.flush();
        oos.close();
        return baos.toByteArray();
    }


    /**
     * (02)、将指定的字节反序列化成一个Object
     *
     * @param bytes 指定的字节
     * @return 经过反序列化得到的Object
     * @throws java.io.IOException    i/o异常
     * @throws ClassNotFoundException 字节数组中不存在对象
     */
    public static Object byteToObject(byte[] bytes) throws IOException, ClassNotFoundException {
        Object object = null;
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        object = ois.readObject();
        ois.close();
        return object;
    }
}
