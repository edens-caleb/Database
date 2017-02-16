import java.lang.*;
import java.util.*;
import java.io.*;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import java.net.*;

// Compile:  javac -cp .:junit-4.12.jar *.java
// Run:  java -cp .:junit-4.12.jar:hamcrest-core-1.3.jar Main

public class Server{
	
	public static void main(String[] args)throws IOException {
		
		Engine egin = new Engine();
		Parser p = new Parser(egin);
		
		//Run unit tests, Must pass all unit tests before connecting to IS
		Result result = JUnitCore.runClasses(unitTest.class);
		for (Failure failure : result.getFailures())
			System.err.println(failure.getMessage());
		if (result.wasSuccessful()) {
			System.out.println("All Engine tests passed.");	
			result = JUnitCore.runClasses(unitTestParser.class);
			for (Failure failure : result.getFailures())
				System.err.println(failure.getMessage());
			if (result.wasSuccessful()) {
				System.out.println("All Parser tests passed.");
				int portNumber = 36000;
				try{
					if (args.length > 0)
						portNumber = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {
					System.err.println("Argument must be integer.");
					System.exit(1);
				}				
				ServerSocket serverSocket = new ServerSocket(portNumber);
				Socket clientSocket = serverSocket.accept();
				PrintStream  out = new PrintStream (clientSocket.getOutputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
				
				String input = "";
				boolean done = false;
				while (!done) {
					try {
						while((input = in.readLine()) != null) {
							input = input.trim();
							if (!input.equals("")) {
								System.out.println("Recieved input: " + input);
								Table output = p.parse(input);
								if (output == null) {
									out.println("FAILURE!");
								}
								else {
									if (input.startsWith("SHOW")) {
										out.println(output);
									}
									else {
										out.println("SUCCESS!");
									}
								}
								out.println("END--MSG");
							}
							if (input.equals("EXIT;"));
								done = true;
						}
					} catch (IOException e) {
						done = true;
						System.err.println("Bad input");
					} catch (Exception e) {
						done = true;
						System.err.println("Exception occured");
					}
				}
				out.close();
				clientSocket.close();
				serverSocket.close();
			}
		}
	}
}
