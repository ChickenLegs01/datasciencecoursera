package nl.salland.scoreboard;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.UnsupportedCommOperationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComPortController {
	
	public  static Logger LOG = LoggerFactory.getLogger(ComPortController.class);
			
	String comPortNumber;
	SerialPort serialPort;
	OutputStream outputStream;
	InputStream inputStream;

	String errorMessage="";
	
	String ack = "{DTACK}";
	String initString="{?53|>U0001|+? }";
	boolean disableComm=false;
	
	public ComPortController(String comportNumber, boolean disableComm) {
		this.comPortNumber = comportNumber;
		this.disableComm = disableComm;
	}
	
	public ComPortController(String comportNumber) {
		this.comPortNumber = comportNumber;
	}

	public void init() {
		
		if(disableComm) {
			LOG.warn("--------------------------------------------");
			LOG.warn("init not performed, running in disabled mode");
			LOG.warn("--------------------------------------------");
			return;
		}
		
		if(serialPort!=null) return;
		try {
			LOG.info("Connecting to port : " +comPortNumber);
			CommPortIdentifier commPortId = getCommPort(comPortNumber); 
			
			serialPort  = (SerialPort) commPortId.open("My connection", 2000);
			outputStream = serialPort.getOutputStream();
			inputStream = serialPort.getInputStream();
			
			serialPort.setSerialPortParams(9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
			
			serialPort.setRTS(false);
			serialPort.setDTR(false);
			//serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
			
		} catch (PortInUseException e) {
			errorMessage=e.getMessage();
			e.printStackTrace();
		} catch (IOException e) {
			errorMessage=e.getMessage();
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			errorMessage=e.getMessage();
			e.printStackTrace();
		}
	}
	
	public void close() throws IOException {

			outputStream.close();
			inputStream.close();
			serialPort.close();
//		serialPort = null;

//		serialPort.close();
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	
	public static CommPortIdentifier getCommPort(String commName)  {
		
		LOG.info("looking for port = " + commName);
		Enumeration portList;
		CommPortIdentifier portId;
		
		portList = CommPortIdentifier.getPortIdentifiers();
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
//			if(portId.getPortType() == CommPortIdentifier.PORT_SERIAL)  {
				if(portId.getName().equals(commName) ) {
					LOG.debug("Found ComPort PortName=" + portId.getName() + ", portType=" + portId.getPortType());
					return portId;
				}
//			}
			LOG.debug("PortName=" + portId.getName() + ", portType=" + portId.getPortType());
		}
		
		return null;
	}

	String readInput()  {
		LOG.debug("readInput() START");
		byte[] readBuffer = new byte[50];
		byte[] copyBuffer;
		try {
			while (inputStream.available() > 0) {
			    int numBytes = inputStream.read(readBuffer);
			    LOG.debug("Number of bytes read=" + numBytes);
			    copyBuffer = new byte[numBytes];
			    for(int i=0; i<numBytes; i++) {
			    	copyBuffer[i]=readBuffer[i];
			    }
			    return new String(copyBuffer);
			}
		} catch (IOException e) {
			e.printStackTrace();		
		}
		return null;
	}
	
	void writeOutput(String output) throws IOException {
		LOG.debug("writeOutput START: " + output);
		outputStream.write(output.getBytes());
	}
	
	public void sendAck() throws IOException {
		writeOutput(ack);
	}

	public String getAck() throws IOException {
		return readInput();
	}
	
	public void sleep(int millisec) {
		try {
			Thread.sleep(millisec);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void sendInitString(){
		LOG.debug("sendInitString() START");
		if(disableComm) return;
		
		try {
			writeOutput(initString);
			sleep(130);
			String read = readInput();
			if(!ack.equals(read)) {
				errorMessage = "init(ack) failed?";
				throw new RuntimeException(errorMessage);
			}
			LOG.info("Init OK!");
		} catch (IOException e) {
			errorMessage=e.getMessage();
			throw new RuntimeException(e);
		}
	}
	
	public void sendLine(String aLine) {
		LOG.debug("sendLine START");	
		if(disableComm) {
			LOG.info("comDisabled: Write to comm --> ["+aLine+"]");
			return;
		}

		try {
			writeOutput(aLine);
			sleep(130);
			String read = readInput();
			if(!ack.equals(read)) {
				errorMessage = "ack failed for : " + aLine.subSequence(6, 11);
				throw new RuntimeException(errorMessage);
			}
			LOG.debug("Line sent");
		} catch (IOException e) {
			errorMessage=e.getMessage();
			throw new RuntimeException(e);
		}
	}
	
	public static String[] getComports() {
		LOG.info("getComports() START");
		Enumeration portList = CommPortIdentifier.getPortIdentifiers();
		CommPortIdentifier portId;
	
		SortedSet set = new TreeSet();
		while (portList.hasMoreElements()) {
            portId = (CommPortIdentifier) portList.nextElement();
            LOG.debug("port found: " + portId.getName());
            if(portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
            	set.add(portId.getName());
            }
        }

		LOG.info("Number of comports found = " + set.size());
		return  (String[]) set.toArray(new String[set.size()]);
	}
	
	public void sendWelcomeMessage() {
		
	}
}
