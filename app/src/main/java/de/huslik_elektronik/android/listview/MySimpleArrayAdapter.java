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

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.huslik_elektronik.android.fcm.R;

public class MySimpleArrayAdapter extends ArrayAdapter<Parameter> {
	private final Context context;
	private final ArrayList<Parameter> values;

	public MySimpleArrayAdapter(Context context, ArrayList<Parameter> values) {
		super(context, R.layout.rowlayout, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
		TextView label = (TextView) rowView.findViewById(R.id.label);
		TextView val = (TextView) rowView.findViewById(R.id.val);
		TextView minVal = (TextView) rowView.findViewById(R.id.minVal);
		TextView maxVal = (TextView) rowView.findViewById(R.id.maxVal);

		Resources r = parent.getContext().getResources();
		float size =  r.getDimension(R.dimen.textsizeVal);
		float sizeLabel =  r.getDimension(R.dimen.textsizeLabel);

		label.setTextSize(sizeLabel);
		val.setTextSize(size);
		minVal.setTextSize(size);
		maxVal.setTextSize(size);

		Parameter p = values.get(position);
		String indent = "";
		for (int i = 0; i < p.getLevel(); i++)
			indent += "     ";
		String status = "";
		if (!p.isChild())
		{
			if (p.isOpen())
				status = " - ";
			else
				status = " + ";
			val.setText("");
			minVal.setText("");
			maxVal.setText("");
			
		}
		else {
			status = "";
			String sval = ((Integer) p.getVal()).toString();
			val.setText(sval);
			String sminval = ((Integer) p.getMinVal()).toString();
			minVal.setText(sminval);
			String smaxval = ((Integer) p.getMaxVal()).toString();
			maxVal.setText(smaxval);
		}

		label.setText(indent + status + p.getName());

		return rowView;
	}
}