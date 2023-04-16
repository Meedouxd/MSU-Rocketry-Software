//Revised version of Jeremiah's RocketDataPacket object, but this one includes GPS (only one implemented at this time)
package com.chase.mrgs;

public class RocketDataPacketNewGPS {
    public final float time;
    public final float gyroX, gyroY, gyroZ;
    public final float accX, accY, accZ;
    public final float pressure;
    public final float GPSNorth, GPSWest;
    public RocketDataPacketNewGPS(String unpacked_data) {
        String[] unpacked_data_array = unpacked_data.split(",");
        int len = unpacked_data_array.length;
        if(len != 8 && len != 10){
            throw new ArrayIndexOutOfBoundsException("Error: packet length must be 8 or 10, it was " + len);
        }
        time = Float.parseFloat(unpacked_data_array[0]) * 0.001f;
        gyroX = Float.parseFloat(unpacked_data_array[1]) * 0.1f;
        gyroY = Float.parseFloat(unpacked_data_array[2]) * 0.1f;
        gyroZ = Float.parseFloat(unpacked_data_array[3]) * 0.1f;
        accX = Float.parseFloat(unpacked_data_array[4]) * 0.01f;
        accY = Float.parseFloat(unpacked_data_array[5]) * 0.01f;
        accZ = Float.parseFloat(unpacked_data_array[6]) * 0.01f;
        pressure = Float.parseFloat(unpacked_data_array[7]) * 0.01f;
        GPSNorth = (Float.parseFloat(unpacked_data_array[8]) * 0.0001f) + 38;
        GPSWest = (Float.parseFloat(unpacked_data_array[9]) * 0.0001f) + 44;
    }
    @Override public String toString() {
        return String.format("%.1f seconds: Gyro - [%.1f, %.1f, %.1f]°/s   Accel - [%.2f, %.2f, %.2f]m/s²   Pressure - %fPa", time, gyroX, gyroY, gyroZ, accX, accY, accZ, pressure);
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
    public float getAltitude(){
        //H = 44330 * [1 - (P/p0)^(1/5.255) ]
        //
        //
        //H = altitude (m)
        //P = measured pressure (Pa) from the sensor
        //p0 = reference pressure at sea level (e.g. 1013.25hPa)

        return (44300 * (1 - (float) (Math.pow(pressure/1013.25, (1/5.255))))) * 3.28084f;

    }
    public String toCVSString(){
        return time + "," + gyroX + "," + gyroY + "," + gyroZ + "," + accX + "," + accY + "," + accZ + "," + pressure + "," + getAltitude() + "," + GPSNorth +","+ GPSWest;
    }
}
