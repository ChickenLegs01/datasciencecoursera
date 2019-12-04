package nl.salland.scoreboard;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScoreBoardEmulator {

	public static Logger LOG = LoggerFactory.getLogger(ScoreBoardEmulator.class);
//	public static String DUMMY_SHUTDOWN_MESSAGE="{?-|>U0005|+L01|+P012345678}";
	public static String DUMMY_SHUTDOWN_MESSAGE="{?-|>U0005|+L01|+PSHUTITNOW}";
	public static void main(String[] args) throws IOException {
		PropertyConfigurator.configure("log4j.properties");
		LOG.info("Starting emulator");
//		CommPortIdentifier commPortId = ComPortController.getCommPort("COM9");

		String comPort = "COM8";

		if(args.length==1) {
			comPort = args[0];
		}
		ComPortController controller = new ComPortController(comPort);
		controller.init();

		String line;
		LOG.info("Waiting for something to happen");
		while(true) {

			line = getInput(controller);//controller.readInput();

			if(controller.initString.equals(line)) {
				LOG.info("Recieved init string: " + line);
				controller.sleep(50);
				controller.sendAck();
			} else {
				LOG.info("recieved: " + line);
				controller.sleep(50);
				controller.sendAck();

				if(line.equals(DUMMY_SHUTDOWN_MESSAGE)) {
					controller.sleep(200);
					LOG.info("Shutdown command recieved : " + line );
					controller.close();
					break;
				}
			}
		}


	}

	static Queue<String> commandQueue = new LinkedList<String>();
	private static String getInput(ComPortController controller) {

		// all messages start with { and end with }

		if(commandQueue.size()>0) {
			return commandQueue.poll();
		}

		boolean messageReceived = false;
		StringBuffer sb = new StringBuffer();
		String line;
		while(messageReceived == false) {
			line = controller.readInput();
			if(line==null) continue;
			sb.append(line);

			if(sb.toString().endsWith("}")) {
				messageReceived=true;
				// check if more than one command has snuck in.
				fillCommandQueue(commandQueue, sb.toString());
			}
		}

		return commandQueue.poll();
	}

	private static void fillCommandQueue(Queue<String> commandQueue, String commandLine) {
		// looking for this pattern {command1}{command2}{command3}
		if(commandLine.contains("}{")) {
			String[] splits = commandLine.split("\\}\\{");

			for(String split: splits) {
				if(!split.startsWith("{")) {
					split= "{"+split;
				}

				if(!split.endsWith("}")) {
					split = split + "}";
				}

				commandQueue.add(split);
//				System.out.println(split);
			}
		} else {
			commandQueue.add(commandLine);
		}

	}
}
//
//		CommPortIdentifier commPortId = ComPortController.getCommPort("COM8");
//
//		OutputStream outputStream;
//		InputStream inputStream;
//		try {
//			SerialPort serialPort  = (SerialPort) commPortId.open("mxuport", 2000);
//
//			System.out.println("BaudRate=" + serialPort.getBaudRate());
//			System.out.println("flowcontrol mode=" + serialPort.getFlowControlMode());
//			System.out.println("ReceiveThreshold=" + serialPort.getReceiveThreshold());
//			System.out.println("OutputBufferSize=" + serialPort.getOutputBufferSize());
//
//			outputStream = serialPort.getOutputStream();
//			inputStream = serialPort.getInputStream();
//
//			serialPort.setSerialPortParams(9600,
//                    SerialPort.DATABITS_8,
//                    SerialPort.STOPBITS_1,
//                    SerialPort.PARITY_NONE);
//
//			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
//
//			// {?53|>U0001|+? }
//			// {?09|>U0001|+L01|+P 1   0   0}
//
//			byte[] readBuffer = new byte[50];
//			byte[] copyBuffer;
//			boolean keepRunning=true;
//			String input;
//			System.out.println("Start Running!");
//			while(keepRunning) {
//
//				try {
//					while (inputStream.available() > 0) {
//					    int numBytes = inputStream.read(readBuffer);
//					    copyBuffer = new byte[numBytes];
//					    for(int i=0; i<numBytes; i++) {
//					    	copyBuffer[i]=readBuffer[i];
//					    }
//					    input = new String(copyBuffer);
////					    if(input.indexOf("U0001|+L01")>0) {
//					    	System.out.println("Number of bytes read=" + numBytes + "["+input+"]");
////					    }
//					    	ComPortController.sleep(10);
//					    	ComPortController.sendAck(outputStream);
//					}
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				CommPortUtils.sleep(50);
//			}
//
//			serialPort.close();
//			System.out.println("comm port closed");
//		} catch (PortInUseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnsupportedCommOperationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//
//	}
//
//}
