package com.leaders.app.views.duel.setup;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.leaders.R;
import com.leaders.app.views.portrait.PortraitView;
import com.leaders.gamelogic.enums.CharacterCard;

public class AltCharacterSelectionView extends ConstraintLayout {
    interface Listener {
        void onCheckedChange(@NonNull CharacterCard altCard,
                             boolean useAltCard);
    }

    private PortraitView ptvBase;
    private PortraitView ptvAlt;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch swtAlt;

    private Listener listener;

    public AltCharacterSelectionView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public AltCharacterSelectionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(@NonNull Context context) {
        inflate(context, R.layout.view_alt_character_selection, this);

        ptvBase = findViewById(R.id.ptvBase_vwAltCharacterSelection);
        ptvAlt = findViewById(R.id.ptvAlt_vwAltCharacterSelection);
        swtAlt = findViewById(R.id.swtAlt_vwAltCharacterSelection);
        swtAlt.setOnCheckedChangeListener(this::onCheckedChanged);
    }

    public void setAltSelection(@NonNull CharacterCard baseCard,
                                @NonNull CharacterCard altCard,
                                boolean useAltCard) {
        ptvBase.setPortraitCard(baseCard);
        ptvAlt.setPortraitCard(altCard);
        swtAlt.setChecked(useAltCard);
    }

    @NonNull
    public CharacterCard getAltCharacter() {
        return ptvAlt.getPortraitCard();
    }

    public void setChecked(boolean checked) {
        swtAlt.setChecked(checked);
    }

    private void onCheckedChanged(@NonNull CompoundButton compoundButton, boolean checked) {
        if (listener != null) {
            listener.onCheckedChange(getAltCharacter(), checked);
        }
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }
}
