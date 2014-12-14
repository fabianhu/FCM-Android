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

import android.graphics.Color;
import de.huslik_elektronik.android.fcm.FcmData;

public class SensorFrame {
	private int[] g;
	private int[] a;
	private int[] m;
	private int[] gov;
	private int[] rc;
	private int h;
	private int[] temp;

	private int dimension;

	// Standard WL
	public enum SENSOR {
		Gyro, Acceleration, Magneto, Governor, RC, Altitude, Temperature
	};

	final public static int[] dim = { 3, 3, 3, 3, 4, 1, 2 };
	final public static String[] labelCount = { "x", "y", "z", "h" };
	final public static int[] colorList = { Color.RED, Color.BLUE, Color.GREEN,
			Color.YELLOW };

	public static String[] getLabel() {
		SENSOR[] vals = SENSOR.values();
		String[] label = new String[vals.length];
		for (int i = 0; i < vals.length; i++)
			label[i] = vals[i].name();
		return (label);
	}

	public static int getDimension(SENSOR idx) {
		
		return dim[idx.ordinal()];		
	}

	public static String getChartLabel(SENSOR labelIdx, int index, int type) {
		String sIdx = "";
		if (type == 0) // x,y,z SubIndex
			sIdx = labelCount[index];
		else
			sIdx = "" + index;

		int end = 4;
		if (labelIdx.name().length() <= end)
			end = labelIdx.name().length();
		String s = labelIdx.name().substring(0, end) + "_" + sIdx;
		return (s);
	}

	public int[] getSensorArray(SENSOR labelIdx) {
		int[] a = new int[3];

		switch (labelIdx) {
		case Gyro:
			a = getG();
			break;
		case Acceleration:
			a = getA();
			break;
		case Magneto:
			a = getM();
			break;
		case Governor:
			a = getGov();
			break;
		case RC:
			a = getRc();
			break;
		case Altitude:
			a[0] = getH();
			break;
		case Temperature:
			a = getTemp();
			break;

		default:
			break;
		}

		return a;
	}

	public SensorFrame() {
		init(3); // Standard, no quaternions
	}

	public SensorFrame(int n) {
		init(n);
	}

	private void init(int n) {
		dimension = n;
		g = new int[n];
		a = new int[n];
		m = new int[n];
		gov = new int[3];
		rc = new int[4];
		temp = new int[2];
	}

	public int[] getG() {
		return g;
	}

	public void setG(int[] g) {
		this.g = g;
	}

	public int[] getA() {
		return a;
	}

	public void setA(int[] a) {
		this.a = a;
	}

	public int[] getM() {
		return m;
	}

	public void setM(int[] m) {
		this.m = m;
	}

	public int[] getGov() {
		return gov;
	}

	public void setGov(int[] gov) {
		this.gov = gov;
	}

	public int[] getRc() {
		return rc;
	}

	public void setRc(int[] rc) {
		this.rc = rc;
	}

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}

	public int[] getTemp() {
		return temp;
	}

	public void setTemp(int[] temp) {
		this.temp = temp;
	}


	public void setFrame(byte[] buf) {
		if (dimension == 3) // Standardframe
		{
			int pos = 0;
			int i = 0;

			int b[] = new int[dimension];
			// Gyro
			for (i = 0; i < dimension; i++) {
				b[i] = FcmData.convFcmAndroidInt32(buf, pos);
				pos += 4;
			}
			setG(b);
			
			// Acc
			int a[] = new int[dimension];
			for (i = 0; i < dimension; i++) {
				a[i] = FcmData.convFcmAndroidInt32(buf, pos);
				pos += 4;
			}
			setA(a);
			// Magnetic
			int m[] = new int[dimension];
			for ( i = 0; i < dimension; i++) {
				m[i] = FcmData.convFcmAndroidInt32(buf, pos);
				pos += 4;
			}
			setM(m);
			// Gov
			int gov [] = new int[3];
			for ( i = 0; i < 3; i++) {
				gov[i] = FcmData.convFcmAndroidInt32(buf, pos);
				pos += 4;
			}
			setGov(gov);
			// RC
			int rc[] = new int[4];
			for ( i = 0; i < 4; i++) {
				rc[i] = FcmData.convFcmAndroidInt32(buf, pos);
				pos += 4;
			}
			setRc(rc);
			// h
			int h = FcmData.convFcmAndroidInt32(buf, pos);
			pos += 4;
			setH(h);
			// temp
			int temp[] = new int[2];
			for ( i = 0; i < 2; i++) {
				temp[i] = FcmData.convFcmAndroidInt16(buf, pos);
				pos += 2;
			}
			setTemp(temp);					
		}
	}

}
