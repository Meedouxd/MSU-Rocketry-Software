package com.chase.mrgs;

public class RocketDataPacket {
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
    //DO THIS
    public double getAltitude(){
        return pressure;
    }
}
