package com.leaders.app.views.selector;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;

public abstract class SelectorView<T> extends ConstraintLayout {

    public interface OnSelectorClickListener {
        void onSelectorClick(@NonNull SelectorView<?> sender);
    }

    public interface OnSelectorLongClickListener {
        boolean onSelectorLongClick(@NonNull SelectorView<?> sender);
    }

    protected MaterialButton btnMain;
    protected ImageView imvChecked;

    @NonNull
    private final T item;

    private boolean checkboxVisible;
    private boolean checked;

    @Nullable
    private OnSelectorClickListener clickListener;

    @Nullable
    private OnSelectorLongClickListener longClickListener;

    protected SelectorView(@NonNull Context context, @NonNull T item) {
        super(context);
        this.item = item;

        inflate(context, getLayoutResId(), this);

        initViews();
        initListeners();
        initDatas();
    }

    @NonNull
    public final T getItem() {
        return item;
    }

    public final void setCheckboxVisible(boolean checkboxVisible) {
        this.checkboxVisible = checkboxVisible;
        updateCheckboxVisibleState();
    }

    public final boolean isCheckboxVisible() {
        return checkboxVisible;
    }

    public final void setChecked(boolean checked) {
        this.checked = checked;
        updateCheckedState();
    }

    public final boolean isChecked() {
        return checked;
    }

    public final void setOnSelectorClickListener(@Nullable OnSelectorClickListener listener) {
        this.clickListener = listener;
    }

    public final void setOnSelectorLongClickListener(@Nullable OnSelectorLongClickListener listener) {
        this.longClickListener = listener;
    }

    @CallSuper
    protected void initViews() {
        btnMain = findViewById(getBtnMainResId());
        imvChecked = findViewById(getImvCheckedResId());
    }

    @CallSuper
    protected void initListeners() {
        btnMain.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onSelectorClick(this);
            }
        });

        btnMain.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                return longClickListener.onSelectorLongClick(this);
            }
            return false;
        });
    }

    @CallSuper
    protected void initDatas() {
        setCheckboxVisible(false);
        setChecked(false);
    }


    protected abstract int getLayoutResId();

    protected abstract int getBtnMainResId();

    protected abstract int getImvCheckedResId();

    protected abstract void updateCheckboxVisibleState();

    protected abstract void updateCheckedState();
}