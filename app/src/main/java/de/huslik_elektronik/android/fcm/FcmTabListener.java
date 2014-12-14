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

import android.app.ActionBar.Tab;
import android.app.ActionBar;			// must be imported manually
import android.app.Fragment;
import android.app.FragmentTransaction;

public class FcmTabListener implements ActionBar.TabListener {
    private Fragment fragment;

    public FcmTabListener(Fragment fragment){
        this.fragment = fragment;
    }
    public void onTabSelected(Tab tab, FragmentTransaction ft){
        ft.replace(R.id.fragment_container, fragment);
    }
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    	// do nothing
    }
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        ft.remove(fragment);
    }
}
