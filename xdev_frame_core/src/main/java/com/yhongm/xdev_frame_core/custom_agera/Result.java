package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Collections;
import java.util.List;

import static com.yhongm.xdev_frame_core.custom_agera.Preconditions.checkArgument;
import static com.yhongm.xdev_frame_core.custom_agera.Preconditions.checkNotNull;
import static com.yhongm.xdev_frame_core.custom_agera.Preconditions.checkState;
import static java.util.Collections.singletonList;

public final class Result<T> {
    @NonNull
    private static final Result<Object> ABSENT;
    @NonNull
    private static final Result<Object> FAILURE;
    @NonNull
    private static final Throwable ABSENT_THROWABLE;

    static {
        final Throwable failureThrowable = new Throwable("Attempt failed");
        failureThrowable.setStackTrace(new StackTraceElement[0]);
        FAILURE = new Result<>(null, failureThrowable);
        ABSENT_THROWABLE = new NullPointerException("Value is absent");
        ABSENT_THROWABLE.setStackTrace(new StackTraceElement[0]);
        ABSENT = new Result<>(null, ABSENT_THROWABLE);
    }

    @Nullable
    private final T value;
    @Nullable
    private transient volatile List<T> list;
    @Nullable
    private final Throwable failure;

    Result(@Nullable final T value, @Nullable final Throwable failure) {
        checkArgument(value != null ^ failure != null, "Illegal Result arguments");
        this.value = value;
        this.failure = failure;
        this.list = value != null ? null : Collections.<T>emptyList();
    }

    @NonNull
    public static <T> Result<T> success(@NonNull final T value) {
        return new Result<>(checkNotNull(value), null);
    }

    @NonNull
    public static <T> Result<T> failure(@NonNull final Throwable failure) {
        return failure == ABSENT_THROWABLE
                ? Result.<T>absent() : new Result<T>(null, checkNotNull(failure));
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static <T> Result<T> failure() {
        return (Result<T>) FAILURE;
    }

    @NonNull
    public static <T> Result<T> present(@NonNull final T value) {
        return success(value);
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public static <T> Result<T> absent() {
        return (Result<T>) ABSENT;
    }

    @NonNull
    public static <T> Result<T> absentIfNull(@Nullable final T value) {
        return value == null ? Result.<T>absent() : present(value);
    }

    public boolean succeeded() {
        return value != null;
    }

    public boolean failed() {
        return value == null;
    }

    public boolean isPresent() {
        return succeeded();
    }

    public boolean isAbsent() {
        return this == ABSENT;
    }

    @NonNull
    public T get() throws FailedResultException {
        if (value != null) {
            return value;
        }
        Log.i("Result", "17:51/get:");// yhongm 2017/03/30 17:51
        if (failure != null) {
            throw new FailedResultException(failure);
        } else {
            Log.i("Result", "17:43/get:failure is null");// yhongm 2017/03/30 17:43
            throw new FailedResultException("failure is null");

        }

    }


    /**
     * Returns a list containing the value if it is present, or an empty list.
     */
    @NonNull
    public List<T> asList() {
        List<T> list = this.list;
        if (list == null) {
            synchronized (this) {
                list = this.list;
                if (list == null) {
                    this.list = list = singletonList(value);
                }
            }
        }
        return list;
    }

    @NonNull
    public Throwable getFailure() {
        checkState(failure != null, "Not a failure");
        return failure;
    }

    @Nullable
    public T orNull() {
        return value;
    }

    @Nullable
    public Throwable failureOrNull() {
        return failure;
    }

    @NonNull
    public Result<T> ifSucceededSendTo(@NonNull final Receiver<? super T> receiver) {
        if (value != null) {
            receiver.accept(value);
        }
        return this;
    }

    @NonNull
    public Result<T> ifFailedSendTo(@NonNull final Receiver<? super Throwable> receiver) {
        if (failure != null) {
            receiver.accept(failure);
        }
        return this;
    }








    public boolean contains(@NonNull final T value) {
        return this.value != null && this.value.equals(value);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Result<?> result = (Result<?>) o;

        if (value != null ? !value.equals(result.value) : result.value != null) {
            return false;
        }
        if (failure != null ? !failure.equals(result.failure) : result.failure != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (failure != null ? failure.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (this == ABSENT) {
            return "Result{Absent}";
        }
        if (this == FAILURE) {
            return "Result{Failure}";
        }
        if (value != null) {
            return "Result{Success; value=" + value + "}";
        }
        return "Result{Failure; failure=" + failure + "}";
    }
}
