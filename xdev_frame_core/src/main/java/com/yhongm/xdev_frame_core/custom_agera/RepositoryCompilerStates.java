package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

public interface RepositoryCompilerStates {

  interface REventSource<TVal, TStart> {

    @NonNull
    RFrequency<TVal, TStart> observe(@NonNull Observable... observables);
  }

  interface RFrequency<TVal, TStart> extends REventSource<TVal, TStart> {

    @NonNull
    RFlow<TVal, TStart, ?> onUpdatesPer(int millis);
    @NonNull
    RFlow<TVal, TStart, ?> onUpdatesPerLoop();
  }

  interface RFlow<TVal, TPre, TSelf extends RFlow<TVal, TPre, TSelf>>
      extends RSyncFlow<TVal, TPre, TSelf> {

    @NonNull
    @Override
    <TCur> RFlow<TVal, TCur, ?> getFrom(@NonNull Supplier<TCur> supplier);

    @NonNull
    @Override
    <TCur> RTermination<TVal, Throwable, RFlow<TVal, TCur, ?>> attemptGetFrom(
            @NonNull Supplier<Result<TCur>> attemptSupplier);

    @NonNull
    @Override
    RTerminationOrContinue<TVal, Throwable, RConfig<TVal>,
        RFlow<TVal, Throwable, ?>> thenAttemptGetFrom(
            @NonNull Supplier<? extends Result<? extends TVal>> attemptSupplier);

    @NonNull
    @Override
    <TAdd, TCur> RFlow<TVal, TCur, ?> mergeIn(@NonNull Supplier<TAdd> supplier,
                                              @NonNull Merger<? super TPre, ? super TAdd, TCur> merger);

    @NonNull
    @Override
    <TAdd, TCur> RTermination<TVal, Throwable, RFlow<TVal, TCur, ?>> attemptMergeIn(
            @NonNull Supplier<TAdd> supplier,
            @NonNull Merger<? super TPre, ? super TAdd, Result<TCur>> attemptMerger);

    @NonNull
    @Override
    <TAdd> RTerminationOrContinue<TVal, Throwable, RConfig<TVal>,
        RFlow<TVal, Throwable, ?>> thenAttemptMergeIn(
            @NonNull Supplier<TAdd> supplier,
            @NonNull Merger<? super TPre, ? super TAdd,
                    ? extends Result<? extends TVal>> attemptMerger);



    @NonNull
    TSelf goTo(@NonNull Executor executor);

    @NonNull
    RSyncFlow<TVal, TPre, ?> goLazy();
  }

  interface RSyncFlow<TVal, TPre, TSelf extends RSyncFlow<TVal, TPre, TSelf>> {

    @NonNull
    <TCur> RSyncFlow<TVal, TCur, ?> getFrom(@NonNull Supplier<TCur> supplier);

    @NonNull
    <TCur>
    RTermination<TVal, Throwable, ? extends RSyncFlow<TVal, TCur, ?>> attemptGetFrom(
            @NonNull Supplier<Result<TCur>> attemptSupplier);

    @NonNull
    <TAdd, TCur> RSyncFlow<TVal, TCur, ?> mergeIn(@NonNull Supplier<TAdd> supplier,
                                                  @NonNull Merger<? super TPre, ? super TAdd, TCur> merger);

    @NonNull
    <TAdd, TCur>
    RTermination<TVal, Throwable, ? extends RSyncFlow<TVal, TCur, ?>> attemptMergeIn(
            @NonNull Supplier<TAdd> supplier,
            @NonNull Merger<? super TPre, ? super TAdd, Result<TCur>> attemptMerger);


    @NonNull
    TSelf sendTo(@NonNull Receiver<? super TPre> receiver);

    @NonNull
    RConfig<TVal> thenSkip();

    @NonNull
    RConfig<TVal> thenGetFrom(@NonNull Supplier<? extends TVal> supplier);

    @NonNull
    RTerminationOrContinue<TVal, Throwable, RConfig<TVal>,
        ? extends RSyncFlow<TVal, Throwable, ?>> thenAttemptGetFrom(
            @NonNull Supplier<? extends Result<? extends TVal>> attemptSupplier);


    @NonNull
    <TAdd> RTerminationOrContinue<TVal, Throwable, RConfig<TVal>,
        ? extends RSyncFlow<TVal, Throwable, ?>> thenAttemptMergeIn(
            @NonNull Supplier<TAdd> supplier,
            @NonNull Merger<? super TPre, ? super TAdd,
                    ? extends Result<? extends TVal>> attemptMerger);

  }

  interface RTermination<TVal, TTerm, TRet> {

  }


  interface RTerminationOrContinue<TVal, TTerm, TRet, TCon>
      extends RTermination<TVal, TTerm, TRet> {

  }

  interface RConfig<TVal> {

    @NonNull
    RConfig<TVal> notifyIf(@NonNull Merger<? super TVal, ? super TVal, Boolean> checker);

    @NonNull
    RConfig<TVal> onDeactivation(@RepositoryConfig int deactivationConfig);

    @NonNull
    RConfig<TVal> onConcurrentUpdate(@RepositoryConfig int concurrentUpdateConfig);

    @NonNull
    RConfig<TVal> sendDiscardedValuesTo(@NonNull Receiver<Object> disposer);

    @NonNull
    Repository<TVal> compile();

    @NonNull
    <TVal2> RFrequency<TVal2, TVal> compileIntoRepositoryWithInitialValue(@NonNull TVal2 value);
  }
}
