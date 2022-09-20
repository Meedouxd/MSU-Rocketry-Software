/*Author: Chase Morgan
This is Jeremiah's salus data decoder ported to java from python.
Its not totally done but almost, DLE stuff needs to be handled.
Possilbe TODO: Turn this into a library to be used in later projects.
 */
import java.io.*;
import java.util.*;

//Takes in raw binary data from a .txt file and converts it back into readable data
public class Decoder {
	public static final byte STX = 0x02;
	public static final byte ETX = 0x03;
	public static final byte DLE = 0x10;

	public static void main(String arg[]) throws IOException {
		// Data Buffer
		byte[] packetBuffer = new byte[1024];
		int packetBufferPos = 0;
		// File that we read from
		File file = new File("/tmp/salus.txt");
		// Byte Array where file's contents are stored
		byte[] bFile = new byte[(int) file.length()];

		// putting the contents of the file into a byte array
		FileInputStream fileInputStream = new FileInputStream(file);
		fileInputStream.read(bFile);
		fileInputStream.close();

		ArrayList<RocketDataPacket> packetList = new ArrayList<>();
		
		// looping through each byte
		boolean dleNext = false;
		for (int i = 0; i < bFile.length; i++) {
			if (bFile[i] == STX) {
				packetBufferPos = 0;
				// System.out.println("STX, now at beginning!");
			} else if (bFile[i] == ETX) {
				// System.out.println("ETX @ position: " + i);
				RocketDataPacket rdp = new RocketDataPacket(packetBuffer, packetBufferPos);
				packetList.add(rdp);
			} else if (bFile[i] == DLE) {
				// System.out.println("DLE @ position: " + i);
				dleNext = true;
			} else {
				int dat = Byte.toUnsignedInt(bFile[i]);
				if (dleNext) {
					dat = 255 - dat;
					dleNext = false;
				}
				packetBuffer[packetBufferPos] = (byte) dat;
				packetBufferPos++;
			}
		}
		System.out.println("bFile length: " + bFile.length);
		System.out.println("Processed file successfully!");
		
		packetList.trimToSize();
		System.gc();
		
		for(RocketDataPacket rdp : packetList)
			if(rdp != null)
				System.out.println(rdp);
		
	}
}
class RocketDataPacket {
	public final float time;  
	public final float gyroX, gyroY, gyroZ; 
	public final float accX, accY, accZ;
	public final long pressure;
	
	public RocketDataPacket(byte[] packet, int len) {
		if(len != 20)
			throw new ArrayIndexOutOfBoundsException("Packet too " + (len < 20 ? "short" : "big") + ", must be exactly 20B (it is " + len + "B)!");
		time = processUnsigned4B(packet, 0) * 0.001f;
		pressure = processUnsigned4B(packet, 16);
		gyroX = processSigned2B(packet, 4) * 0.1f;
		gyroY = processSigned2B(packet, 6) * 0.1f;
		gyroZ = processSigned2B(packet, 8) * 0.1f;
		accX = processSigned2B(packet, 10) * 0.01f;
		accY = processSigned2B(packet, 12) * 0.01f;
		accZ = processSigned2B(packet, 14) * 0.01f;
	}
	
	@Override public String toString() {
		return String.format("%.1f seconds: Gyro - [%.1f, %.1f, %.1f]°/s   Accel - [%.2f, %.2f, %.2f]m/s²   Pressure - %dPa", time, gyroX, gyroY, gyroZ, accX, accY, accZ, pressure);
	}
	
	public float getAccelerationMagnitude() {
		return (float) Math.sqrt(accX * accX + accY * accY + accZ * accZ);
	}
	public float[] getAccelerationVector() {
		return new float[] { accX, accY, accZ };
	}
	public float getGyrationMagnitude() {
		return (float) Math.sqrt(gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ);
	}
	public float[] getGyrationVector() {
		return new float[] { gyroX, gyroY, gyroZ };
	}
	public static long processUnsigned4B(byte[] pkt, int pos) {
    	long out = Byte.toUnsignedInt(pkt[pos++]);
    	out |= Byte.toUnsignedInt(pkt[pos++]) << 8;
    	out |= Byte.toUnsignedInt(pkt[pos++]) << 16;
    	out |= Byte.toUnsignedInt(pkt[pos++]) << 24;
    	return out;
    }
    public static short processSigned2B(byte[] pkt, int pos) {
    	short out = (short) Byte.toUnsignedInt(pkt[pos++]);
    	out |= Byte.toUnsignedInt(pkt[pos++]) << 8;
    	return out;
    }
}
