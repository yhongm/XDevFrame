package com.yhongm.xdev_frame_core.custom_agera;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Arrays;

import static android.os.SystemClock.elapsedRealtime;
import static com.yhongm.xdev_frame_core.custom_agera.Preconditions.checkNotNull;
import static com.yhongm.xdev_frame_core.custom_agera.Preconditions.checkState;
import static com.yhongm.xdev_frame_core.custom_agera.WorkerHandler.MSG_LAST_REMOVED;
import static com.yhongm.xdev_frame_core.custom_agera.WorkerHandler.MSG_UPDATE;
import static com.yhongm.xdev_frame_core.custom_agera.WorkerHandler.workerHandler;

public abstract class BaseObservable implements Observable {
    @NonNull
    private static final Object[] NO_UPDATABLES_OR_HANDLERS = new Object[0];
    @NonNull
    private final WorkerHandler handler;
    @NonNull
    private final Object token = new Object();
    final int shortestUpdateWindowMillis;
    // Pairs of updatables and their associated handlers. Always of even length.
    @NonNull
    private Object[] updatablesAndHandlers;
    private int size;
    private long lastUpdateTimestamp;
    private boolean pendingUpdate = false;

    protected BaseObservable() {
        this(0);
    }

    BaseObservable(final int shortestUpdateWindowMillis) {
        checkState(Looper.myLooper() != null, "Can only be created on a Looper thread");
        this.shortestUpdateWindowMillis = shortestUpdateWindowMillis;
        this.handler = workerHandler();
        this.updatablesAndHandlers = NO_UPDATABLES_OR_HANDLERS;
        this.size = 0;
    }

    @Override
    public final void addUpdatable(@NonNull final Updatable updatable) {
        checkState(Looper.myLooper() != null, "Can only be added on a Looper thread");
        checkNotNull(updatable);
        boolean activateNow = false;
        synchronized (token) {
            add(updatable, workerHandler());
            if (size == 1) {
                if (handler.hasMessages(MSG_LAST_REMOVED, this)) {
                    handler.removeMessages(MSG_LAST_REMOVED, this);
                } else if (Looper.myLooper() == handler.getLooper()) {
                    activateNow = true;
                } else {
                    handler.obtainMessage(WorkerHandler.MSG_FIRST_ADDED, this).sendToTarget();
                }
            }
        }
        if (activateNow) {
            observableActivated();
        }
    }

    @Override
    public final void removeUpdatable(@NonNull final Updatable updatable) {
        checkState(Looper.myLooper() != null, "Can only be removed on a Looper thread");
        checkNotNull(updatable);
        synchronized (token) {
            remove(updatable);
            if (size == 0) {
                handler.obtainMessage(MSG_LAST_REMOVED, this).sendToTarget();
                handler.removeMessages(MSG_UPDATE, this);
                pendingUpdate = false;
            }
        }
    }


    private void add(@NonNull final Updatable updatable, @NonNull final Handler handler) {
        int indexToAdd = -1;
        for (int index = 0; index < updatablesAndHandlers.length; index += 2) {
            if (updatablesAndHandlers[index] == updatable) {
                throw new IllegalStateException("Updatable already added, cannot add.");
            }
            if (updatablesAndHandlers[index] == null) {
                indexToAdd = index;
            }
        }
        if (indexToAdd == -1) {
            indexToAdd = updatablesAndHandlers.length;
            updatablesAndHandlers = Arrays.copyOf(updatablesAndHandlers,
                    indexToAdd < 2 ? 2 : indexToAdd * 2);
        }
        updatablesAndHandlers[indexToAdd] = updatable;
        updatablesAndHandlers[indexToAdd + 1] = handler;
        size++;
    }

    private void remove(@NonNull final Updatable updatable) {
        for (int index = 0; index < updatablesAndHandlers.length; index += 2) {
            if (updatablesAndHandlers[index] == updatable) {
                WorkerHandler handler = (WorkerHandler) updatablesAndHandlers[index + 1];
                handler.removeUpdatable(updatable, token);
                updatablesAndHandlers[index] = null;
                updatablesAndHandlers[index + 1] = null;
                size--;
                return;
            }
        }
        throw new IllegalStateException("Updatable not added, cannot remove.");
    }

    protected void dispatchUpdate() {
        synchronized (token) {
            if (!pendingUpdate) {
                pendingUpdate = true;
                handler.obtainMessage(MSG_UPDATE, this).sendToTarget();
            }
        }
    }

    void sendUpdate() {
        synchronized (token) {
            if (!pendingUpdate) {
                return;
            }
            if (shortestUpdateWindowMillis > 0) {
                final long elapsedRealtimeMillis = elapsedRealtime();
                final long timeFromLastUpdate = elapsedRealtimeMillis - lastUpdateTimestamp;
                if (timeFromLastUpdate < shortestUpdateWindowMillis) {
                    handler.sendMessageDelayed(handler.obtainMessage(MSG_UPDATE, this),
                            shortestUpdateWindowMillis - timeFromLastUpdate);
                    return;
                }
                lastUpdateTimestamp = elapsedRealtimeMillis;

            }
            pendingUpdate = false;
            Log.i("BaseObservable", "17:00/sendUpdate:upateablesAndHandlers:" + appendEveryOne(updatablesAndHandlers));// yhongm 2017/03/29 17:00
            for (int index = 0; index < updatablesAndHandlers.length; index = index + 2) {
                final Updatable updatable = (Updatable) updatablesAndHandlers[index];
                final WorkerHandler handler =
                        (WorkerHandler) updatablesAndHandlers[index + 1];
                if (updatable != null) {
                    handler.update(updatable, token);
                }
            }
        }
    }


    /**
     * return every item tostring
     *
     * @param updatablesAndHandlers
     * @return
     */
    private String appendEveryOne(Object[] updatablesAndHandlers) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < updatablesAndHandlers.length; i++) {
            builder.append(updatablesAndHandlers[i].getClass().getName() + ",");
        }
        return builder.toString();
    }

    protected void observableActivated() {
    }

    protected void observableDeactivated() {
    }
}
