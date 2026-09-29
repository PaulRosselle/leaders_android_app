package com.leaders.app.export.entities;

import androidx.annotation.NonNull;

import com.leaders.app.utilities.LbeUtils;
import com.leaders.gamelogic.entities.Position;

public class ExportPosition {
    @NonNull
    private final String positionStr;

    private ExportPosition(@NonNull String positionStr) {
        this.positionStr = positionStr;
    }

    public ExportPosition(@NonNull Position position) {
        this(LbeUtils.getPositionExportStr(position));
    }

    @NonNull
    public Position getPosition() {
        return LbeUtils.getPositionFromExportStr(null, positionStr);
    }

    @NonNull
    public String toJsonString() {
        return positionStr;
    }

    public static ExportPosition fromJsonString(@NonNull String jsonString) {
        return new ExportPosition(jsonString);
    }
}
