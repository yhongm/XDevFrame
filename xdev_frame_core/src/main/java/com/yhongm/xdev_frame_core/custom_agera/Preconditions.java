package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.NonNull;

public final class Preconditions {
  public static void checkState(final boolean expression, @NonNull final String errorMessage) {
    if (!expression) {
      throw new IllegalStateException(errorMessage);
    }
  }

  public static void checkArgument(final boolean expression, @NonNull final String errorMessage) {
    if (!expression) {
      throw new IllegalArgumentException(errorMessage);
    }
  }

  @SuppressWarnings("ConstantConditions")
  @NonNull
  public static <T> T checkNotNull(@NonNull final T object) {
    if (object == null) {
      throw new NullPointerException();
    }
    return object;
  }

  private Preconditions() {}
}
