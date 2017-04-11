package com.yhongm.xdev_frame_core.custom_agera;

import android.support.annotation.NonNull;

public interface Observable {

  void addUpdatable(@NonNull Updatable updatable);

  void removeUpdatable(@NonNull Updatable updatable);
}
