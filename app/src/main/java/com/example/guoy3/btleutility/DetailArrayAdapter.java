package com.example.guoy3.btleutility;

/**
 * Created by Cerise on 4/19/2015.
 */
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class DetailArrayAdapter extends ArrayAdapter<DetailListViewData> {
    private final Context context;
    private final List<DetailListViewData> values;

    public DetailArrayAdapter(Context context, List<DetailListViewData> values) {
        super(context, R.layout.btdetailrow, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if( position >= values.size()) {
            throw new IllegalArgumentException("invalid position in DetailArrayAdapter::getView");
        }

        View rowView = inflater.inflate(R.layout.btdetailrow, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.BTDetailInfoRow);
        String s = values.get(position).textForDisplay;
        if (null != s) {
              textView.append(s);
        }
        if( 0 == position )
        {
            float sizef = textView.getTextSize();
            textView.setTextSize(18);
            textView.setTextColor(Color.BLUE);
        }

        //ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        //textView.setText(values[position]);
        // change the icon for Windows and iPhone

        //if (s.startsWith("iPhone")) {
        //    imageView.setImageResource(R.drawable.no);
        //} else {
        //    imageView.setImageResource(R.drawable.ok);
        //}

        return rowView;
    }
}