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

// Dialog for setting Framerate of BT Connection
// used from GPS, Sensors

package de.huslik_elektronik.android.fcmUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;

import de.huslik_elektronik.android.fcm.R;

public class FrameRate extends Activity {

    final public static String FRAMERATE = "FrameRate";

    final public static String MIN = "Min";
    final public static String MAX = "Max";
    final public static String VALUE = "Value";

    private int min, max, value;

    // GUI
    private NumberPicker np_Framerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        min = intent.getIntExtra(MIN, 1);
        max = intent.getIntExtra(MAX, 100);
        value = intent.getIntExtra(VALUE, 100);

        setContentView(R.layout.activity_frame_rate);
        np_Framerate = (NumberPicker) findViewById(R.id.gui_framerate);
        np_Framerate.setMinValue(min);
        np_Framerate.setMaxValue(max);
        np_Framerate.setValue(value);
    }

    public void onSetFrameRate(View v)
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(FRAMERATE, np_Framerate.getValue());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_frame_rate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
