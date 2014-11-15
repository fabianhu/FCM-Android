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

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	 
	 private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();

	 public MyItemizedOverlay(Drawable pDefaultMarker,
	   ResourceProxy pResourceProxy) {
	  super(pDefaultMarker, pResourceProxy);
	  // TODO Auto-generated constructor stub
	 }
	 
	 public void addItem(GeoPoint p, String title, String snippet){
	  OverlayItem newItem = new OverlayItem(title, snippet, p);
	  overlayItemList.add(newItem);
	  populate(); 
	 }

	 @Override
	 public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3) {
	  // TODO Auto-generated method stub
	  return false;
	 }

	 @Override
	 protected OverlayItem createItem(int arg0) {
	  // TODO Auto-generated method stub
	  return overlayItemList.get(arg0);
	 }

	 @Override
	 public int size() {
	  // TODO Auto-generated method stub
	  return overlayItemList.size();
	 }


}
