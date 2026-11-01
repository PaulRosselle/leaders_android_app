package com.leaders.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import com.leaders.app.utilities.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {
    private static final String LOG_TAG = "BaseActivity";

    /**
     * Flag stored in the saved instance state once the snapshot has been written.
     * It tells a recreation of the activity (process death, configuration change) apart from
     * a fresh start, so that a leftover snapshot file is never loaded by mistake.
     */
    private static final String STATE_SNAPSHOT_SAVED = "BASE_ACTIVITY.SNAPSHOT_SAVED";

    protected List<Handler> handlersToInterruptOnFinish;

    /** Saved instance state given back by the system, {@code null} on a fresh start. */
    @Nullable
    private Bundle savedState;
    private boolean snapshotSaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        savedState = savedInstanceState;
        snapshotSaved = savedInstanceState != null &&
                savedInstanceState.getBoolean(STATE_SNAPSHOT_SAVED, false);

        if (!snapshotSaved && supportsSnapshot()) {
            // Fresh start: any remaining snapshot belongs to an abandoned session
            JsonUtils.deleteSnapshot(this, getSnapshotName());
        }

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

    //region SNAPSHOT METHODS

    /**
     * Indicates whether the work in progress must survive the destruction of the activity by
     * Android (process killed while the app is in background). Disabled by default.
     * When enabled, {@link #createSnapshot()} must be implemented and the activity must give
     * priority to {@link #loadSnapshot()} over its usual initial data.
     */
    protected boolean supportsSnapshot() {
        return false;
    }

    /**
     * Serializes the work in progress. Called each time the system asks the activity to save
     * its state. Must never be called by the activity itself.
     *
     * @return the snapshot, or {@code null} if there is nothing to save yet
     */
    @Nullable
    protected JSONObject createSnapshot() throws JSONException {
        return null;
    }

    /**
     * Returns the snapshot taken before the activity was destroyed by the system.
     *
     * @return the snapshot, or {@code null} on a fresh start: the activity must then use its
     * usual initial data
     */
    @Nullable
    protected final JSONObject loadSnapshot() {
        if (!snapshotSaved || !supportsSnapshot()) {
            return null;
        }
        return JsonUtils.loadSnapshot(this, getSnapshotName());
    }

    /** To be called when the content of a snapshot turns out to be unusable. */
    protected final void discardSnapshot() {
        JsonUtils.deleteSnapshot(this, getSnapshotName());
    }

    /**
     * Returns the small state saved along the snapshot (see {@link #onSaveInstanceState(Bundle)}).
     * Subclasses can use it to store what is needed to rebuild the context of the snapshot.
     *
     * @return the restored state, or {@code null} if there is no valid snapshot to restore
     */
    @Nullable
    protected final Bundle getRestoredState() {
        return snapshotSaved ? savedState : null;
    }

    /**
     * Returns the state saved by the previous instance of the activity, snapshot or not.
     * Meant for the small UI options, which do not need a snapshot.
     *
     * @return the saved state, or {@code null} on a fresh start
     */
    @Nullable
    protected final Bundle getSavedState() {
        return savedState;
    }

    @NonNull
    private String getSnapshotName() {
        return getActivityType().name();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!supportsSnapshot()) {
            return;
        }

        try {
            JSONObject joSnapshot = createSnapshot();
            if (joSnapshot != null) {
                // The snapshot is too big for a Bundle (size limit shared by the whole transaction),
                // so it goes to a file. The Bundle only keeps the flag telling that the file is valid.
                JsonUtils.saveSnapshot(this, getSnapshotName(), joSnapshot);
                outState.putBoolean(STATE_SNAPSHOT_SAVED, true);
            }
        } catch (IOException | JSONException | RuntimeException e) {
            // Saving the state must never crash the app. Without the flag, the activity
            // restarts from its initial data.
            Log.w(LOG_TAG, "Unable to save the snapshot", e);
        }
    }

    @Override
    protected void onDestroy() {
        // Leaving on purpose (not a destruction by the system): nothing to restore anymore
        if (isFinishing() && supportsSnapshot()) {
            JsonUtils.deleteSnapshot(this, getSnapshotName());
        }

        super.onDestroy();
    }

    //endregion

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