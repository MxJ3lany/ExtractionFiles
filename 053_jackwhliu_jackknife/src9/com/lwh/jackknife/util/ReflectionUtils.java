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

package com.lwh.jackknife.util;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ReflectionUtils {

    public static <T> T newInstance(Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> c : constructors) {
            c.setAccessible(true);
            Class[] cls = c.getParameterTypes();
            if (cls.length == 0) {
                try {
                    return (T) c.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                Object[] objs = new Object[cls.length];
                for (int i = 0; i < cls.length; i++) {
                    objs[i] = getPrimitiveDefaultValue(cls[i]);
                }
                try {
                    return (T) c.newInstance(objs);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Object getPrimitiveDefaultValue(Class clazz) {
        if (clazz.isPrimitive()) {
            return clazz == boolean.class ? false : 0;
        }
        return null;
    }

    public static Class<?> getComponentType(Field field) {
        return field.getType().getComponentType();
    }

    public static Class<?> getGenericType(Field field) {
        Type type = field.getGenericType();
        if (type instanceof ParameterizedType) {
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
            if (type instanceof Class<?>) {
                return (Class<?>) type;
            }
        } else if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        return null;
    }

    public static Class<?> getArrayType(Field f) {
        return f.getType().getComponentType();
    }

    public static Class<?> getFirstGenericType(Field f) {
        return getGenericType(f, 0);
    }

    public static Class<?> getGenericType(Field f, int genericTypeIndex) {
        Type type = f.getGenericType();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            type = parameterizedType.getActualTypeArguments()[genericTypeIndex];
            return (Class<?>) type;
        }
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        }
        return null;
    }

    public static void setNumber(Object obj, Field f, long n) {
        f.setAccessible(true);
        Class<?> fieldType = f.getType();
        try {
            if (fieldType == long.class) {
                f.setLong(obj, n);
            } else if (fieldType == int.class) {
                f.setInt(obj, (int) n);
            } else if (fieldType == short.class) {
                f.setShort(obj, (short) n);
            } else if (fieldType == byte.class) {
                f.setByte(obj, (byte) n);
            } else if (fieldType == Long.class) {
                f.set(obj, Long.valueOf(n));
            } else if (fieldType == Integer.class) {
                f.set(obj, Integer.valueOf((int) n));
            } else if (fieldType == Short.class) {
                f.set(obj, new Short((short) n));
            } else if (fieldType == Byte.class) {
                f.set(obj, Byte.valueOf((byte) n));
            } else {
                throw new RuntimeException("Field is not a number class.");
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static boolean isNumber(Class<?> numberCls) {
        return numberCls == long.class
                || numberCls == Long.class
                || numberCls == int.class
                || numberCls == Integer.class
                || numberCls == short.class
                || numberCls == Short.class
                || numberCls == byte.class
                || numberCls == Byte.class;
    }

    public static Object getFieldValue(Field f, Object obj) {
        f.setAccessible(true);
        try {
            return f.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object getStaticFieldValue(Field f) {
        return getStaticFieldValue(f, null);
    }

    public static Object getStaticFieldValue(Field f, Class<?> fieldCls) {
        f.setAccessible(true);
        if (isStaticField(f)) {
            try {
                return f.get(fieldCls);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        throw new RuntimeException("Field is not static.");
    }

    public static void setFieldValue(Field f, Object obj, Object value) {
        f.setAccessible(true);
        try {
            f.set(obj, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void setStaticFieldValue(Field f, Object value) {
        setStaticFieldValue(f, null, value);
    }

    public static void setStaticFieldValue(Field f, Class<?> fieldCls, Object value) {
        f.setAccessible(true);
        if (isStaticField(f)) {
            try {
                f.set(fieldCls, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isSerializable(Field f) {
        Class<?>[] classes = f.getType().getInterfaces();
        for (Class<?> cls : classes) {
            if (Serializable.class == cls) {
                return true;
            }
        }
        return false;
    }

    public static boolean isStaticField(Field f) {
        return Modifier.isStatic(f.getModifiers());
    }

    public static boolean isFinalField(Field f) {
        return Modifier.isFinal(f.getModifiers());
    }

    public static boolean isSynchronizedField(Field f) {
        return Modifier.isSynchronized(f.getModifiers());
    }

    public static boolean isAbstract(Field f) {
        return Modifier.isAbstract(f.getModifiers());
    }

    public static boolean isNative(Field f) {
        return Modifier.isNative(f.getModifiers());
    }

    public static boolean isVolatile(Field f) {
        return Modifier.isVolatile(f.getModifiers());
    }

    public static boolean isTransient(Field f) {
        return Modifier.isTransient(f.getModifiers());
    }
}
