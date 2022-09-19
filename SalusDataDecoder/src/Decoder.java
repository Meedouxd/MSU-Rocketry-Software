/*Author: Chase Morgan
This is Jeremiah's salus data decoder ported to java from python.
Its not totally done but almost, DLE stuff needs to be handled.
Possilbe TODO: Turn this into a library to be used in later projects.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;

//Takes in raw binary data from a .txt file and converts it back into readable data
public class Decoder {
    private static final DecimalFormat df = new DecimalFormat("0.000");
    public static void main(String arg[]) throws IOException, WrongByteSizeException {
        //STX means "Start of Text" ETX means "End of Text" and DLE means "Data Link Escape"
        byte STX = 0x02;
        byte ETX = 0x03;
        byte DLE = 0x10;
        int dlecount = 0;
        int etxcount = 0;
        int count = 0;

        //Data Buffer
        ArrayList<Byte>Chunk = new ArrayList<>();
        //File that we read from
        File file = new File("SALUS1.txt");
        //Byte Array where file's contents are stored
        byte[] bFile = new byte[(int)file.length()];

        //putting the contents of the file into a byte array
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.read(bFile);
        fileInputStream.close();

        //looping through each byte
        for (int i = 0; i < bFile.length; i++) {
            if(bFile[i] == DLE){
                //something needs to happen right here
                dlecount++;
            }
            if(bFile[i] == ETX){
                etxcount++;
            }
            //Searching for "start of text";
            if(bFile[i] == STX){
                for(int a = i+1; a < bFile.length - (i+1); a++){
                    if(bFile[a] != ETX){
                        //actual data
                        Chunk.add(bFile[a]);
                    }else{
                        //process the "Chunk" array list
                        //send that data somewhere
                        //clear chunk array list
                        System.out.println("Chunk Data: " + Chunk.toString());
                        //TODO: CHECK THIS
                        if(Chunk.contains(DLE)){
                            Chunk.remove(DLE);
                        }
                        System.out.println("Unpacked Data: " + unpack((arrayListToByteArray(Chunk))));
                        Chunk.clear();
                        break;
                    }
                }
                count++;
            }
        }
        System.out.println("STX Count: " + count + "\nETX Count: " + etxcount + "\n" + "DLE count: " + dlecount);
        System.out.println("bFile length: " + bFile.length);
    }
    //Pretty messy but takes each "chunk" or "packet" of data and converts its raw binary content to integer or short variables.
    public static String unpack(byte[] packet) throws WrongByteSizeException {
        //Byte arrays that will each be individually processed
        byte[] time = new byte[4];
        byte[] gyroX = new byte[2];
        byte[] gyroY = new byte[2];
        byte[] gyroZ = new byte[2];
        byte[] accX = new byte[2];
        byte[] accY = new byte[2];
        byte[] accZ = new byte[2];
        byte[] pressure = new byte[4];
        //Assigning bytes from the chunk to each variable
        for(int i = 0; i < 4; i++){
            time[i] = packet[i];
        }
        gyroX[0] = packet[4];
        gyroX[1] = packet[5];

        gyroY[0] = packet[6];
        gyroY[1] = packet[7];

        gyroZ[0] = packet[8];
        gyroZ[1] = packet[9];

        accX[0] = packet[10];
        accX[1] = packet[11];

        accY[0] = packet[12];
        accY[1] = packet[13];

        accZ[0] = packet[14];
        accZ[1] = packet[15];

        pressure[0] = packet[16];
        pressure[1] = packet[17];
        pressure[2] = packet[18];
        pressure[3] = packet[19];
        //actual data decoding
        double timeNum = bytesToInt(time) * 0.001;
        double gyroXNum = bytesToShort(gyroX) * 0.1;
        double gyroYNum = bytesToShort(gyroY) * 0.1;
        double gyroZNum = bytesToShort(gyroZ) * 0.1;
        double accXNum = bytesToShort(accX) * 0.01;
        double accYNum = bytesToShort(accY) * 0.01;
        double accZNum = bytesToShort(accZ) * 0.01;
        int pressureNum = bytesToInt(pressure);
        //return "Time: " + df.format(timeNum) + " GX: " + df.format(gyroXNum) + " GY: " + df.format(gyroYNum) + " GZ: " + df.format(gyroZNum) + " AX: " + df.format(accXNum) + " AY: " + df.format(accYNum) + " AZ: " + df.format(accZNum) +" Pressure: " + df.format(pressureNum);
        return df.format(timeNum) + "," + df.format(gyroXNum) + "," + df.format(gyroYNum) + "," + df.format(gyroZNum) + "," + df.format(accXNum) + "," + df.format(accYNum) + "," + df.format(accZNum) +"," + df.format(pressureNum);

    }
    public static int bytesToInt(byte[] chunk) throws WrongByteSizeException {
        if(chunk.length != 4 && chunk.length != 2){
            throw new WrongByteSizeException("Wrong byte size entered. Needs to be 2 or 4");
        }
        return ByteBuffer.wrap(reverseByteArray(chunk)).getInt();
    }
    public static int bytesToShort(byte[] chunk) throws WrongByteSizeException {
        if(chunk.length != 4 && chunk.length != 2){
            throw new WrongByteSizeException("Wrong byte size entered. Needs to be 2 or 4");
        }
        return ByteBuffer.wrap(reverseByteArray(chunk)).getShort();
    }
    //Probably a simpler way but yeah
    public static byte[] arrayListToByteArray(ArrayList<Byte> byteArrayList){
        byte[] toReturn = new byte[byteArrayList.size()];
        for(int i = 0; i < toReturn.length; i++){
            toReturn[i] = byteArrayList.get(i);
        }
        return toReturn;
    }
    //STM Data is reversed for some reason? So we have to reverse the byte arrays to get the correct data
    public static byte[] reverseByteArray(byte[] bArray){
        byte[] toReturn = new byte[4];
        if(bArray.length == 2){
            toReturn[0] = 0;
            toReturn[1] = 0;
            toReturn[2] = bArray[1];
            toReturn[3] = bArray[0];
        }
        for(int i = 0; i < bArray.length; i++){
            toReturn[i] = bArray[bArray.length-1-i];
        }
        return toReturn;
    }
}
//Because we only want byte arrays of size 2 or 4
class WrongByteSizeException extends Exception{
    public WrongByteSizeException(String message)
    {
        super(message);
    }
}