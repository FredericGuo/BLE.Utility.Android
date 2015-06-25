package com.example.guoy3.btleutility;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class BTDeviceAdapter extends RecyclerView.Adapter<BTDeviceAdapter.BTViewHolder> {

	private final List<DeviceListViewData> values;
	private OnItemClickListener onItemClickListener;
	private OnItemLongClickListener onItemLongClickListener;

	public BTDeviceAdapter(ArrayList<DeviceListViewData> list) {
		this.values = list;
	}

	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return values.size();
	}

	@Override
	public void onBindViewHolder(BTViewHolder viewHolder, final int position) {
		viewHolder.textViewName.setText(values.get(position).text1);
		viewHolder.textViewMac.setText(values.get(position).text2);
		viewHolder.textRSSI.setText(Integer.toString( values.get(position).RSSIValue));


		//-40 to -55 is a very strong connection 0 ~ -60
		//-70 and above represents a good connection  -61 ~ -70
		//-100 and below represents a bad connection   -71 ~ -90
		//-110 and below is almost unusable  -90 ~ less
		final int RSSILevel = values.get(position).RSSIValue;
		if( -60 < RSSILevel )
		{
			viewHolder.rssiImage.setBackgroundResource(R.drawable.signal5);
		}
		else if( -70 < RSSILevel )
		{
			viewHolder.rssiImage.setBackgroundResource(R.drawable.signal4);
		}
		else if( -90 < RSSILevel )
		{
			viewHolder.rssiImage.setBackgroundResource(R.drawable.signal3);
		}
		else if( -100 < RSSILevel )
		{
			viewHolder.rssiImage.setBackgroundResource(R.drawable.signal2);
		}
		else//( -100 < RSSILevel )
		{
			viewHolder.rssiImage.setBackgroundResource(R.drawable.signal1);
		}



		//Log.i(MainActivity.TAG_NAME, "onBindViewHolder pos: " + position +
		//		" " + values.get(position).text1 + ", " + values.get(position).text2 );

		viewHolder.itemView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (onItemClickListener != null) {
					onItemClickListener.onClick(v, position);
				}
			}
		});
		viewHolder.itemView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				if (onItemLongClickListener != null) {
					return onItemLongClickListener.onLongClick(v, position);
				}
				return false;
			}
		});
	}

	@Override
	public BTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View rowView = LayoutInflater.from(parent.getContext()).inflate(
				R.layout.btdevicerow, null);

		BTViewHolder holder = new BTViewHolder(rowView);
		return holder;
	}

	public void remove(int position){
		values.remove(position);
		notifyItemRemoved(position);
	}

	public OnItemClickListener getOnItemClickListener() {
		return onItemClickListener;
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public OnItemLongClickListener getOnItemLongClickListener() {
		return onItemLongClickListener;
	}

	public void setOnItemLongClickListener(
			OnItemLongClickListener onItemLongClickListener) {
		this.onItemLongClickListener = onItemLongClickListener;
	}

	public class BTViewHolder extends RecyclerView.ViewHolder {

		public TextView textViewName;
		public TextView textViewMac;
		public ImageView rssiImage;
		public TextView textRSSI;
		public View itemView;

		public BTViewHolder(View view) {
			super(view);
			// TODO Auto-generated constructor stub
			itemView = view;
			textViewName = (TextView) view.findViewById(R.id.BTLEListViewRow1);
			textViewMac  = (TextView) view.findViewById(R.id.BTLEListViewRow2);
			rssiImage  = (ImageView) view.findViewById(R.id.BTLERSSIIMG);
			textRSSI = (TextView) view.findViewById(R.id.BTLERSSI);
		}
	}

	public interface OnItemClickListener {
		public void onClick(View parent, int position);
	}

	public interface OnItemLongClickListener {
		public boolean onLongClick(View parent, int position);
	}

}
