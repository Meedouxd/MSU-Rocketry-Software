package com.chase.mrgs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Scanner;
import com.fazecast.jSerialComm.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class GUI extends JFrame implements ActionListener{
    float altitude = 0, gyroX = 0, gyroY = 0, gyroZ = 0, accX = 0, accY = 0, accZ = 0, pressure = 0;
    int time = 0;
    String status = "Unconnected";
    //All of our objects to be put to the window
    JFrame frame = new JFrame();
    JLabel statusLabel = new JLabel("Status: " + status);
    JLabel altitudeLabel = new JLabel("Current Altitude: " + altitude);
    JLabel gyroLabel = new JLabel(String.format("Gryo XYZ: [%.2f, %.2f, %.2f]", gyroX, gyroY, gyroZ));
    JLabel accLabel = new JLabel(String.format("Accel XYZ: [%.2f, %.2f, %.2f]", accX, accY, accZ));
    JLabel pressureLabel = new JLabel("Pressure: " + pressure);
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
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Window Closed, Program shutting down.");
                ///*
                 try {
                    Main.closeFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);

                }//*/

            }
        });
        //Add all the labels buttons etc to the frame
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
            //if there is a successful connection
            if(rightPort.openPort()){
                System.out.println("Port Opened Successfully.");
                status = "Connected";
                //thread for getting the data and graphing it.
                Thread serialThread = new Thread(){
                    @Override
                    public void run(){
                        Scanner data = new Scanner(rightPort.getInputStream());
                        while(data.hasNextLine()){
                            System.out.println(data.nextLine());
                            pressure = Float.parseFloat(data.nextLine());
                            series.add(time++, pressure);
                            try {
                                Main.writeToFile("Pressure: " + pressure + "\n");
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                            updateLabels();
                        }
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
         altitudeLabel.setText("Current Altitude: " + altitude);
         gyroLabel.setText(String.format("Gryo XYZ: [%.2f, %.2f, %.2f]", gyroX, gyroY, gyroZ));
         accLabel.setText(String.format("Accel XYZ: [%.2f, %.2f, %.2f]", accX, accY, accZ));
         pressureLabel.setText("Pressure: " + pressure);
    }
}
