package com.leaders.app.utilities;

import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.views.board.BoardView;
import com.leaders.app.views.duel.PlayerBottomView;

import java.util.List;

public class AlignmentUtils {
    private AlignmentUtils(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static void alignBoardView(boolean alignBottom,
                                      @NonNull BoardView bdvBoard,
                                      @NonNull PlayerBottomView pbvPlayer,
                                      @NonNull List<View> boundedViews,
                                      @NonNull List<View> fadingViews) {
        ConstraintLayout parent = (ConstraintLayout) bdvBoard.getParent();
        TransitionSet transition = new TransitionSet();

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.addTarget(bdvBoard);
        changeBounds.addTarget(pbvPlayer);
        for (View boundedView : boundedViews) {
            changeBounds.addTarget(boundedView);
        }
        changeBounds.excludeTarget(R.id.txvPlayerName_vwPlayerBottom, true);
        transition.addTransition(changeBounds);

        if (!fadingViews.isEmpty()) {
            Fade fade = new Fade();
            for (View fadingView : fadingViews) {
                fade.addTarget(fadingView);
            }
            transition.addTransition(fade);
        }

        transition.setDuration(300);
        TransitionManager.beginDelayedTransition(parent, transition);

        ConstraintLayout.LayoutParams boardParams = (ConstraintLayout.LayoutParams) bdvBoard.getLayoutParams();
        ConstraintLayout.LayoutParams playerViewParams = (ConstraintLayout.LayoutParams) pbvPlayer.getLayoutParams();
        // When recruiting, every view is aligned on top of each other
        if (alignBottom) {
            boardParams.verticalBias = 0f;
            playerViewParams.verticalBias = 0f;
            float dpRatio = bdvBoard.getResources().getDisplayMetrics().density;
            int boardHeight = bdvBoard.getMeasuredHeight();
            float playerHeaderHeight = boardHeight * (72f / 1177f);
            int boardMargin = 8;
            int playerViewMargin = boardMargin + 8;

            boardParams.topMargin = (int) (playerHeaderHeight + boardMargin * dpRatio);
            playerViewParams.topMargin = (int) (boardHeight - pbvPlayer.getMeasuredHeight() +
                    playerHeaderHeight * 2 + playerViewMargin * dpRatio);

        // By default, each playerView is on a vertical extremity while the board is centered
        } else {
            boardParams.verticalBias = 0.5f;
            playerViewParams.verticalBias = 1f;
            boardParams.topMargin = 0;
            playerViewParams.topMargin = 0;
        }
        bdvBoard.setLayoutParams(boardParams);
        pbvPlayer.setLayoutParams(playerViewParams);
    }
}
