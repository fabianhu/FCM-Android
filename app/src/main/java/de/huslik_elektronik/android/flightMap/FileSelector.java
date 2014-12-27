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

package de.huslik_elektronik.android.flightMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import de.huslik_elektronik.android.fcm.R;

public class FileSelector extends Activity {

    private ListView lv_files;
    private ArrayAdapter<String> filesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_selector);

        lv_files = (ListView) findViewById(R.id.fileSelector);


        // Scan Fcm Directory for kml
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getAbsolutePath().matches(".*\\.kml");
            }
        };

        File f;
        List<String> paths;
        try {
            f = new File(this.getExternalFilesDir(null).getAbsolutePath());
            // Scan dir
            paths = new ArrayList<String>();
            File[] files = f.listFiles(filter);
            for (int i = 0; i < files.length; ++i) {
                paths.add(files[i].getName());//  getAbsolutePath());
            }

            filesAdapter =
                    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, paths);
            lv_files.setAdapter(filesAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        lv_files.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                String filename = filesAdapter.getItem(position);
                Intent returnIntent = new Intent();
                returnIntent.putExtra(FlightMap.FILENAME, filename);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_selector, menu);
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
