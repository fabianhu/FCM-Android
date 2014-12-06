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
// (c) Joachim Weisahutp
// created 06.12.2014 
//
// + get raw gps data from FCM
// - interpret fcm kml export, integrate intent filter for kml tracks
// - Show Track, Overlay
// - Show Table with track details
// - menu analyse flight - max speed, altitude, etc.
// - landscape layout

package de.huslik_elektronik.android.flightMap;

import java.util.ArrayList;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Activity;
import android.os.Bundle;
import de.huslik_elektronik.android.Gps.GpsFrame;
import de.huslik_elektronik.android.fcm.R;

public class FlightMap extends Activity {

	private ArrayList<GpsFrame> flightLog = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flight_map_portrait);

		// get Intent Data
		Bundle data = getIntent().getExtras();
		if (data != null) {
			flightLog = (ArrayList<GpsFrame>) data
					.getSerializable("flightLog");
		}
		
		int s = flightLog.size();
		
		MapView map = (MapView) findViewById(R.id.fm_map);
		map.setTileSource(TileSourceFactory.MAPQUESTOSM);

		map.setBuiltInZoomControls(true);
		map.setMultiTouchControls(true);
		map.getController().setZoom(19);
		map.getController().setCenter(new GeoPoint(48.5667, 10.4833));

	}
}
