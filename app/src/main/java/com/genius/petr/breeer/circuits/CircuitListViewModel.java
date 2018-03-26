package com.genius.petr.breeer.circuits;

import com.genius.petr.breeer.database.Circuit;

import java.util.List;

/**
 * Created by Petr on 24. 3. 2018.
 */

public class CircuitListViewModel {
    private final List<Circuit> circuits;

    public CircuitListViewModel(List<Circuit> circuits) {
        this.circuits = circuits;
    }

    public List<Circuit> getCircuits() {
        return circuits;
    }

}
