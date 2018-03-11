package com.genius.petr.breeer.circuits;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.database.Place;

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
        collection.addView(layout);
        return layout;
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
