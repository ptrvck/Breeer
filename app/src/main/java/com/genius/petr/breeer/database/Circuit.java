package com.genius.petr.breeer.database;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Relation;

import java.util.List;

/**
 * Created by Petr on 23. 2. 2018.
 */

public class Circuit {
    @Embedded
    CircuitBase circuitBase;

    @Relation(parentColumn = CircuitBase.COLUMN_ID, entityColumn = CircuitNode.COLUMN_CIRCUIT_ID)
    public List<CircuitNode> nodes;

    @Ignore
    public long getId() {
        return circuitBase.getId();
    }

    @Ignore
    public String getName() {
        return circuitBase.getName();
    }

    @Ignore
    public String getDescription() {
        return circuitBase.getDescription();
    }

    @Ignore
    public int getType() {
        return circuitBase.getType();
    }
}
