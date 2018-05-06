package com.genius.petr.breeer.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.genius.petr.breeer.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.util.HashMap;
import java.util.Map;

public class CustomMapClusterRenderer<T extends ClusterItem> extends DefaultClusterRenderer<T> {

    private Map<Integer, BitmapDescriptor> markerIcons;

    CustomMapClusterRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
        super(context, map, clusterManager);
        markerIcons = setupMarkerIcons(context);
    }

    private Map<Integer, BitmapDescriptor> setupMarkerIcons(Context context) {
        Map<Integer, BitmapDescriptor> markerIcons = new HashMap<>();
        markerIcons.put(0, MapUtils.bitmapDescriptorFromVector(context, R.drawable.marker_area));
        markerIcons.put(1, MapUtils.bitmapDescriptorFromVector(context, R.drawable.marker_misc));
        markerIcons.put(2, MapUtils.bitmapDescriptorFromVector(context, R.drawable.marker_food));
        markerIcons.put(3, MapUtils.bitmapDescriptorFromVector(context, R.drawable.marker_nature));
        markerIcons.put(4, MapUtils.bitmapDescriptorFromVector(context, R.drawable.marker_coffee));
        markerIcons.put(5, MapUtils.bitmapDescriptorFromVector(context, R.drawable.marker_beer));
        markerIcons.put(6, MapUtils.bitmapDescriptorFromVector(context, R.drawable.marker_bar));
        markerIcons.put(7, MapUtils.bitmapDescriptorFromVector(context, R.drawable.marker_music));
        markerIcons.put(8, MapUtils.bitmapDescriptorFromVector(context, R.drawable.marker_art));

        return markerIcons;

    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
        //start clustering if 2 or more items overlap
        return cluster.getSize() >= 2;
    }

    @Override
    protected void onBeforeClusterItemRendered(T item,
                                               MarkerOptions markerOptions) {
        if (item instanceof PlaceCluster) {
            PlaceCluster cluster = (PlaceCluster) item;
            markerOptions.icon(markerIcons.get(cluster.getCategory()));
            markerOptions.title(item.getTitle());
            markerOptions.snippet(item.getSnippet());
        }
    }

    @Override
    protected int getColor(int clusterSize) {
        return Color.parseColor("#000000");
    }
}
