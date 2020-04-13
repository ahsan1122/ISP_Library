package com.esp.library.exceedersesp.controllers.tindercard.internal;

import android.view.animation.Interpolator;

import com.esp.library.exceedersesp.controllers.tindercard.Direction;


public interface AnimationSetting {
    Direction getDirection();
    int getDuration();
    Interpolator getInterpolator();
}
