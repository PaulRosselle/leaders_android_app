package com.leaders.app.views.decoration;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;

public final class OrnateFrameView extends ConstraintLayout {
    public OrnateFrameView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.view_ornate_frame, this);

        try (TypedArray customAttrs = context.obtainStyledAttributes(attrs, R.styleable.OrnateFrameView)) {
            int colorId = customAttrs.getResourceId(R.styleable.OrnateFrameView_colorTint, R.color.app_golden);
            ColorStateList colorTintList = AppCompatResources.getColorStateList(context, colorId);

            ImageView[] imvCorners = new ImageView[] {
                    findViewById(R.id.imvCornerBg1_vwOrnateFrame),
                    findViewById(R.id.imvCornerBg2_vwOrnateFrame),
                    findViewById(R.id.imvCornerBg3_vwOrnateFrame),
                    findViewById(R.id.imvCornerBg4_vwOrnateFrame)
            };
            for (ImageView imvCorner : imvCorners) {
                imvCorner.setImageTintList(colorTintList);
            }

            ImageView imvRectLines = findViewById(R.id.imvRectLines_vwOrnateFrame);
            imvRectLines.setImageTintList(colorTintList);
        }
    }
}
