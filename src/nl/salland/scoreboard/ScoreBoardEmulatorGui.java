package nl.salland.scoreboard;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ScoreBoardEmulatorGui {

	private JFrame mainFrame;

	String line1tt = "BAT# TOTAL BAT#";
	String line2tt = "RUNS WKTS RUNS";
	String line3tt = "BWL#  LW    LM";
	String line4tt = "BWL# 1stINN RR";
	String line5tt = "OV  D/L  OVREM";

	JTextField scoreLine1 = new JTextField();
	JTextField scoreLine2 = new JTextField();
	JTextField scoreLine3 = new JTextField();
	JTextField scoreLine4 = new JTextField();
	JTextField scoreLine5 = new JTextField();

	Font font = new Font("monospaced", Font.BOLD, 20);
	Font fontLine = new Font("monospaced", Font.BOLD, 40);

	String DUMMY_SHUTDOWN_MESSAGE="{?-|>U0005|+L01|+PSHUTITNOW}";
	Queue<String> commandQueue = new LinkedList<String>();

	public JTextField makeTextField(String text) {
        JTextField tf1 = new JTextField(text, 10);
        tf1.setFont(fontLine);
        tf1.setEditable(false);
        return tf1;
	}

	public JLabel makeLabel(String text, String tt) {
		JLabel label = new JLabel(text);
		label.setFont(font);
		label.setToolTipText(tt);
		return label;
	}

	private void prepareGUI() {

		mainFrame = new JFrame("My ScoreBoard Emulator");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(600, 400);

		JPanel content = new JPanel(new GridLayout(5,1));

		content.add(makeline(scoreLine1, "AAAAAAAAAA", "Line1", line1tt));
		content.add(makeline(scoreLine2, "BBBBBBBBBB", "Line2", line2tt));
		content.add(makeline(scoreLine3, "CCCCCCCCCC", "Line3", line3tt));
		content.add(makeline(scoreLine4, "DDDDDDDDDD", "Line4", line4tt));
		content.add(makeline(scoreLine5, "EEEEEEEEEE", "Line5", line5tt));

		mainFrame.getContentPane().add(BorderLayout.CENTER, content);
		mainFrame.setVisible(true);
	}

	private JPanel makeline(JTextField textField, String text, String labelText, String labeltt) {
		textField.setFont(fontLine);
		textField.setEditable(false);
		textField.setText(text);

		JPanel panel = new JPanel();
        panel.add(makeLabel(labelText, labeltt));
        panel.add(textField);

		return panel;
	}


	public void runGui(String comPort) throws IOException {
		prepareGUI();

		ComPortController controller = new ComPortController(comPort);
		controller.init();

		String line;
		System.out.println("Waiting for something to happen");
		while(true) {

			line = getInput(controller);//controller.readInput();

			if(controller.initString.equals(line)) {
				System.out.println("Recieved init string: " + line);
				controller.sleep(50);
				controller.sendAck();
			} else {
				System.out.println("recieved: " + line);
				setLineText(line);
				controller.sleep(50);
				controller.sendAck();

				if(line.equals(DUMMY_SHUTDOWN_MESSAGE)) {
					controller.sleep(200);
					System.out.println("Shutdown command recieved : " + line );
					controller.close();
					break;
				}
			}
		}

	}

	private void setLineText(String input) {
		String text=null;
		int indexOfEnd = input.indexOf("}");
//		System.out.println("input = [" +input+"]");
		if(input.contains(">U0001|")) {
			text = input.substring(18,indexOfEnd);
			scoreLine1.setText(text);
		} else if(input.contains(">U0002|")) {
			text = input.substring(18,indexOfEnd);
			scoreLine2.setText(text);
		} else if(input.contains(">U0003|")) {
			text = input.substring(18,indexOfEnd);
			scoreLine3.setText(text);
		} else if(input.contains(">U0004|")) {
			text = input.substring(18,indexOfEnd);
			scoreLine4.setText(text);
		} else if(input.contains(">U0005|")) {
			text = input.substring(18,indexOfEnd);
			scoreLine5.setText(text);
		} else {
			System.out.println("Don't know what to do with cmd [" +input+ "]");
			// nothing to do.
		}
		System.out.println("text=[" + text + "]");
	}

	private String getInput(ComPortController controller) {

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

	private void fillCommandQueue(Queue<String> commandQueue, String commandLine) {
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
	public static void main(String[] args) throws IOException {

		String comPort = "COM7";
		if(args.length==1) {
			comPort = args[0];
		}

		ScoreBoardEmulatorGui sc = new ScoreBoardEmulatorGui();
		sc.runGui(comPort);
	}

}
