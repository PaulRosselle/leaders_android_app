package com.leaders.app.activities;


import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.leaders.R;
import com.leaders.app.entities.LeadersApplication;
import com.leaders.app.entities.Settings;
import com.leaders.app.enums.ActivityType;
import com.leaders.app.enums.AnimationSpeed;
import com.leaders.app.enums.HighlightColor;
import com.leaders.app.utilities.JsonUtils;
import com.leaders.app.views.settings.AnimationSpeedView;
import com.leaders.app.views.settings.CharacterSkinFormView;
import com.leaders.app.views.settings.CharacterSkinView;
import com.leaders.app.views.settings.ContactFormView;
import com.leaders.app.views.settings.ContactView;
import com.leaders.app.views.settings.HighlightColorView;
import com.leaders.app.views.settings.PlayableItemsAnimationView;
import com.leaders.app.views.settings.UserNameView;

public class SettingsActivity extends BaseActivity {
    private UserNameView unvUserName;
    private AnimationSpeedView asvAnimationSpeed;
    private PlayableItemsAnimationView piasAnimatePlayableItems;
    private HighlightColorView hcvHighlightColor;
    private ContactView ctvContact;
    private View vwDialogBg;
    private ContactFormView cfvContact;
    private CharacterSkinView csvSkins;
    private CharacterSkinFormView csfvSkins;

    private Settings settings;


    //region BASE ACTIVITY OVERRIDEN METHODS

    @Override
    protected void initViews() {
        super.initViews();

        unvUserName = findViewById(R.id.unvUserName_actSettings);
        asvAnimationSpeed = findViewById(R.id.asvAnimationSpeed_actSettings);
        piasAnimatePlayableItems = findViewById(R.id.piasAnimatePlayableItems_actSettings);
        hcvHighlightColor = findViewById(R.id.hcvHighlightColor_actSettings);

        ctvContact = findViewById(R.id.ctvContact_actSettings);
        vwDialogBg = findViewById(R.id.vwDialogBg_actSettings);
        cfvContact = findViewById(R.id.cfvContact_actSettings);

        csvSkins = findViewById(R.id.csvSkins_actSettings);
        csfvSkins = findViewById(R.id.csfvSkins_actSettings);
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        unvUserName.setChangeListener(this::onNameChange);
        asvAnimationSpeed.setChangeListener(this::onAnimationSpeedChange);
        piasAnimatePlayableItems.setOnCheckedChangeListener(this::onAnimatePlayableItemsChanged);
        hcvHighlightColor.setChangeListener(this::onColorChange);

        ctvContact.setOnOpenFormClickListener(this::onOpenContactFormClick);
        vwDialogBg.setOnClickListener(this::onDialogBgClick);
        csvSkins.setOnOpenFormClickListener(this::onOpenSkinsFormClick);
    }

    @Override
    protected void initDatas() {
        super.initDatas();

        settings = ((LeadersApplication) getApplication()).getSettings();

        unvUserName.setName(settings.getUserName());
        asvAnimationSpeed.setSpeed(settings.getAnimationSpeed());
        hcvHighlightColor.setColor(settings.getHighlightColor());
        piasAnimatePlayableItems.setChecked(settings.animatePlayableItems());
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_settings;
    }

    @Override
    protected int getRootGuidelineResId() {
        return R.id.gdlRoot_actSettings;
    }

    @Nullable
    @Override
    protected Integer getBtnBackResId() {
        return R.id.btnBack_actSettings;
    }

    @Override
    protected boolean isImmersiveActivity() {
        return true;
    }

    @Override
    protected boolean overrideOnBackPressed() {
        return true;
    }

    @Override
    protected boolean askForConfirmationBeforeFinish() {
        return false;
    }

    @NonNull
    @Override
    public ActivityType getActivityType() {
        return ActivityType.Settings;
    }

    //endregion

    //region CHANGE LISTENERS

    private void onNameChange(@NonNull String name) {
        settings.setUserName(name);
        JsonUtils.saveSettings(this, settings);
    }

    private void onAnimationSpeedChange(@NonNull AnimationSpeed speed) {
        settings.setAnimationSpeed(speed);
        JsonUtils.saveSettings(this, settings);
    }

    public void onAnimatePlayableItemsChanged(@NonNull CompoundButton compoundButton,
                                              boolean animatePlayableItems) {
        settings.setAnimatePlayableItems(animatePlayableItems);
        JsonUtils.saveSettings(this, settings);
    }

    private void onColorChange(@NonNull HighlightColor color) {
        settings.setHighlightColor(color);
        JsonUtils.saveSettings(this, settings);
    }

    private void onOpenContactFormClick(View v) {
        setContactFormVisible(true);
    }

    private void onOpenSkinsFormClick(View v) {
        csfvSkins.setVisibility(View.VISIBLE);
    }

    private void onDialogBgClick(View v) {
        setContactFormVisible(false);
    }

    private void setContactFormVisible(boolean visible) {
        if (visible) {
            cfvContact.show();
        } else {
            cfvContact.hide();
        }
        vwDialogBg.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    //endregion
}