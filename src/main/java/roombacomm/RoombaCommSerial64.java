package roombacomm;

import jssc.*;

import java.io.*;
import java.util.*;

public class RoombaCommSerial64 extends RoombaComm implements SerialPortEventListener {
    private int rate = 57600;
    private static final int databits = 8;
    private static final int parity = SerialPort.PARITY_NONE;
    private static final int stopbits = SerialPort.STOPBITS_1;
    private String protocol = "SCI";

    /**
     * contains a list of all the ports
     * keys are port names (e.g. "/dev/usbserial1")
     * values are Boolean in-use indicator
     */
    private static Map<String, Boolean> ports = null;

    /**
     * The RXTX port object, normally you don't need access to this
     */
    private SerialPort port = null;
    private String portname = null;   //"/dev/cu.KeySerial1" for instance

    /**
     * Some "virtual" serial ports like Bluetooth serial on Windows
     * return weird errors deep inside RXTX if an opened port is used
     * before the virtual COM port is ready.  One way to check that it
     * is ready is to look for the DSR line going high.
     * However, most simple, real serial ports do not do hardware handshaking
     * so never set DSR high.
     * Thus, if using Bluetooth serial on Windows, do:
     * roombacomm.waitForDSR = true;
     * before using it and see if it works.
     */
    private boolean waitForDSR = false;

    private byte[] buffer = new byte[32768];
    private int bufferLast;

    //int bufferSize = 26;  // how big before reset or event firing
    //boolean bufferUntil;
    //int bufferUntilByte;

    /**
     * Let you check to see if a port is in use by another Rooomba
     * before trying to use it.
     */
    public static boolean isPortInUse(String pname) {
        Boolean inuse = ports.get(pname);
        if (inuse != null) {
            return inuse;
        }
        return false;
    }

    // constructor
    public RoombaCommSerial64() {
        super();
        makePorts();
        readConfigFile();
    }

    public RoombaCommSerial64(boolean autoupdate) {
        super(autoupdate);
        makePorts();
        readConfigFile();
    }

    public RoombaCommSerial64(boolean autoupdate, int updateTime) {
        super(autoupdate, updateTime);
        makePorts();
        readConfigFile();
    }

    private void makePorts() {
        if (ports == null)
            ports = Collections.synchronizedMap(new TreeMap<>());
    }

    /**
     * Connect to a serial port specified by portid
     * doesn't guarantee connection to Roomba, just to serial port
     *
     * @param portid name of port, e.g. "/dev/cu.KeySerial1" or "COM3"
     * @return true if connect was successful, false otherwise
     */
    public boolean connect(String portid) {
        logmsg("connecting to port '" + portid + "'");
        portname = portid;
        writeConfigFile(portname, protocol, waitForDSR ? 'Y' : 'N');

        if (isPortInUse(portid)) {
            logmsg("port is in use");
            return false;
        }

        connected = open_port();

        if (connected) {
            // log in the global ports hash if the port is in use now or not
            ports.put(portname, true);
            sensorsValid = false;
        } else {
            disconnect();
        }

        return connected;
    }

    /**
     * Disconnect from serial port
     */
    public void disconnect() {
        connected = false;

        // log in the global ports hash if the port is in use now or not
        ports.put(portname, false);

        try {
            if (port != null) port.closePort();  // close the port
        } catch (Exception e) {
            e.printStackTrace();
        }
        port = null;
    }

    /**
     * subclassed.  FIXME:
     */
    public boolean send(byte[] bytes) {
        try {
            port.writeBytes(bytes);
            // output.write(bytes);
            // if( flushOutput ) output.flush();   // hmm, not sure if a good idea
        } catch (Exception e) { // null pointer or serial port dead
            e.printStackTrace();
        }
        return true;
    }

    /**
     * This will handle both ints, bytes and chars transparently.
     */
    public boolean send(int b) {  // will also cover char or byte
        try {
            logmsg(Integer.toString(b));
            logmsg(Byte.toString((byte) b));
            //output.write(b & 0xff);  // for good measure do the &
            port.writeByte((byte) b);
            // if( flushOutput ) output.flush();   // hmm, not sure if a good idea
        } catch (Exception e) { // null pointer or serial port dead
            //errorMessage("send", e);
            e.printStackTrace();
        }
        return true;
    }

    /**
     * toggles DD line via serial port DTR  (if available)
     */
    public void wakeup() {
        try {
            port.setDTR(false);
            pause(500);
            port.setDTR(true);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update sensors.  Block for up to 1000 ms waiting for update
     * To use non-blocking, call sensors() and then poll sensorsValid()
     */
    public boolean updateSensors() {
        sensorsValid = false;
        sensors();
        for (int i = 0; i < 20; i++) {
            if (sensorsValid) {
                logmsg("updateSensors: sensorsValid!");
                break;
            }
            logmsg("updateSensors: pausing...");
            pause(50);
        }

        return sensorsValid;
    }

    /**
     * Update sensors.  Block for up to 1000 ms waiting for update
     * To use non-blocking, call sensors() and then poll sensorsValid()
     */
    public boolean updateSensors(int packetcode) {
        sensorsValid = false;
        sensors(packetcode);
        for (int i = 0; i < 20; i++) {
            if (sensorsValid) {
                logmsg("updateSensors: sensorsValid!");
                break;
            }
            logmsg("updateSensors: pausing...");
            pause(50);
        }

        return sensorsValid;
    }

    /**
     * called by serialEvent when we have enough bytes to make sensors valid
     */
    private void computeSensors() {
        sensorsValid = true;
        sensorsLastUpdateTime = System.currentTimeMillis();
        computeSafetyFault();
    }

    public String[] listPorts() {
        Map<String, Boolean> ps = Collections.synchronizedMap(new LinkedHashMap<>());
        try {
            String[] portNames = SerialPortList.getPortNames();
            for(String name : portNames ) {
                logmsg("Found port: " + port);

                    Boolean state = ports.get( name );
                    if( state==null ) state = Boolean.FALSE;
                    ps.put( name, state );
            }
        } catch (UnsatisfiedLinkError | Exception e) {
            errorMessage("listPorts", e);
        }

        ports = ps;

        TreeSet<String> treeSet = new TreeSet<>(ports.keySet());

        return treeSet.toArray(new String[0]);
    }


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        if (protocol.equals("SCI")) {
            rate = 57600;
        } else if (protocol.equals("OI")) {
            rate = 115200;
        }
        this.protocol = protocol;
        logmsg("Protocol: " + protocol);
        writeConfigFile(portname, protocol, waitForDSR?'Y':'N');
    }

    public boolean isWaitForDSR() {
        return waitForDSR;
    }

    public void setWaitForDSR(boolean waitForDSR) {
        this.waitForDSR = waitForDSR;
        writeConfigFile(portname, protocol, waitForDSR?'Y':'N');
    }

    public String getPortname() {
        return portname;
    }

    public void setPortname(String p) {
        portname = p;
        logmsg("Port: " + portname);
        writeConfigFile(portname, protocol, waitForDSR?'Y':'N');

    }

    // -------------------------------------------------------------
    // below only used internally to this class
    // -------------------------------------------------------------

    /**
     * internal method, used by connect()
     * FIXME: make it faile more gracefully, recognize bad port
     */
    private boolean open_port() {
        boolean success = false;
        try {
            String[] portNames = SerialPortList.getPortNames();
            for(String name: portNames) {
                    logmsg("found " + name);
                    if (name.equals(portname)) {
                        logmsg("open_port:"+ name);
                        port = new SerialPort(name);
                        port.openPort();
                        port.setParams(rate,databits,stopbits,parity);
                        port.addEventListener(this);
                        logmsg("port "+portname+" opened successfully");

                        if( waitForDSR ) {
                            int i=40;
                            while( !port.isDSR() && i-- != 0) {
                                logmsg("DSR not ready yet");
                                pause(150); // 150*40 = 6 seconds
                            }
                            success = port.isDSR();
                        } else {
                            success = true;
                        }
                    }
                }

        } catch (Exception e) {
            logmsg("connect failed: "+e);
            port = null;
        }
        return success;
    }

    /**
     * callback for SerialPortEventListener
     * (from processing.serial.Serial)
     */
    synchronized public void serialEvent(SerialPortEvent serialEvent) {

        if(serialEvent.isRXCHAR() && serialEvent.getEventValue() > 0) {
            try {
                byte[] bufferBytes = port.readBytes();

                for (byte b : bufferBytes) {
                    buffer[bufferLast++] = b;
                    if (bufferLast == 26) {
                        bufferLast = 0;
                        System.arraycopy(buffer, 0, sensor_bytes, 0, 26);
                        computeSensors();
                    }
                }
            } catch (SerialPortException e) {
                errorMessage("serialEvent", e);
            }
        }
    }

    /**
     * Write a config file with current settings
     */
    private void writeConfigFile(String port, String protocol, char waitForDSR)
    {
        try {
            FileWriter f = new FileWriter(".roomba_config", false);
            BufferedWriter w = new BufferedWriter(f); // create file
            w.write(port);
            w.newLine();
            w.write(protocol);
            w.newLine();
            w.write(waitForDSR);
            w.newLine();
            w.close();
            f.close();
        } catch (IOException e) {
            logmsg("unable to write .roomba_config " + e);
        }
    }
    private void readConfigFile()
    {
        try {
            FileReader f = new FileReader(".roomba_config");
            BufferedReader r = new BufferedReader(f);
            portname = r.readLine();
            protocol = r.readLine();
            if (protocol.equals("OI")) {
                rate = 115200;
            }
            waitForDSR = r.readLine().equals("Y");
            logmsg("read config port: " + port + " protocol: " + protocol + " waitDSR: " + waitForDSR);
        } catch (IOException e) {
            logmsg("unable to read .roomba_config " + e);
        }
    }
}
