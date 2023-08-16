package com.chase.mrgs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Arrays;
import com.fazecast.jSerialComm.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class GUI extends JFrame implements ActionListener{
    public static final byte STX = 0x02;
    public static final byte ETX = 0x03;
    public static final byte DLE = 0x10;
    public static final char[] littleStruct = {'I', 'h','h','h','h','h','h','I'};
    public static final char[] bigStruct = {'I', 'h','h','h','h','h','h','I','H','H'};

    float altitude = 0, gyroX = 0, gyroY = 0, gyroZ = 0, accX = 0, accY = 0, accZ = 0, pressure = 0, time = 0, GPSNorth = 0, GPSWest = 0;
    String status = "Unconnected";
    //for incoming data
    byte dataBuffer[] = new byte[125];

    int dataBufferPos = 0;
    byte[] DLEPacket = new byte[24];
    int DLEPacketPos = 0;


    int count = 0;

    //how many chunks of data from serial we have recieved
    int dataFromSerialRecieved = 0;
    //All of our objects to be put to the window
    JFrame frame = new JFrame();

    JLabel statusLabel = new JLabel("Status: " + status);
    JLabel altitudeLabel = new JLabel("Altitude: " + altitude + " ft");
    JLabel gyroLabel = new JLabel(String.format("Gryo XYZ: [%.2f, %.2f, %.2f]", gyroX, gyroY, gyroZ));
    JLabel accLabel = new JLabel(String.format("Accel XYZ: [%.2f, %.2f, %.2f]", accX, accY, accZ));
    JLabel pressureLabel = new JLabel("Pressure: " + pressure);
    JLabel GPSLabel = new JLabel("GPS NW: " + GPSNorth + "," + GPSWest);

    JButton connectButton = new JButton("Connect");

    SerialPort ports[] = SerialPort.getCommPorts();

    JComboBox<SerialPort> comboBox = new JComboBox<SerialPort>(ports);

    XYSeries series = new XYSeries("Rocket Altitude");
    XYSeriesCollection dataset = new XYSeriesCollection(series);

    JFreeChart chart = ChartFactory.createXYLineChart("Rocket Altitude", "Time (seconds)", "Altitude (feet)", dataset);

    ChartPanel chartPanel = new ChartPanel(chart);
    JPanel livePanel = new JPanel();

    //makes it pretty
    public GUI() throws IOException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Setting stuff up for the main frame
        frame.setBounds(100, 100, 1000, 600);
        frame.setVisible(true);
        frame.setTitle("MSU Rocketry Grounds System" + " " + Main.VERSION);
        frame.setResizable(true);
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter(){
            //so we dont lose our data after flight
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Window Closed, Program shutting down.");

                 try {
                    Main.closeFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);

                }

            }
        });
        //Add all the labels buttons etc to the frame
        frame.add(GPSLabel);
        frame.add(statusLabel);
        frame.add(altitudeLabel);
        frame.add(gyroLabel);
        frame.add(accLabel);
        frame.add(pressureLabel);
        frame.add(comboBox);
        frame.add(chartPanel);
        frame.add(livePanel);

        livePanel.add(comboBox,BorderLayout.NORTH);
        livePanel.add(connectButton,BorderLayout.NORTH);

        //Setting bounds and such for all the labels and buttons.
        statusLabel.setBounds(700,25,200,25);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        altitudeLabel.setBounds(700,50,800,25);
        altitudeLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        GPSLabel.setBounds(700,150,800,25);
        GPSLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        gyroLabel.setBounds(700,75,400,25);
        gyroLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        accLabel.setBounds(700,100,400,25);
        accLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        pressureLabel.setBounds(700,125,200,25);
        pressureLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        connectButton.setBounds(500,10,100,25);
        comboBox.setBounds(250,10,200,25);
        comboBox.setVisible(true);
        chartPanel.setBounds(0,50,700,500);

        //Action Listeners
        connectButton.addActionListener(this);

    }
    //Event Listener for the Connect Button
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == connectButton){
            updateLabels();
            //Setting up the Serial Port and Attempting to connect to it
            SerialPort rightPort = (SerialPort) comboBox.getSelectedItem();
            rightPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0,0);
            //SETTING BAUD RATE TO 2400 IS EXTREMELY IMPORTANT!!!! NEEDS TO BE 2400 TO GET DATA CORRECTLY
            rightPort.setBaudRate(2400);
            //if there is a successful connection
            if(rightPort.openPort()){
                System.out.println("Port Opened Successfully.");
                status = "Connected";
                //thread for getting the data and graphing it.
                Thread serialThread = new Thread(){
                    @Override
                    public void run(){
                        rightPort.addDataListener(new SerialPortDataListener() {
                            @Override
                            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }
                            @Override
                            public void serialEvent(SerialPortEvent event)
                            {
                                if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_RECEIVED){
                                    return;
                                }
                                System.out.println("[DEBUG] Data has been received");
                                byte[] packet = event.getReceivedData();
                                //add our data recieved from serial to a buffer
                                System.arraycopy(packet, 0, dataBuffer, dataBufferPos, packet.length);
                                dataBufferPos += packet.length;
                                dataFromSerialRecieved++;
                                if(dataFromSerialRecieved >= 3){
                                    dataFromSerialRecieved = 0;
                                    sniffPackets(dataBuffer, dataBufferPos);
                                    dataBufferPos = 0;
                                    Arrays.fill(dataBuffer, (byte)0);
                                }

                            }
                        });
                    }
                };serialThread.start();
            }else{
                System.out.println("ERROR: Could not connect to port");
            }

        }
        updateLabels();
    }
    public void updateLabels(){
         statusLabel.setText("Status: " + status);
         altitudeLabel.setText("Altitude: " + altitude + " ft");
         gyroLabel.setText(String.format("Gryo XYZ: [%.2f, %.2f, %.2f]", gyroX, gyroY, gyroZ));
         accLabel.setText(String.format("Accel XYZ: [%.2f, %.2f, %.2f]", accX, accY, accZ));
         pressureLabel.setText("Pressure: " + pressure);
         GPSLabel.setText("GPS NW: " + GPSNorth + "," + GPSWest);
    }
        //unpack data and update GUI graph and labels, also
        public void updateDataWithGPS(byte[] packet){
            if(packet.length != 24){
                System.out.println("Bad packet size: " + packet.length);
            }
            String unpacked_data = StructString.unpack(bigStruct, packet);
            RocketDataPacketNewGPS rpdngps = new RocketDataPacketNewGPS(unpacked_data);
            time = rpdngps.time;
            System.out.println(time);
            gyroX = rpdngps.gyroX;
            gyroY = rpdngps.gyroY;
            gyroZ = rpdngps.gyroZ;
            accX = rpdngps.accX;
            accY = rpdngps.accY;
            accZ = rpdngps.accZ;
            pressure = rpdngps.pressure;
            altitude = rpdngps.getAltitude();
            GPSNorth = rpdngps.GPSNorth;
            GPSWest = rpdngps.GPSWest;
            //if the data is not ridiculous, updata graph gui and labels and save it to a .txt file
            if(altitude > 0 && altitude < 8000){
                series.add(time, altitude);
                updateLabels();
                try {
                     Main.writeToFile(rpdngps.toCVSString() + "\n");
                     } catch (IOException ex) {
                      throw new RuntimeException(ex);
                    }
            }
            //
        }

        //given an array of bytes, remove the DLE and the data will be completely deframed and ready to be unpacked
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
            return toReturn;

        }
        //given a buffer of bytes from the serial, we find the complete packets and return them
        public void sniffPackets(byte[] haystack, int pos){
            boolean shouldListen = false;
            for(int i = 0; i < pos; i++){
                if(haystack[i] == STX){
                    shouldListen = true;
                    System.out.println("Found STX");
                }
                //then we know to send it off!
                else if(haystack[i] == ETX){
                    //System.out.println(StructString.unpack(bigStruct, removeDLEs(DLEPacket)));
                    updateDataWithGPS(removeDLEs(DLEPacket));
                    DLEPacketPos = 0;
                    Arrays.fill(DLEPacket, (byte) 0);
                }else{
                    if(shouldListen){
                        if(DLEPacketPos <24){
                            DLEPacket[DLEPacketPos++] = haystack[i];
                        }
                    }
                }
            }
        }
}
