package com.leaders.app.activities.rules;

import com.leaders.app.activities.BaseActivity;
import com.leaders.app.views.rules.RulesNavigationView;

public abstract class RulesActivity extends BaseActivity implements RulesNavigationView.IRulesNavigation {
    private RulesNavigationView rnvNavigation;

    @Override
    protected void initViews() {
        super.initViews();

        rnvNavigation = findViewById(getRnvNavigationResId());
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        rnvNavigation.setNavigator(this);
    }

    @Override
    protected boolean isImmersiveActivity() {
        return true;
    }

    @Override
    protected boolean overrideOnBackPressed() {
        return true;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    protected abstract int getRnvNavigationResId();

    @Override
    public BaseActivity getActivity() {
        return this;
    }
}
