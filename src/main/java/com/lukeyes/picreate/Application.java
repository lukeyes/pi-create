package com.lukeyes.picreate;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Keyboard;
import roombacomm.RoombaComm;
import roombacomm.RoombaCommSerial64;

public class Application {

    RoombaComm roomba;
    XBoxInput mInput;
    boolean m_isSetup = false;

    public Application() {
        this.roomba = new RoombaCommSerial64(true);
        this.mInput = XBoxInput.create();
    }

    public static void main(String[] args) {
        System.out.println("Hello world");

        Application application = new Application();


        application.run();


    }

    public void run() {


        boolean found = false;

        while (!found) {
            found = tryFindRoomba();
            if (!found) {
                tryWait(2000);
            }
        }

        System.out.println("Roomba found!");
        tryWait(3000);
        roomba.playNote(90, 32);  // C7
        roomba.pause(200);

        tryWait(1000);

        roomba.playNote(50, 32);  // C1
        roomba.pause(200);


        System.out.println("Roomba started!");

        while (true) {
            if (!m_isSetup) {
                m_isSetup = mInput.setup();
                System.out.println("No  set up");
                continue;
            }

            if (!mInput.poll()) {
                System.out.println("Could not poll");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            updateRemote();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    public boolean tryFindRoomba() {
        String[] ports = roomba.listPorts();

        // try to connect to each port
        for(String port : ports) {

            System.out.println("Port: " + port);

            boolean connected = tryConnect(port);
            if(connected) {
                break;
            }
        }

        if(roomba.connected()) {
            System.out.println("Connected");
        } else {
            System.out.println("Disconnected");
        }
        return roomba.connected();
    }

    public boolean tryConnect(String port) {
        boolean connected = roomba.connect(port);
        if(!connected) {
            roomba.disconnect();
            return false;
        }

        System.out.println("Port: " + port);
        roomba.startup();
        roomba.control();
        roomba.playNote( 72, 10 );  // C , test note
        roomba.pause( 200 );

        boolean foundSensors = roomba.updateSensors();
        if(foundSensors) {
            return true;
        }

        roomba.disconnect();
        return false;
    }

    public boolean tryWait(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    void updateRemote()
    {
        if(mInput.forward()) {
            roomba.goForward();
            //System.out.println("Move forward");
        } else if(mInput.backward()) {
            //System.out.println("Move backward");
            roomba.goBackward();
        } else if(mInput.left()) {
            //System.out.println("Move left");
            roomba.spinLeft();
        } else if(mInput.right()) {
            //System.out.println("Move right");
            roomba.spinRight();
        } else {
            roomba.stop();
        }
    }
}
