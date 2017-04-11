package com.yhongm.xdev_frame_core.custom_agera;

import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import static com.yhongm.xdev_frame_core.custom_agera.Common.NULL_OPERATOR;
import static com.yhongm.xdev_frame_core.custom_agera.CompiledRepository.addEnd;
import static com.yhongm.xdev_frame_core.custom_agera.CompiledRepository.addGetFrom;
import static com.yhongm.xdev_frame_core.custom_agera.CompiledRepository.addGoLazy;
import static com.yhongm.xdev_frame_core.custom_agera.CompiledRepository.addGoTo;
import static com.yhongm.xdev_frame_core.custom_agera.CompiledRepository.addMergeIn;
import static com.yhongm.xdev_frame_core.custom_agera.CompiledRepository.addSendTo;
import static com.yhongm.xdev_frame_core.custom_agera.CompiledRepository.compiledRepository;
import static com.yhongm.xdev_frame_core.custom_agera.Mergers.objectsUnequal;
import static com.yhongm.xdev_frame_core.custom_agera.Preconditions.checkNotNull;
import static com.yhongm.xdev_frame_core.custom_agera.Preconditions.checkState;

@SuppressWarnings({"unchecked, rawtypes"})
final class RepositoryCompiler implements
        RepositoryCompilerStates.RFrequency,
        RepositoryCompilerStates.RFlow,
        RepositoryCompilerStates.RTerminationOrContinue,
        RepositoryCompilerStates.RConfig {

    private static final ThreadLocal<RepositoryCompiler> compilers = new ThreadLocal<>();

    @NonNull
    static <TVal> RepositoryCompilerStates.REventSource<TVal, TVal> repositoryWithInitialValue(
            @NonNull final TVal initialValue) {
        checkNotNull(Looper.myLooper());
        RepositoryCompiler compiler = compilers.get();
        if (compiler == null) {
            compiler = new RepositoryCompiler();
        } else {
            // Remove compiler from the ThreadLocal to prevent reuse in the middle of a compilation.
            // recycle(), called by compile(), will return the compiler here. ThreadLocal.set(null) keeps
            // the entry (with a null value) whereas remove() removes the entry; because we expect the
            // return of the compiler, don't use the heavier remove().
            compilers.set(null);
        }
        return compiler.start(initialValue);
    }

    private static void recycle(@NonNull final RepositoryCompiler compiler) {
        compilers.set(compiler);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NOTHING, FIRST_EVENT_SOURCE, FREQUENCY_OR_MORE_EVENT_SOURCE, FLOW,
            TERMINATE_THEN_FLOW, TERMINATE_THEN_END, CONFIG})
    private @interface Expect {
    }

    private static final int NOTHING = 0;
    private static final int FIRST_EVENT_SOURCE = 1;
    private static final int FREQUENCY_OR_MORE_EVENT_SOURCE = 2;
    private static final int FLOW = 3;
    private static final int TERMINATE_THEN_FLOW = 4;
    private static final int TERMINATE_THEN_END = 5;
    private static final int CONFIG = 6;

    private Object initialValue;
    private final ArrayList<Observable> eventSources = new ArrayList<>();
    private int frequency;
    private final ArrayList<Object> directives = new ArrayList<>();
    private boolean goLazyUsed;
    private Merger notifyChecker = objectsUnequal();
    @RepositoryConfig
    private int deactivationConfig;
    @RepositoryConfig
    private int concurrentUpdateConfig;
    @NonNull
    private Receiver discardedValueDisposer = NULL_OPERATOR;

    @Expect
    private int expect;

    private RepositoryCompiler() {
    }

    @NonNull
    private RepositoryCompiler start(@NonNull final Object initialValue) {
        checkExpect(NOTHING);
        expect = FIRST_EVENT_SOURCE;
        this.initialValue = initialValue;
        return this;
    }

    private void checkExpect(@Expect final int accept) {
        checkState(expect == accept, "Unexpected compiler state");
    }

    private void checkExpect(@Expect final int accept1, @Expect final int accept2) {
        checkState(expect == accept1 || expect == accept2, "Unexpected compiler state");
    }

    private void checkGoLazyUnused() {
        checkState(!goLazyUsed, "Unexpected occurrence of async directive after goLazy()");
    }

    //region REventSource

    @NonNull
    @Override
    public RepositoryCompiler observe(@NonNull final Observable... observables) {
        checkExpect(FIRST_EVENT_SOURCE, FREQUENCY_OR_MORE_EVENT_SOURCE);
        for (Observable observable : observables) {
            eventSources.add(checkNotNull(observable));
        }
        expect = FREQUENCY_OR_MORE_EVENT_SOURCE;
        return this;
    }

    //endregion REventSource

    //region RFrequency

    @NonNull
    @Override
    public RepositoryCompiler onUpdatesPer(int millis) {
        checkExpect(FREQUENCY_OR_MORE_EVENT_SOURCE);
        frequency = Math.max(0, millis);
        expect = FLOW;
        return this;
    }

    @NonNull
    @Override
    public RepositoryCompiler onUpdatesPerLoop() {
        return onUpdatesPer(0);
    }


    @NonNull
    @Override
    public RepositoryCompiler getFrom(@NonNull final Supplier supplier) {
        checkExpect(FLOW);
        Log.i("RepositoryCompiler", "14:40/getFrom:" + supplier.getClass().getName());// yhongm 2017/03/24 14:40
        addGetFrom(supplier, directives);
        return this;
    }

    @NonNull
    @Override
    public RepositoryCompiler mergeIn(@NonNull final Supplier supplier,
                                      @NonNull final Merger merger) {
        checkExpect(FLOW);
        addMergeIn(supplier, merger, directives);
        return this;
    }


    @NonNull
    @Override
    public RepositoryCompiler sendTo(@NonNull final Receiver receiver) {
        checkExpect(FLOW);
        addSendTo(checkNotNull(receiver), directives);
        return this;
    }

    @NonNull
    @Override
    public RepositoryCompiler thenSkip() {
        endFlow(true);
        return this;
    }

    @NonNull
    @Override
    public RepositoryCompiler thenGetFrom(@NonNull final Supplier supplier) {
        getFrom(supplier);
        endFlow(false);
        return this;
    }



    private void endFlow(final boolean skip) {
        addEnd(skip, directives);
        expect = CONFIG;
    }

    @NonNull
    @Override
    public RepositoryCompiler attemptGetFrom(@NonNull final Supplier attemptSupplier) {
        getFrom(attemptSupplier);
        expect = TERMINATE_THEN_FLOW;
        return this;
    }

    @NonNull
    @Override
    public RepositoryCompiler attemptMergeIn(
            @NonNull final Supplier supplier, @NonNull final Merger attemptMerger) {
        mergeIn(supplier, attemptMerger);
        expect = TERMINATE_THEN_FLOW;
        return this;
    }


    @NonNull
    @Override
    public RepositoryCompiler thenAttemptGetFrom(@NonNull final Supplier attemptSupplier) {
        getFrom(attemptSupplier);
        expect = TERMINATE_THEN_END;
        return this;
    }

    @NonNull
    @Override
    public RepositoryCompiler thenAttemptMergeIn(
            @NonNull final Supplier supplier, @NonNull final Merger attemptMerger) {
        mergeIn(supplier, attemptMerger);
        expect = TERMINATE_THEN_END;
        return this;
    }


    @NonNull
    @Override
    public RepositoryCompiler goTo(@NonNull final Executor executor) {
        checkExpect(FLOW);
        checkGoLazyUnused();
        addGoTo(executor, directives);
        return this;
    }

    @NonNull
    @Override
    public RepositoryCompiler goLazy() {
        checkExpect(FLOW);
        checkGoLazyUnused();
        addGoLazy(directives);
        goLazyUsed = true;
        return this;
    }

    @NonNull
    @Override
    public RepositoryCompiler notifyIf(@NonNull final Merger notifyChecker) {
        checkExpect(CONFIG);
        this.notifyChecker = checkNotNull(notifyChecker);
        return this;
    }

    @NonNull
    @Override
    public RepositoryCompiler onDeactivation(@RepositoryConfig final int deactivationConfig) {
        checkExpect(CONFIG);
        this.deactivationConfig = deactivationConfig;
        return this;
    }

    @NonNull
    @Override
    public RepositoryCompiler onConcurrentUpdate(@RepositoryConfig final int concurrentUpdateConfig) {
        checkExpect(CONFIG);
        this.concurrentUpdateConfig = concurrentUpdateConfig;
        return this;
    }

    @NonNull
    @Override
    public RepositoryCompiler sendDiscardedValuesTo(@NonNull final Receiver disposer) {
        checkExpect(CONFIG);
        discardedValueDisposer = checkNotNull(disposer);
        return this;
    }

    @NonNull
    @Override
    public Repository compile() {
        Repository repository = compileRepositoryAndReset();
        recycle(this);
        return repository;
    }

    @NonNull
    @Override
    public RepositoryCompiler compileIntoRepositoryWithInitialValue(@NonNull final Object value) {
        Repository repository = compileRepositoryAndReset();
        // Don't recycle, instead sneak in the first directive and start the second repository
        addGetFrom(repository, directives);
        return start(value).observe(repository);
    }

    @NonNull
    private Repository compileRepositoryAndReset() {
        checkExpect(CONFIG);
        Repository repository = compiledRepository(initialValue, eventSources, frequency, directives,
                notifyChecker, concurrentUpdateConfig, deactivationConfig, discardedValueDisposer);
        expect = NOTHING;
        initialValue = null;
        eventSources.clear();
        frequency = 0;
        directives.clear();
        goLazyUsed = false;
        notifyChecker = objectsUnequal();
        deactivationConfig = RepositoryConfig.CONTINUE_FLOW;
        concurrentUpdateConfig = RepositoryConfig.CONTINUE_FLOW;
        discardedValueDisposer = NULL_OPERATOR;
        return repository;
    }

    //endregion RConfig
}
