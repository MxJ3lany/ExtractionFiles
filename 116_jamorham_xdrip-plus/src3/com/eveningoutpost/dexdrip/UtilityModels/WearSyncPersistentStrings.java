package com.eveningoutpost.dexdrip.UtilityModels;

import com.eveningoutpost.dexdrip.G5Model.G6CalibrationParameters;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

// jamorham

// persistent store strings, will be set to "" if undefined

public class WearSyncPersistentStrings {

    @Getter
    private static final List<String> persistentStrings = new ArrayList<>();

    static {
        persistentStrings.add(G6CalibrationParameters.PREF_CURRENT_CODE);
    }

}

