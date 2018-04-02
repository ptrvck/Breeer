package com.genius.petr.breeer.places;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.database.Place;

import java.util.List;

/**
 * Created by Petr on 20. 2. 2018.
 */

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<Place> placeList;
    private View.OnClickListener onClickListener;


    public PlaceAdapter(List<Place> placeList, View.OnClickListener onClickListener) {
        this.placeList = placeList;
        this.onClickListener = onClickListener;
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PlaceViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_place, parent, false));
    }

    @Override
    public void onBindViewHolder(final PlaceViewHolder holder, int position) {
        Place place = placeList.get(position);
        holder.tvName.setText(place.getName());
        holder.tvId.setText(Long.toString(place.getId()));
        holder.itemView.setTag(place);
        holder.itemView.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public void addItems(List<Place> placeList) {
        this.placeList = placeList;
        notifyDataSetChanged();
    }


    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private TextView tvId;

        PlaceViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.place_item_name);
            tvId = view.findViewById(R.id.place_item_id);
        }
    }

}
