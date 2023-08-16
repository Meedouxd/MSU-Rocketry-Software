/*
NOTES:::
Struct String is working properly I tested the output of pico
set every baud rate to 2400!!!!



package com.chase.mrgs;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class SerialByteTesting {
    public static final byte STX = 0x02;
    public static final byte ETX = 0x03;
    public static final byte DLE = 0x10;

    public static final char[] bigStruct = {'I', 'h','h','h','h','h','h','I','H','H'};

    public static int dataBufferPos = 0;
    public static int numOfPacketsRecieved = 0;
    public static boolean shouldListen = false;
    public static int packetShellPos = 0;

    public static void main(String args[]) {
        byte[] dataBuffer = new byte[200];
        byte[] packetShell = new byte[24];
        Scanner term_reader = new Scanner(System.in);
        System.out.println("Select which port you want.");
        SerialPort ports[] = SerialPort.getCommPorts();
        int i = 0;
        int choice;
        for (SerialPort port : ports) {
            System.out.println(i++ + ". " + port.toString());
        }
        choice = term_reader.nextInt();
        SerialPort chosenPort = ports[choice];
        chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
        chosenPort.setBaudRate(2400);
        if (chosenPort.openPort()) {
            System.out.println("Opened Port");
        }
        chosenPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }
            @Override
            public void serialEvent(SerialPortEvent event)
            {
                if (event.getEventType() == SerialPort.LISTENING_EVENT_DATA_RECEIVED){
                    //if we have enough packets in our packet buffer we will attempt to remove data framing bytes and decode them
                    if(numOfPacketsRecieved >=5){
                        System.out.println("We got enough pacekts");
                        numOfPacketsRecieved = 0;
                        dataBufferPos = 0;
                        for(int i = 0; i < dataBuffer.length; i++){
                            if(dataBuffer[i] == STX){
                                shouldListen = true;
                            }else if(shouldListen){
                                    System.out.println("We are listening");
                                    packetShell[packetShellPos++] = dataBuffer[i];
                            }
                            //send off our packets here!
                            if(dataBuffer[i] == ETX && shouldListen){
                                System.out.println("We are listening and found an ETX!");
                                System.out.println("Decoded Data: " + StructString.unpack(bigStruct, packetShell) + "Packet Shell Pos: " + packetShellPos);
                                shouldListen = false;
                                packetShellPos = 0;
                            }
                        }
                    }
                    //System.out.println("we outta dat");
                    byte[] packet = event.getReceivedData();
                    System.out.println(Arrays.toString(packet) + " size: " + packet.length);
                    if(packet.length == 24){
                        System.out.println(StructString.unpack(bigStruct, packet));
                    }

                   // System.arraycopy(packet, 0, dataBuffer, dataBufferPos, packet.length);
                    //dataBufferPos += packet.length;
                    //numOfPacketsRecieved++;
                    //System.out.println("Data has been received, size of: " + event.getReceivedData().length);
                }
            }
        });
    }
}
*/