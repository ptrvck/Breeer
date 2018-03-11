package com.genius.petr.breeer.places;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.genius.petr.breeer.R;
import com.genius.petr.breeer.database.PlaceConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Petr on 10. 3. 2018.
 */

public class CategoriesAdapter extends BaseAdapter {

    private final Context context;
    private List<PlaceCategory> categories = new ArrayList<>();

    public CategoriesAdapter(Context context) {
        this.context = context;

        for (int number : PlaceConstants.CATEGORIES) {
            PlaceCategory category = new PlaceCategory();
            category.setNumber(number);
            category.setName(PlaceConstants.CATEGORY_NAMES.get(number));
            categories.add(category);
        }
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public long getItemId(int position) {
        if (position >= categories.size()) {
            return -1;
        }

        return categories.get(position).getNumber();
    }

    @Override
    public Object getItem(int position) {
        if (position >= categories.size()) {
            return null;
        }

        return categories.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final PlaceCategory category = categories.get(position);

        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.griditem_category, null);
        }

        final ImageView imageView = convertView.findViewById(R.id.iv_categoryImage);
        final TextView nameTextView = convertView.findViewById(R.id.tv_categoryName);

        //imageView.setImageResource(category.getImageResource());
        nameTextView.setText(category.getName());

        return convertView;
    }
}
