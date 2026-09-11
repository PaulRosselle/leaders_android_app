package com.leaders.app.activities.rules.tutorial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.activities.PlayableActivity;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;

public abstract class RulesTutorialActivity extends PlayableActivity {

    @Override
    protected int getBoardViewId() {
        return R.id.bdvBoard_actRulesTutorial;
    }

    @Override
    protected int getUndoLastActionButtonId() {
        return R.id.btnUndoLastAction_actRulesTutorial;
    }

    @Override
    protected int getCharacterNotificationViewId() {
        return R.id.cnvCardInfo_actRulesTutorial;
    }

    @Override
    protected int getEndGameViewId() {
        return R.id.egvEndGame_actRulesTutorial;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_rules_tutorial;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actRulesTutorial;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actRulesTutorial;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @Override
    public void onGameEnded(@NonNull Player winner) {
        // TODO
    }

    @Override
    public void onPhaseChanged(@NonNull GamePhase phase) {
        // TODO
    }
}