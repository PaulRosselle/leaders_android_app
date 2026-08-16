package com.leaders.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.leaders.R;
import com.leaders.app.views.MainMenuView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        startLightAnimation();

        MainMenuView mmvMainMenu = findViewById(R.id.mmvMainMenu_actMain);
        mmvMainMenu.setOnPuzzlesClickListener(this::btnNotImplementedClick);
        mmvMainMenu.setOnPlayClickListener(this::btnNotImplementedClick);
        mmvMainMenu.setOnReplayClickListener(this::btnNotImplementedClick);
        mmvMainMenu.setOnRulesClickListener(this::btnNotImplementedClick);
        mmvMainMenu.setOnSettingsClickListener(this::btnNotImplementedClick);
    }

    private void startLightAnimation() {
        ImageView imvLightRays = findViewById(R.id.imvLightRays_actMain);
        ObjectAnimator raysRotation = getAnimator(imvLightRays, View.ROTATION, 60000,
                new AccelerateDecelerateInterpolator(),
                ValueAnimator.RESTART, 0f, 360f);
        ObjectAnimator raysScaleX = getAnimator(imvLightRays, View.SCALE_X, 5000,
                new AccelerateDecelerateInterpolator(),
                ValueAnimator.REVERSE, 1.2f, 1.5f, 1.2f);
        ObjectAnimator raysScaleY = getAnimator(imvLightRays, View.SCALE_Y, 5000,
                new AccelerateDecelerateInterpolator(),
                ValueAnimator.REVERSE, 1.2f, 1.5f, 1.2f);

        ImageView imvLightHalo = findViewById(R.id.imvLightHalo_actMain);
        ObjectAnimator haloScaleX = getAnimator(imvLightHalo, View.SCALE_X, 8000,
                new AccelerateDecelerateInterpolator(),
                ValueAnimator.RESTART, 1.15f, 1.25f, 1.15f);
        ObjectAnimator haloScaleY = getAnimator(imvLightHalo, View.SCALE_Y, 8000,
                new AccelerateDecelerateInterpolator(),
                ValueAnimator.RESTART, 1.15f, 1.25f, 1.15f);

        AnimatorSet lightAnimations = new AnimatorSet();
        lightAnimations.playTogether(raysRotation, raysScaleX, raysScaleY, haloScaleX, haloScaleY);
        lightAnimations.start();
    }

    private ObjectAnimator getAnimator(@NonNull ImageView imvToAnimate,
                                       @NonNull Property<View, Float> property,
                                       int duration,
                                       Interpolator interpolator,
                                       int repeatMode,
                                       float... values) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(imvToAnimate, property, values);
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(repeatMode);
        return animator;
    }

    private void btnNotImplementedClick(View v) {
        Toast.makeText(v.getContext(), "Not implemented", Toast.LENGTH_SHORT).show();
    }
}