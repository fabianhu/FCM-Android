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

//import java.util.Arrays;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

public class FcmData {

	public final static String sPrefix = "---";
	public final static String sPostfix = "~~~";
	public final static byte[] prefix = { '-', '-', '-' };
	public final static byte[] postfix = { '~', '~', '~' };
	
	public final static int maxCmdLen = prefix.length + 5;

	private static COMMAND lastCMD;

	public enum COMMAND {
		FCM, MNU0, MNUM, MNUm, MNUP, MNUp, MNUE, TXT, PARL, PAR, STD, STS, STG, D, G
	};

	public String getCmdStr(COMMAND number) {
		if (number == COMMAND.MNU0 | number == COMMAND.MNUM
				| number == COMMAND.MNUm | number == COMMAND.MNUP
				| number == COMMAND.MNUp || number == COMMAND.MNUE)
			lastCMD = COMMAND.MNU0;
		else
			lastCMD = number;

		return (sPrefix + number.name() + sPostfix);
	}

	public COMMAND getLastCmd() {
		return (lastCMD);
	}

	public byte[] getCmdStart(COMMAND number) {
		byte[] c = new byte[6];
		int i = 0;
		for (i = 0; i < 3; i++)
			c[i] = (byte) FcmData.sPrefix.charAt(i);
		for (i = 0; i < 3; i++)
			c[i + 3] = (byte) number.name().charAt(i);
		return c;
	}
	
	public static int convFcmAndroidInt16(byte[] b, int pos)
	{
		byte[] conv = new byte[2];
		// high, low byte order must be changed
		conv[0] = b[pos+1];				
		conv[1] = b[pos];
		int v = (int) GattUtils.getIntValue(conv, GattUtils.FORMAT_SINT16, 0);
		return v;
	}
	
	public static int convFcmAndroidInt32(byte[] b, int pos)
	{
		byte[] conv = new byte[4];
		// high, low byte order must be changed
		conv[0] = b[pos+3];				
		conv[1] = b[pos+2];
		conv[2] = b[pos+1];
		conv[3] = b[pos];
		int v = (int) GattUtils.getIntValue(conv, GattUtils.FORMAT_SINT32, 0);
		return v;
	}
	
	public static float convFcmAndroidFloat32(byte[] b, int pos)
	{
		byte[] conv = new byte[4];
		// high, low byte order must be changed
		conv[0] = b[pos+3];				
		conv[1] = b[pos+2];
		conv[2] = b[pos+1];
		conv[3] = b[pos];
		float v = (float) GattUtils.getFloatValue(conv, GattUtils.FORMAT_SFLOAT, 0);
		return v;
	}
	
	public static byte[] getParCmd(byte id, int value)
	{
		String str = "---PARVxxx~~~";        
        byte[] ba = str.getBytes(); 
        ba[7] = (byte)id;
        ba[8] = (byte)((value & 0xff00) >> 8);
        ba[9] = (byte)(value & 0xff);
        
       return(ba);
	}
}
