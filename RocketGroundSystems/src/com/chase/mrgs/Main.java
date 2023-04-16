//Author: Chase Morgan 9/15/22
//First working version actually used in a rocket launch April 2023
package com.chase.mrgs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static String VERSION = "v1";
    public static FileWriter fw;
    public static void main(String args[]) throws IOException {
        //for logging our data
        File file = new File("FlightData_" + System.currentTimeMillis() + ".txt");
        fw = new FileWriter(file);
        //file header
        writeToFile("time,gyroxyz,accxyz,pressure,altitude,gpsnw\n");
        GUI gui = new GUI();
    }
    public static void writeToFile(String data) throws IOException {
        fw.write(data);
    }
    public static void closeFile() throws IOException {
        fw.close();
    }
}
