package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class Observables {

    @NonNull
    public static Observable compositeObservable(@NonNull final Observable... observables) {
        return compositeObservable(0, observables);
    }

    @NonNull
    static Observable compositeObservable(final int shortestUpdateWindowMillis,
                                          @NonNull final Observable... observables) {
        if (observables.length == 0) {
            return new CompositeObservable(shortestUpdateWindowMillis);
        }

        if (observables.length == 1) {
            final Observable singleObservable = observables[0];
            if (singleObservable instanceof CompositeObservable
                    && ((CompositeObservable) singleObservable).shortestUpdateWindowMillis == 0) {
                return new CompositeObservable(shortestUpdateWindowMillis,
                        ((CompositeObservable) singleObservable).observables);
            } else {
                return new CompositeObservable(shortestUpdateWindowMillis, singleObservable);
            }
        }

        final List<Observable> flattenedDedupedObservables = new ArrayList<>();
        for (final Observable observable : observables) {
            if (observable instanceof CompositeObservable
                    && ((CompositeObservable) observable).shortestUpdateWindowMillis == 0) {
                for (Observable subObservable : ((CompositeObservable) observable).observables) {
                    if (!flattenedDedupedObservables.contains(subObservable)) {
                        flattenedDedupedObservables.add(subObservable);
                    }
                }
            } else {
                if (!flattenedDedupedObservables.contains(observable)) {
                    flattenedDedupedObservables.add(observable);
                }
            }
        }
        return new CompositeObservable(shortestUpdateWindowMillis,
                flattenedDedupedObservables.toArray(new Observable[flattenedDedupedObservables.size()]));
    }


    @NonNull
    public static Observable perMillisecondObservable(
            final int shortestUpdateWindowMillis, @NonNull final Observable... observables) {
        return compositeObservable(shortestUpdateWindowMillis, observables);
    }

    @NonNull
    public static Observable perLoopObservable(@NonNull final Observable... observables) {
        return compositeObservable(observables);
    }

    @NonNull
    public static UpdateDispatcher updateDispatcher() {
        return new AsyncUpdateDispatcher(null);
    }

    @NonNull
    public static UpdateDispatcher updateDispatcher(
            @NonNull final ActivationHandler activationHandler) {
        return new AsyncUpdateDispatcher(activationHandler);
    }

    private static final class CompositeObservable extends BaseObservable implements Updatable {
        @NonNull
        private final Observable[] observables;

        CompositeObservable(final int shortestUpdateWindowMillis,
                            @NonNull final Observable... observables) {
            super(shortestUpdateWindowMillis);
            this.observables = observables;
        }

        @Override
        protected void observableActivated() {
            for (final Observable observable : observables) {
                observable.addUpdatable(this);
            }
        }

        @Override
        protected void observableDeactivated() {
            for (final Observable observable : observables) {
                observable.removeUpdatable(this);
            }
        }

        @Override
        public void update() {
            dispatchUpdate();
        }
    }


    private static final class AsyncUpdateDispatcher extends BaseObservable
            implements UpdateDispatcher {

        @Nullable
        private final ActivationHandler activationHandler;

        private AsyncUpdateDispatcher(@Nullable final ActivationHandler activationHandler) {
            this.activationHandler = activationHandler;
        }

        @Override
        protected void observableActivated() {
            if (activationHandler != null) {
                activationHandler.observableActivated(this);
            }
        }

        @Override
        protected void observableDeactivated() {
            if (activationHandler != null) {
                activationHandler.observableDeactivated(this);
            }
        }

        @Override
        public void update() {
            dispatchUpdate();
        }
    }

    private Observables() {
    }
}
