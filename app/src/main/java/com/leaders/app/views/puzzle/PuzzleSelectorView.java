package com.leaders.app.views.puzzle;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.leaders.R;
import com.leaders.app.views.selector.SelectorView;
import com.leaders.puzzlelogic.entities.PuzzleSave;

@SuppressLint("ViewConstructor")
public final class PuzzleSelectorView extends SelectorView<PuzzleSave> {
    private ImageView imvSolved;
    private TextView txvName;
    private TextView txvAuthor;

    private final String puzzleSolvedToast;

    public PuzzleSelectorView(@NonNull Context context, @NonNull PuzzleSave puzzleSave) {
        super(context, puzzleSave);

        puzzleSolvedToast = String.format(
                context.getString(puzzleSave.isSolved() ?
                        R.string.puzzle_solved_format : R.string.puzzle_unsolved_format),
                puzzleSave.getName()
        );
    }

    @Override
    protected void initViews() {
        super.initViews();

        imvSolved = findViewById(R.id.imvPuzzleSolved_vwPuzzleSelector);
        txvName = findViewById(R.id.txvPuzzleName_vwPuzzleSelector);
        txvAuthor = findViewById(R.id.txvPuzzleAuthor_vwPuzzleSelector);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void initListeners() {
        super.initListeners();

        imvSolved.setOnTouchListener(this::onImvSolvedTouch);
    }


    @Override
    protected void initDatas() {
        super.initDatas();

        PuzzleSave puzzleSave = getItem();

        imvSolved.setImageResource(puzzleSave.isSolved() ?
                R.drawable.icon_highlighted_crown : R.drawable.icon_empty_crown
        );

        txvName.setText(puzzleSave.getName());

        ConstraintLayout.LayoutParams txvNameLayoutParams = (ConstraintLayout.LayoutParams) txvName.getLayoutParams();

        if (!puzzleSave.getAuthor().isEmpty()) {
            txvNameLayoutParams.verticalBias = 0.0f;
            txvAuthor.setVisibility(VISIBLE);
            txvAuthor.setText(String.format(getResources().getString(R.string.by_author), puzzleSave.getAuthor()));
        } else {
            txvNameLayoutParams.verticalBias = 0.5f;
            txvAuthor.setVisibility(GONE);
        }

        txvName.setLayoutParams(txvNameLayoutParams);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.view_puzzle_selector;
    }

    @Override
    protected int getBtnMainResId() {
        return R.id.btnMain_vwPuzzleSelector;
    }

    @Override
    protected int getImvCheckedResId() {
        return R.id.imvChecked_vwPuzzleSelector;
    }

    @Override
    protected void updateCheckboxVisibleState() {
        imvChecked.setVisibility(isCheckboxVisible() ? VISIBLE : INVISIBLE);
    }

    @Override
    protected void updateCheckedState() {
        imvChecked.setImageResource(isChecked() ? R.drawable.checked_box : R.drawable.unchecked_box);

        int strokeWidth = getResources().getDimensionPixelSize(R.dimen.default_stroke_width);
        int backgroundColorId = R.color.darker_background;
        int strokeColorId = R.color.font;
        int textColorId = R.color.font;

        if (isChecked()) {
            strokeWidth *= 2;
            backgroundColorId = R.color.ultra_dark_background;
            strokeColorId = R.color.white;
            textColorId = R.color.white;
        }

        int textColor = getResources().getColor(textColorId, getContext().getTheme());

        btnMain.setStrokeWidth(strokeWidth);
        btnMain.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), backgroundColorId));
        btnMain.setStrokeColor(ContextCompat.getColorStateList(getContext(), strokeColorId));

        txvName.setTextColor(textColor);
        txvAuthor.setTextColor(textColor);
    }

    private boolean onImvSolvedTouch(View view, MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            Toast.makeText(view.getContext(), puzzleSolvedToast, Toast.LENGTH_SHORT).show();
        }

        MotionEvent forwardedEvent = MotionEvent.obtain(event);

        try {
            btnMain.dispatchTouchEvent(forwardedEvent);
        } finally {
            forwardedEvent.recycle();
        }

        return true;
    }

    @NonNull
    public PuzzleSave getPuzzleSave() {
        return getItem();
    }
}