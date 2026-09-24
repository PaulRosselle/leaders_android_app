package com.leaders.app.views.character;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.leaders.app.enums.CharacterSkinType;
import com.leaders.gamelogic.enums.CharacterCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ClassEscapesDefinedScope")
public class CharacterCardAdapter extends RecyclerView.Adapter<CharacterCardAdapter.ViewHolder> {
    private final List<Map.Entry<CharacterCard, CharacterSkinType>> cards;
    @Nullable
    private CharacterSkinType selectedSkinType;

    public CharacterCardAdapter(@NonNull List<Map.Entry<CharacterCard, CharacterSkinType>> cards,
                                @Nullable CharacterSkinType selectedSkinType) {
        this.cards = new ArrayList<>(cards);
        this.selectedSkinType = selectedSkinType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(new CharacterCardView(parent.getContext()));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map.Entry<CharacterCard, CharacterSkinType> entry = cards.get(position);
        holder.cardView.setCard(entry.getKey(), entry.getValue());
        holder.cardView.setHighlighted(selectedSkinType == entry.getValue());
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void setSelectedSkinType(@Nullable CharacterSkinType selectedSkinType) {
        this.selectedSkinType = selectedSkinType;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final CharacterCardView cardView;

        ViewHolder(@NonNull CharacterCardView itemView) {
            super(itemView);
            cardView = itemView;
        }
    }
}
