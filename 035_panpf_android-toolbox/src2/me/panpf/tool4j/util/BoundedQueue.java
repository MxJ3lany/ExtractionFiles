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

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * 这是一个有界队列，队列的长度是固定的，当队列满了的时候新的元素会将队列头部的元素挤出队列
 *
 * @param <E>
 */
public class BoundedQueue<E> {
    private int maxSize;    // 最大容量
    private Queue<E> queue;    //队列

    /**
     * 创建一个圆圈，同时你必须指定它的最大容量
     *
     * @param queue   使用指定的队列来实现圆圈
     * @param maxSize 最大容量
     */
    public BoundedQueue(Queue<E> queue, int maxSize) {
        this.queue = queue;
        setMaxSize(maxSize);
    }

    /**
     * 创建一个圆圈，同时你必须指定它的最大容量
     *
     * @param maxSize 最大容量
     */
    public BoundedQueue(int maxSize) {
        this(new LinkedList<E>(), maxSize);
    }

    /**
     * 腾出位置
     *
     * @param addSize 新添加的个数，如果添加后超出最大容量就从头开始腾地儿
     */
    private void check(int addSize) {
        if ((queue.size() + addSize) > maxSize) {
            for (int w = 0, size = ((queue.size() + addSize) - maxSize); w < size; w++) {
                try {
                    queue.poll();
                } catch (NoSuchElementException e) {
                }
            }
        }
    }

    /**
     * 是否已经满了
     *
     * @return
     */
    public boolean isFull() {
        return queue.size() >= maxSize;
    }

    /**
     * 添加一个元素
     *
     * @param e
     * @return
     */
    public boolean add(E e) {
        if (e != null) {
            check(1);
            return queue.add(e);
        } else {
            return false;
        }
    }

    /**
     * 弹出一个元素
     *
     * @return
     */
    public E poll() {
        return queue.poll();
    }

    /**
     * 当前容量
     *
     * @return
     */
    public int size() {
        return queue.size();
    }

    /**
     * 清空
     */
    public void clear() {
        queue.clear();
    }

    /**
     * 获取存储元素的队列
     *
     * @return
     */
    public Queue<E> getQueue() {
        return queue;
    }

    /**
     * 设置存储元素的队列
     *
     * @param queue
     */
    public void setQueue(Queue<E> queue) {
        this.queue = queue;
    }

    /**
     * 获取最大容量
     *
     * @return 最大容量
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * 设置最大容量
     *
     * @param maxSize 最大容量
     */
    public void setMaxSize(int maxSize) {
        if (maxSize < 1) {
            throw new IllegalArgumentException("maxSize not less than 1");
        }
        this.maxSize = maxSize;
        check(0);
    }
}