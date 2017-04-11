package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.NonNull;

public interface ActivationHandler {

  void observableActivated(@NonNull UpdateDispatcher caller);

  void observableDeactivated(@NonNull UpdateDispatcher caller);
}
