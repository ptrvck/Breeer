package com.genius.petr.breeer.circuits;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.genius.petr.breeer.database.AppDatabase;
import com.genius.petr.breeer.database.Circuit;
import com.genius.petr.breeer.database.CircuitBase;
import com.genius.petr.breeer.database.CircuitNode;
import com.genius.petr.breeer.database.Place;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr on 11. 3. 2018.
 */

public class CircuitMapViewModel {

    private List<Place> stops;
    private List<LatLng> path;
    private String name;
    private long id;
    private int type;

    public CircuitMapViewModel(AppDatabase db, long id) {
        CircuitBase circuit = db.circuit().selectById(id);
        name = circuit.getName();
        this.id = circuit.getId();
        this.type = circuit.getType();

        stops = db.circuit().getStopsOfCircuit(id);
        List<CircuitNode> nodes = db.circuit().getNodesOfCircuit(id);

        path = new ArrayList<>();

        for (CircuitNode node : nodes) {
            path.add(node.getPosition());
        }
    }

    //todo: this is so unclean
    public CircuitMapViewModel(long id) {
        this.id = id;
    }

    public List<Place> getStops() {
        return stops;
    }

    public List<LatLng> getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public int getType() {
        return type;
    }
}
