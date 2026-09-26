package com.leaders.app.views.rules;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.RippleDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.core.content.ContextCompat;

import com.leaders.R;

import java.util.Objects;

public class RulesMenuButtonView extends ConstraintLayout {
    private enum TitleAlignment {
        Left,
        Right
    }

    private final static float CUT_SIZE_RATIO = 0.05f;

    private final Guideline gdlVertical;
    private final TextView txvTitle;
    private final SkewedRectangleDrawable srdTitle;
    private final ImageView imvCharacter;

    private TitleAlignment titleAlignment;


    public RulesMenuButtonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_rules_menu_button, this);

        // Views initialization
        imvCharacter = findViewById(R.id.imvCharacter_vwRulesMenuButton);
        txvTitle = findViewById(R.id.txvTitle_vwRulesMenuButton);
        gdlVertical = findViewById(R.id.gdlVertical_vwRulesMenuButton);

        // Title background initalization
        srdTitle = new SkewedRectangleDrawable(
                context.getColor(R.color.ultra_dark_background),
                context.getColor(R.color.app_golden),
                getWidth() * CUT_SIZE_RATIO,
                SkewedRectangleDrawable.CutSide.Right,
                getResources().getDimensionPixelSize(R.dimen.default_stroke_width)
        );
        txvTitle.setBackground(srdTitle);

        // Default properties initialization
        setBackgroundColor(context.getColor(R.color.darker_background));
        setClickable(true);

        // Loading XML attributes
        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.RulesMenuButtonView)) {
            setCharacter(customAttrs.getResourceId(
                    R.styleable.RulesMenuButtonView_characterResId,
                    R.drawable.rules_menu_acrobat
            ));
            setTitle(customAttrs.getResourceId(
                    R.styleable.RulesMenuButtonView_titleResId,
                    R.string.learn_using_the_tutorial
            ));
            setTitleAlignment(TitleAlignment.values()[customAttrs.getInteger(
                    R.styleable.RulesMenuButtonView_titleAlignemnt,
                    TitleAlignment.Left.ordinal()
            )]);
            setRippleColor(customAttrs.getResourceId(
                    R.styleable.RulesMenuButtonView_rippleColorResId,
                    R.color.golden_ripple_effect
            ));
        }
    }

    public void setCharacter(@DrawableRes int characterResId) {
        imvCharacter.setImageResource(characterResId);
    }

    public void setTitle(@StringRes int titleResId) {
        txvTitle.setText(titleResId);
    }

    private void setTitleAlignment(TitleAlignment titleAlignment) {
        this.titleAlignment = titleAlignment;

        boolean alignLeft = titleAlignment == TitleAlignment.Left;
        srdTitle.setCutSide(alignLeft ?
                SkewedRectangleDrawable.CutSide.Right : SkewedRectangleDrawable.CutSide.Left
        );

        gdlVertical.setGuidelinePercent(alignLeft ? 0.85f : 0.15f);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) txvTitle.getLayoutParams();
        // First we reset all horizontal constraints
        params.startToStart = ConstraintLayout.LayoutParams.UNSET;
        params.startToEnd = ConstraintLayout.LayoutParams.UNSET;
        params.endToStart = ConstraintLayout.LayoutParams.UNSET;
        params.endToEnd = ConstraintLayout.LayoutParams.UNSET;

        // Then we apply only the ones required for the title alignment
        if (alignLeft) {
            params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
            params.endToStart = gdlVertical.getId();
        } else {
            params.startToStart = gdlVertical.getId();
            params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        }
        txvTitle.setLayoutParams(params);
    }

    private void setRippleColor(@ColorRes int colorResId) {
        ConstraintLayout clyMain = findViewById(R.id.clyMain_vwRulesMenuButton);
        RippleDrawable ripple = (RippleDrawable) clyMain.getBackground();

        ripple.setColor(Objects.requireNonNull(
                ContextCompat.getColorStateList(getContext(), colorResId),
                "Invalid color resource id: " + colorResId
        ));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float cutSize = MeasureSpec.getSize(widthMeasureSpec) * CUT_SIZE_RATIO;

        int titleHorzPadding = getTitleHorzPadding();
        int cutPadding = (int) (titleHorzPadding + cutSize);
        int startPadding = titleAlignment == TitleAlignment.Right ? cutPadding : titleHorzPadding;
        int endPadding = titleAlignment == TitleAlignment.Left ? cutPadding : titleHorzPadding;
        int titleVertPadding = getTitleVertPadding();

        srdTitle.setCutSize(cutSize);
        txvTitle.setPaddingRelative(
                startPadding,
                titleVertPadding,
                endPadding,
                titleVertPadding
        );

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int getTitleHorzPadding() {
        return (int) (8 * getResources().getDisplayMetrics().density);
    }

    private int getTitleVertPadding() {
        return (int) (4 * getResources().getDisplayMetrics().density);
    }
}
