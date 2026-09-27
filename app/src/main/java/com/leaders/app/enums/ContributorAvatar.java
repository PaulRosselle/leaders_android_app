package com.leaders.app.enums;

import com.leaders.R;

public enum ContributorAvatar {
    Acrobat,
    Assassin,
    Brewmaster,
    ClawLauncher,
    Frog,
    Illusionist,
    Jailer,
    LeaderKing,
    LeaderQueen,
    LeaderVermillion,
    Nemesis,
    Protector,
    RoyalGuard,
    Tactician,
    Vizier,
    Wanderer;

    public int getDrawableResId() {
        switch (this) {
            case Acrobat: return R.drawable.character_piece_acrobat_w;
            case Assassin: return R.drawable.character_piece_assassin_w;
            case Brewmaster: return R.drawable.character_piece_brewmaster_w;
            case ClawLauncher: return R.drawable.character_piece_claw_launcher_w;
            case Frog: return R.drawable.character_piece_frog_w;
            case Illusionist: return R.drawable.character_piece_illusionist_w;
            case Jailer: return R.drawable.character_piece_jailer_w;
            case LeaderKing: return R.drawable.character_piece_leader_king_w;
            case LeaderQueen: return R.drawable.character_piece_leader_queen_w;
            case LeaderVermillion: return R.drawable.character_piece_leader_vermillion_w;
            case Nemesis: return R.drawable.character_piece_nemesis_w;
            case Protector: return R.drawable.character_piece_protector_w;
            case RoyalGuard: return R.drawable.character_piece_royal_guard_w;
            case Tactician: return R.drawable.character_piece_tactician_w;
            case Vizier: return R.drawable.character_piece_vizier_w;
            case Wanderer: return R.drawable.character_piece_wanderer_w;
            default: throw new IllegalStateException("No drawable found for avatar: " + this);
        }
    }
}
