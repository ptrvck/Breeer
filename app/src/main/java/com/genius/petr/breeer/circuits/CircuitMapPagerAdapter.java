package com.genius.petr.breeer.circuits;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.database.Circuit;
import com.genius.petr.breeer.database.Place;
import com.genius.petr.breeer.database.PlaceConstants;

import org.w3c.dom.Text;

/**
 * Created by Petr on 11. 3. 2018.
 */

public class CircuitMapPagerAdapter extends PagerAdapter {

    private CircuitMapViewModel viewModel;
    private Context context;

    public CircuitMapPagerAdapter(Context context, CircuitMapViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {

        Place place = viewModel.getStops().get(position);
        LayoutInflater inflater = LayoutInflater.from(context);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.view_circuit_stop_on_map, collection, false);
        TextView tvName = layout.findViewById(R.id.tv_name);
        tvName.setText(place.getName());
        TextView tvDescription = layout.findViewById(R.id.tv_description);
        tvDescription.setText(place.getDescription());

        int color = ContextCompat.getColor(context, PlaceConstants.CATEGORY_COLORS.get(viewModel.getType()));
        Button buttonDetail = layout.findViewById(R.id.button_detail);
        buttonDetail.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

        final long id = place.getId();
        buttonDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback(id);
            }
        });

        ImageButton leftButton =  layout.findViewById(R.id.leftButton);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leftButton();
            }
        });

        if (position == 0) {
            leftButton.setEnabled(false);
        } else {
            leftButton.setEnabled(true);
        }

        ImageButton rightButton =  layout.findViewById(R.id.rightButton);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rightButton();
            }
        });

        if (position == viewModel.getStops().size()-1) {
            rightButton.setEnabled(false);
        } else {
            rightButton.setEnabled(true);
        }

        collection.addView(layout);
        return layout;
    }


    public void callback(long placeId){
        Log.i("callbackTest", "from adapter");
    }

    public void leftButton(){

    }

    public void rightButton(){

    }


    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return viewModel.getStops().size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Place place = viewModel.getStops().get(position);
        return place.getName();
    }
}
