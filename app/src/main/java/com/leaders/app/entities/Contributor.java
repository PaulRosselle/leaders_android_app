package com.leaders.app.entities;

import androidx.annotation.NonNull;

import com.leaders.app.enums.ContributorAvatar;

import org.json.JSONException;
import org.json.JSONObject;

public class Contributor {
    @NonNull
    private final ContributorAvatar avatar;
    @NonNull
    private final String name;
    private final boolean hasContributedToAlpha;
    private final boolean hasContributedToBeta;

    private Contributor(@NonNull ContributorAvatar avatar,
                        @NonNull String name,
                        boolean hasContributedToAlpha,
                        boolean hasContributedToBeta) {
        this.avatar = avatar;
        this.name = name;
        this.hasContributedToAlpha = hasContributedToAlpha;
        this.hasContributedToBeta = hasContributedToBeta;
    }

    @NonNull
    public JSONObject getAsJson() {
        JSONObject joContributor = new JSONObject();

        try {
            joContributor.put("avatar", avatar.name());
            joContributor.put("name", name);
            joContributor.put("has_contributed_to_alpha", hasContributedToAlpha);
            joContributor.put("has_contributed_to_beta", hasContributedToBeta);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return joContributor;
    }

    public static Contributor getFromJson(@NonNull JSONObject joContributor) throws JSONException {
        return new Contributor(
                ContributorAvatar.valueOf(joContributor.getString("avatar")),
                joContributor.getString("name"),
                joContributor.getBoolean("has_contributed_to_alpha"),
                joContributor.getBoolean("has_contributed_to_beta")
        );
    }

    @NonNull
    public ContributorAvatar getAvatar() {
        return avatar;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public boolean hasContributedToAlpha() {
        return hasContributedToAlpha;
    }

    public boolean hasContributedToBeta() {
        return hasContributedToBeta;
    }
}
