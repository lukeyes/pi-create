# pi-create
Test project controlling an IRobot create from a Raspberry Pi

## Requirements
Requires 4 peices of hardware.  
1 - IRobot Create (version 1 based on the 400 series, later versiona are based on the 500 series and use a different protocol)  
2 - IRobot Create communication cable  
3 - XBox360 controller for PC (either wired or wireless with dongle)  
4 - Computer (tested on both laptop and Raspberry Pi)  

## Building

Uses maven to build, call `mvn clean install` to build and package  

This will generate a shaded jar in the `targets` folder, and a bunch of native dlls for jinput in `targets\natives`  

## Running

### Windows (PC)

Easiest way is to go to the `targets` directory where the shaded jar lives and run the following command  
`java -Djava.library.path=natives -jar pi-create-1.0-SNAPSHOT-shaded.jar`.  
That's all you'll need to do for Windows as it is set to load the correct dlls and doesn't need special permissions  

### Linux (PC)

Follow the same instructions above, but before running the first time, you might need to give the user the permission to access the serial port.  

To enable user access, you must open a terminal and enter the following commands before pi-create will be able to access the ports on your system. Don't worry if some of the commands fail. All of these groups may not exist on every Linux distro. (Note, this process must only be done once for each user):  

`sudo usermod -a -G uucp username`  
`sudo usermod -a -G dialout username`  
`sudo usermod -a -G lock username`  
`sudo usermod -a -G tty username`  
Replace the username parameter with your current username.

### Linux (Raspberry Pi)

Follow the instructions for Linux (PC) above to enable access of the serial ports.  
However, since the jinput code is only set up to build drivers for Linux on PCs, you will need to set up drivers for using jinput on ARM.  

First, install jinput drivers by running the following command in the terminal.  

`sudo apt-get install libjinput-jni`  

This will put a driver called "libjinput.so" under the `/usr/lib/jni` folder.  
However, the code expects the driver to have an `64` appended to the name if the code is not running on an `i386` architecture (like ARM).  So you will need to do a hack to get the driver to load correct.  

`sudo cp /usr/lib/jni/libjinput.so /usr/lib/jni/libjinput/libjinput-linux64.so`  

Now you can run the application by going to the `targets` directory in the terminal and running the following command (notice the different option on the command line).  

`java -Dnet.java.games.input.librarypath=/usr/lib/jni -jar pi-create-1.0-SNAPSHOT-shaded.jar`  

I recommend that if you are running this headless on a Raspberry pi, that you add the command to start the program to `rc.local` with fully qualified paths, like so.  

`java -Dnet.java.games.input.librarypath=/usr/lib/jni -jar /home/pi/pi-create/target/pi-create-1.0-SNAPSHOT-shaded.jar &`  

This will make the program start automatically without needing to ssh to start it.  

### What it does

The program starts by scanning all the serial ports to find the Create.  It finds the Create by connecting to each port, sending a signal out to start and then listening for sensor updates.  It will continuously loop until it finds the Create.

Once the Create is found, you should hear the Create generate a high beep followed by a low beep.

Then the program tries to find the XBox 360 controller. If you have more than one connected, it will use the first one it finds.  I recommend only connecting one, especially when running headless.

Once it finds the XBox 360 controller, you can now drive around the Create by using up/down on the left stick for forward/back, and left/right on the right stick to spin left/right accordingly.


