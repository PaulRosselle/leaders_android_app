package com.leaders.app.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.leaders.R;
import com.leaders.puzzlelogic.entities.OfficialPuzzleSave;
import com.leaders.puzzlelogic.entities.PuzzleSave;

import org.json.JSONObject;

public final class PuzzleSelectorView extends ConstraintLayout {
    public interface OnPuzzleClickListener {
        void onPuzzleClick(@NonNull PuzzleSelectorView psvSender);
    }

    public interface OnPuzzleLongClickListener {
        boolean onPuzzleLongClick(@NonNull PuzzleSelectorView psvSender);
    }

    private MaterialButton btnMain;
    private ImageView imvSolved;
    private ImageView imvChecked;
    private boolean checkboxVisible;
    private boolean isChecked;
    private TextView txvName;
    private TextView txvAuthor;
    private final PuzzleSave puzzleSave;
    private final String puzzleSolvedToast;


    @Nullable
    private OnPuzzleClickListener onPuzzleClickListener;
    @Nullable
    private OnPuzzleLongClickListener onPuzzleLongClickListener;


    public PuzzleSelectorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        // Dummy constructor to allow instanciation in editor
        this(context, new OfficialPuzzleSave(context, 1, new JSONObject(), false));
    }

    public PuzzleSelectorView(@NonNull Context context, @NonNull PuzzleSave puzzleSave) {
        super(context, null);
        this.puzzleSave = puzzleSave;
        puzzleSolvedToast = String.format(context.getString(
                puzzleSave.isSolved() ? R.string.puzzle_solved_format : R.string.puzzle_unsolved_format),
                puzzleSave.getName()
        );

        inflate(context, R.layout.view_puzzle_selector, this);

        initViews();
        initListeners();
        loadPuzzleSave();

        // We apply the default selection behavior
        setCheckboxVisible(false);
        setChecked(false);
    }



    private void initViews() {
        imvSolved = findViewById(R.id.imvPuzzleSolved_vwPuzzleSelector);
        imvChecked = findViewById(R.id.imvChecked_vwPuzzleSelector);
        btnMain = findViewById(R.id.btnMain_vwPuzzleSelector);
        txvName = findViewById(R.id.txvPuzzleName_vwPuzzleSelector);
        txvAuthor = findViewById(R.id.txvPuzzleAuthor_vwPuzzleSelector);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initListeners() {
        btnMain.setOnClickListener(v -> {
            if (onPuzzleClickListener != null) {
                onPuzzleClickListener.onPuzzleClick(PuzzleSelectorView.this);
            }
        });
        btnMain.setOnLongClickListener(v -> {
            if (onPuzzleLongClickListener != null) {
                return onPuzzleLongClickListener.onPuzzleLongClick(PuzzleSelectorView.this);
            }
            return false;
        });

        imvSolved.setOnTouchListener(this::onImvSolvedTouch);
    }

    private void loadPuzzleSave() {
        // SOLVED
        imvSolved.setImageResource(puzzleSave.isSolved() ? R.drawable.icon_highlighted_crown : R.drawable.icon_empty_crown);
        // NAME
        txvName.setText(puzzleSave.getName());
        // AUTHOR
        LayoutParams txvNameLayoutParams = (LayoutParams) txvName.getLayoutParams();
        if (!puzzleSave.getAuthor().isEmpty()) {
            txvNameLayoutParams.verticalBias = 0.0f;
            txvAuthor.setVisibility(VISIBLE);
            txvAuthor.setText(String.format(getResources().getString(R.string.by_author), puzzleSave.getAuthor()));
        } else {
            txvNameLayoutParams.verticalBias = 0.5f;
            txvAuthor.setVisibility(GONE);
        }
    }

    public void setCheckboxVisible(boolean checkboxVisible) {
        this.checkboxVisible = checkboxVisible;
        updateCheckboxVisibleState();
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        updateCheckedState();
    }

    public boolean isChecked() {
        return isChecked;
    }

    private void updateCheckboxVisibleState() {
        imvChecked.setVisibility(checkboxVisible ? VISIBLE : INVISIBLE);
    }

    private void updateCheckedState() {
        imvChecked.setImageResource(isChecked ? R.drawable.checked_box : R.drawable.unchecked_box);

        int strokeWidth = getResources().getDimensionPixelSize(R.dimen.default_stroke_width);
        int bgColorId = R.color.darker_background;
        int strokeColorId = R.color.font;
        int nameColorId = R.color.font;
        int authorColorId = R.color.font;
        if (isChecked) {
            strokeWidth *= 2;
            bgColorId = R.color.black;
            strokeColorId = R.color.white;
            nameColorId = R.color.white;
            authorColorId = R.color.white;
        }

        btnMain.setStrokeWidth(strokeWidth);
        btnMain.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), bgColorId));
        btnMain.setStrokeColor(ContextCompat.getColorStateList(getContext(), strokeColorId));
        txvName.setTextColor(getResources().getColor(nameColorId, getContext().getTheme()));
        txvAuthor.setTextColor(getResources().getColor(authorColorId, getContext().getTheme()));
    }

    public void setOnPuzzleClickListener(@Nullable OnPuzzleClickListener onPuzzleClickListener) {
        this.onPuzzleClickListener = onPuzzleClickListener;
    }

    public void setOnPuzzleLongClickListener(@Nullable OnPuzzleLongClickListener onPuzzleLongClickListener) {
        this.onPuzzleLongClickListener = onPuzzleLongClickListener;
    }

    public PuzzleSave getPuzzleSave() {
        return puzzleSave;
    }

    private boolean onImvSolvedTouch(View v, MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            Toast.makeText(v.getContext(), puzzleSolvedToast, Toast.LENGTH_SHORT).show();
        }

        // Every motion event impacting imvSolved must be repercuted to btnMain
        MotionEvent forwardedEvent = MotionEvent.obtain(event);
        try {
            btnMain.dispatchTouchEvent(forwardedEvent);
        } finally {
            forwardedEvent.recycle();
        }

        return true;
    }
}
