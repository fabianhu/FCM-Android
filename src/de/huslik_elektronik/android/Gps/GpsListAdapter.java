/*
 * (c) 2014 by Joachim Weishaupt
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package de.huslik_elektronik.android.Gps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.huslik_elektronik.android.Gps.GpsFrame.SPEED;
import de.huslik_elektronik.android.fcm.R;

public class GpsListAdapter extends ArrayAdapter<GpsFrame> {
	private final Context context;

	public GpsListAdapter(Context context, GpsFrame[] values) {
		super(context, R.layout.gpsrowlayout, values);
		this.context = context;
		this.addAll(values);
	}
	
	public GpsListAdapter(Context context) {
		super(context, R.layout.gpsrowlayout);
		this.context = context;		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.gpsrowlayout, parent, false);

		TextView tvFrameNo = (TextView) rowView.findViewById(R.id.frameNo);
		TextView tvLoc = (TextView) rowView.findViewById(R.id.location);
		TextView tvSpeed = (TextView) rowView.findViewById(R.id.speed);
		TextView tvDistToDest = (TextView) rowView
				.findViewById(R.id.distToDestination);

		tvFrameNo.setText("FrameNo: " + position);
		GpsFrame actual = this.getItem(position);
		tvLoc.setText(actual.getLocation());		
		tvSpeed.setText(actual.getSpeed(SPEED.ms));	
		tvDistToDest.setText("TODO");

		return rowView;
	}
}
