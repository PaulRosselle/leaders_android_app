package com.leaders.app.activities.rules.tutorial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;

import com.leaders.app.activities.PlayableActivity;
import com.leaders.app.enums.ActivityType;
import com.leaders.gamelogic.entities.GamePhase;
import com.leaders.gamelogic.entities.Player;

public class RulesTutorialGameActivity extends PlayableActivity {


    @Override
    protected int getBoardViewId() {
        return R.id.bdvBoard_actRulesTutorialGame;
    }

    @Override
    protected int getUndoLastActionButtonId() {
        return R.id.btnUndoLastAction_actRulesTutorialGame;
    }

    @Override
    protected int getCharacterNotificationViewId() {
        return R.id.cnvCardInfo_actRulesTutorialGame;
    }

    @Override
    protected int getEndGameViewId() {
        return R.id.egvEndGame_actRulesTutorialGame;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_rules_tutorial_game;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actRulesTutorialGame;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actRulesTutorialGame;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.RulesTutorialGame;
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