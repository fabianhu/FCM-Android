/**
 * 28.05.2014 - Changes ArrayList -> Vector due to synchronized List effort
 * 				implements BufferProcessing Thread 
 * 
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

import java.util.Arrays;
import java.util.Vector;

import android.os.Handler;
import android.util.Log;
import de.huslik_elektronik.android.Gps.FragmentGps;
import de.huslik_elektronik.android.Sensor.SensorFrame;
import de.huslik_elektronik.android.fcm.FcmData.COMMAND;
import de.huslik_elektronik.android.listview.FragmentPara;

public class FcmByteBuffer extends Vector<Byte> {

	static final long serialVersionUID =0;

	public static String TAG = "Processing";
	private Processing mProcessingThread = null;
	private Fcm main;
	private boolean timeToQuit = true;

	// searchstring
	byte[] search = new byte[FcmData.maxCmdLen];

	// Handler Cascade
	private Handler menuHandler;
	private Handler paraHandler;
	private Handler gpsHandler;

	public FcmByteBuffer(Fcm main) {
		super();
		this.main = main;

		// initial searchstring
		int i;
		for (i = 0; i < FcmData.prefix.length; i++)
			search[i] = FcmData.prefix[i];
	}

	// Handler
	public void regMenuHandler(Handler handler) {
		this.menuHandler = handler;
	}

	public void regParaHandler(Handler handler) {
		this.paraHandler = handler;
	}

	public void regGpsHandler(Handler handler) {
		this.gpsHandler = handler;
	}

	// Thread Methods

	public void startProcessing() {
		timeToQuit = false;
		if (!running()) {
			mProcessingThread = new Processing(main, this); // everytime a new
															// Thread, no reuse
															// possible
			mProcessingThread.start();
		}
	}

	public void stopProcessing() {
		timeToQuit = true;
		mProcessingThread = null;
	}

	public boolean running() {
		return (mProcessingThread != null);
	}

	public void setLastCmd(COMMAND cmd) {
		if (mProcessingThread != null) // don't update cmd if Processing is
										// stopped
			mProcessingThread.setLastCmd(cmd);
	}

	// Data Processing

	public synchronized void add(byte[] seq, int readBytes) {
		for (int i = 0; i < readBytes; i++)
			this.add((Byte) seq[i]);
	}

	public synchronized int find(byte[] seq, int len, int start, int end) {
		int p = -1, j = 1;
		boolean found = false;
		for (int i = start; i < end - len + 1; i++) {
			try {

				if (this.get(i) == (Byte) seq[0] && !found) {
					p = i;
					found = true;
				} else {
					if (this.get(i) == (Byte) seq[j] && j < len) {
						j++;
						if (len == j) {
							// found
							break;
						}
					} else {
						found = false;
						j = 1;
						p = -1;
					}
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				Log.d(TAG, "Index out of bound, size: " + this.size());
			}
		}
		return p;
	}

	public synchronized byte[] rangeArray(int start, int end) {
		byte[] result = new byte[end - start + 1];
		int n = 0;

		for (int i = start; i <= end; i++) {
			try {
				result[n] = (byte) this.get(i);
			}

			catch (IndexOutOfBoundsException e) {
				Log.e(TAG, "delete: start " + start + " end " + end
						+ " buffersize " + this.size() + "\n" + e);
			}
			n++;
		}

		return result;
	}

	public synchronized void delete(int start, int end) {
		try {
			this.removeRange(start, end);
		} catch (IndexOutOfBoundsException e) {
			Log.e(TAG, "delete: start " + start + " end " + end
					+ " buffersize " + this.size() + "\n" + e);
		}
	}

	// Buffer Processing Thread
	private class Processing extends Thread {

		private COMMAND lastCmd;
		private Fcm main;
		private FcmByteBuffer buffer;
		private byte[] result;
		private int menuLoop = 0;

		public Processing(Fcm main, FcmByteBuffer buffer) {
			this.main = main;
			this.lastCmd = null;
			this.buffer = buffer;
			this.menuLoop = 0;
		}

		public void setLastCmd(COMMAND lastcmd) {
			this.lastCmd = lastcmd;
		}

		@Override
		public void run() {
			if (Fcm.D)
				Log.d(TAG, "Processing Buffer started");
			while (!timeToQuit) // repeat until stop -> other command
								// or STS, etc.
			{
				// Menue Command
				if (lastCmd == FcmData.COMMAND.MNU0) {

					StringBuilder sb = new StringBuilder();

					if ((menuLoop > Fcm.MENUREPEAT)
							&& (result = ProcessBuffer(buffer, COMMAND.TXT)) != null) {
						byte[] b;
						menuLoop = 0; // reset update menu
						// getParameter(result);
						if (result.length >= 21 * 8) {
							b = Arrays.copyOfRange(result, 0, 0 + 21 * 8);
							String menu = new String(b);
							for (int i = 0; i < 8; i++) {
								String part = menu.substring(21 * i,
										21 * (i + 1) - 1);
								sb.append(part + "\n");
							}
						} else {
							if (Fcm.D)
								Log.d(TAG, "Menu Result to short");
						}

					}
					menuLoop++;

					if (buffer.size() > 500) // clear when streaming MNU
					{
						buffer.clear();
						menuLoop = 0; // reset update menu
					}

					// Send the obtained bytes to the UI Activity
					if (sb.length() > 1)
						menuHandler.obtainMessage(FragmentMenu.MENU,
								sb.toString().length(), -1, sb.toString())
								.sendToTarget();
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						Log.e(TAG, "Processing " + e);
					}
				}

				// Parameter

				if (lastCmd == FcmData.COMMAND.PARL) {
					if ((result = ProcessBuffer(buffer, COMMAND.PAR)) != null) {
						paraHandler.obtainMessage(FragmentPara.PARAMETER,
								result.length, -1, result).sendToTarget();

						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							Log.e(TAG, "Processing " + e);
						}
					}
				}

				// Fcm Hello

				if (lastCmd == FcmData.COMMAND.FCM) {
					if ((result = ProcessBuffer(buffer, COMMAND.FCM)) != null) {
						main.setVersionH(((0xFF & (int) result[0]) * 256)
								+ ((0xFF & (int) result[1])));
						main.setVersionL(((0xFF & (int) result[2]) * 256)
								+ ((0xFF & (int) result[3])));
						int para = ((0xFF & (int) result[4]) * 256)
								+ ((0xFF & (int) result[5]));
						if (Fcm.M_PARA)
							main.getfPara().setAmount(para);
					}
				}

				// Sensor Data, packetlen = 72+1 ? TODO test
				// Todo put data via handler
				if (lastCmd == FcmData.COMMAND.STD) {
					if ((result = ProcessBuffer(buffer, COMMAND.D)) != null) {
						SensorFrame sensorFrame = new SensorFrame();
						if (result.length == 73) {
							sensorFrame.setFrame(result);
							main.getfSens().addSensorFrame(sensorFrame);
						}

					}
				}

				// Gps Data
				if (lastCmd == FcmData.COMMAND.STG) {					
					result = ProcessBuffer(buffer, COMMAND.G);
					if (result != null) {
						gpsHandler.obtainMessage(FragmentGps.GPS,
								result.length, -1, result).sendToTarget();
					}
				}

			}
			if (Fcm.D)
				Log.d(TAG, "Processing Buffer stopped");
		}

		// moved to Processing thread
		private synchronized byte[] ProcessBuffer(FcmByteBuffer buf, COMMAND cmd) {
			int i;
			int len = FcmData.prefix.length + cmd.name().length();

			for (i = 0; i < len - FcmData.prefix.length; i++)
				search[i + FcmData.prefix.length] = (byte) cmd.name().charAt(i);

			int idxS = 0, idxE = 0;
			byte[] result = null;
			idxS = buf.find(search, len, 0, buf.size());
			if (idxS != -1) {
				idxE = buf.find(FcmData.postfix, FcmData.postfix.length, idxS
						+ len, buf.size());
				if (idxE != -1) {
					result = buf.rangeArray(idxS + len, idxE);
					buf.delete(idxS, idxE + FcmData.postfix.length);
					idxS = buf.find(search, len, 0, buf.size());
				} else
					idxS = -1;
			}
			return result;
		}

	}

}
