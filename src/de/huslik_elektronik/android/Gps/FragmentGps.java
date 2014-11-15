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

// GPS Fragment, fcm -> android via bt conversion
// Listview - Data
// planned:
// + Intent to OSM Droid Maps
// + KML Export

package de.huslik_elektronik.android.Gps;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.location.GpsStatus;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import de.huslik_elektronik.android.fcm.Fcm;
import de.huslik_elektronik.android.fcm.FcmByteBuffer;
import de.huslik_elektronik.android.fcm.FcmData;
import de.huslik_elektronik.android.fcm.FcmData.COMMAND;
import de.huslik_elektronik.android.fcm.R;

public class FragmentGps extends Fragment {
	public static String TAG = "FCM_GPS";
	public final static int GPS = 3;

	// References
	private Fcm fcm = null;
	private FcmData fd = new FcmData();

	// Layout
	private View rootView;
	private TextView tvGpsData;
	private Button btn_start, btn_stop;
	private ListView lvGpsList;

	// div
	private byte fR = 100; // Framerate 100 * 10 ms

	// data
	private ArrayList<GpsFrame> lGpsFrame = new ArrayList<>();
	private GpsListAdapter gpsListAdapterView;

	public void setFcm(Fcm fcm) {
		this.fcm = fcm;
	}

	private void regHandler() {
		fcm.getBuffer().regGpsHandler(gHandler);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (Fcm.D)
			Log.d(TAG, "setupFcmGps()");

		rootView = inflater.inflate(R.layout.gps, container, false);

		regHandler();
		setupFcmGps();

		return rootView;
	}

	private void setupFcmGps() {
		lvGpsList = (ListView) rootView.findViewById(R.id.gpsData);
		gpsListAdapterView = new GpsListAdapter(this.getActivity());
		lvGpsList.setAdapter(gpsListAdapterView);

		tvGpsData = (TextView) rootView.findViewById(R.id.gpsInfo);
		btn_start = (Button) rootView.findViewById(R.id.gpsStart);

		btn_start.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				String cmd = fd.getCmdStr(COMMAND.STG);
				StringBuffer s = new StringBuffer(cmd);

				// frameRate
				char fRC = (char) fR;
				s.insert(6, fRC);
				cmd = s.toString();
				fcm.sendMessage(cmd);
			}
		});

		btn_stop = (Button) rootView.findViewById(R.id.gpsStop);

		btn_stop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				FcmByteBuffer buffer = (FcmByteBuffer) (fcm.getBuffer())
						.clone();
				fcm.sendMessage(fd.getCmdStr(COMMAND.STS));
			}

		});
	}

	@Override
	public void onStart() {
		super.onStart();
		lGpsFrame.clear();

	}

	@Override
	public void onStop() {
		super.onStop();
		// TODO Save list to file

	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler gHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GPS:
				byte[] result = (byte[]) msg.obj;

				int pos = 0;
				int longitude = FcmData.convFcmAndroidInt32(result, pos);
				pos += 4;
				int latitude = FcmData.convFcmAndroidInt32(result, pos);
				pos += 4;
				int height = FcmData.convFcmAndroidInt32(result, pos);
				// Speed
				pos += 4;
				float xSpeed = FcmData.convFcmAndroidFloat32(result, pos);
				pos += 4;
				float ySpeed = FcmData.convFcmAndroidFloat32(result, pos);
				pos += 4;
				float zSpeed = FcmData.convFcmAndroidFloat32(result, pos);
				// Distance to target
				pos += 4;
				float xDist = FcmData.convFcmAndroidFloat32(result, pos);
				pos += 4;
				float yDist = FcmData.convFcmAndroidFloat32(result, pos);
				pos += 4;
				float zDist = FcmData.convFcmAndroidFloat32(result, pos);
				int satNum = result[pos + 4];

				GpsFrame gpsFrame = new GpsFrame(longitude, latitude, height,
						xSpeed, ySpeed, zSpeed, xDist, yDist, zDist, satNum);

				lGpsFrame.add(gpsFrame);
				gpsListAdapterView.add(gpsFrame);

				String s = "Longitude: " + longitude + "\n" + "Latitude: "
						+ latitude + "\n" + "Height: " + height + "\n"
						+ "Speed: " + xSpeed + "/" + ySpeed + "/" + zSpeed
						+ "\n" + "Distance to Target: " + xDist + "/" + yDist
						+ "/" + zDist + "\n" + "Number of Satellites: "
						+ satNum;
				tvGpsData.setText(s);
			}
		}
	};
}
