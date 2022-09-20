#include "MPU.h"
#include "BME280.h"
#include "Wire.h"
#include <SPI.h>
#include <SD.h>

#define STX 0x02
#define ETX 0x03
#define DLE 0x10

MPU6050 thingy; //Declare the MPU6050 device
BME280 bme; // Declare the BME280 device
File myFile;

void setup(){
  thingy.begin(); //Initialize it, you can pass in a speed in hertz
  bme.begin();
  SD.begin(10);
  myFile = SD.open("drop1.txt", FILE_WRITE);

  pinMode(PC13, OUTPUT);
}

void loop(){
  float tbuf1[3];   // Temporary buffer for conversions
  int16_t tbuf2[3]; // Temporary buffer for conversions
  uint8_t mbuf[32]; // Main buffer, 4B for time, 6B for gyrometer, 6B for accelerometer, 4B for pressure total 20B
  uint8_t len = 0;  // Keep track of how much data we've written

  // Write the time to the buffer
  uint32_t t = millis();
  memcpy(mbuf + len, &t, 4); len += 4;

  thingy.readGyroDPS(tbuf1);
  // Were really not going to achieve over 0.1 degree accuracy so we will store tenths of a degree
  // This saves 6B of data from being written since we can store as an integer
  tbuf2[0] = round(tbuf1[0] * 10.0f);
  tbuf2[1] = round(tbuf1[1] * 10.0f);
  tbuf2[2] = round(tbuf1[2] * 10.0f);
  memcpy(mbuf + len, tbuf2, 6); len += 6;


  thingy.readAccelMS(tbuf1);
  // Same goes for acceleration, we will store as centimeters per second^2 allowing decimal precision
  // The sensor has a +/-16G limit, well within the +/-32G cap this introduces
  // This saves 6B of data from being written since we can store as an integer
  int16_t ax, ay, az;
  tbuf2[0] = round(tbuf1[0] * 100.0f);
  tbuf2[1] = round(tbuf1[1] * 100.0f);
  tbuf2[2] = round(tbuf1[2] * 100.0f);
  memcpy(mbuf + len, tbuf2, 6); len += 6;

  // The temperature sensor is very inaccurate so we can just round it off and store it directly
  //mbuf[len++] = (int8_t) round(bme.readPressurePSI());
  float pres = bme.readPressurePa();
  uint32_t presround = round(pres); // Pascals are a tiny unit so this doesn't need multiplied, just rounded
  memcpy(mbuf + len, &presround, 4); len += 4;

  //Now to write to the SD card
  // To know where the data starts we use an STX (Start TX) header, typically 0x02 is used
  // To know where it ends we use an ETX (End TX) header,  typically 0x03 is used
  // In case of a STX or ETX appearing in our raw data we use a DLE (Delimeter), typically 0x10 is used
  myFile.write(STX);
  for (uint8_t i = 0; i < len; i++) {
    uint8_t dat = mbuf[i];
    if (dat == STX || dat == ETX || dat == DLE) {
      myFile.write(DLE);
      myFile.write(255 - dat);
    } else myFile.write(dat);
  }
  myFile.write(ETX);
  // Wait for the SD card to catchup
  digitalWrite(PC13, HIGH);
  myFile.flush();
  digitalWrite(PC13, LOW);
  delay(1);
}
