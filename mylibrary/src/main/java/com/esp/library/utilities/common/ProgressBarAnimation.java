package com.esp.library.utilities.common;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ProgressBar;

/**
 * Created by aliirfanuppal on 13/11/18.
 */

public class ProgressBarAnimation extends Animation {
    private ProgressBar progressBar;
    float from = 0f;
    float to= 100f;

    public ProgressBarAnimation(ProgressBar progressBar) {
        super();
        this.progressBar = progressBar;

    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);
        float value = from + (to - from) * interpolatedTime;
        int progress = (int) value;
        this.progressBar.setProgress(progress);

    }



}
