package me.panpf.tool4a.app;

import android.os.Bundle;

public interface ArgumentsFactory<T> {
    public Bundle onCreateArguments(T item);
}