package com.leaders.gamelogic.factories;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Game;
import com.leaders.gamelogic.entities.GameHistory;
import com.leaders.gamelogic.resolvers.CharacterActionResolver;
import com.leaders.gamelogic.resolvers.characters.AcrobatActionResolver;
import com.leaders.gamelogic.resolvers.characters.BrewmasterActionResolver;
import com.leaders.gamelogic.resolvers.characters.BruiserActionResolver;
import com.leaders.gamelogic.resolvers.characters.ClawLauncherActionResolver;
import com.leaders.gamelogic.resolvers.characters.IllusionistActionResolver;
import com.leaders.gamelogic.resolvers.characters.ManipulatorActionResolver;
import com.leaders.gamelogic.resolvers.characters.NemesisActionResolver;
import com.leaders.gamelogic.resolvers.characters.RiderActionResolver;
import com.leaders.gamelogic.resolvers.characters.RoyalGuardActionResolver;
import com.leaders.gamelogic.resolvers.characters.WandererActionResolver;

public final class CharacterActionResolverFactory {
    private CharacterActionResolverFactory(){
        throw new AssertionError("Cannot instantiate utility class");
    }

    public static CharacterActionResolver create(@NonNull Game game,
                                                 @NonNull GameHistory gameHistory,
                                                 @NonNull Character character) {
        switch (character.getCharacterType()) {
            case Acrobat: return new AcrobatActionResolver(game, gameHistory, character);
            case Brewmaster: return new BrewmasterActionResolver(game, gameHistory, character);
            case Bruiser: return new BruiserActionResolver(game, gameHistory, character);
            case ClawLauncher: return new ClawLauncherActionResolver(game, gameHistory, character);
            case Illusionist: return new IllusionistActionResolver(game, gameHistory, character);
            case Manipulator: return new ManipulatorActionResolver(game, gameHistory, character);
            case Nemesis: return new NemesisActionResolver(game, gameHistory, character);
            case Rider: return new RiderActionResolver(game, gameHistory, character);
            case RoyalGuard: return new RoyalGuardActionResolver(game, gameHistory, character);
            case Wanderer: return new WandererActionResolver(game, gameHistory, character);
            default: return new CharacterActionResolver(game, gameHistory, character);
        }
    }
}
