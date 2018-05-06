package com.genius.petr.breeer.circuits;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.AppCompatDrawableManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.database.Circuit;
import com.genius.petr.breeer.database.PlaceConstants;

/**
 * Created by Petr on 24. 3. 2018.
 */

public class CircuitListPagerAdapter extends PagerAdapter {

    private Context context;
    private  CircuitListViewModel viewModel;
    private static final String TAG = "circuitsLog";

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

        TextView tvDescription = layout.findViewById(R.id.tv_description);
        //tvDescription.setText(circuit.getDescription());

        ImageView backgroundLayout = layout.findViewById(R.id.backgroundImage);
        int color = ContextCompat.getColor(context, PlaceConstants.CATEGORY_COLORS.get(circuit.getType()));
        backgroundLayout.setColorFilter(color);


        Button startButton = layout.findViewById(R.id.button_startCircuit);
        startButton.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback(circuit);
            }
        });




        Log.i(TAG, "circuit type: " + circuit.getType());
        Log.i(TAG, "circuit color: " + color);


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
