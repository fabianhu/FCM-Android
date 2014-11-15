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

package de.huslik_elektronik.android.fcm;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FragementInfo extends Fragment {

	public static String TAG = "FCM_INFO";

	// Layout FCM Info
	private TextView tvInfo;

	// References
	private Fcm fcm = null;
	private FcmData fd = new FcmData();
	
	private String sModules = "";

	public void setFcm(Fcm fcm) {
		this.fcm = fcm;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.info, container, false);
		setupFcmInfo(view);
		return view;
	}
	
	public void setModules(String s)
	{
		sModules = "\nActivated Modules: " + s;
	}
	

	private void setupFcmInfo(View v) {
		Log.d(TAG, "setupFcmInfo()");
		tvInfo = (TextView) v.findViewById(R.id.i_info);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		String sInfo = fcm.getString(R.string.i_infoText);

		int h = (fcm.getVersionH() & 0xff00) >> 8;
		int l = fcm.getVersionH() & 0x00ff;
		String sFcmVersion = "FCM Version " + h + "." + l + "."
				+ fcm.getVersionL() + "\n";
		String sParameters = "";
		if (fcm.M_PARA)
			sParameters = "Parameters: " + fcm.getfPara().getAmount();
				
		tvInfo.setText(sInfo + sFcmVersion + sParameters + sModules);
	}

}
