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

package de.huslik_elektronik.android.Gps;

import java.util.Formatter;

public class GpsFrame {

	public static enum SPEED {
		ms, kmh
	};

	private int longitude, latitude; // Longitude in 10.000.000th degrees
	private int height;
	private float xSpeed, ySpeed, zSpeed; // Speed [m/s]
	private float xDist, yDist, zDist; // Distance to target [m]

	private int nSatellites;

	public GpsFrame(int lo, int la, int h, float xS, float yS, float zS,
			float xD, float yD, float zD, int nSat) {
		longitude = lo;
		latitude = la;
		height = h;
		xSpeed = xS;
		ySpeed = yS;
		zSpeed = zS;
		xDist = xD;
		yDist = yD;
		zDist = zD;
		nSatellites = nSat;
	}

	public float getLongitude() {
		return (float) (((float) longitude) / 10000000.);
	}

	public float getLatitude() {
		return (float) (((float) latitude) / 10000000.);
	}

	public int getHeight() {
		return height;
	}

	public float getxSpeed() {
		return xSpeed;
	}

	public float getySpeed() {
		return ySpeed;
	}

	public float getzSpeed() {
		return zSpeed;
	}

	public float getxDist() {
		return xDist;
	}

	public float getyDist() {
		return yDist;
	}

	public float getzDist() {
		return zDist;
	}

	public int getnSatellites() {
		return nSatellites;
	}

	public String getLocation() {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter();

		// lat 48N, lon 10E, height		
		sb.append(f.format("%2.4f; %2.4f; %4.1f", (double) getLatitude(),
				(double) getLongitude(), (double) getHeight()).toString());

		return sb.toString();
	}

	public String getSpeed(SPEED s) {
		double factor = 1.;
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter();

		if (s.equals(SPEED.ms))
			factor = 1.;
		else if (s.equals(SPEED.kmh))
			factor = 3.6;
		// x, y, z -> norm
		double norm =  Math.sqrt((double) (getxSpeed() * factor
				* getxSpeed() * factor + getySpeed() * factor * getySpeed()
				* factor + getzSpeed() * factor * getzSpeed() * factor));
		sb.append(f.format("%3.2f, %3.2f, %3.2f, %3.2f", getxSpeed() * factor,
				getySpeed() * factor, getzSpeed() * factor, norm).toString());

		return sb.toString();
	}

}
