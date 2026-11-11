
package com.leaders.gamelogic.queries;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.leaders.gamelogic.actions.CharacterActionMotion;
import com.leaders.gamelogic.actions.CharacterActionTarget;
import com.leaders.gamelogic.entities.Character;
import com.leaders.gamelogic.entities.Position;
import com.leaders.gamelogic.entities.TransformedState;
import com.leaders.gamelogic.enums.CharacterMotionType;
import com.leaders.gamelogic.enums.CharacterType;
import com.leaders.gamelogic.enums.TeamColor;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CharacterTransformUtilsTest {

    private Character createCharacter(CharacterType characterType) {
        return Character.create(characterType, TeamColor.Black);
    }

    private CharacterActionMotion createTransformMotion(
            CharacterMotionType motionType,
            List<CharacterActionTarget> targets
    ) {
        return new CharacterActionMotion(motionType, targets);
    }

    private CharacterActionTarget createTarget(
            Character character,
            Position source,
            Position destination
    ) {
        return new CharacterActionTarget(character, source, destination);
    }

    @Test
    public void isTransformMotion_shouldReturnTrueForTransformMotion() {
        Character character = createCharacter(
                CharacterType.Bruiser
        );

        CharacterActionMotion motion = createTransformMotion(
                CharacterMotionType.Transform,
                List.of(
                        createTarget(character, new Position(3, 3), new Position(3, 3)),
                        createTarget(
                                Character.transform(character, CharacterType.Cub, TeamColor.Black),
                                new Position(3, 3),
                                new Position(3, 3)
                        )
                )
        );

        assertTrue(CharacterTransformUtils.isTransformMotion(motion));
    }

    @Test
    public void isTransformMotion_shouldReturnTrueForUntransformMotion() {
        Character character = createCharacter(
                CharacterType.Bruiser
        );

        Character transformedCharacter = Character.transform(
                character,
                CharacterType.Cub,
                TeamColor.Black
        );

        CharacterActionMotion motion = createTransformMotion(
                CharacterMotionType.Untransform,
                List.of(
                        createTarget(character, new Position(3, 3), new Position(3, 3)),
                        createTarget(transformedCharacter, new Position(3, 3), new Position(3, 3))
                )
        );

        assertTrue(CharacterTransformUtils.isTransformMotion(motion));
    }

    @Test
    public void isTransformMotion_shouldReturnFalseForMoveMotion() {
        Character character = createCharacter(
                CharacterType.Bruiser
        );

        CharacterActionMotion motion = createTransformMotion(
                CharacterMotionType.Move,
                List.of(
                        createTarget(character, new Position(3, 3), new Position(3, 2))
                )
        );

        assertFalse(CharacterTransformUtils.isTransformMotion(motion));
    }

    @Test
    public void isTransformMotion_shouldReturnFalseForOtherMotionTypes() {
        for (CharacterMotionType motionType : CharacterMotionType.values()) {
            if (motionType == CharacterMotionType.Transform
                    || motionType == CharacterMotionType.Untransform) {
                continue;
            }

            Character character = createCharacter(
                    CharacterType.Bruiser
            );

            CharacterActionMotion motion = createTransformMotion(
                    motionType,
                    new ArrayList<>()
            );

            assertFalse(CharacterTransformUtils.isTransformMotion(motion));
        }
    }

    @Test
    public void getStateFromTransformMotion_shouldReturnOriginalAndTransformedCharacters() {
        Character character = createCharacter(
                CharacterType.Bruiser
        );

        Character transformedCharacter = Character.transform(
                character,
                CharacterType.Cub,
                TeamColor.Black
        );

        CharacterActionMotion motion = createTransformMotion(
                CharacterMotionType.Transform,
                List.of(
                        createTarget(character, new Position(3, 3), new Position(3, 3)),
                        createTarget(transformedCharacter, new Position(3, 3), new Position(3, 3))
                )
        );

        TransformedState state =
                CharacterTransformUtils.getStateFromTranformMotion(motion);

        assertSame(character, state.getCharacterBefore());
        assertSame(transformedCharacter, state.getCharacterAfter());
    }

    @Test
    public void getStateFromTransformMotion_shouldWorkForUntransformMotion() {
        Character character = createCharacter(
                CharacterType.Bruiser
        );

        Character transformedCharacter = Character.transform(
                character,
                CharacterType.Cub,
                TeamColor.Black
        );

        CharacterActionMotion motion = createTransformMotion(
                CharacterMotionType.Untransform,
                List.of(
                        createTarget(transformedCharacter, new Position(3, 3), new Position(3, 3)),
                        createTarget(character, new Position(3, 3), new Position(3, 3))
                )
        );

        TransformedState state =
                CharacterTransformUtils.getStateFromTranformMotion(motion);

        assertSame(transformedCharacter, state.getCharacterBefore());
        assertSame(character, state.getCharacterAfter());
    }

    @Test
    public void getStateFromTransformMotion_shouldThrowWhenMotionTypeIsInvalid() {
        Character character = createCharacter(
                CharacterType.Bruiser
        );

        CharacterActionMotion motion = createTransformMotion(
                CharacterMotionType.Move,
                List.of(
                        createTarget(character, new Position(3, 3), new Position(3, 2)),
                        createTarget(character, new Position(3, 2), new Position(3, 1))
                )
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> CharacterTransformUtils.getStateFromTranformMotion(motion)
        );

        assertTrue(Objects.requireNonNull(exception.getMessage()).contains("Invalid transform motion type"));
    }

    @Test
    public void getStateFromTransformMotion_shouldThrowWhenThereAreNoTargets() {
        CharacterActionMotion motion = createTransformMotion(
                CharacterMotionType.Transform,
                new ArrayList<>()
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> CharacterTransformUtils.getStateFromTranformMotion(motion)
        );

        assertEquals(
                "Invalid transform motion: exactly 2 targets expected",
                exception.getMessage()
        );
    }

    @Test
    public void getStateFromTransformMotion_shouldThrowWhenThereIsOnlyOneTarget() {
        Character character = createCharacter(
                CharacterType.Bruiser
        );

        CharacterActionMotion motion = createTransformMotion(
                CharacterMotionType.Transform,
                List.of(
                        createTarget(character, new Position(3, 3), new Position(3, 3))
                )
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> CharacterTransformUtils.getStateFromTranformMotion(motion)
        );

        assertEquals(
                "Invalid transform motion: exactly 2 targets expected",
                exception.getMessage()
        );
    }

    @Test
    public void getStateFromTransformMotion_shouldThrowWhenThereAreMoreThanTwoTargets() {
        Character character = createCharacter(
                CharacterType.Bruiser
        );

        Character transformedCharacter = Character.transform(
                character,
                CharacterType.Cub,
                TeamColor.Black
        );

        CharacterActionMotion motion = createTransformMotion(
                CharacterMotionType.Transform,
                List.of(
                        createTarget(character, new Position(3, 3), new Position(3, 3)),
                        createTarget(transformedCharacter, new Position(3, 3), new Position(3, 3)),
                        createTarget(character, new Position(3, 3), new Position(3, 3))
                )
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> CharacterTransformUtils.getStateFromTranformMotion(motion)
        );

        assertEquals(
                "Invalid transform motion: exactly 2 targets expected",
                exception.getMessage()
        );
    }

    @Test
    public void getStateFromTransformMotion_shouldThrowWhenTargetsHaveDifferentCharacters() {
        Character character = createCharacter(
                CharacterType.Bruiser
        );

        Character anotherCharacter = createCharacter(
                CharacterType.Acrobat
        );

        CharacterActionMotion motion = createTransformMotion(
                CharacterMotionType.Transform,
                List.of(
                        createTarget(character, new Position(3, 3), new Position(3, 3)),
                        createTarget(anotherCharacter, new Position(3, 3), new Position(3, 3))
                )
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> CharacterTransformUtils.getStateFromTranformMotion(motion)
        );

        assertEquals(
                "Invalid transform motion: a single character is required",
                exception.getMessage()
        );
    }
}