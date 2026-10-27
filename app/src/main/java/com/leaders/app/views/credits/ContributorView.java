package com.leaders.app.views.credits;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.leaders.R;
import com.leaders.app.entities.Contributor;
import com.leaders.app.enums.ContributorAvatar;

public class ContributorView extends LinearLayout {
    private final ImageView imvAvatar;
    private final TextView txvName;
    private final TextView txvAlpha;
    private final TextView txvBeta;

    public ContributorView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_contributor, this);

        imvAvatar = findViewById(R.id.imvAvatar_vwContributor);
        txvName = findViewById(R.id.txvName_vwContributor);
        txvAlpha = findViewById(R.id.txvAlpha_vwContributor);
        txvBeta = findViewById(R.id.txvBeta_vwContributor);
    }

    public ContributorView(@NonNull Context context, @NonNull Contributor contributor) {
        this(context, (AttributeSet) null);

        imvAvatar.setImageResource(getAvatarResId(contributor.getAvatar()));
        imvAvatar.setBackground(AppCompatResources.getDrawable(context, R.drawable.character_piece_empty_w));
        txvName.setText(String.format(" %s", contributor.getName()));
        txvAlpha.setVisibility(contributor.hasContributedToAlpha() ? View.VISIBLE : View.INVISIBLE);
        txvBeta.setVisibility(contributor.hasContributedToBeta() ? View.VISIBLE : View.INVISIBLE);
    }
    private int getAvatarResId(@NonNull ContributorAvatar avatar) {
        switch (avatar) {
            case Acrobat: return R.drawable.character_piece_acrobat;
            case Archer: return R.drawable.character_piece_archer;
            case Assassin: return R.drawable.character_piece_assassin;
            case Brewmaster: return R.drawable.character_piece_brewmaster;
            case ClawLauncher: return R.drawable.character_piece_claw_launcher;
            case Illusionist: return R.drawable.character_piece_illusionist;
            case Jailer: return R.drawable.character_piece_jailer;
            case LeaderKing: return R.drawable.character_piece_leader_king;
            case LeaderQueen: return R.drawable.character_piece_leader_queen;
            case LeaderVermillion: return R.drawable.character_piece_leader_vermillion;
            case Nemesis: return R.drawable.character_piece_nemesis;
            case Protector: return R.drawable.character_piece_protector;
            case Rider: return R.drawable.character_piece_rider;
            case RoyalGuard: return R.drawable.character_piece_royal_guard;
            case Tactician: return R.drawable.character_piece_tactician;
            case Vizier: return R.drawable.character_piece_vizier;
            case Wanderer: return R.drawable.character_piece_wanderer;
            default: throw new IllegalStateException("No drawable found for avatar: " + this);
        }
    }
}
