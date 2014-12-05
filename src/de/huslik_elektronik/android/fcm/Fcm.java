/*
 * (c) 2014 by Joachim Weishaupt; Based on the example "Bluetooth Chat"; Apache license seems to be ok to re-release under GPLv3
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
 *
 * original license:
 * 
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.huslik_elektronik.android.fcm;

//import com.example.android.BluetoothChat.R;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import de.huslik_elektronik.android.Gps.FragmentGps;
import de.huslik_elektronik.android.Sensor.FragmentSensor;
import de.huslik_elektronik.android.fcm.FcmData.COMMAND;
import de.huslik_elektronik.android.listview.FragmentPara;

/**
 * This is the main Activity that displays the current chat session.
 */
public class Fcm extends Activity {
	// Debugging

	private static final String TAG = "FCM";
	public static final boolean D = true;

	// Modules
	public static final boolean M_MENU = true;
	public static final boolean M_PARA = false;
	public static final boolean M_SENS = true;
	public static final boolean M_GPS = true;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;

	private static final int REQUEST_ENABLE_BT = 3;

	// Preferences FCM
	public static String PREFS_NAME = "FCM_Android";
	public static String LAST_BT = "lastKnownDevice";
	SharedPreferences setting;
	public static int MENUREPEAT = 5; // after MENUREPEAT loops - menu was
										// written to ui

	// FcmStatus
	public static final int FCM_NOTCONNECT = 0;
	public static final int FCM_CONNECTED = 1;

	// Tabbed Style
	public static final String FMENU = "FRAGMENU";
	private ActionBar.Tab tbMenu;
	private FragmentMenu fMenu;

	public static final String FINFO = "FRAGINFO";
	private ActionBar.Tab tbInfo;
	private FragementInfo fInfo;

	public static final String FPARA = "FRAGPARA";
	private ActionBar.Tab tbPara;
	private FragmentPara fPara;

	public static final String FSENS = "FRAGSENS";
	private ActionBar.Tab tbSens;
	private FragmentSensor fSens;

	public static final String FGPS = "FRAGGPS";
	private ActionBar.Tab tbGps;
	private FragmentGps fGps;

	// Power Manager
	public PowerManager pm = null;
	protected PowerManager.WakeLock mWakeLock;

	// FCM Version
	private int versionH = -1, versionL = -1;

	// FCM Data Handling
	private FcmData fd = new FcmData();
	private FcmByteBuffer buffer = new FcmByteBuffer(this);

	// Name of the connected device
	private String mConnectedDeviceName = null;

	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private FcmService mFcmService = null;
	// FcmStatus
	public int mFcmConnectStatus = FCM_NOTCONNECT;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// Power Manager
		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Setup Fragements
		StringBuilder mInfo = new StringBuilder();

		if (M_MENU) {
			fMenu = new FragmentMenu();
			fMenu.setFcm(this);
			tbMenu = actionBar.newTab().setText("Menue");
			tbMenu.setTabListener(new FcmTabListener(fMenu));
			actionBar.addTab(tbMenu);
			mInfo.append("Menue");
		}

		if (M_PARA) {
			fPara = new FragmentPara();
			fPara.setFcm(this);
			tbPara = actionBar.newTab().setText("Parameters");
			tbPara.setTabListener(new FcmTabListener(fPara));
			actionBar.addTab(tbPara);
			mInfo.append(", Parameter");
		}

		if (M_SENS) {
			fSens = new FragmentSensor();
			fSens.setFcm(this);
			tbSens = actionBar.newTab().setText("Sensors");
			tbSens.setTabListener(new FcmTabListener(fSens));
			actionBar.addTab(tbSens);
			mInfo.append(", Sensors");
		}

		if (M_GPS) {
			fGps = new FragmentGps();
			fGps.setFcm(this);
			tbGps = actionBar.newTab().setText("Gps");
			tbGps.setTabListener(new FcmTabListener(fGps));
			actionBar.addTab(tbGps);
			mInfo.append(", GPS");
		}

		fInfo = new FragementInfo();
		fInfo.setModules(mInfo.toString());
		fInfo.setFcm(this);
		tbInfo = actionBar.newTab().setText("Info");
		tbInfo.setTabListener(new FcmTabListener(fInfo));
		actionBar.addTab(tbInfo);

		// Set up the window layout
		setContentView(R.layout.main);

		// Preferences
		setting = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

		File f = getDatabasePath(PREFS_NAME + ".xml");

		if (f != null)
			Log.i("TAG", f.getAbsolutePath());

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// Screenlock
		getScreenLock();

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

			// Otherwise, setup the chat session
		} else {
			if (mFcmService == null)

				startupFcmService();
		}

	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		// reconnect, if connected
		if (D)
			Log.e(TAG, "+ ON RESUME +");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");

		// Release Screenlock
		releaseScreenLock();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Stop the Bluetooth chat services
		if (mFcmService != null)
			shutsownFcmService();

		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	// Screenlock
	public void getScreenLock() {
		this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"My Tag");
		this.mWakeLock.acquire();
	}

	public void releaseScreenLock() {
		this.mWakeLock.release();
		super.onDestroy();
	}

	/**
	 * Sends a message.
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	public void sendMessage(String message) {
		byte[] send = message.getBytes();
		sendMessage(send);
	}

	public void sendMessage(byte[] message) {
		// Check that we're actually connected before trying anything
		if (mFcmService.getState() != FcmService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		if (message.length > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			COMMAND lastCmd = fd.getLastCmd();
			if (lastCmd != COMMAND.MNU0)
				mFcmService.write(fd.getCmdStart(COMMAND.STS));
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Log.e("FCM", "sendMessage - wait before delete buffer");
			}
			if (buffer != null)
				buffer.delete(0, buffer.size()); // need to be synchronized

			byte[] send = message;
			mFcmService.write(send);
			// Reset out string buffer to zero and clear the edit text field
			// mOutStringBuffer.setLength(0);

			buffer.setLastCmd(fd.getLastCmd());
		}
	}

	private final void setStatus(int resId) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(resId);
	}

	private final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case FcmService.STATE_CONNECTED:
					setStatus(getString(R.string.title_connected_to,
							mConnectedDeviceName));
					break;
				case FcmService.STATE_CONNECTING:
					setStatus(R.string.title_connecting);
					break;
				case FcmService.STATE_LISTEN:
				case FcmService.STATE_NONE:
					setStatus(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_WRITE:
				//byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				//String writeMessage = new String(writeBuf);
				break;
			case MESSAGE_READ:
				// byte[] readBuf = (byte[]) msg.obj;

				byte[] readBuf = (byte[]) msg.obj;
				buffer.add(readBuf, msg.arg1); // byte buffer solves 0x10 LF
				break;

			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public FcmByteBuffer getBuffer() {
		return buffer;
	}

	public FragmentMenu getfMenu() {
		return fMenu;
	}

	public FragmentPara getfPara() {
		return fPara;
	}

	public FragmentSensor getfSens() {
		return fSens;
	}

	public int getVersionH() {
		return versionH;
	}

	public void setVersionH(int versionH) {
		this.versionH = versionH;
	}

	public int getVersionL() {
		return versionL;
	}

	public void setVersionL(int versionL) {
		this.versionL = versionL;
	}

	public PowerManager getPowerManager() {
		return pm;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true, false);
			}
			break;
		/*
		 * case REQUEST_CONNECT_DEVICE_INSECURE: // When DeviceListActivity
		 * returns with a device to connect if (resultCode ==
		 * Activity.RESULT_OK) { connectDevice(data, false); } break;
		 */
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				// setupFcmMenu();
			} else {
				// User did not enable Bluetooth or an error occurred
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data, boolean secure, boolean lastConnect) {
		String address;

		if (lastConnect)
			address = setting.getString(LAST_BT, "");
		else
			address = data.getExtras().getString(
					DeviceListActivity.EXTRA_DEVICE_ADDRESS); // Get the device
																// MAC address

		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

		// Save last BT device
		if (device != null) {
			Editor editor = setting.edit();
			editor.putString(LAST_BT, address);
			editor.commit();
		}

		// StartupFcmSerivce and Processing

		startupFcmService();

		// Attempt to connect to the device
		mFcmService.connect(device);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Log.d(TAG, "Wait to complete Connection \n" + e);
		}

		// FCM Hello
		sendMessage(fd.getCmdStr(COMMAND.FCM));
	}

	private void disconnectDevice() {
		shutsownFcmService();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.last_connect:
			// TODO connection works, but no streaming
			if (mFcmConnectStatus == FCM_NOTCONNECT) {
				connectDevice(null, true, true);
				mFcmConnectStatus = FCM_CONNECTED;
				item.setIcon(R.drawable.ic_disconnect);

			} else {
				disconnectDevice();
				mFcmConnectStatus = FCM_NOTCONNECT;
				item.setIcon(R.drawable.ic_call_start);

			}

			return true;
		case R.id.secure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return true;
			// case R.id.insecure_connect_scan:
			// // Launch the DeviceListActivity to see devices and do scan
			// serverIntent = new Intent(this, DeviceListActivity.class);
			// startActivityForResult(serverIntent,
			// REQUEST_CONNECT_DEVICE_INSECURE);
			// return true;
			// case R.id.discoverable:
			// // Ensure this device is discoverable by others
			// ensureDiscoverable();
			// return true;
		}
		return false;
	}

	private void startupFcmService() {

		// Initialize the BluetoothChatService to perform bluetooth
		// connections
		if (mFcmService == null)
			mFcmService = new FcmService(this, mHandler);
		// Buffer Processing start
		if (buffer.running() == false)
			buffer.startProcessing();
	}

	private void shutsownFcmService() {
		sendMessage(fd.getCmdStr(COMMAND.STS));
		mFcmService.stop();
		buffer.stopProcessing(); // stop worker task
	}

}
