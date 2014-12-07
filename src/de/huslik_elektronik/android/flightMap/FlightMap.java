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

// Flight Map
// (c) Joachim Weisahupt
// created 06.12.2014 
//
// + get raw gps data from FCM
// - interpret fcm kml export, integrate intent filter for kml tracks
// + Show Track, Overlay
// + Show Table with track details
// - menu analyse flight - max speed, altitude, etc.
// - landscape layout

package de.huslik_elektronik.android.flightMap;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import de.huslik_elektronik.android.Gps.GpsFrame;
import de.huslik_elektronik.android.Gps.GpsFrame.SPEED;
import de.huslik_elektronik.android.Gps.GpsListAdapter;
import de.huslik_elektronik.android.fcm.R;

public class FlightMap extends Activity {

	private ArrayList<GpsFrame> flightLog = null;

	// GUI
	private MapView map;
	private TextView tv_info;
	private ListView gpsData;
	private GpsListAdapter gpsListAdapterView;

	// Overlay
	private ItemizedIconOverlay<OverlayItem> currentLocationOverlay;
	private ArrayList<OverlayItem> poi;
	private DefaultResourceProxyImpl resourceProxy;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flight_map_portrait);

		// get Intent Data
		Bundle data = getIntent().getExtras();
		if (data != null) {
			flightLog = (ArrayList<GpsFrame>) data.getSerializable("flightLog");
		}

		// simulate flight Track
		//simulateTrack();

		// Info Table
		gpsData = (ListView) findViewById(R.id.fm_table);
		gpsListAdapterView = new GpsListAdapter(this);
		gpsData.setAdapter(gpsListAdapterView);
		for (int j = 0; j < flightLog.size(); j++)
			gpsListAdapterView.add(flightLog.get(j));

		gpsData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				map.getController().setCenter(
						new GeoPoint(flightLog.get(position).getLatitude(),
								flightLog.get(position).getLongitude()));
			}

			public void onNothingSelected(AdapterView parentView) {

			}
		});

		// Flight Info
		tv_info = (TextView) findViewById(R.id.fm_info);
		double speedMax = 0.;
		double heightMin = 0., heightMax = 0.;
		for (int k = 0; k < flightLog.size(); k++) {
			double speed = flightLog.get(k).getSpeedValue(SPEED.ms);
			if (speedMax < speed)
				speedMax = speed;

			double height = flightLog.get(k).getAltitude();
			if (heightMin > height)
				heightMin = height;
			if (heightMax < height)
				heightMax = height;
		}

		StringBuilder flightInfo = new StringBuilder();
		flightInfo.append("max. Speed " + speedMax + " m/s\n");
		flightInfo.append("min. Height " + heightMin + ", max. Height "
				+ heightMax);
		tv_info.setText(flightInfo.toString());

		// Map Issues
		map = (MapView) findViewById(R.id.fm_map);
		map.setTileSource(TileSourceFactory.MAPQUESTOSM);

		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		map.getController().setZoom(16);
		map.getController().setCenter(getCenterPoint());

		// Overlay Path

		PathOverlay myPath = new PathOverlay(Color.RED, this);
		for (int i = 0; i < flightLog.size(); i++) {
			myPath.addPoint(new GeoPoint(flightLog.get(i).getLatitude(),
					flightLog.get(i).getLongitude()));
		}
		map.getOverlays().add(myPath);

		// Special Points
		resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
		poi = new ArrayList<OverlayItem>();

		OverlayItem start = new OverlayItem("Start", "Position",
				new GeoPoint(flightLog.get(0).getLatitude(), flightLog.get(0)
						.getLongitude()));
		Drawable blkMarker = getResources().getDrawable(
				R.drawable.map_blk_marker_small);
		start.setMarker(blkMarker);
		poi.add(start);

		OverlayItem end = new OverlayItem("End", "Position", new GeoPoint(
				flightLog.get(flightLog.size() - 1).getLatitude(), flightLog
						.get(flightLog.size() - 1).getLongitude()));
		Drawable blueMarker = getResources().getDrawable(
				R.drawable.map_blue_marker_small);
		end.setMarker(blueMarker);
		poi.add(end);
		POIItemizedOverlay overlay = new POIItemizedOverlay(this, poi);
		map.getOverlays().add(overlay);

	}

	private GeoPoint getCenterPoint() {
		if (flightLog.size() != 0)
			return (new GeoPoint(flightLog.get(0).getLatitude(), flightLog.get(
					0).getLongitude()));
		else
			return new GeoPoint(0., 0.);
	}

	private void simulateTrack() {
		flightLog = new ArrayList<GpsFrame>();

		flightLog.add(new GpsFrame(10.332f, 48.389f, 440f, 2f, 0f, 0f, 0f, 0f,
				0f, 6));
		flightLog.add(new GpsFrame(10.342f, 48.389f, 410f, 0f, 3f, 0f, 0f, 0f,
				0f, 6));
		flightLog.add(new GpsFrame(10.342f, 48.383f, 420f, 1f, 2f, 0f, 0f, 0f,
				0f, 6));
		flightLog.add(new GpsFrame(10.332f, 48.383f, 440f, 0f, 0f, 0f, 0f, 0f,
				0f, 6));

	}
}
