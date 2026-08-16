package com.leaders.app.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public class DecorativeBackgroundView extends ConstraintLayout {
    public DecorativeBackgroundView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_decorative_background, this);
        // Initializing views
        ImageView imvTopLeft = findViewById(R.id.imvCornerTopLeft_vwDecorativeBackground);
        ImageView imvTopRight = findViewById(R.id.imvCornerTopRight_vwDecorativeBackground);
        ImageView imvBottomRight = findViewById(R.id.imvCornerBottomRight_vwDecorativeBackground);
        ImageView imvBottomLeft = findViewById(R.id.imvCornerBottomLeft_vwDecorativeBackground);
        // Loading XML attributes
        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.DecorativeBackgroundView)) {
            setImvCornerVisible(imvTopLeft, customAttrs.getBoolean(R.styleable.DecorativeBackgroundView_cornerTopLeftVisible, true));
            setImvCornerVisible(imvTopRight, customAttrs.getBoolean(R.styleable.DecorativeBackgroundView_cornerTopRightVisible, true));
            setImvCornerVisible(imvBottomRight, customAttrs.getBoolean(R.styleable.DecorativeBackgroundView_cornerBottomRightVisible, true));
            setImvCornerVisible(imvBottomLeft, customAttrs.getBoolean(R.styleable.DecorativeBackgroundView_cornerBottomLeftVisible, true));
        }
    }

    private void setImvCornerVisible(ImageView imvCorner, boolean visible) {
        if (visible) {
            imvCorner.setVisibility(VISIBLE);
        } else {
            imvCorner.setVisibility(GONE);
        }
    }
}
