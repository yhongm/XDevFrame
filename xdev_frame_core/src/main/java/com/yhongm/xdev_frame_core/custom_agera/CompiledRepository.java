package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.Executor;

import static com.yhongm.xdev_frame_core.custom_agera.Observables.compositeObservable;
import static com.yhongm.xdev_frame_core.custom_agera.Preconditions.checkNotNull;
import static com.yhongm.xdev_frame_core.custom_agera.Preconditions.checkState;
import static com.yhongm.xdev_frame_core.custom_agera.RepositoryConfig.CANCEL_FLOW;
import static com.yhongm.xdev_frame_core.custom_agera.RepositoryConfig.RESET_TO_INITIAL_VALUE;
import static com.yhongm.xdev_frame_core.custom_agera.RepositoryConfig.SEND_INTERRUPT;
import static com.yhongm.xdev_frame_core.custom_agera.WorkerHandler.MSG_CALL_ACKNOWLEDGE_CANCEL;
import static com.yhongm.xdev_frame_core.custom_agera.WorkerHandler.MSG_CALL_MAYBE_START_FLOW;
import static com.yhongm.xdev_frame_core.custom_agera.WorkerHandler.workerHandler;
import static java.lang.Thread.currentThread;

@SuppressWarnings({"rawtypes", "unchecked"})
final class CompiledRepository extends BaseObservable
        implements Repository, Updatable, Runnable {

    private boolean init = false;

    @NonNull
    static Repository compiledRepository(
            @NonNull final Object initialValue,
            @NonNull final List<Observable> eventSources,
            final int frequency,
            @NonNull final List<Object> directives,
            @NonNull final Merger<Object, Object, Boolean> notifyChecker,
            @RepositoryConfig final int concurrentUpdateConfig,
            @RepositoryConfig final int deactivationConfig,
            @NonNull final Receiver discardedValuesDisposer) {
        final Object[] directiveArray = directives.toArray();
        return new CompiledRepository(initialValue, compositeObservable(frequency,
                eventSources.toArray(new Observable[eventSources.size()])),
                directiveArray, notifyChecker, deactivationConfig, concurrentUpdateConfig,
                discardedValuesDisposer);
    }


    @NonNull
    private final Object initialValue;
    @NonNull
    private final Observable eventSource;
    @NonNull
    private final Object[] directives;
    @NonNull
    private final Merger<Object, Object, Boolean> notifyChecker;
    @RepositoryConfig
    private final int deactivationConfig;
    @RepositoryConfig
    private final int concurrentUpdateConfig;
    @NonNull
    private final Receiver discardedValuesDisposer;
    @NonNull
    private final WorkerHandler workerHandler;

    CompiledRepository(
            @NonNull final Object initialValue,
            @NonNull final Observable eventSource,
            @NonNull final Object[] directives,
            @NonNull final Merger<Object, Object, Boolean> notifyChecker,
            @RepositoryConfig final int deactivationConfig,
            @RepositoryConfig final int concurrentUpdateConfig,
            @NonNull final Receiver discardedValuesDisposer) {
        this.initialValue = initialValue;
        this.currentValue = initialValue;
        this.intermediateValue = initialValue; // non-final field but with @NonNull requirement
        this.eventSource = eventSource;
        this.directives = directives;
        this.notifyChecker = notifyChecker;
        this.deactivationConfig = deactivationConfig;
        this.concurrentUpdateConfig = concurrentUpdateConfig;
        this.discardedValuesDisposer = discardedValuesDisposer;
        this.workerHandler = workerHandler();
        this.init = false;
    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({IDLE, RUNNING, CANCEL_REQUESTED, PAUSED_AT_GO_TO, PAUSED_AT_GO_LAZY, RUNNING_LAZILY})
    private @interface RunState {
    }

    private static final int IDLE = 0;
    private static final int RUNNING = 1;
    private static final int CANCEL_REQUESTED = 2;
    private static final int PAUSED_AT_GO_TO = 3;
    private static final int PAUSED_AT_GO_LAZY = 4;
    private static final int RUNNING_LAZILY = 5;

    @RunState
    private int runState = IDLE;
    private boolean restartNeeded;
    private int lastDirectiveIndex = -1;
    @NonNull
    private Object currentValue;
    @NonNull
    private Object intermediateValue;
    @Nullable
    private Thread currentThread;

    @Override
    protected void observableActivated() {
        eventSource.addUpdatable(this);
        maybeStartFlow();
    }

    @Override
    protected void observableDeactivated() {
        eventSource.removeUpdatable(this);
        maybeCancelFlow(deactivationConfig, false);
    }

    @Override
    public void update() {
        maybeCancelFlow(concurrentUpdateConfig, true);
        init = true;
        maybeStartFlow();
    }

    void maybeStartFlow() {
        synchronized (this) {
            if (runState == IDLE || runState == PAUSED_AT_GO_LAZY) {
                runState = RUNNING;
                lastDirectiveIndex = -1; // this could be pointing at the goLazy directive
                restartNeeded = false;
            } else {
                if (runState == CANCEL_REQUESTED) {
                    // flow may still be processing the previous deactivation;
                    // make sure to restart
                    restartNeeded = true;
                }
                return; // flow already running or scheduled to restart, do not continue
            }
        }
        intermediateValue = currentValue;
        runFlowFrom(0, false);
    }

    private void maybeCancelFlow(@RepositoryConfig final int config, final boolean scheduleRestart) {
        synchronized (this) {
            if (runState == RUNNING || runState == PAUSED_AT_GO_TO) {
                restartNeeded = scheduleRestart;

                // If config forbids cancellation, exit now after scheduling the restart, to skip the
                // cancellation request.
                if ((config & CANCEL_FLOW) == 0) {
                    return;
                }

                runState = CANCEL_REQUESTED;

                if ((config & SEND_INTERRUPT) == SEND_INTERRUPT && currentThread != null) {
                    currentThread.interrupt();
                }
            }

            // Resetting to the initial value should be done even if the flow is not running.
            if (!scheduleRestart && (config & RESET_TO_INITIAL_VALUE) == RESET_TO_INITIAL_VALUE) {
                setNewValueLocked(initialValue);
            }
        }
    }

    private boolean checkCancellationLocked() {
        if (runState == CANCEL_REQUESTED) {
            workerHandler.obtainMessage(MSG_CALL_ACKNOWLEDGE_CANCEL, this).sendToTarget();
            return true;
        }
        return false;
    }

    void acknowledgeCancel() {
        boolean shouldStartFlow = false;
        Object discardedIntermediateValue = null;
        synchronized (this) {
            if (runState == CANCEL_REQUESTED) {
                runState = IDLE;
                if (intermediateValue != currentValue) {
                    discardedIntermediateValue = intermediateValue;
                    intermediateValue = currentValue; // GC the intermediate value but keep field non-null.
                }
                shouldStartFlow = restartNeeded;
            }
        }
        if (discardedIntermediateValue != null) {
            discardedValuesDisposer.accept(discardedIntermediateValue);
        }
        if (shouldStartFlow) {
            maybeStartFlow();
        }
    }

    private void checkRestartLocked() {
        if (restartNeeded) {
            workerHandler.obtainMessage(MSG_CALL_MAYBE_START_FLOW, this).sendToTarget();
        }
    }


    private static final int END = 0;
    private static final int GET_FROM = 1;
    private static final int MERGE_IN = 2;
    private static final int GO_TO = 5;
    private static final int GO_LAZY = 6;
    private static final int SEND_TO = 7;

    private void runFlowFrom(final int index, final boolean asynchronously) {
        final Object[] directives = this.directives;
        final int length = directives.length;
        int i = index;
        Log.i("CompiledRepository", "17:32/runFlowFrom:");// yhongm 2017/03/29 17:32
        while (0 <= i && i < length) {
            final int directiveType = (Integer) directives[i];
            if (asynchronously || directiveType == GO_TO || directiveType == GO_LAZY) {
                synchronized (this) {
                    if (checkCancellationLocked()) {
                        Log.i("CompiledRepository", "17:32/runFlowFrom:");// yhongm 2017/03/29 17:32
                        break;
                    }
                    if (directiveType == GO_TO) {
                        setPausedAtGoToLocked(i);
                        Log.i("CompiledRepository", "17:32/runFlowFrom:goto");// yhongm 2017/03/29 17:32
                    } else if (directiveType == GO_LAZY) {
                        Log.i("CompiledRepository", "17:32/runFlowFrom:golazy");// yhongm 2017/03/29 17:32
                        setLazyAndEndFlowLocked(i);
                        return;
                    }
                }
            }

            switch (directiveType) {
                case GET_FROM:
                    i = runGetFrom(directives, i);
                    break;
                case MERGE_IN:
                    i = runMergeIn(directives, i);
                    break;
                case GO_TO:
                    i = runGoTo(directives, i);
                    break;
                case SEND_TO:
                    i = runSendTo(directives, i);
                    break;
                case END:
                    i = runEnd(directives, i);
                    break;
            }
        }
    }

    static void addGetFrom(@NonNull final Supplier supplier,
                           @NonNull final List<Object> directives) {
        directives.add(GET_FROM);
        directives.add(supplier);
    }

    private int runGetFrom(@NonNull final Object[] directives, final int index) {
        if (init) {
            final Supplier supplier = (Supplier) directives[index + 1];
            intermediateValue = checkNotNull(supplier.get());
            Log.i("CompiledRepository", "18:19/runGetFrom:" + intermediateValue);// yhongm 2017/03/29 18:19
        }
        return index + 2;
    }

    static void addMergeIn(@NonNull final Supplier supplier, @NonNull final Merger merger,
                           @NonNull final List<Object> directives) {
        directives.add(MERGE_IN);
        directives.add(supplier);
        directives.add(merger);
    }

    private int runMergeIn(@NonNull final Object[] directives, final int index) {
        final Supplier supplier = (Supplier) directives[index + 1];
        final Merger merger = (Merger) directives[index + 2];
        Log.i("CompiledRepository", "17:30/runMergeIn:");// yhongm 2017/04/10 17:30
        intermediateValue = checkNotNull(merger.merge(intermediateValue, supplier.get()));
        return index + 3;
    }

    static void addGoTo(@NonNull final Executor executor, @NonNull final List<Object> directives) {
        directives.add(GO_TO);
        directives.add(executor);
    }

    private int runGoTo(@NonNull final Object[] directives, final int index) {
        Executor executor = (Executor) directives[index + 1];
        executor.execute(this);
        return -1;
    }

    private static int continueFromGoTo(@NonNull final Object[] directives, final int index) {
        checkState(directives[index].equals(GO_TO), "Inconsistent directive state for goTo");
        return index + 2;
    }

    static void addGoLazy(@NonNull final List<Object> directives) {
        directives.add(GO_LAZY);
    }

    private static int continueFromGoLazy(@NonNull final Object[] directives, final int index) {
        checkState(directives[index].equals(GO_LAZY), "Inconsistent directive state for goLazy");
        return index + 1;
    }

    static void addSendTo(@NonNull final Receiver receiver, @NonNull final List<Object> directives) {
        directives.add(SEND_TO);
        directives.add(receiver);
    }

    private int runSendTo(@NonNull final Object[] directives, final int index) {
        Receiver receiver = (Receiver) directives[index + 1];
        Log.i("CompiledRepository", "18:14/runSendTo:initermediateValue:" + intermediateValue);// yhongm 2017/03/29 18:14
        receiver.accept(intermediateValue);
        return index + 2;
    }

    static void addEnd(final boolean skip, @NonNull final List<Object> directives) {
        directives.add(END);
        directives.add(skip);
    }

    private int runEnd(@NonNull final Object[] directives, final int index) {
        final boolean skip = (Boolean) directives[index + 1];
        if (skip) {
            skipAndEndFlow();
        } else {
            setNewValueAndEndFlow(intermediateValue);
        }
        return -1;
    }


    private void skipAndEndFlow() {
        Object discardedIntermediateValue = null;
        synchronized (this) {
            runState = IDLE;
            if (intermediateValue != currentValue) {
                discardedIntermediateValue = intermediateValue;
                intermediateValue = currentValue; // GC the intermediate value but keep field non-null.
            }
            checkRestartLocked();
        }
        if (discardedIntermediateValue != null) {
            discardedValuesDisposer.accept(discardedIntermediateValue);
        }
    }

    private synchronized void setNewValueAndEndFlow(@NonNull final Object newValue) {
        Object discardedIntermediateValue = null;
        synchronized (this) {
            final boolean wasRunningLazily = runState == RUNNING_LAZILY;
            runState = IDLE;
            Log.i("CompiledRepository", "18:31/setNewValueAndEndFlow:newValue:" + newValue);// yhongm 2017/03/29 18:31
            if (intermediateValue != newValue) {
                Log.i("CompiledRepository", "18:30/setNewValueAndEndFlow!=:newValue:" + newValue);// yhongm 2017/03/29 18:30
                discardedIntermediateValue = intermediateValue;
                intermediateValue = newValue; // GC the intermediate value but keep field non-null.
            }
            if (wasRunningLazily) {
                currentValue = newValue; // Don't notify if this new value is produced lazily
            } else {
                setNewValueLocked(newValue); // May notify otherwise
            }
            checkRestartLocked();
        }
        Log.i("CompiledRepository", "18:27/setNewValueAndEndFlow:");// yhongm 2017/03/29 18:27
        if (discardedIntermediateValue != null) {
            Log.i("CompiledRepository", "18:27/setNewValueAndEndFlow:!=null");// yhongm 2017/03/29 18:27
            discardedValuesDisposer.accept(discardedIntermediateValue);
        }
    }

    private void setNewValueLocked(@NonNull final Object newValue) {
        if (init) {
            final boolean shouldNotify = notifyChecker.merge(currentValue, newValue);
            currentValue = newValue;
            Log.i("CompiledRepository", "9:34/setNewValueLocked:" + shouldNotify + ",newValue:" + newValue);// yhongm 2017/03/30 9:34
            if (shouldNotify) {
                dispatchUpdate();
            }
        }
    }

    private void setPausedAtGoToLocked(final int resumeIndex) {
        lastDirectiveIndex = resumeIndex;
        runState = PAUSED_AT_GO_TO;
    }

    @Override
    public void run() {
        final Thread myThread = currentThread();
        final int index;
        synchronized (this) {
            Log.i("CompiledRepository", "17:26/run:");// yhongm 2017/03/30 17:26
            index = lastDirectiveIndex;
            checkState(runState == PAUSED_AT_GO_TO || runState == CANCEL_REQUESTED,
                    "Illegal call of Runnable.run()");
            lastDirectiveIndex = -1;

            if (checkCancellationLocked()) {
                return;
            }
            runState = RUNNING;
            // allow thread interruption (set this when still holding the lock)
            currentThread = myThread;
        }
        // leave the synchronization lock to run the rest of the flow
        runFlowFrom(continueFromGoTo(directives, index), true);
        // consume any unconsumed interrupted flag
        Thread.interrupted();
        // disallow interrupting the current thread, but chances are the next directive has started
        // asynchronously, so check currentThread is still this thread. This also works if a goTo
        // directive is given a synchronous executor, in which case the next part of the flow will
        // have been completed by now and currentThread will have been reset by that invocation of
        // runFlowFrom().
        synchronized (this) {
            if (currentThread == myThread) {
                currentThread = null;
            }
        }
    }

    private void setLazyAndEndFlowLocked(final int resumeIndex) {
        lastDirectiveIndex = resumeIndex;
        runState = PAUSED_AT_GO_LAZY;
        dispatchUpdate();
        checkRestartLocked();
    }

    @NonNull
    @Override
    public synchronized Object get() {
        if (runState == PAUSED_AT_GO_LAZY) {
            final int index = lastDirectiveIndex;
            runState = RUNNING_LAZILY;
            runFlowFrom(continueFromGoLazy(directives, index), false);
        }
        Log.i("CompiledRepository", "17:23/get:" + currentValue);// yhongm 2017/03/29 17:23
        return currentValue;
    }

}
