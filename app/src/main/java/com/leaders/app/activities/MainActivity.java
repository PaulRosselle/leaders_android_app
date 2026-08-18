package com.leaders.app.activities;

import androidx.annotation.NonNull;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.Property;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.leaders.R;
import com.leaders.app.entities.crash.CrashLog;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.mainmenu.CrashLogView;
import com.leaders.app.views.mainmenu.MainMenuView;

public final class MainActivity extends BaseActivity {
    private CrashLogView clvCrashDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startLightAnimation();
    }

    @Override
    protected void initViews() {
        super.initViews();

        clvCrashDialog = findViewById(R.id.clvCrashDialog_actMain);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        MainMenuView mmvMainMenu = findViewById(R.id.mmvMainMenu_actMain);
        mmvMainMenu.setOnPuzzlesClickListener(v -> goToActivity(ActivityType.PuzzleSelection));
        mmvMainMenu.setOnPlayClickListener(this::btnNotImplementedClick);
        mmvMainMenu.setOnReplayClickListener(this::btnNotImplementedClick);
        mmvMainMenu.setOnRulesClickListener(this::btnNotImplementedClick);
        mmvMainMenu.setOnSettingsClickListener(this::btnNotImplementedClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        CrashLog crashLog = JsonUtils.loadCrashLog(this);
        if (crashLog != null) {
            clvCrashDialog.show(crashLog);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actMain;
    }

    @Override
    protected Integer getBtnBackResId() {
        return null;
    }

    @Override
    protected boolean isImmersiveActivity() {
        return false;
    }

    @Override
    protected boolean overrideOnBackPressed() {
        return false;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.Main;
    }

    private void startLightAnimation() {
        ImageView imvLightRays = findViewById(R.id.imvLightRays_actMain);
        ObjectAnimator raysRotation = getAnimator(imvLightRays, View.ROTATION, 60000,
                new LinearInterpolator(), ValueAnimator.RESTART, 0f, 360f);
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

    private ObjectAnimator getAnimator(@NonNull ImageView imvToAnimate, @NonNull Property<View, Float> property,
                                       int duration, @NonNull Interpolator interpolator, int repeatMode,
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