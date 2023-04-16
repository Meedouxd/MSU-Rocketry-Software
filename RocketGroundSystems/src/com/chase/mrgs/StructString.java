package com.chase.mrgs;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

//Struct library to java https://docs.python.org/3/library/struct.html
public class StructString {
    public static String unpack(char[] format, byte[] packed_data){
        String toReturn = "";
        int[] pos = new int[1];
        for(int i = 0; i < format.length; i++){
            toReturn += rawToString(format[i], packed_data, pos) + ",";
        }
        return toReturn.substring(0, toReturn.length()-1);
    }
    public static byte[] pack(char[] format, String unpacked_data){
        byte[] packed_data = new byte[getByteSize(format)];
        String[] unpacked_data_array = unpacked_data.split(":");
        //array so we can pass it by reference
        int[] pos = new int[1];
        for(int i = 0; i < format.length; i++){
            switch(format[i]){
                case('h'):
                    short unsignedSD = Short.parseShort(unpacked_data_array[i]);
                    insertPackedData(unsignedSD,true, false, packed_data, pos);
                    break;
                case('H'):
                    short signed = Short.parseShort(unpacked_data_array[i]);

                    //insertPackedData(signedSD, true,true, packed_data, pos);
                    break;
                case('i'):
                    break;
                case('I'):
                    break;
            }
        }
        return packed_data;
    }
    private static String rawToString(char formatChar, byte[] packed_data, int[] pos){
        String toReturn = "";
        switch(formatChar){
            //signed short
            case('h'):
                short out = (short) Byte.toUnsignedInt(packed_data[pos[0]++]);
                out |= Byte.toUnsignedInt(packed_data[pos[0]++]) << 8;
                return Short.toString(out);
            //unsigned short
            case('H'):
                byte[] ushortbytes = {packed_data[pos[0]++], packed_data[pos[0]++]};
                ByteBuffer wrapped = ByteBuffer.wrap(ushortbytes); // big-endian by default
                short shortVal = wrapped.getShort(); // 1
                int intVal = shortVal >= 0 ? shortVal : 0x10000 + shortVal;
                return Integer.toString(intVal);
            //signed int
            case('i'):
                byte[] sintbytes = {packed_data[pos[0]++], packed_data[pos[0]++], packed_data[pos[0]++], packed_data[pos[0]++]};
                int signedIntReturn = ByteBuffer.wrap(sintbytes).getInt();
                return Integer.toString(signedIntReturn);
            //unsigned int
            case('I'):
                long long_out = Byte.toUnsignedInt(packed_data[pos[0]++]);
                long_out |= Byte.toUnsignedInt(packed_data[pos[0]++]) << 8;
                long_out |= Byte.toUnsignedInt(packed_data[pos[0]++]) << 16;
                long_out |= Byte.toUnsignedInt(packed_data[pos[0]++]) << 24;
                return Long.toString(long_out);
        }
        return toReturn;
    }
    //make sure pos is int array of size 1
    private static void insertPackedData(short data, byte[] array, int[] pos){
    }
    private static void insertPackedData(int data, boolean isAShort, boolean signed, byte[] array, int[] pos){
    }
    private static void insertPackedData(long data, boolean isAnInt, boolean signed, byte[] array, int[] pos){
    }
    private static void insertPackedData(double data, byte[] array, int[] pos){
    }
    private static void insertPackedData(float data, byte[] array, int[] pos){
    }
    private static int getByteSize(char[] format){
        int size = 0;
        Map<Character, Integer> table = new HashMap<>();
        table.put('x',0);
        table.put('c',1);
        table.put('b',1);
        table.put('B',1);
        table.put('?',1);
        table.put('h',2);
        table.put('H',2);
        table.put('i',4);
        table.put('I',4);
        table.put('l',4);
        table.put('L',4);
        table.put('q',8);
        table.put('Q',8);
        table.put('f',4);
        table.put('d',8);
        table.put('s',1);
        table.put('p',1);
        table.put('P',1);
        for(char pos : format){
            if(!table.containsKey(pos)){
                System.out.println("Error: Invalid format character " + pos);
                return -1;
            }
            size += table.get(pos);
        }
        return size;
    }
    private static int binaryToInt(String binary){
        int conversion = 0;
        for(int i = 0; i < binary.length(); i++){
            if(binary.charAt(i) == '1'){
                conversion += Math.pow(2, binary.length() - (i+1));
            }
        }
        return conversion;
    }
}

