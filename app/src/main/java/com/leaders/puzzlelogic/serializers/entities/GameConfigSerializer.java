package com.leaders.puzzlelogic.serializers.entities;

import androidx.annotation.NonNull;

import com.leaders.gamelogic.actions.BanishmentAction;
import com.leaders.gamelogic.actions.CharacterAction;
import com.leaders.gamelogic.actions.IGameAction;
import com.leaders.gamelogic.actions.RecruitmentAction;
import com.leaders.gamelogic.actions.TransitionAction;
import com.leaders.gamelogic.actions.WarningAction;
import com.leaders.gamelogic.entities.GameConfig;
import com.leaders.gamelogic.entities.Player;
import com.leaders.gamelogic.enums.CharacterCard;
import com.leaders.gamelogic.enums.GameActionType;
import com.leaders.gamelogic.enums.GameMode;
import com.leaders.puzzlelogic.serializers.IJsonSerializer;
import com.leaders.puzzlelogic.serializers.actions.RecruitmentActionSerializer;
import com.leaders.puzzlelogic.serializers.SerializationContext;
import com.leaders.puzzlelogic.serializers.actions.TransitionActionSerializer;
import com.leaders.puzzlelogic.serializers.actions.WarningActionSerializer;
import com.leaders.puzzlelogic.serializers.actions.BanishmentActionSerializer;
import com.leaders.puzzlelogic.serializers.actions.CharacterActionSerializer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class GameConfigSerializer implements IJsonSerializer<GameConfig> {
    @NonNull
    @Override
    public GameConfig getFromJson(@NonNull JSONObject jsonObject,
                                  @NonNull SerializationContext srlContext) throws JSONException {
        PlayerSerializer playerSerializer = new PlayerSerializer();

        List<Player> players = new ArrayList<>();
        JSONArray jaPlayers = jsonObject.getJSONArray("players");
        for (int i = 0; i < jaPlayers.length(); i++) {
            players.add(playerSerializer.getFromJson(jaPlayers.getJSONObject(i), srlContext));
        }

        Player firstPlayer = playerSerializer.getFromJsonName(jsonObject, srlContext, "first_player");

        GameMode gameMode = GameMode.valueOf(jsonObject.getString("game_mode"));

        List<CharacterCard> initialRecruitableCards = new ArrayList<>();
        JSONArray jaInitialRecruitableCards =
                jsonObject.getJSONArray("initial_recruitable_cards");

        for (int i = 0; i < jaInitialRecruitableCards.length(); i++) {
            initialRecruitableCards.add(CharacterCard.valueOf(jaInitialRecruitableCards.getString(i)));
        }

        List<IGameAction> initialPlacements = new ArrayList<>();
        JSONArray jaInitialPlacements =
                jsonObject.getJSONArray("initial_placements");

        for (int i = 0; i < jaInitialPlacements.length(); i++) {
            JSONObject actionJson = jaInitialPlacements.getJSONObject(i);

            GameActionType actionType = GameActionType.valueOf(actionJson.getString("type"));
            IGameAction action;

            switch (actionType) {
                case Transition:
                    action = new TransitionActionSerializer().getFromJson(actionJson, srlContext);
                    break;
                case CharacterAction:
                    action = new CharacterActionSerializer().getFromJson(actionJson, srlContext);
                    break;
                case Recruitment:
                    action = new RecruitmentActionSerializer().getFromJson(actionJson, srlContext);
                    break;
                case Banishment:
                    action = new BanishmentActionSerializer().getFromJson(actionJson, srlContext);
                    break;
                case Warning:
                    action = new WarningActionSerializer().getFromJson(actionJson, srlContext);
                    break;

                default: throw new JSONException("Unsupported game action type: " + actionType);
            }

            initialPlacements.add(action);
        }

        return new GameConfig(
                players,
                firstPlayer,
                gameMode,
                initialRecruitableCards,
                initialPlacements
        );
    }

    @NonNull
    @Override
    public JSONObject getAsJson(@NonNull GameConfig object) throws JSONException {
        PlayerSerializer playerSerializer = new PlayerSerializer();

        JSONObject jsonObject = new JSONObject();

        JSONArray jaPlayers = new JSONArray();
        for (Player player : object.getPlayers()) {
            jaPlayers.put(playerSerializer.getAsJson(player));
        }
        jsonObject.put("players", jaPlayers);

        jsonObject.put("first_player", playerSerializer.getAsJson(object.getFirstPlayer()));

        jsonObject.put("game_mode", object.getGameMode().name());

        JSONArray jaInitialRecruitableCards = new JSONArray();
        for (CharacterCard card : object.getInitialRecruitableCards()) {
            jaInitialRecruitableCards.put(card.name());
        }
        jsonObject.put("initial_recruitable_cards", jaInitialRecruitableCards);

        JSONArray jaInitialPlacements = new JSONArray();

        for (IGameAction action : object.getInitialPlacements()) {
            JSONObject actionJson;
            switch (action.getActionType()) {
                case Transition: actionJson = new TransitionActionSerializer().getAsJson((TransitionAction) action);
                    break;
                case CharacterAction: actionJson = new CharacterActionSerializer().getAsJson((CharacterAction) action);
                    break;
                case Recruitment: actionJson = new RecruitmentActionSerializer().getAsJson((RecruitmentAction) action);
                    break;
                case Banishment: actionJson = new BanishmentActionSerializer().getAsJson((BanishmentAction) action);
                    break;
                case Warning: actionJson = new WarningActionSerializer().getAsJson((WarningAction) action);
                    break;
                default:
                    throw new JSONException("Unsupported game action type: " + action.getActionType());
            }
            actionJson.put("type", action.getActionType().name());
            jaInitialPlacements.put(actionJson);
        }

        jsonObject.put("initial_placements", jaInitialPlacements);

        return jsonObject;
    }
}