package com.leaders.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.leaders.R;
import com.leaders.app.enums.ActivityTransitionType;
import com.leaders.app.enums.ActivityType;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {
    protected List<Handler> handlersToInterruptOnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handlersToInterruptOnFinish = new ArrayList<>();
        // Handle system bar insets manually using the root guideline.
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(getLayoutResId());

        initViews();
        initListeners();
        initDatas();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (isImmersiveActivity() && hasFocus) {
            applyImmersiveMode();
        }
    }

    /**
     * Initializes the activity views.
     * Called after the activity layout has been set.
     */
    @CallSuper
    protected void initViews() {
        // Dynamically positions the root guideline below the status bar area.
        Guideline gdlRoot = findViewById(getRootGuidelineResId());
        ViewCompat.setOnApplyWindowInsetsListener(gdlRoot, (v, insets) -> {
            ((Guideline) v).setGuidelineBegin(insets.getInsets(WindowInsetsCompat.Type.statusBars()).top);
            return insets;
        });
        ViewCompat.requestApplyInsets(gdlRoot);
    }

    /**
     * Initializes the activity listeners and back navigation behavior.
     * Called after the activity views have been initialized.
     */
    @CallSuper
    protected void initListeners() {
        if (overrideOnBackPressed()) {
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    doOnBackPressed();
                }
            });
        }
        if (getBtnBackResId() != null) {
            findViewById(getBtnBackResId()).setOnClickListener(this::btnBackClick);
        }
    }

    /**
     * Initializes the activity data.
     * Called after the activity listeners have been initialized.
     */
    @CallSuper
    protected void initDatas() {
        // No default implementation
    }

    /**
     * Returns the resource ID of the activity layout.
     *
     * @return the layout resource ID
     */
    protected abstract int getLayoutResId();

    /**
     * Returns the resource ID of the root view.
     *
     * @return the root view resource ID
     */
    protected abstract int getRootGuidelineResId();

    /**
     * Returns the resource ID of the button used to navigate back.
     *
     * @return the button resource ID, or {@code null} if the activity has no back button
     */
    @Nullable
    protected abstract Integer getBtnBackResId();

    /**
     * Indicates whether the activity should use immersive mode.
     *
     * @return {@code true} if immersive mode should be enabled
     */
    protected abstract boolean isImmersiveActivity();

    /**
     * Indicates whether the activity should override the default back navigation behavior.
     *
     * @return {@code true} if the activity handles back navigation itself
     */
    protected abstract boolean overrideOnBackPressed();

    /**
     * Indicates whether navigating back to the main activity requires user confirmation.
     *
     * @return {@code true} if confirmation should be requested before leaving the activity
     */
    protected abstract boolean askForConfirmationBeforeFinish();

    /**
     * Returns the type identifying this activity.
     *
     * @return this activity's type
     */
    @NonNull
    public abstract ActivityType getActivityType();

    private void applyImmersiveMode() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Configure the behavior of the hidden navigation bar.
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
    }

    /**
     * Applies the activity transition using the legacy {@code overridePendingTransition} API.
     *
     * <p>This application uses a linear navigation model where the current activity is
     * systematically finished before navigating to the next one. As a result, activities
     * are not kept in the back stack and the application does not rely on Android's
     * predictive back navigation model.</p>
     *
     * <p>The newer {@code overrideActivityTransition} API is primarily designed around
     * separate OPEN and CLOSE transitions, which are useful when navigating through an
     * activity back stack and supporting predictive back. In this application, this model
     * provides no practical benefit and would unnecessarily complicate the existing
     * transition handling, which only requires a single enter/exit animation pair for
     * each navigation.</p>
     *
     * <p>We therefore intentionally keep using {@code overridePendingTransition} while
     * the API remains supported by Android. The deprecated API is isolated in this method
     * so that it can be replaced easily if it is eventually removed or becomes
     * incompatible with future Android versions.</p>
     */
    @SuppressWarnings("deprecation")
    private void applyTransition(@NonNull ActivityTransitionType transitionType) {
        overridePendingTransition(
                transitionType.getEnterAnimation(),
                transitionType.getExitAnimation()
        );
    }

    /**
     * Navigates to the specified activity using the given transition.
     *
     * @param activityType the activity to navigate to
     * @param transitionType the transition to use
     */
    public final void goToActivity(@NonNull ActivityType activityType,
                                   @NonNull ActivityTransitionType transitionType) {
        goToActivity(activityType.getIntent(this), transitionType);
    }

    /**
     * Navigates to the specified activity using the default transition.
     *
     * @param activityType the activity to navigate to
     */
    public final void goToActivity(@NonNull ActivityType activityType) {
        // The default activity transition animation is SlideRight
        goToActivity(activityType.getIntent(this), ActivityTransitionType.SlideRight);
    }

    /**
     * Navigates to the activity specified by the intent using the default transition.
     *
     * @param intent the intent describing the activity to navigate to
     */
    public final void goToActivity(@NonNull Intent intent) {
        // The default activity transition animation is SlideRight
        goToActivity(intent, ActivityTransitionType.SlideRight);
    }

    /**
     * Navigates to the activity specified by the intent using the given transition.
     *
     * @param intent the intent describing the activity to navigate to
     * @param transitionType the transition to use
     */
    public final void goToActivity(@NonNull Intent intent, @NonNull ActivityTransitionType transitionType) {
        startActivity(intent);
        applyTransition(transitionType);
        finish();
    }

    /**
     * Navigates to the main activity using the default back navigation transition.
     */
    protected final void goBackToMainActivity() {
        goToActivity(new Intent(this, MainActivity.class), ActivityTransitionType.SlideLeft);
    }

    /**
     * Handles navigation to the main activity when the back action is triggered.
     * Implementations may require user confirmation before navigating away.
     */
    protected void doOnBackPressed() {
        if (askForConfirmationBeforeFinish()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.alert_dialog_theme);
            builder.setTitle(R.string.back);
            builder.setMessage(R.string.go_back_to_main_menu);
            builder.setPositiveButton(R.string.yes, (dialog, which) -> goBackToMainActivity());
            builder.setNegativeButton(R.string.no, null);
            builder.show();
        } else {
            goBackToMainActivity();
        }
    }

    @Override
    public void finish() {
        for (Handler handler : handlersToInterruptOnFinish) {
            handler.removeCallbacksAndMessages(null);
        }

        super.finish();
    }

    /**
     * Handles the click on the activity's back button.
     *
     * @param v the clicked view
     */
    private void btnBackClick(View v) {
        doOnBackPressed();
    }
}
