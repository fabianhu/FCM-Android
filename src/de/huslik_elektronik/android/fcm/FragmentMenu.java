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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.huslik_elektronik.android.fcm.FcmData.COMMAND;

public class FragmentMenu extends Fragment {

	public static String TAG = "FCM_MENU";
	public final static int MENU = 1;

	// Layout FCM Menu
	private TextView tvMenu;
	private Button btn_mm, btn_m, btn_ent, btn_p, btn_pp, btn_mnu, btn_fcm;

	// References
	private Fcm fcm = null;
	private FcmData fd = new FcmData();

	public void setFcm(Fcm fcm) {
		this.fcm = fcm;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.menu, container, false);
		regHandler();
		setupFcmMenu(view);
		return view;
	}
	
	private void regHandler()
	{
		fcm.getBuffer().regMenuHandler(mHandler);
	}
	

	public void setMenuText(String t) {
		tvMenu.setText(t);
	}

	private void setupFcmMenu(View v) {
		Log.d(TAG, "setupFcmMenu()");

		Resources r = v.getResources();
		float sizeMenu =  r.getDimension(R.dimen.textsizeMenu);	
		
		tvMenu = (TextView) v.findViewById(R.id.m_in);
		tvMenu.setTextSize(sizeMenu);

		btn_fcm = (Button) v.findViewById(R.id.m_fcm);
		btn_fcm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				fcm.sendMessage(fd.getCmdStr(COMMAND.FCM));
			}
		});
		btn_mnu = (Button) v.findViewById(R.id.m_mnu);
		btn_mnu.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				fcm.sendMessage(fd.getCmdStr(COMMAND.MNU0));
			}
		});

		btn_mm = (Button) v.findViewById(R.id.m_mm);
		btn_mm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				fcm.sendMessage(fd.getCmdStr(COMMAND.MNUM));
			}
		});

		btn_m = (Button) v.findViewById(R.id.m_m);
		btn_m.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				fcm.sendMessage(fd.getCmdStr(COMMAND.MNUm));
			}
		});

		btn_ent = (Button) v.findViewById(R.id.m_ent);
		btn_ent.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				fcm.sendMessage(fd.getCmdStr(COMMAND.MNUE));
			}
		});

		btn_p = (Button) v.findViewById(R.id.m_p);
		btn_p.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				fcm.sendMessage(fd.getCmdStr(COMMAND.MNUp));
			}
		});

		btn_pp = (Button) v.findViewById(R.id.m_pp);
		btn_pp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				fcm.sendMessage(fd.getCmdStr(COMMAND.MNUP));
			}
		});

	}
	
	// The Handler that gets information back from the BluetoothChatService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MENU:
				String menuText = (String) msg.obj;
				setMenuText(menuText);
			}
		}
	};
}
