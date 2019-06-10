/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.functional.throwing;

import java.util.Objects;

import me.zhanghai.android.douya.functional.FunctionalException;
import me.zhanghai.android.douya.functional.extension.QuadPredicate;

@FunctionalInterface
public interface ThrowingQuadPredicate<T, U, V, W> extends QuadPredicate<T, U, V, W> {

    boolean testThrows(T t, U u, V v, W w) throws Exception;

    default boolean test(T t, U u, V v, W w) {
        try {
            return testThrows(t, u, v, w);
        } catch (Exception e) {
            throw new FunctionalException(e);
        }
    }

    default ThrowingQuadPredicate<T, U, V, W> and(
            ThrowingQuadPredicate<? super T, ? super U, ? super V, ? super W> other) {
        Objects.requireNonNull(other);
        return (T t, U u, V v, W w) -> testThrows(t, u, v, w) && other.testThrows(t, u, v, w);
    }

    default ThrowingQuadPredicate<T, U, V, W> negate() {
        return (T t, U u, V v, W w) -> !testThrows(t, u, v, w);
    }

    default ThrowingQuadPredicate<T, U, V, W> or(
            ThrowingQuadPredicate<? super T, ? super U, ? super V, ? super W> other) {
        Objects.requireNonNull(other);
        return (T t, U u, V v, W w) -> testThrows(t, u, v, w) || other.testThrows(t, u, v, w);
    }
}
