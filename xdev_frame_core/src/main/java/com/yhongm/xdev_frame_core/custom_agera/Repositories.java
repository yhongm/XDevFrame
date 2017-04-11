package com.yhongm.xdev_frame_core.custom_agera;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import static com.yhongm.xdev_frame_core.custom_agera.Preconditions.checkNotNull;


public final class Repositories {

    @NonNull
    public static <T> Repository<T> repository(@NonNull final T object) {
        return new SimpleRepository<>(object);
    }

    @NonNull
    public static <T> RepositoryCompilerStates.REventSource<T, T> repositoryWithInitialValue(@NonNull final T initialValue) {
        return RepositoryCompiler.repositoryWithInitialValue(initialValue);
    }

    @NonNull
    public static <T> MutableRepository<T> mutableRepository(@NonNull final T object) {
        return new SimpleRepository<>(object);
    }

    private static final class SimpleRepository<T> extends BaseObservable
            implements MutableRepository<T> {
        @NonNull
        private T reference;

        SimpleRepository(@NonNull final T reference) {
            this.reference = checkNotNull(reference);
        }

        @NonNull
        @Override
        public synchronized T get() {
            Looper.myLooper().toString();
            Log.i("SimpleRepository", "17:23/get:threadName:" + Thread.currentThread().getName());// yhongm 2017/03/28 17:23
            return reference;
        }

        @Override
        public void accept(@NonNull final T reference) {
            synchronized (this) {
                if (reference.equals(this.reference)) {
                    // Keep the old reference to have a slight performance edge if GC is generational.
                    return;
                }
                this.reference = reference;
            }
            dispatchUpdate();
        }
    }

    private Repositories() {
    }
}
