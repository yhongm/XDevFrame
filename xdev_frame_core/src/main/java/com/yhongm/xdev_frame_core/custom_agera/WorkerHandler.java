package com.yhongm.xdev_frame_core.custom_agera;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.WeakReference;

final class WorkerHandler extends Handler {
    static final int MSG_FIRST_ADDED = 0;
    static final int MSG_LAST_REMOVED = 1;
    static final int MSG_UPDATE = 2;
    static final int MSG_CALL_UPDATABLE = 3;
    static final int MSG_CALL_MAYBE_START_FLOW = 4;
    static final int MSG_CALL_ACKNOWLEDGE_CANCEL = 5;
    private static final ThreadLocal<WeakReference<WorkerHandler>> handlers = new ThreadLocal<>();
    @NonNull
    private final IdentityMultimap<Updatable, Object> scheduledUpdatables;

    @NonNull
    static WorkerHandler workerHandler() {
        final WeakReference<WorkerHandler> handlerReference = handlers.get();
        WorkerHandler handler = handlerReference != null ? handlerReference.get() : null;
        if (handler == null) {
            handler = new WorkerHandler();
            handlers.set(new WeakReference<>(handler));
        }
        return handler;
    }

    private WorkerHandler() {
        this.scheduledUpdatables = new IdentityMultimap<>();
    }

    synchronized void removeUpdatable(@NonNull final Updatable updatable,
                                      @NonNull final Object token) {
        scheduledUpdatables.removeKeyValuePair(updatable, token);
    }

    synchronized void update(@NonNull final Updatable updatable, @NonNull final Object token) {
        if (scheduledUpdatables.addKeyValuePair(updatable, token)) {
            obtainMessage(WorkerHandler.MSG_CALL_UPDATABLE, updatable).sendToTarget();
        }
    }

    @Override
    public void handleMessage(final Message message) {
        switch (message.what) {
            case MSG_UPDATE:
                Log.i("WorkerHandler", "11:50/handleMessage:update");// yhongm 2017/03/23 11:50
                ((BaseObservable) message.obj).sendUpdate();
                break;
            case MSG_FIRST_ADDED:
                ((BaseObservable) message.obj).observableActivated();
                break;
            case MSG_LAST_REMOVED:
                ((BaseObservable) message.obj).observableDeactivated();
                break;
            case MSG_CALL_UPDATABLE:
                Log.i("WorkerHandler", "13:03/handleMessage:update");// yhongm 2017/03/23 13:03
                final Updatable updatable = (Updatable) message.obj;
                Log.i("WorkerHandler", "13:53/handleMessage:" + updatable.getClass().getSimpleName());// yhongm 2017/03/23 13:53
                if (scheduledUpdatables.removeKey(updatable)) {
                    updatable.update();
                    Log.i("WorkerHandler", "13:22/handleMessage:update:" + updatable.getClass().getName());// yhongm 2017/03/23 13:22
                }
                break;
            case MSG_CALL_MAYBE_START_FLOW:
                ((CompiledRepository) message.obj).maybeStartFlow();
                break;
            case MSG_CALL_ACKNOWLEDGE_CANCEL:
                ((CompiledRepository) message.obj).acknowledgeCancel();
                break;
            default:
        }
    }
}
