package com.genius.petr.breeer.database;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.database.DatabaseRoomManager;

/**
 * Created by Petr on 9. 2. 2018.
 */

public class FragmentDb extends Fragment {

    private Button startButton;
    private Button testButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_db, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startButton = view.findViewById(R.id.button_start);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DatabaseRoomManager db = new DatabaseRoomManager(getContext());
                db.updateDatabase(getContext());
            }
        });

        testButton = view.findViewById(R.id.button_test);
        testButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("DB", "test");
            }
        });
    }
}
