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
 
package de.huslik_elektronik.android.listview;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import de.huslik_elektronik.android.fcm.Fcm;
import de.huslik_elektronik.android.fcm.FcmData;
import de.huslik_elektronik.android.fcm.FcmData.COMMAND;
import de.huslik_elektronik.android.fcm.R;

public class FragmentPara extends Fragment {

	public static String TAG = "FCM_PARA";
	public final static int PARAMETER = 2;

	// Layout FCM Para
	private ListView lv;
	private EditText value;
	private Button bSet, bm, bp;

	private int parameters;
	private Parameter selParaId = null;

	// List Data
	private ArrayList<Parameter> list = new ArrayList<>();
	private ArrayList<Parameter> tree = new ArrayList<>();
	private MySimpleArrayAdapter adapter = null;

	// References
	private Fcm fcm = null;
	private FcmData fd = new FcmData();

	public void setFcm(Fcm fcm) {
		this.fcm = fcm;
	}

	private void regHandler() {
		fcm.getBuffer().regParaHandler(pHandler);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (Fcm.D)
			Log.d(TAG, "setupFcmPara()");

		View view = inflater.inflate(R.layout.vparameter, container, false);
		regHandler();
		setupFcmPara(view);
		getFcmPara();
		// setupData();
		// setupFcmPara();
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	private void getFcmPara() {
		list.clear();
		fcm.sendMessage(fd.getCmdStr(COMMAND.PARL));
	}

	public void setAmount(int number) {
		parameters = number;
	}

	public int getAmount() {
		return parameters;
	}

	public void newParameter(byte[] val, String t) {
		byte id = val[0];
		byte parentId = val[1];

		int v = FcmData.convFcmAndroidInt16(val, 2);
		int vMax = FcmData.convFcmAndroidInt16(val, 4);
		int vMin = FcmData.convFcmAndroidInt16(val, 6);

		String tag = t + " (" + id + ")";

		list.add(new Parameter(id, parentId, tag, v, vMin, vMax));

		if (id >= parameters - 1)
			setupFcmPara();
	}

	private void setupFcmPara() {
		checkIfChild(list);
		checkRoot();
		prepareVisTree(null);
		if (adapter != null)
			adapter.notifyDataSetChanged();
	}

	private void setupFcmPara(View v) {

		lv = (ListView) v.findViewById(R.id.listview);
		value = (EditText) v.findViewById(R.id.ed_value);

		bSet = (Button) v.findViewById(R.id.bset);

		bp = (Button) v.findViewById(R.id.bplus);
		bm = (Button) v.findViewById(R.id.bminus);

		adapter = new MySimpleArrayAdapter(v.getContext(), tree);
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				// final Parameter item = (Parameter) parent
				// .getItemAtPosition(position);
				Parameter p = tree.get(position);
				if (!p.isChild()) {
					p.toggleOpen();
					prepareVisTree(null);
					adapter.notifyDataSetChanged();
					selParaId = null;

				} else {

					selParaId = p;
					value.setText(((Integer) p.getVal()).toString());

				}
			}
		});

		bSet.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int val = Integer.parseInt(value.getText().toString());
				value.setText("");
				changeValue(selParaId, 0, val);

				// DATA to FCM
				byte[] data = FcmData.getParCmd((byte) selParaId.getId(),
						selParaId.getVal());
				fcm.sendMessage(data);

				adapter.notifyDataSetChanged();
			}
		});
		bm.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int val = Integer.parseInt(value.getText().toString());
				if (val > selParaId.getMinVal() && val < selParaId.getMaxVal()) {
					val--;
					value.setText(((Integer) val).toString());
				}
			}
		});
		bp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				int val = Integer.parseInt(value.getText().toString());
				if (val > selParaId.getMinVal() && val < selParaId.getMaxVal()) {
					val++;
					value.setText(((Integer) val).toString());
				}
			}
		});

	}

	private void changeValue(Parameter p, int step, int value) {
		int newVal;
		if (step != 0)
			newVal = p.getVal() + step;
		else
			newVal = value;

		if (newVal >= p.getMinVal() && newVal <= p.getMaxVal())
			p.setVal(newVal);
	}

	private void updateLists(int val) {
		int i;
		if (selParaId != null) {
			// list.get(selParaId.getPos()).setVal(val);
			for (i = 0; i < list.size(); i++)
				if (selParaId.getId() == list.get(i).getId()) {
					list.get(i).setVal(val);
					break;
				}
			// tree.get(selParaId.getPos()).setVal(val);
			for (i = 0; i < tree.size(); i++)
				if (selParaId.getId() == tree.get(i).getId()) {
					tree.get(i).setVal(val);
					break;
				}
			adapter.notifyDataSetChanged();
		}
	}

	private void checkIfChild(ArrayList<Parameter> list) {
		for (int i = 0; i < list.size(); i++)
			for (int j = i; j < list.size(); j++)
				if (list.get(i).getId() == list.get(j).getParentId())
					list.get(i).setChild(false);
	}

	private void checkRoot() {
		// root must be child, Version Info
		for (int i = 0; i < list.size(); i++)
			if (list.get(i).getId() == 0)
				list.get(i).setChild(true);
	}

	private void prepareVisTree(Parameter p) {
		prepareVisTree(p, 0);
	}

	private void prepareVisTree(Parameter parent, int level) {
		Parameter p = null;
		if (parent == null) {
			MenuItem.resetCount();
			tree.clear();
		}
		for (int i = 0; i < list.size(); i++) {
			p = (Parameter) list.get(i);

			if (parent == null) {
				if (p.getParentId() == 0) {
					p.setLevel(level);
					if (!(p.isChild() && p.getMinVal() == 0 && p.getMaxVal() == 0)) {
						tree.add(p);
						if (p.isOpen())
							prepareVisTree(p, level + 1);
					}
				}

			} else if (parent.getId() == p.getParentId() & parent.isOpen()) {
				p.setLevel(level);
				if (!(p.isChild() && p.getMinVal() == 0 && p.getMaxVal() == 0)) {
					tree.add(p);
					if (p.isOpen())
						prepareVisTree(p, level + 1);
				}
			}

		}
	}

	// The Handler that gets information back from the BluetoothChatService
	private final Handler pHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PARAMETER:
				byte[] result = (byte[]) msg.obj;

				byte[] tag = Arrays.copyOfRange(result, 8, result.length);
				String netTag = new String(tag);
				String showTag = "";
				for (int i = 0; i < tag.length; i++)
					if (tag[i] == (byte) 0x00) {
						showTag = netTag.substring(0, i);
						break;
					}
				newParameter(result, showTag);
				if (Fcm.D)
					Log.d(TAG, "Parameterframe " + result[0]);

			}
		}
	};
}