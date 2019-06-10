/*
 * Copyright (C) 2016.  BoBoMEe(wbwjx115@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bobomee.android.common.util;

import java.util.LinkedList;

/**
 * 一个抽象的池，定义了固定的大小，可以获得或回收对应object
 * <p>
 * Created by zhaiyifan on 2015/8/3.
 */
public abstract class Pool<T> {

    private final int mCapacity;

    /**
     * Construct a pool with corresponding capacity.
     *
     * @param capacity Capacity of this pool.
     */
    public Pool(int capacity) {
        mCapacity = capacity;
    }

    /**
     * Get capacity of this pool.
     *
     * @return Capacity of this pool.
     */
    public final int capacity() {
        return mCapacity;
    }

    /**
     * Put (recycle) item into this pool.
     *
     * @param item Item to put.
     */
    public abstract void put(T item);

    /**
     * Get item out from this pool
     */
    public abstract T get();

    /**
     * Create item if needed. This is called when pool is empty and {@link #get()} is required.
     *
     * @return A newly created item.
     */
    protected T create() {
        return null;
    }

    /**
     * Create a simple pool.
     *
     * @param capacity Capacity of this pool.
     * @return A newly created simple pool, which is not thread-safe.
     */
    public static <T> Pool<T> simplePool(int capacity) {
        return new SimplePool<T>(capacity);
    }

    /**
     * Create a synchronized thread-safe pool.
     *
     * @param capacity Capacity of this pool.
     * @return A newly created synchronized thread-safe pool.
     */
    public static <T> Pool<T> synchronizedPool(int capacity) {
        return new SynchronizedPool<T>(capacity);
    }

    /**
     * Create a thread local thread-safe pool.
     *
     * @param capacity Capacity of this pool.
     * @return A newly created thread local thread-safe pool.
     */
    public static <T> Pool<T> threadLocalPool(int capacity) {
        return new ThreadLocalPool<T>(capacity);
    }

    public static class SimplePool<T> extends Pool<T> {

        private final LinkedList<T> mList = new LinkedList<T>();

        public SimplePool(int capacity) {
            super(capacity);
        }

        @Override
        public void put(T item) {
            if (item == null) {
                return;
            }
            int capacity = capacity();
            if (capacity <= 0 || size() < capacity) {
                mList.offer(item);
            }
        }

        @Override
        public T get() {
            return size() <= 0 ? create() : mList.poll();
        }

        private int size() {
            return mList.size();
        }
    }

    /**
     * A synchronized thread-safe pool.
     */
    public static class SynchronizedPool<T> extends SimplePool<T> {

        private final Object mLock = new Object();

        public SynchronizedPool(int capacity) {
            super(capacity);
        }

        @Override
        public void put(T item) {
            synchronized (mLock) {
                super.put(item);
            }
        }

        @Override
        public T get() {
            synchronized (mLock) {
                return super.get();
            }
        }
    }

    /**
     * A thread local thread-safe pool.
     */
    public static class ThreadLocalPool<T> extends Pool<T> {

        private final ThreadLocal<LinkedList<T>> mThreadLocalList
                = new ThreadLocal<LinkedList<T>>() {
            @Override
            protected LinkedList<T> initialValue() {
                return new LinkedList<T>();
            }
        };

        public ThreadLocalPool(int capacity) {
            super(capacity);
        }

        @Override
        public void put(T item) {
            if (item == null) {
                return;
            }
            LinkedList<T> list = mThreadLocalList.get();
            int capacity = capacity();
            if (capacity <= 0 || list.size() < capacity) {
                list.offer(item);
            }
        }

        @Override
        public T get() {
            LinkedList<T> array = mThreadLocalList.get();
            return array.size() <= 0 ? create() : array.poll();
        }
    }
}
