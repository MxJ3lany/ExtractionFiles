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

package me.panpf.tool4a.graphics.drawable;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Handler;

/**
 * 图片级别控制器，用来控制Drawable的级别
 */
public class DrawableLevelController {
    private Activity activity;
    private Drawable drawable;
    private Handler handler;
    private Handle handle;
    private boolean running;
    private int delayed = 10;    //延迟，默认10
    private int incremental = 150;    //增量，默认150
    private int repeatCount = -1;    //重复次数，默认-1表示永不停止
    private RepeatMode repeatMode = RepeatMode.ORDER;    //重复方式
    private Integer hasRepeatCount = 0;    //已经重复的次数
    private Listener listener;    //监听器
    private int minLevel = 0;    //最小级别，默认0
    private int maxLevel = 10000;    //最大级别，默认10000
    private boolean delayStop;    //是否需要延迟停止

    /**
     * 创建一个图片级别控制器
     *
     * @param activity 当activity销毁的时候会停止
     * @param drawable 要控制的图片
     */
    public DrawableLevelController(Activity activity, Drawable drawable) {
        this.activity = activity;
        this.drawable = drawable;
        handler = new Handler();
        handle = new Handle();
    }

    /**
     * 是否正在运行
     *
     * @return 正在运行
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * 开始执行
     *
     * @return true：启动成功；false：启动失败，以为当前正在运行中
     */
    public boolean start() {
        if (!isRunning()) {
            running = true;
            if (repeatMode == RepeatMode.ORDER) {
                if (drawable.getLevel() < minLevel || drawable.getLevel() >= maxLevel) {
                    drawable.setLevel(minLevel);
                }
            } else if (repeatMode == RepeatMode.REVERSE) {
                if (drawable.getLevel() <= minLevel || drawable.getLevel() > maxLevel) {
                    drawable.setLevel(maxLevel);
                }
            } else if (repeatMode == RepeatMode.MIRROR) {
                if (drawable.getLevel() < minLevel || drawable.getLevel() >= maxLevel) {
                    if (hasRepeatCount % 2 == 0) {
                        drawable.setLevel(minLevel);
                    } else {
                        drawable.setLevel(maxLevel);
                    }
                }
            }
            handler.post(handle);
            if (listener != null) {
                listener.onStart();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 暂停执行
     *
     * @return true：暂停成功；false：暂停失败，以为当前没有运行，所以无需暂停
     */
    public boolean pause() {
        if (isRunning()) {
            running = false;
            handler.removeCallbacks(handle);
            if (listener != null) {
                listener.onPause();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 停止执行，停止后进度会清零
     *
     * @param delayStop 是否需要延迟到当前这一圈执行完再停止
     * @return true：停止成功；false：停止失败，以为当前没有运行，所以无需停止
     */
    public boolean stop(boolean delayStop) {
        if (isRunning()) {
            this.delayStop = delayStop;
            if (!this.delayStop) {
                running = false;
                handler.removeCallbacks(handle);
                drawable.setLevel(minLevel);
                if (listener != null) {
                    listener.onStop();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 停止执行，停止后进度会清零
     *
     * @return true：停止成功；false：停止失败，以为当前没有运行，所以无需停止
     */
    public boolean stop() {
        return stop(false);
    }

    /**
     * 重置，重置已执行的次数以及图片的级别，重置的时候并不会停止执行
     *
     * @param isNoticeMonitor 是否通知监听器
     */
    public void reset(boolean isNoticeMonitor) {
        synchronized (hasRepeatCount) {
            hasRepeatCount = 0;
        }
        synchronized (drawable) {
            if (repeatMode == RepeatMode.ORDER) {
                drawable.setLevel(minLevel);
            } else if (repeatMode == RepeatMode.REVERSE) {
                drawable.setLevel(maxLevel);
            } else if (repeatMode == RepeatMode.MIRROR) {
                drawable.setLevel(minLevel);
            }
        }
        if (isNoticeMonitor && listener != null) {
            listener.onReset();
        }
    }

    /**
     * 重置，重置已执行的次数以及图片的级别，重置的时候并不会停止执行
     */
    public void reset() {
        reset(true);
    }

    /**
     * 处理
     */
    private class Handle implements Runnable {
        @Override
        public void run() {
            if (!activity.isFinishing()) {
                if (repeatMode == RepeatMode.ORDER) {
                    orderExecuteHandle();
                } else if (repeatMode == RepeatMode.REVERSE) {
                    reveresExecuteHandle();
                } else if (repeatMode == RepeatMode.MIRROR) {
                    mirrorExecuteHandle();
                }
            } else {
                running = false;
                handler.removeCallbacks(handle);
            }
        }
    }

    /**
     * 顺序执行
     */
    private void orderExecuteHandle() {
        int nextLevel = drawable.getLevel() + incremental;    //因为当前是顺向执行，所以下一个级别就是当前级别加上级别增量

        if (nextLevel >= maxLevel) {    //如果当前正好完成一圈了
            if (delayStop) {    //如果需要延迟停止
                stop();
            } else {
                hasRepeatCount++;
                if (listener != null) {
                    listener.onCompletedACircle(repeatCount, hasRepeatCount);
                }
                if (repeatCount > 0 && hasRepeatCount >= repeatCount) {
                    pause();
                    reset(false);
                } else {
                    nextLevel %= maxLevel;
                    if (nextLevel < minLevel) {
                        nextLevel += minLevel;
                    }
                    drawable.setLevel(nextLevel);
                    handler.postDelayed(handle, delayed);
                }
            }
        } else {
            nextLevel %= maxLevel;
            if (nextLevel < minLevel) {
                nextLevel += minLevel;
            }
            drawable.setLevel(nextLevel);
            handler.postDelayed(handle, delayed);
        }
    }

    /**
     * 逆向执行
     */
    private void reveresExecuteHandle() {
        //因为当前是逆向执行，所以下一个级别就是当前级别减去级别增量
        int nextLevel = drawable.getLevel() - incremental;
        //如果重复次数有限制并且已经完成了重复任务，那么就暂停执行，否则继续执行
        if (nextLevel <= minLevel) {
            if (delayStop) {    //如果需要延迟停止
                stop();
            } else {
                hasRepeatCount++;
                if (listener != null) {
                    listener.onCompletedACircle(repeatCount, hasRepeatCount);
                }
                if (repeatCount > 0 && hasRepeatCount >= repeatCount) {
                    pause();
                    reset(false);
                } else {
                    if (nextLevel < minLevel) {
                        if (nextLevel < 0) {
                            nextLevel = maxLevel + nextLevel;
                        } else {
                            nextLevel = maxLevel - (minLevel - nextLevel);
                        }
                    } else if (nextLevel == minLevel) {
                        nextLevel = maxLevel;
                    }
                    drawable.setLevel(nextLevel);
                    handler.postDelayed(handle, delayed);
                }
            }
        } else {
            if (nextLevel < minLevel) {
                if (nextLevel < 0) {
                    nextLevel = maxLevel + nextLevel;
                } else {
                    nextLevel = maxLevel - (minLevel - nextLevel);
                }
            }
            drawable.setLevel(nextLevel);
            handler.postDelayed(handle, delayed);
        }
    }

    /**
     * 镜像执行
     */
    public void mirrorExecuteHandle() {
        if (hasRepeatCount % 2 == 0) {
            //因为当前是顺向执行，所以下一个级别就是当前级别加上级别增量
            int nextLevel = drawable.getLevel() + incremental;
            //如果重复次数有限制并且已经完成了重复任务，那么就暂停执行，否则继续执行
            if (nextLevel >= maxLevel) {
                if (delayStop) {    //如果需要延迟停止
                    stop();
                } else {
                    hasRepeatCount++;
                    if (listener != null) {
                        listener.onCompletedACircle(repeatCount, hasRepeatCount);
                    }
                    if (repeatCount > 0 && hasRepeatCount >= repeatCount) {
                        pause();
                        reset(false);
                    } else {
                        if (nextLevel > maxLevel) {
                            nextLevel = maxLevel;
                        }
                        drawable.setLevel(nextLevel);
                        handler.postDelayed(handle, delayed);
                    }
                }
            } else {
                if (nextLevel > maxLevel) {
                    nextLevel = maxLevel;
                }
                drawable.setLevel(nextLevel);
                handler.postDelayed(handle, delayed);
            }
        } else {
            //因为当前是逆向执行，所以下一个级别就是当前级别减去级别增量
            int nextLevel = drawable.getLevel() - incremental;
            //如果重复次数有限制并且已经完成了重复任务，那么就暂停执行，否则继续执行
            if (nextLevel <= minLevel) {
                if (delayStop) {    //如果需要延迟停止
                    stop();
                } else {
                    hasRepeatCount++;
                    if (listener != null) {
                        listener.onCompletedACircle(repeatCount, hasRepeatCount);
                    }
                    if (repeatCount > 0 && hasRepeatCount >= repeatCount) {
                        pause();
                        reset(false);
                    } else {
                        if (nextLevel < minLevel) {
                            nextLevel = minLevel;
                        }
                        drawable.setLevel(nextLevel);
                        handler.postDelayed(handle, delayed);
                    }
                }
            } else {
                if (nextLevel < minLevel) {
                    nextLevel = minLevel;
                }
                drawable.setLevel(nextLevel);
                handler.postDelayed(handle, delayed);
            }
        }
    }

    /**
     * 获取延迟，每次完成之后都会等待一段时间才会进行下一次，这就是所谓的延迟
     *
     * @return 延迟，单位毫秒，默认10
     */
    public int getDelayed() {
        return delayed;
    }

    /**
     * 设置延迟，每次完成之后都会等待一段时间才会进行下一次，这就是所谓的延迟
     *
     * @param delayed 延迟，单位毫秒，默认10
     */
    public void setDelayed(int delayed) {
        this.delayed = delayed;
    }

    /**
     * 获取增量，例如本次级别为100，下次为150，那么增量就是50
     *
     * @return 增量，默认150
     */
    public int getIncremental() {
        return incremental;
    }

    /**
     * 设置增量，例如本次级别为100，下次为150，那么增量就是50
     *
     * @param incremental 增量，默认150
     */
    public void setIncremental(int incremental) {
        this.incremental = incremental;
    }

    /**
     * 获取重复次数
     *
     * @return 重复次数，默认为-1，-1表示永不停止
     */
    public int getRepeatCount() {
        return repeatCount;
    }

    /**
     * 设置重复次数
     *
     * @param repeatCount 重复次数，默认为-1，-1表示永不停止
     */
    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    /**
     * 获取重复方式
     *
     * @return 重复方式，默认为RepeatMode.ORDER
     */
    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    /**
     * 设置重复方式
     *
     * @param repeatMode 重复方式，默认为RepeatMode.ORDER
     */
    public void setRepeatMode(RepeatMode repeatMode) {
        this.repeatMode = repeatMode;
    }

    /**
     * 获取事件监听器
     *
     * @return 事件监听器
     */
    public Listener getListener() {
        return listener;
    }

    /**
     * 设置事件监听器
     *
     * @param listener 事件监听器
     */
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /**
     * 获取最小级别
     *
     * @return 最小级别，默认0
     */
    public int getMinLevel() {
        return minLevel;
    }

    /**
     * 设置最小级别
     *
     * @param minLevel 最小级别，默认0
     */
    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    /**
     * 获取最大级别
     *
     * @return 最大级别，默认10000
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * 设置最大级别
     *
     * @param minLevel 最大级别，默认10000
     */
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    /**
     * 重复方式
     */
    public enum RepeatMode {
        /**
         * 顺序执行，例如：从最小级别到最大级别，再从最小级别到最大级别...
         */
        ORDER,
        /**
         * 反向执行，例如：从最大级别到最小级别，再从最大级别到最小级别...
         */
        REVERSE,
        /**
         * 镜像执行，例如：从最小级别到最大级别，再从最大级别-最小级别，再从最小级别到最大级别...
         */
        MIRROR;
    }

    /**
     * 事件监听器
     */
    public interface Listener {
        /**
         * 当启动的时候
         */
        public void onStart();

        /**
         * 当暂停的时候
         */
        public void onPause();

        /**
         * 当重置的时候
         */
        public void onReset();

        /**
         * 当停止
         */
        public void onStop();

        /**
         * 当完成一圈的时候
         *
         * @param totalRepeatCount 总圈数
         * @param hasRepeatCount   已经完成了圈数
         */
        public void onCompletedACircle(int totalRepeatCount, int hasRepeatCount);
    }
}
