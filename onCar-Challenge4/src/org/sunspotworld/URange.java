/*
 * URange.java
 *
 * Created on Jul 4, 2012 4:35:45 PM;
 */

package org.sunspotworld;

import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.resources.Resources;
//import com.sun.spot.resources.transducers.IIOPin;
import com.sun.spot.resources.transducers.IAnalogInput;
//import com.sun.spot.sensorboard.io.AnalogInput;
import com.sun.spot.resources.transducers.ISwitch;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.IServo;
import com.sun.spot.sensorboard.peripheral.Servo;
import com.sun.spot.service.BootloaderListenerService;
import com.sun.spot.util.IEEEAddress;
import com.sun.spot.util.Utils;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * The startApp method of this class is called by the VM to start the
 * application.
 * 
 * The manifest specifies this class as MIDlet-1, which means it will
 * be selected for execution.
 * @author  Yuting Zhang <ytzhang@bu.edu>
 */
public class URange extends MIDlet {
     // devices
    private EDemoBoard eDemo = EDemoBoard.getInstance();
    // left servo from driver's view
    private IServo servo1 = new Servo(eDemo.getOutputPins()[EDemoBoard.H1]);
    // right servo from driver's view
    private IServo servo2 = new Servo(eDemo.getOutputPins()[EDemoBoard.H0]);
    private static final int SERVO_CENTER_VALUE = 1500;
        private static final int SERVO2_HIGH = 10; //speeding step high
    private static final int SERVO2_LOW = 5; //speeding step low
        private static final int SERVO2_MAX_VALUE = 1600;
    
    public static final double voltage = 5.0; 
    public static final double scaleFactor = voltage/512;
    
    private static final int HOST_PORT = 65;
    private int current2 = SERVO_CENTER_VALUE;
    private int step2 = SERVO2_LOW;
    private ITriColorLEDArray leds = (ITriColorLEDArray) Resources.lookup(ITriColorLEDArray.class);
    protected void startApp() throws MIDletStateChangeException {
        servo1.setValue(SERVO_CENTER_VALUE-200);
        servo2.setValue(SERVO_CENTER_VALUE);
        RadiogramConnection rCon = null;
        Datagram dg = null;
        BootloaderListenerService.getInstance().start();   // monitor the USB (if connected) and recognize commands from host

        long ourAddr = RadioFactory.getRadioPolicyManager().getIEEEAddress();
        System.out.println("Our radio address = " + IEEEAddress.toDottedHex(ourAddr));

        IAnalogInput rightCarSensor = EDemoBoard.getInstance().getAnalogInputs()[EDemoBoard.A0];
        IAnalogInput leftCarSensor = EDemoBoard.getInstance().getAnalogInputs()[EDemoBoard.A1];
        ITriColorLED led = leds.getLED(0);
        led.setRGB(100,0,0);   // set color to moderate red
        //start_wheels
        

   while(true){
       forward();
        try {
            try {
            // Open up a broadcast connection to the host port
            // where the 'on Desktop' portion of this demo is listening
            rCon = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
            dg = rCon.newDatagram(50);  // only sending 12 bytes of data
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
            notifyDestroyed();
        }
           //    led.setOn();                        // Blink LED
           //    Utils.sleep(250);
                double inchesCarRight = (rightCarSensor.getVoltage()/scaleFactor);
                double inchesCarLeft = (leftCarSensor.getVoltage())/scaleFactor;
                dg.reset();
                //enoc
                dg.writeDouble(inchesCarLeft);
                dg.writeDouble(inchesCarRight);
                System.out.println("Sending datagram: " +inchesCarLeft + " " + inchesCarRight);
                rCon.send(dg);
                Utils.sleep(50);
            } catch (IOException ex){
                ex.printStackTrace();
            }
          //  Utils.sleep(100);
     //       notifyDestroyed();
        }
     }

    protected void pauseApp() {
        // This is not currently called by the Squawk VM
    }

    /**
     * Called if the MIDlet is terminated by the system.
     * It is not called if MIDlet.notifyDestroyed() was called.
     *
     * @param unconditional If true the MIDlet must cleanup and release all resources.
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
    }
    private void forward() {
        System.out.println("forward");
        //servo1.setValue(0);
        current2= servo2.getValue();

        for (int i=0;i<3;i++){
            current2= servo2.getValue();
            if (current2 + step2 <SERVO2_MAX_VALUE){
                servo2.setValue(current2+step2);
                Utils.sleep(50);
            } else {
                servo2.setValue(SERVO2_MAX_VALUE);
                Utils.sleep(50);
            }
        }
            
  /*     while(current2 + step2 <SERVO2_MAX_VALUE){
            servo2.setValue(current2+step2);
            current2= servo2.getValue();
            Utils.sleep(50);
         }*/
    }
}
