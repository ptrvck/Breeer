package com.genius.petr.breeer.circuits;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.database.Circuit;

/**
 * Created by Petr on 24. 3. 2018.
 */

public class CircuitListPagerAdapter extends PagerAdapter {

    private Context context;
    private  CircuitListViewModel viewModel;

    public CircuitListPagerAdapter(Context context, CircuitListViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {

        final Circuit circuit = viewModel.getCircuits().get(position);
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.view_circuit, collection, false);
        TextView tvName = layout.findViewById(R.id.tv_name);
        tvName.setText(circuit.getName());
        Button startButton = layout.findViewById(R.id.button_startCircuit);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback(circuit);
            }
        });

        collection.addView(layout);
        return layout;
    }

    public void callback(Circuit circuit){
        Log.i("callbackTest", "from adapter");
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return viewModel.getCircuits().size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Circuit circuit = viewModel.getCircuits().get(position);
        return circuit.getName();
    }
}
