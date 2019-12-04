package nl.salland.scoreboard;

import java.util.LinkedList;
import java.util.Queue;

public class TestSplit {

	public static void main(String[] args) {

		Queue<String> commandQueue = new LinkedList<String>();
		String commandLine = "{yayaya}{donkey}{kong}";


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
				System.out.println(split);
			}
		} else {
			commandQueue.add(commandLine);
		}

		System.out.println("Command(s) = " + commandQueue.toString());

	}

}
