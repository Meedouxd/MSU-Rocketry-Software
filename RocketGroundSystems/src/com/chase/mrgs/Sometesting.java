
//Just testing the StructString Library among other things
/*
package com.chase.mrgs;

public class Sometesting {
    public static final byte STX = 0x02;
    public static final byte ETX = 0x03;
    public static final byte DLE = 0x10;
    public static void main(String args[]){

        char[] bigStruct = {'I', 'h','h','h','h','h','h','I','H','H'};
        byte[] hi = {126, 73, 0, 0, 23, 0, 12, 0, 0, 0, 6, 0, 16, 4, 0, -101, -1, 101, 116, 1, 0, 0, 0, 0, 0,};
        System.out.println(hi.length);
        RocketDataPacketNewGPS poop = new RocketDataPacketNewGPS(StructString.unpack(bigStruct, removeDLEs(hi)));
        System.out.println(poop.toString());
    }
    public static byte[] removeDLEs(byte[] framedData){
        byte[] toReturn = new byte[24];
        int toReturnpos = 0;
        boolean DLENext = false;
        for(int i = 0; i < framedData.length; i++){
            if(framedData[i] == DLE){
                DLENext = true;
            }else{
                if(DLENext){
                    toReturn[toReturnpos++] = (byte) (framedData[i]-2);
                    DLENext = false;
                }else{
                    toReturn[toReturnpos++] = framedData[i];
                }

            }
        }
        // System.out.println("packet before DLE's removed: " + Arrays.toString(framedData));
        //System.out.println(":D packet after dles are removed: " + Arrays.toString(toReturn));
        return toReturn;

    }
}
*/