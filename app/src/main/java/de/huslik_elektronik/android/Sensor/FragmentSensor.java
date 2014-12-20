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

package de.huslik_elektronik.android.Sensor;

import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.app.Fragment;
//import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import de.huslik_elektronik.android.Sensor.SensorFrame.SENSOR;
import de.huslik_elektronik.android.fcm.Fcm;

import de.huslik_elektronik.android.fcm.FcmData;
import de.huslik_elektronik.android.fcm.FcmData.COMMAND;
import de.huslik_elektronik.android.fcm.R;

public class FragmentSensor extends Fragment implements OnItemSelectedListener,
		OnSeekBarChangeListener {

	public static String TAG = "FCM_SENSOR";
	public final static int SENSOR_S = 3;

	// Chart
	private ArrayList<SensorFrame> data = new ArrayList<SensorFrame>();
	private XYMultipleSeriesDataset mDataset = null; // new
														// XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer = null; // new
														// XYMultipleSeriesRenderer();

	// Layout
	private View rootView;
	private GraphicalView chart;
	private Button logStart, logStop;
	private Spinner spDataset;
	private LinearLayout chartContainer;
	private SeekBar frameRate;
	private TextView tvFrameRate;

	// References
	private Fcm fcm = null;
	private FcmData fd = new FcmData();

	// Settings
	private static int repaintCycle = 100; // repaint every $repaintcylce$ adds
	private byte fR = 20; // Framerate

	public FragmentSensor() {
	}

	public void setFcm(Fcm fcm) {
		this.fcm = fcm;
	}

	private void regHandler() {
		fcm.getBuffer().regParaHandler(sHandler);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.sensor, container, false);
		regHandler();
		setupFcmSensor(rootView);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();
		// getScreenLock();
	}

	@Override
	public void onStop() {
		super.onStop();
		// releaseScreenLock();
	}

	public void addSensorFrame(SensorFrame sf) {
		data.add(sf);

		Log.d(TAG, "Frame No. " + data.size());

		SENSOR sens = SENSOR.Gyro; // Gyro
		// for (int i = 0; i < data.size(); i++) {
		int[] d = sf.getSensorArray(sens);
		for (int j = 0; j < mDataset.getSeriesCount(); j++) {
			XYSeries serie = mDataset.getSeriesAt(j);
			serie.add(data.size(), d[j]);
		}

		// }

		int s = getSensorFrameSize();
		int updateRate = repaintCycle / fR;
		if (updateRate < 1)
			updateRate = 1;
		if (s % repaintCycle == 0) { // set Min
			if (Fcm.D) {
				String str = "Graph min:" + s;
				Log.d(TAG, str);
			}
			mRenderer.setXAxisMin(s - repaintCycle);
		}
		if (s % updateRate == 0) { // Repaint
			if (Fcm.D)
				Log.d(TAG, "Graph repaint");
			mRenderer.setXAxisMax(s); // set max on every update
			chart.repaint();

		}

	}

	public int getSensorFrameSize() {
		return (data.size());
	}

	public void clearSensorFrameData() {
		data.clear();
	}

	private GraphicalView setupChart(View v, SENSOR sens) {

		// SENSOR sens = SENSOR.Gyro; // Gyro

		mDataset = new XYMultipleSeriesDataset();
		mRenderer = new XYMultipleSeriesRenderer();

		int dim = SensorFrame.getDimension(sens);

		for (int j = 0; j < dim; j++) {
			// Series
			XYSeries f = new XYSeries(SensorFrame.getChartLabel(sens, j, 0));
			mDataset.addSeries(f);
			// Renderer
			XYSeriesRenderer renderer = new XYSeriesRenderer();
			renderer.setColor(SensorFrame.colorList[j]);
			renderer.setFillPoints(true);
			renderer.setPointStyle(PointStyle.POINT);
			renderer.setLineWidth(5.0f);
			mRenderer.addSeriesRenderer(renderer);
		}

		Resources r = v.getContext().getResources();
		float size = r.getDimension(R.dimen.textsizeChart);
		mRenderer.setLabelsTextSize((float) (size * 0.8));
		mRenderer.setLegendTextSize(size);

		mRenderer.setZoomEnabled(true);
		// mRenderer.setZoomButtonsVisible(true);
		// not sizeable -> need to put own buttons -
		// http://stackoverflow.com/questions/13444672/achartengine-zoom-button-size

		for (int i = 0; i < data.size(); i++) {
			int[] d = data.get(i).getSensorArray(sens);
			for (int j = 0; j < mDataset.getSeriesCount(); j++) {
				XYSeries serie = mDataset.getSeriesAt(j);
				serie.add(i, d[j]);
			}
		}

		GraphicalView chart = ChartFactory.getLineChartView(this.getActivity(),
				mDataset, mRenderer);
		return chart;

	}

	private void setupFcmSensor(View v) {
		Log.d(TAG, "setupFcmSensor()");

		Resources r = v.getResources();
		float sizeMenu = r.getDimension(R.dimen.textsizeMenu);

		logStart = (Button) v.findViewById(R.id.logStart);
		logStart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				clearSensorFrameData();
				String cmd = fd.getCmdStr(COMMAND.STD);
				StringBuffer s = new StringBuffer(cmd);

				// frameRate
				fR = (byte) Byte.parseByte(tvFrameRate.getText().toString());
				char fRC = (char) fR;
				s.insert(6, fRC);
				cmd = s.toString();
				fcm.sendMessage(cmd);
			}
		});
		logStop = (Button) v.findViewById(R.id.logStop);
		logStop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				// FcmByteBuffer buffer = (FcmByteBuffer)
				// (fcm.getBuffer()).clone();
				fcm.sendMessage(fd.getCmdStr(COMMAND.STS));

				mRenderer.setXAxisMin(0);
				mRenderer.setXAxisMax(getSensorFrameSize());
				chart.repaint();

			}

		});

		frameRate = (SeekBar) v.findViewById(R.id.frameRate);
		frameRate.setMax(100);
		frameRate.setProgress(fR);
		frameRate.setOnSeekBarChangeListener(this); // set seekbar listener.

		tvFrameRate = (TextView) v.findViewById(R.id.textframeRate);
		tvFrameRate.setText("" + frameRate.getProgress());

		ArrayAdapter<String> datasetAdapter = new ArrayAdapter<>(
				v.getContext(), android.R.layout.simple_spinner_dropdown_item,
				SensorFrame.getLabel());
		spDataset = (Spinner) v.findViewById(R.id.dataType);
		spDataset.setAdapter(datasetAdapter);
		spDataset.setOnItemSelectedListener(this);

		// setupData();

		chartContainer = (LinearLayout) v.findViewById(R.id.chart);
		chart = setupChart(v, SENSOR.Acceleration); // .Gyro);
		chartContainer.addView(chart);

	}

	// Spinner Methods
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		// An item was selected. You can retrieve the selected item using
		// parent.getItemAtPosition(pos)

		String[] sensArray = SensorFrame.getLabel();
		String sens = sensArray[pos];

		// Chart replace for neu sensordata
		chartContainer.removeView(chart);
		chart = null;
		chart = setupChart(rootView, SENSOR.valueOf(sens));
		chartContainer.addView(chart);
		chartContainer.invalidate();

	}

	public void onNothingSelected(AdapterView<?> parent) {
		// Another interface callback
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub

		// change progress text label with current seekbar value
		tvFrameRate.setText("" + (progress + 1));

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler sHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SENSOR_S:
				byte[] result = (byte[]) msg.obj;

				SensorFrame sensorFrame = new SensorFrame();
				if (result.length == 73) {
					sensorFrame.setFrame(result);
					addSensorFrame(sensorFrame);
				}
			}
		}
	};

}
