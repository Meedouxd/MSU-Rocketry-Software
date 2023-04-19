#baud rate is 2400, pressure needs divided by 25600, time by 1000
#time, gyroX, gyroY, gyroZ, accelX, accelY, accelZ, press
import struct
import utime
from imu import MPU6050
from machine import Pin, I2C
from bme280_int import *
from machine import Pin, UART
import _thread

led = Pin(25, Pin.OUT)
buzzer = Pin(10, Pin.OUT)
radio_uart = UART(0, baudrate=2400)
gps_uart = UART(1, baudrate=9600)#, tx=Pin(11), rx=Pin(12))

LITTLE_STRUCT = "IhhhhhhI" #No GPS
BIG_STRUCT = "IhhhhhhIHH" #GPS
STX = b'\x02' #2
ETX = b'\x03' #3
DLE = b'\x10' #16

#MPU Init, MUST BE INITIALIZED BEFORE BME!!!!!
i2cMPU = I2C(0, sda=Pin(4), scl=Pin(5), freq=400000)
imu = MPU6050(i2cMPU)

#BME Init
i2c=I2C(0)
bme280 = BME280(i2c=i2c)

north = 0
west = 0

packets_sent = 0

calibrated = False
lift_off = False
print("Setup Complete")
while calibrated == False:
    #print("awaiting calibration")
    if radio_uart.any(): 
        data = radio_uart.read() 
        if data== b'calibrate cheesecake':
            print("Calibration Complete")
            calibrated = True
            led.value(1)
#Initial time on startup for logging time
start_time = utime.ticks_ms()
current_time = (utime.ticks_diff(utime.ticks_ms(), start_time))
gx=int(round(imu.gyro.x*10))
gy=int(round(imu.gyro.y*10))
gz=int(round(imu.gyro.z*10))
ax=int(round(imu.accel.x,2) * 100)
ay=int(round(imu.accel.y,2) * 100)
az=int(round(imu.accel.z,2) * 100)
pressure = int(round(bme280.get_pressure() /256))
#base data
radio_uart.write(struct.pack(BIG_STRUCT,current_time, gx,gy,gz,ax,ay,az,pressure,north,west))
while lift_off == False:
    #print("Awaiting Lift off Command")
    if radio_uart.any(): 
        data = radio_uart.read() 
        if data== b'initiate cheesecake eclipse':
            print("We have lift off!")
            lift_off = True
            lift_off_time = utime.ticks_ms()
while True:
    if packets_sent >= 10:
        if gps_uart.any():
            data = gps_uart.readline()
            if data[:6] == b"$GPGLL":
                data = data.split(b",")
                north = int(float(data[1][2:]) * 100000)
                west = int(float(data[3][3:]) * 100000)
        packets_sent = 0
    led.toggle()
    current_time = (utime.ticks_diff(utime.ticks_ms(), start_time))
    if(current_time - lift_off_time > 100000):
        buzzer.value(1)
    gx=int(round(imu.gyro.x*10))
    gy=int(round(imu.gyro.y*10))
    gz=int(round(imu.gyro.z*10))
    ax=int(round(imu.accel.x,2) * 100)
    ay=int(round(imu.accel.y,2) * 100)
    az=int(round(imu.accel.z,2) * 100)
    pressure = int(round(bme280.get_pressure() /256))
    #if gps_changed == True:
    #    packed_data= struct.pack(BIG_STRUCT, current_time, gx,gy,gz,ax,ay,az,pressure,north,west)
    #    gps_changed = False
   # else:
     #   packed_data = struct.pack(LITTLE_STRUCT, current_time, gx,gy,gz,ax,ay,az,pressure)
   # packed_data = struct.pack(BIG_STRUCT, current_time, gx,gy,gz,ax,ay,az,pressure,north,west)
    packed_data = struct.pack(BIG_STRUCT,current_time, gx,gy,gz,ax,ay,az,pressure,north,west)
    framed_data_pos = 1
    dle_count = 26
    dle_next = False
    #determine how large our framed data byte array should be
    for i in range(24):
        if packed_data[i] == 2 or packed_data[i] == 3 or packed_data[i] == 16:
            dle_count+=1
    #put our packed data into a framed data byte array
    framed_data = bytearray(dle_count)
    for i in range(24):
        if packed_data[i] == 2 or packed_data[i] == 3 or packed_data[i] == 16:
            framed_data[framed_data_pos] = 16
            framed_data_pos +=1
            framed_data[framed_data_pos] = 2 + packed_data[i]
            framed_data_pos +=1
        else:
            framed_data[framed_data_pos] = packed_data[i]
            framed_data_pos+=1
    framed_data[0] = 2
    framed_data[len(framed_data)-1] = 3
    radio_uart.write(framed_data)
    packets_sent+=1