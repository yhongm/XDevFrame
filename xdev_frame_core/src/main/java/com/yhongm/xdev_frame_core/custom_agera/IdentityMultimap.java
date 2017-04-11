package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Arrays;

final class IdentityMultimap<K, V> {
    @NonNull
    private static final Object[] NO_KEY_VALUES = new Object[0];

    @NonNull
    private Object[] keysValues = NO_KEY_VALUES;

    synchronized boolean addKeyValuePair(@NonNull final K key, @NonNull final V value) {
        int size = 0;
        int indexToAdd = -1;
        boolean hasValue = false;
        for (int index = 0; index < keysValues.length; index += 2) {
            final Object keysValue = keysValues[index];
            if (keysValue == null) {
                indexToAdd = index;
            }
            if (keysValue == key) {
                size++;
                if (keysValues[index + 1] == value) {
                    indexToAdd = index;
                    hasValue = true;
                }
            }
        }
        if (indexToAdd == -1) {
            indexToAdd = keysValues.length;
            keysValues = Arrays.copyOf(keysValues, indexToAdd < 2 ? 2 : indexToAdd * 2);
        }
        if (!hasValue) {
            keysValues[indexToAdd] = key;
            keysValues[indexToAdd + 1] = value;
        }
        Log.i("IdentityMultimap", "13:19/addKeyValuePair:size:" + size + ",key:" + key.getClass().getName());// yhongm 2017/03/29 13:19
        return size == 0;
    }

    synchronized void removeKeyValuePair(@NonNull final K key, @NonNull final V value) {
        for (int index = 0; index < keysValues.length; index += 2) {
            if (keysValues[index] == key && keysValues[index + 1] == value) {
                keysValues[index] = null;
                keysValues[index + 1] = null;
            }
        }
    }

    synchronized boolean removeKey(@NonNull final K key) {
        boolean removed = false;
        for (int index = 0; index < keysValues.length; index += 2) {
            if (keysValues[index] == key) {
                Log.i("IdentityMultimap", "13:29/removeKey:key:" + key.getClass().getName());// yhongm 2017/03/24 13:29
                keysValues[index] = null;
                keysValues[index + 1] = null;
                removed = true;
            }
        }
        Log.i("IdentityMultimap", "13:24/removeKey:" + removed + ",key:" + key.getClass().getName());// yhongm 2017/03/24 13:24
        return removed;
    }
}
