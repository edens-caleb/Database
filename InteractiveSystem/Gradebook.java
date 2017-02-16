import java.lang.*;
import java.util.*;
import java.io.*;
import java.net.*;

// Compile:  javac Gradebook.java
// Run:  java Gradebook


public class Gradebook{
	static Socket socket = null;
	static PrintStream out = null;
	static BufferedReader in = null;
	static BufferedReader inputReader = null;
	
	static String response = "";

	public static void main(String[] args){
		try{
			int portNumber = 36000;  //default portNumber
			if (args.length > 0)
				portNumber = Integer.parseInt(args[0]);
			
			socket = new Socket("compute-linux1", portNumber);
			out = new PrintStream(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			inputReader = new BufferedReader(new InputStreamReader(System.in));
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: compute-linux1");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Server not found.");
			System.exit(1);
		} catch (NumberFormatException e) {
			System.err.println("Argument must be integer.");
			System.exit(1);
		}
		initializeDatabase();
		
		System.out.print("\033[H\033[2J");
		System.out.flush();
		welcomeWindow();
		mainMenu();
		System.exit(0);
	}
	
	public static void closeSystem() {
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			System.err.println("Server not found.");
			System.exit(1);
		}
	} 
	
	public static void initializeDatabase() {
		if (!writeToServer("OPEN students;")) {
			writeToServer("CREATE TABLE students (UIN INTEGER, Name VARCHAR(16), Major VARCHAR(16)) PRIMARY KEY (UIN);");
			if (!writeToServer("WRITE students;")) {
				System.err.println("Fatal error:  Could not create students database.");
				System.exit(1);
			}	
		}
		if (!writeToServer("OPEN professors;")) {
			writeToServer("CREATE TABLE professors (PUIN INTEGER, Name VARCHAR(16)) PRIMARY KEY (PUIN);");
			if (!writeToServer("WRITE professors;")) {
				System.err.println("Fatal error:  Could not create professors database.");
				System.exit(1);
			}	
		}
		if (!writeToServer("OPEN classes;")) {
			writeToServer("CREATE TABLE classes (CRN INTEGER, Name VARCHAR(16), Hours INTEGER) PRIMARY KEY (CRN);");
			if (!writeToServer("WRITE classes;")) {
				System.err.println("Fatal error:  Could not create classes database.");
				System.exit(1);
			}	
		}
	} 
	
	public static String getResponse() {
		String output = "";
		try {
			while ((output = in.readLine()) != null) {
				while (true) {
					String current = in.readLine();
					if(current.equals("END--MSG"))
						break;
					output += "\n" + current;
				}
				break;
			}
		}
		catch (IOException e) {
			System.out.println("                      Bad input. ");
			return "FAILURE!";
		}
		return output;
	}
	
	public static boolean writeToServer(String command) {
		out.println(command);
		response = getResponse();
		if (response.equals("FAILURE!"))
			return false;
		return true;
	} 
	
	
	
	public static boolean printTable(int desiredSpace, String id){
		LinkedList<String> rows = new LinkedList<String>(Arrays.asList(response.split("\\r?\\n")));
		rows.remove();
		rows.remove();
		System.out.println();
		if (rows.size() == 1) {
			System.out.println( "                       Table is empty.");
			return false;
		}
		int id_size = id.length();
		int diff = id_size-2;
		String buff = "";
		for(int i = diff; i>0; i--) {
			buff += " ";
		}
		String whiteSpace = new String(new char[desiredSpace]).replace("\0", "            ");
		
		for(int i = 0; i < rows.size(); i++) {
			if(i==0) {
				String text = rows.get(i);
				String text2 = text.replace("-1"+buff, id);
				rows.set(i, whiteSpace + text2);
				//rows.set(i, whiteSpace.substring(0,whiteSpace.length()-diff) + text2);
			} else {
				rows.set(i, whiteSpace + rows.get(i));
			}
		}
		for(String r : rows)
			System.out.println(r);
		return true;
	}

//***Level 1***
	public static void welcomeWindow(){
		
		System.out.println();
		System.out.println();
		System.out.println("              Welcome to the TAMU Database Management System!\n\n");
		System.out.println("                    Developer(s)     |     Team gitRekt");
		System.out.println("                         Version     |     1.0");
		System.out.println("                  Stable Release     |     October 2016");
		System.out.println("              Development Status     |     Active");
		System.out.println("                      Written In     |     Java\n\n");
		System.out.println("                           Press Enter to begin! \n");
		Scanner keyboard = new Scanner(System.in);
		keyboard.nextLine();
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
//***Level 2***
	public static void mainMenu(){
		while(true) {
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println("");
			System.out.println("");
			System.out.println("                                 Main Menu\n\n");
			System.out.println("                          1) Student Menu");
			System.out.println("                          2) Professor Menu");
			System.out.println("                          3) Class Menu");
			System.out.println("                          4) Exit\n\n");
			System.out.print("                    Insert Selection and Press Enter: ");
			Scanner keyboard = new Scanner(System.in);
			String choice = keyboard.next();
			switch (choice) {
				case "1":
				studentMenu();
					break;
				case "2": 
				professorMenu();
					break;
				case "3":
				classMenu();
					break;
				case "4":
				writeToServer("EXIT;");
				closeSystem();
				exitWindow();
					break;
				default:
			}
		}
	}

//***Level 3***
	public static void studentMenu(){
		while (true) {
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Student Menu\n\n");
			System.out.println("                          1) Find Student");
			System.out.println("                          2) Add Student");
			System.out.println("                          3) Search Students");
			System.out.println("                          4) Back\n\n");
			System.out.print("                    Insert Selection and Press Enter: ");
			Scanner keyboard = new Scanner(System.in);
			String choice = keyboard.next();
			switch (choice) {
				case "1":
				findStudent();
					break;
				case "2": 
				addStudent();
					break;
				case "3":
				searchStudents();
					break;
				case "4":
				return;
				default:
			}
		}
	}
	
	//***Level 4***
	public static void findStudent(){
		while(true) {
			boolean flag = true;
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Find Student\n\n");
			Scanner keyboard = new Scanner(System.in);
			String uin = "-12";
			int uinInt = -12;
			while(flag == true){
				flag = false;
				System.out.print("           Please enter the Student UIN and press Enter: ");
				uin = keyboard.next();
				if(uin.isEmpty()){
					flag = true;
				}
				try{
					uinInt = Integer.parseInt(uin);
				}
				catch (NumberFormatException e){
					flag = true;
					System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
					continue;
				}
				if(uinInt < 0){
					flag = true;
					System.out.println("           Invalid Entry: Please enter a positive value ");
				}
			}
			keyboard.nextLine();
			System.out.println("           Students Information: ");
			writeToServer("SHOW (select (-1==" + uinInt + ") students);");
			if (!printTable(1, "UIN")) {
				System.out.println();
				System.out.println("           What action would you like to do next?");
				System.out.println();
				System.out.println("                          1) Search for another student");
				System.out.println("                          2) Exit");
				System.out.print("\n\n                    Insert Selection and Press Enter: ");
				flag = false;
				while(flag == false){
					flag = false;
					String choice = keyboard.next();
					
					switch (choice) {
						case "1":
						flag = true;
						break;
						case "2":
						return;
						default:
						//Unexpected output
					}
				}
			}
			else {
				System.out.println();
				System.out.println("           What action would you like to do next?");
				System.out.println();
				System.out.println("                          1) Search for another student");
				System.out.println("                          2) Update student information");
				System.out.println("                          3) Delete student from record");
				System.out.println("                          4) Exit");
				System.out.print("\n\n                    Insert Selection and Press Enter: ");
				flag = false;
				while(flag == false){
					flag = false;
					String choice = keyboard.next();
					
					switch (choice) {
						case "1":
						flag = true;
						break;
						case "2":
						updateStudent(uinInt);
						return;
						case "3":
						writeToServer("DELETE FROM students WHERE (-1==" + uinInt + ");");
						writeToServer("WRITE students;");
						System.out.print("               Delete successful.  Press Enter to continue:  ");
						keyboard.nextLine();
						keyboard.nextLine();
						return;
						case "4":
						return;
						default:
						//Unexpected output
					}
				}
			}
		}
	}
	
	//***Level 5***
	public static void updateStudent(int uinInt) {
		while (true) {
			boolean flag = true;
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                          Update Student\n\n");
			Scanner keyboard = new Scanner(System.in);
			String newName = "";
			while(flag == true){
				flag = false;
				System.out.print("       Please enter the students new name and press Enter: ");
				newName = keyboard.nextLine();
				if(newName.isEmpty()){
					flag = true;
				}
			}
			flag = true;
			String newMajor = "";
			while(flag == true){
				flag = false;
				System.out.print("       Please enter the students new major and press Enter: ");
				newMajor = keyboard.nextLine();
				if(newMajor.isEmpty()){
					flag = true;
				}
			}
			writeToServer("UPDATE students SET (Name=\"" + newName + "\", Major=\"" + newMajor + "\") WHERE (-1==\"" + uinInt + "\");");
			if (writeToServer("WRITE students;")) {
				System.out.print("\n                  Update successful. Press Enter to continue: ");
				keyboard.nextLine();
				break;
			}
			System.out.print("\n                  Update error. Press Enter to try again: ");
			keyboard.nextLine();
		}
	}
	
	//***Level 4***
	public static void addStudent(){
		while(true) {
			boolean flag = true;
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Add Student\n\n");
			Scanner keyboard = new Scanner(System.in);
			String uin = "-12";
			int uinInt = -12;
			while(flag == true){
				flag = false;
				System.out.print("           Please enter the students UIN and press Enter: ");
				uin = keyboard.next();
				if(uin.isEmpty()){
					flag = true;
				}
				try{
					uinInt = Integer.parseInt(uin);
				}
				catch (NumberFormatException e){
					flag = true;
					System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
					continue;
				}
				if(uinInt < 0){
					flag = true;
					System.out.println("           Invalid Entry: Please enter a positive value ");
				}
			}
			keyboard.nextLine();
			System.out.print("           Please enter the students Name and press Enter: ");
			String name = keyboard.nextLine();
			System.out.print("           Please enter the students Major and press Enter: ");
			String major = keyboard.nextLine();
			System.out.println("           Would you like to keep the following entry? ");
			System.out.println("           UIN: " + uinInt + " | Name: " + name + " | Major: " + major);
			System.out.println("                          1) Yes");
			System.out.println("                          2) No and Exit");
			System.out.println("                          3) No and Try Again");
			System.out.print("\n\n                    Insert Selection and Press Enter: ");
			flag = false;
			while(flag == false){
				flag = false;
				String choice = keyboard.next();
				
				switch (choice) {
					case "1":
					writeToServer("INSERT INTO students VALUES FROM (" + uinInt + ",\"" + name + "\",\"" + major + "\");");
					if (response.equals("FAILURE!")) {
						System.out.print("         Insertion not successful. Press enter to try again: ");
						keyboard.nextLine();
						keyboard.nextLine();
						flag = true;
						break;
					}
					writeToServer("WRITE students;");
					System.out.print("                    Saved!  Press Enter to continue: ");
					keyboard.nextLine();
					keyboard.nextLine();
					return;
					case "2":
					return;
					case "3":
					flag = true;
					break;
					default:
					//Unexpected output
				}
			}
		}
	}
	
	//***Level 4***
	public static void searchStudents(){
		while(true) {
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Search Students\n\n");
			System.out.println("           How would you like to display student data?");
			System.out.println("                          1) Display all");
			System.out.println("                          2) Display all without major");
			System.out.println("                          3) Display based on major");
			System.out.println("                          4) Display based on name");
			System.out.println("                          5) Display based on UIN");
			System.out.println("                          6) Back");
			System.out.print("\n\n                    Insert Selection and Press Enter: ");
			Scanner keyboard = new Scanner(System.in);
			String choice = keyboard.next();
			switch (choice) {
					case "1":
						displayAllStudents();
						break;
					case "2":
						displayStudentsWoMajor();
						break;
					case "3":
						displayMajorStudents();
						break;
					case "4":
						displayNameStudents();
						break;
					case "5":
						displayUinStudents();
						break;
					case "6":
						return;
					default:
					//Unexpected output
			}
		}
	}
	
	//***Level 5***
	public static void displayAllStudents() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display All Students\n\n");
		writeToServer("SHOW students;");
		printTable(1, "UIN");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}
	
	public static void displayStudentsWoMajor() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display All Students\n\n");
		writeToServer("SHOW (project (Name) students);");
		printTable(2, "UIN");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}
	
	//***Level 5***
	public static void displayMajorStudents() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display by Major\n\n");
		System.out.print("              Please enter the name of the major: ");
		String major = keyboard.nextLine();
		writeToServer("SHOW (select (Major==\"" + major + "\") students);");
		printTable(1, "UIN");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}
	
	//***Level 5***
	public static void displayNameStudents() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display by Name\n\n");
		System.out.print("              Please enter the student name: ");
		String name = keyboard.nextLine();
		writeToServer("SHOW (select (Name==\"" + name + "\") students);");
		printTable(1, "UIN");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}
	
	//***Level 5***
	public static void displayUinStudents() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display by UIN\n\n");
		String uin = "-12";
		int uinInt = -12;
		boolean flag = true;
		while(flag == true){
			flag = false;
			System.out.print("           Please enter the student UIN and press Enter: ");
			uin = keyboard.next();
			if(uin.isEmpty()){
				flag = true;
			}
			try{
				uinInt = Integer.parseInt(uin);
			}
			catch (NumberFormatException e){
				flag = true;
				System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
				continue;
			}
			if(uinInt < 0){
				flag = true;
				System.out.println("           Invalid Entry: Please enter a positive value ");
			}
		}
		keyboard.nextLine();
		writeToServer("SHOW (select (-1==" + uinInt + ") students);");
		printTable(1, "UIN");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}
	
	
	//***Level 3***
	public static void professorMenu(){
		while(true) {
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Professor Menu\n\n");
			System.out.println("                          1) Find Professor");
			System.out.println("                          2) Add Professor");
			System.out.println("                          3) Search Professor");
			System.out.println("                          4) Back\n\n");
			System.out.print("                    Insert Selection and Press Enter: ");
			Scanner keyboard = new Scanner(System.in);
			String choice = keyboard.next();
			switch (choice) {
				case "1":
				findProfessor();
					break;
				case "2": 
				addProfessor();
					break;
				case "3":
				searchProfessors();
					break;
				case "4":
				return;
				default:
				//Unexpected output
			}
		}
	}
	
	//***Level 4***
	public static void findProfessor(){
		while(true) {
			boolean flag = true;
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Find Professor\n\n");
			Scanner keyboard = new Scanner(System.in);
			String puin = "-12";
			int puinInt = -12;
			while(flag == true){
				flag = false;
				System.out.print("           Please enter the Professors PUIN and press Enter: ");
				puin = keyboard.next();
				if(puin.isEmpty()){
					flag = true;
				}
				try{
					puinInt = Integer.parseInt(puin);
				}
				catch (NumberFormatException e){
					flag = true;
					System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
					continue;
				}
				if(puinInt < 0){
					flag = true;
					System.out.println("           Invalid Entry: Please enter a positive value ");
				}
				
			}
			keyboard.nextLine();
			System.out.println("           Professors Information: ");
			writeToServer("SHOW (select (-1==" + puinInt + ") professors);");
			if (!printTable(2, "PUIN")) {
				System.out.println();
				System.out.println("           What action would you like to do next?");
				System.out.println();
				System.out.println("                          1) Search for another professor");
				System.out.println("                          2) Exit");
				System.out.print("\n\n                    Insert Selection and Press Enter: ");
				flag = false;
				while(flag == false){
					flag = false;
					String choice = keyboard.next();
					
					switch (choice) {
						case "1":
						flag = true;
						break;
						case "2":
						return;
						default:
						//Unexpected output
					}
				}
			}
			else {
				System.out.println("\n\n           Teaching List: ");
				writeToServer("OPEN Teaching"+puinInt+";");
				writeToServer("SHOW Teaching"+puinInt+";");
				printTable(1, "CRN");
				System.out.println();
				System.out.println("           What action would you like to do next?");
				System.out.println();
				System.out.println("                          1) Search for another professor");
				System.out.println("                          2) Update professor information");
				System.out.println("                          3) Add Classes to Teaching List");
				System.out.println("                          4) Remove Classes from Teaching List");
				System.out.println("                          5) Delete professor from record");
				System.out.println("                          6) Exit");
				System.out.print("\n\n                    Insert Selection and Press Enter: ");
				flag = false;
				while(flag == false){
					flag = false;
					String choice = keyboard.next();
					
					switch (choice) {
						case "1":
							flag = true;
							break;
						case "2":
							updateProfessorInfo(puinInt);
							return;
						case "3":
							addClassesToList(puinInt);
							return;
						case "4":
							removeClassesFromList(puinInt);
							return;
						case "5":
							writeToServer("DELETE FROM professors WHERE (-1==" + puinInt + ");");
							writeToServer("WRITE professors;");
							System.out.print("               Delete successful.  Press Enter to continue:  ");
							keyboard.nextLine();
							keyboard.nextLine();
							return;
						case "6":
							return;
						default:
						//Unexpected output
					}
				}
			}
		}
	}
	
	//(project (name) Roster101) + (project (Name) Roster102)
	public static void showTaughtStudents(LinkedList<Integer> crn_list) {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		System.out.println();
		System.out.println("");
		System.out.println("                             Taught Students:\n\n");
		Scanner keyboard = new Scanner(System.in);
		String begin_command = "SHOW (";
		String command = "";
		String end_command = "";
		for(int i = 0; i < crn_list.size(); i++) {
			end_command += ")";
		}
		end_command += ");";
		
		for(int i=crn_list.size()-1; i>=0; i--) {
			if(i == 0) {
				command += "(project (-1) Roster"+crn_list.get(i)+"";
			} else {
				command += "(project (-1) Roster"+crn_list.get(i)+"+";
			}
		}
		//System.out.println(begin_command+command+end_command);
		//writeToServer(begin_command+command+end_command);
		writeToServer("SHOW ((project (-1) Roster101) + (project (-1) Roster102));");
		System.out.print("\n                  Press Enter to continue: ");
		keyboard.nextLine();
	}

	public static void addClassesToList(int puinInt) {
		while (true) {
			boolean flag = true;
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Add Classes to Teaching List\n\n");
			Scanner keyboard = new Scanner(System.in);
			boolean more_classes = true;
			while(more_classes) {
				System.out.print("           Enter CRN of class: ");
				String crn = "-12";
				int crnInt = -12;
				while(flag == true){
					flag = false;
					crn = keyboard.next();
					if(crn.isEmpty()){
						flag = true;
					}
					try{
						crnInt = Integer.parseInt(crn);
					}
					catch (NumberFormatException e){
						flag = true;
						System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
					}
					writeToServer("SHOW (select(-1==" + crnInt +") classes);");
					LinkedList<String> rows = new LinkedList<String>(Arrays.asList(response.split("\\r?\\n")));
					rows.remove();
					rows.remove();
					if (rows.size() == 1) {
						flag = true;
						System.out.print("           Invalid Entry: CRN does not exist in class database ");
						keyboard.nextLine();
						keyboard.nextLine();
						return;
					}
				}
				keyboard.nextLine();
				System.out.println("           Would you like to keep the following entry? ");
				System.out.println("           CRN: " + crnInt);
				System.out.println("                          1) Yes, and Continue");
				System.out.println("                          2) Yes, and Exit");
				System.out.println("                          3) No, and Continue");
				System.out.println("                          4) No, and Exit");
				System.out.print("\n\n                    Enter Selection and Press Enter: ");
				String selection = keyboard.next();
				switch(selection) {
					case "1":
						writeToServer("INSERT INTO Teaching"+puinInt+" VALUES FROM("+crnInt+");");
						more_classes = false;
						break;
					case "2":
						writeToServer("INSERT INTO Teaching"+puinInt+" VALUES FROM("+crnInt+");");
						writeToServer("WRITE Teaching"+puinInt+";");
						more_classes = false;
						return;
					case "3":
						more_classes = false;
						break;
					case "4":
						more_classes = false;
						return;
					default:
						//unexpected input
						return;
				}
			}
		}
	}
	
	public static void removeClassesFromList(int puinInt) {
		while (true) {
			boolean flag = true;
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Remove Classes From List\n\n");
			Scanner keyboard = new Scanner(System.in);
			boolean more_classes = true;
			while(more_classes) {
				System.out.print("           Enter CRN of class: ");
				String crn = "-12";
				int crnInt = -12;
				while(flag == true){
					flag = false;
					crn = keyboard.next();
					if(crn.isEmpty()){
						flag = true;
					}
					try{
						crnInt = Integer.parseInt(crn);
					}
					catch (NumberFormatException e){
						flag = true;
						System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
					}
					writeToServer("SHOW (select(-1==" + crnInt +") classes);");
					LinkedList<String> rows = new LinkedList<String>(Arrays.asList(response.split("\\r?\\n")));
					rows.remove();
					rows.remove();
					if (rows.size() == 1) {
						flag = true;
						System.out.print("           Invalid Entry: CRN does not exist in class database ");
						keyboard.nextLine();
						keyboard.nextLine();
						return;
					}
				}
				keyboard.nextLine();
				System.out.println("           Would you like to delete this class? ");
				System.out.println("           CRN: " + crnInt);
				System.out.println("                          1) Yes, and Continue");
				System.out.println("                          2) Yes, and Exit");
				System.out.println("                          3) No, and Continue");
				System.out.println("                          4) No, and Exit");
				System.out.print("\n\n                    Enter Selection and Press Enter: ");
				String selection = keyboard.next();
				switch(selection) {
					case "1":
						writeToServer("DELETE FROM Teaching"+puinInt+" WHERE(-1 == "+crnInt+");");
						more_classes = false;
						break;
					case "2":
						writeToServer("DELETE FROM Teaching"+puinInt+" WHERE(-1 == "+crnInt+");");
						writeToServer("WRITE Teaching"+puinInt+";");
						more_classes = false;
						return;
					case "3":
						more_classes = false;
						break;
					case "4":
						more_classes = false;
						return;
					default:
						//unexpected input
						return;
				}
			}
		}
	}
	
	//***Level 5***
	public static void updateProfessorInfo(int puinInt) {
		while (true) {
			boolean flag = true;
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                          Update Professor\n\n");
			Scanner keyboard = new Scanner(System.in);
			String newName = "";
			while(flag == true){
				flag = false;
				System.out.print("       Please enter the professors new name and press Enter: ");
				newName = keyboard.nextLine();
				if(newName.isEmpty()){
					flag = true;
				}
			}
			writeToServer("UPDATE professors SET (Name=\"" + newName + "\") WHERE (-1==" + puinInt + ");");
			if (writeToServer("WRITE professors;")) {
				System.out.print("\n                  Update successful. Press Enter to continue: ");
				keyboard.nextLine();
				break;
			}
			System.out.print("\n                  Update error. Press Enter to try again: ");
			keyboard.nextLine();
		}
	}
	
//***Level 4***
	public static void addProfessor(){
		while(true) {
			boolean flag = true;
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Add Professor\n\n");
			Scanner keyboard = new Scanner(System.in);
			String puin = "-12";
			int puinInt = -12;
			while(flag == true){
				flag = false;
				System.out.print("           Please enter the professors PUIN and press Enter: ");
				puin = keyboard.next();
				if(puin.isEmpty()){
					flag = true;
				}
				try{
					puinInt = Integer.parseInt(puin);
				}
				catch (NumberFormatException e){
					flag = true;
					System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
					continue;
				}
				if(puinInt < 0){
					flag = true;
					System.out.println("           Invalid Entry: Please enter a positive value ");
				}
				
			}
			keyboard.nextLine();
			System.out.print("           Please enter the Professors Name and press Enter: ");
			String name = keyboard.nextLine();
			System.out.println("           Would you like to keep the following entry? ");
			System.out.println("           PUIN: " + puinInt + " | Name: " + name);
			System.out.println("                          1) Yes");
			System.out.println("                          2) Yes, and add Classes Taught");
			System.out.println("                          3) No and Exit");
			System.out.println("                          4) No and Try Again");
			System.out.print("\n\n                    Insert Selection and Press Enter: ");
			flag = false;
			while(flag == false){
				flag = false;
				String choice = keyboard.next();
				
				switch (choice) {
					case "1":
						writeToServer("INSERT INTO professors VALUES FROM (" + puinInt + ",\"" + name + "\");");
						if (response.equals("FAILURE!")) {
							System.out.print("         Insertion not successful. Press enter to try again: ");
							keyboard.nextLine();
							keyboard.nextLine();
							flag = true;
							break;
						}
						writeToServer("WRITE professors;");
						writeToServer("CREATE TABLE Teaching" + puinInt + " (CRN INTEGER) PRIMARY KEY (CRN);");
						writeToServer("WRITE Teaching" + puinInt + ";");
						System.out.print("                    Saved!  Press Enter to continue: ");
						keyboard.nextLine();
						keyboard.nextLine();
						return;
					case "2":
						writeToServer("INSERT INTO professors VALUES FROM (" + puinInt + ",\"" + name + "\");");
						if (response.equals("FAILURE!")) {
							System.out.print("         Insertion not successful. Press enter to try again: ");
							keyboard.nextLine();
							keyboard.nextLine();
							flag = true;
							break;
						}
						writeToServer("WRITE professors;");
						writeToServer("CREATE TABLE Teaching" + puinInt + " (CRN INTEGER) PRIMARY KEY (CRN);");
						writeToServer("WRITE Teaching" + puinInt + ";");
						addClassesToList(puinInt);
					case "3":
						return;
					case "4":
						flag = true;
						break;
					default:
					//Unexpected output
				}
			}
			
		}
	}
	
	//***Level 4***
	public static void searchProfessors(){
		while(true) {
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Search Professors\n\n");
			System.out.println("           How would you like to display professor data?");
			System.out.println("                          1) Display all");
			System.out.println("                          2) Display based on name");
			System.out.println("                          3) Display based on PUIN");
			System.out.println("                          4) Back");
			System.out.print("\n\n                    Insert Selection and Press Enter: ");
			Scanner keyboard = new Scanner(System.in);
			String choice = keyboard.next();
			switch (choice) {
				case "1":
				displayAllProfessors();
				return;
				case "2":
				displayNameProfessors();
				return;
				case "3":
				displayPuinProfessors();
				return;
				case "4":
				return;
				default:
				//Unexpected output
			}
		}
	}
	
	//***Level 5***
	public static void displayAllProfessors() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display All Professors\n\n");
		writeToServer("SHOW professors;");
		printTable(2, "PUIN");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}
	
	//***Level 5***
	public static void displayNameProfessors() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display by Name\n\n");
		System.out.print("              Please enter the professor name: ");
		String name = keyboard.nextLine();
		writeToServer("a <- (select (Name==\"" + name + "\") professors);");
		writeToServer("SHOW a;");
		printTable(2, "PUIN");
		writeToServer("CLOSE a;");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}
	
	//***Level 5***
	public static void displayPuinProfessors() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display by PUIN\n\n");
		String puin = "-12";
		int puinInt = -12;
		boolean flag = true;
		while(flag == true){
			flag = false;
			System.out.print("           Please enter the Professors PUIN and press Enter: ");
			puin = keyboard.next();
			if(puin.isEmpty()){
				flag = true;
			}
			try{
				puinInt = Integer.parseInt(puin);
			}
			catch (NumberFormatException e){
				flag = true;
				System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
				continue;
			}
			if(puinInt < 0){
				flag = true;
				System.out.println("           Invalid Entry: Please enter a positive value ");
			}
		}
		keyboard.nextLine();
		writeToServer("SHOW (select (-1==" + puinInt + ") professors);");
		printTable(2, "PUIN");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}	
	//***Level 3***
	public static void classMenu(){
		while(true) {
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Class Menu\n\n");
			System.out.println("                          1) Find Class");
			System.out.println("                          2) Add Class");
			System.out.println("                          3) Search Class");
			System.out.println("                          4) Back\n\n");
			System.out.print("                    Insert Selection and Press Enter: ");
			Scanner keyboard = new Scanner(System.in);
			String choice = keyboard.next();
			switch (choice) {
				case "1":
				findClass();
					break;
				case "2": 
				addClass();
					break;
				case "3":
				searchClass();
					break;
				case "4":
				return;
				default:
				//Unexpected output
			}
		}
	}
	
	//***Level 4***
	public static void findClass(){
		while(true) {
			boolean flag = true;
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Find Class\n\n");
			Scanner keyboard = new Scanner(System.in);
			String crn = "-12";
			int crnInt = -12;
			while(flag == true){
				flag = false;
				System.out.print("           Please enter the Class CRN and press Enter: ");
				crn = keyboard.next();
				if(crn.isEmpty()){
					flag = true;
				}
				try{
					crnInt = Integer.parseInt(crn);
				}
				catch (NumberFormatException e){
					flag = true;
					System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
				}
				
			}
			keyboard.nextLine();
			System.out.println("           Class Information: ");
			writeToServer("SHOW (select (-1==" + crnInt + ") classes);");
			if (!printTable(1, "CRN")) {
				System.out.println();
				System.out.println("           What action would you like to do next?");
				System.out.println();
				System.out.println("                          1) Search for another class");
				System.out.println("                          2) Exit");
				System.out.print("\n\n                    Insert Selection and Press Enter: ");
				flag = false;
				while(flag == false){
					flag = false;
					String choice = keyboard.next();
					
					switch (choice) {
						case "1":
						flag = true;
						break;
						case "2":
						return;
						default:
						//Unexpected output
					}
				}
			}
			else {
				System.out.println("\n\n           Class Roster: ");
				writeToServer("OPEN Roster"+crnInt+";");
				writeToServer("SHOW Roster"+crnInt+";");
				printTable(1, "UIN");
				System.out.println();
				System.out.println("           What action would you like to do next?");
				System.out.println();
				System.out.println("                          1) Search for another class");
				System.out.println("                          2) Update class information");
				System.out.println("                          3) Add Students to roster");
				System.out.println("                          4) Remove Students from roster");
				System.out.println("                          5) Delete class from record");
				System.out.println("                          6) Exit");
				System.out.print("\n\n                    Insert Selection and Press Enter: ");
				flag = false;
				while(flag == false){
					flag = false;
					String choice = keyboard.next();
					
					switch (choice) {
						case "1":
							flag = true;
							break;
						case "2":
							updateClassInfo(crnInt);
							return;
						case "3":
							addStudentsToClass(crnInt);
							return;
						case "4":
							removeStudentsFromClass(crnInt);
							return;
						case "5":
							writeToServer("DELETE FROM classes WHERE (-1==" + crnInt + ");");
							writeToServer("WRITE classes;");
							System.out.print("               Delete successful.  Press Enter to continue:  ");
							keyboard.nextLine();
							keyboard.nextLine();
							return;
						case "6":
							return;
						default:
						//Unexpected output
					}
				}
			}
		}
	}
	public static void addStudentsToClass(int crnInt) {
		while(true) {
			boolean flag = true;
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Add Students to Class\n\n");
			Scanner keyboard = new Scanner(System.in);
			boolean more_students = true;
			while(more_students) {
				System.out.print("           Enter UIN of student: ");
				String uin = "-12";
				int uinInt = -12;
				while(flag == true){
					flag = false;
					uin = keyboard.next();
					if(uin.isEmpty()){
						flag = true;
					}
					try{
						uinInt = Integer.parseInt(uin);
					}
					catch (NumberFormatException e){
						flag = true;
						System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
						continue;
					}
					if(uinInt < 0){
						flag = true;
						System.out.println("           Invalid Entry: Please enter a positive value ");
					}
					writeToServer("SHOW (select(-1==" + uinInt +") students);");
					LinkedList<String> rows = new LinkedList<String>(Arrays.asList(response.split("\\r?\\n")));
					rows.remove();
					rows.remove();
					if (rows.size() == 1) {
						flag = true;
						System.out.print("           Invalid Entry: UIN does not exist in student database ");
						keyboard.nextLine();
						keyboard.nextLine();
						return;
					}
				}
				flag = true;
				keyboard.nextLine();
				System.out.print("           Enter Grade of student: ");
				String grade = "-12";
				int gradeInt = -12;
				while(flag == true){
					flag = false;
					grade = keyboard.next();
					if(grade.isEmpty()){
						flag = true;
					}
					try{
						gradeInt = Integer.parseInt(grade);
					}
					catch (NumberFormatException e){
						flag = true;
						System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
						continue;
					}
					if(gradeInt < 0){
						flag = true;
						System.out.println("           Invalid Entry: Please enter a positive value ");
					}
				}
				keyboard.nextLine();
				System.out.println("           Would you like to keep the following entry? ");
				System.out.println("           UIN: " + uinInt + " | Grade: " + gradeInt);
				System.out.println("                          1) Yes, and Continue");
				System.out.println("                          2) Yes, and Exit");
				System.out.println("                          3) No, and Continue");
				System.out.println("                          4) No, and Exit");
				System.out.print("\n\n                    Enter Selection and Press Enter: ");
				String selection = keyboard.next();
				switch(selection) {
					case "1":
						writeToServer("INSERT INTO Roster"+crnInt+" VALUES FROM("+uinInt+", "+gradeInt+");");
						more_students = false;
						break;
					case "2":
						writeToServer("INSERT INTO Roster"+crnInt+" VALUES FROM("+uinInt+", "+gradeInt+");");
						writeToServer("WRITE Roster"+crnInt+";");
						more_students = false;
						return;
					case "3":
						more_students = false;
						break;
					case "4":
						more_students = false;
						return;
					default:
						//unexpected input
						return;
				}
			}

		}
	}
	
	public static void removeStudentsFromClass(int crnInt) {
		while (true) {
			boolean flag = true;
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Remove Students from Roster\n\n");
			Scanner keyboard = new Scanner(System.in);
			boolean more_students = true;
			while(more_students) {
				System.out.print("           Enter UIN of student: ");
				String uin = "-12";
				int uinInt = -12;
				while(flag == true){
					flag = false;
					uin = keyboard.next();
					if(uin.isEmpty()){
						flag = true;
					}
					try{
						uinInt = Integer.parseInt(uin);
					}
					catch (NumberFormatException e){
						flag = true;
						System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
						continue;
					}
					if(uinInt < 0){
						flag = true;
						System.out.println("           Invalid Entry: Please enter a positive value ");
					}
					writeToServer("SHOW (select(-1==" + uinInt +") students);");
					LinkedList<String> rows = new LinkedList<String>(Arrays.asList(response.split("\\r?\\n")));
					rows.remove();
					rows.remove();
					if (rows.size() == 1) {
						flag = true;
						System.out.print("           Invalid Entry: UIN does not exist in student database ");
						keyboard.nextLine();
						keyboard.nextLine();
						return;
					}
				}
				keyboard.nextLine();
				System.out.println("           Would you like to delete this student? ");
				System.out.println("           UIN: " + uinInt);
				System.out.println("                          1) Yes, and Continue");
				System.out.println("                          2) Yes, and Exit");
				System.out.println("                          3) No, and Continue");
				System.out.println("                          4) No, and Exit");
				System.out.print("\n\n                    Enter Selection and Press Enter: ");
				String selection = keyboard.next();
				switch(selection) {
					case "1":
						writeToServer("DELETE FROM Roster"+crnInt+" WHERE(-1 == "+uinInt+");");
						more_students = false;
						break;
					case "2":
						writeToServer("DELETE FROM Roster"+crnInt+" WHERE(-1 == "+uinInt+");");
						writeToServer("WRITE Roster"+crnInt+";");
						more_students = false;
						return;
					case "3":
						more_students = false;
						break;
					case "4":
						more_students = false;
						return;
					default:
						//unexpected input
						return;
				}
			}
		}
	}
	
	//***Level 5***
	public static void updateClassInfo(int crnInt) {
		while (true) {
			boolean flag = true;
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                          Update Class\n\n");
			Scanner keyboard = new Scanner(System.in);
			String newName = "";
			
			while(flag == true){
				flag = false;
				System.out.print("       Please enter the new class name and press Enter: ");
				newName = keyboard.nextLine();
				if(newName.isEmpty()){
					flag = true;
				}
			}
			flag = true;
			String newHours = "";
			int newHoursInt = -12;
			while(flag == true){
				flag = false;
				System.out.print("       Please enter the new class hours and press Enter: ");
				newHours = keyboard.next();
				if(newHours.isEmpty()){
					flag = true;
				}
				try{
					newHoursInt = Integer.parseInt(newHours);
				}
				catch (NumberFormatException e){
					flag = true;
					System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
				}
			}
			keyboard.nextLine();
			writeToServer("UPDATE classes SET (Name=\"" + newName + "\", Hours=" + newHoursInt + ") WHERE (-1==" + crnInt + ");");
			if (writeToServer("WRITE classes;")) {
				System.out.print("\n                  Update successful. Press Enter to continue: ");
				keyboard.nextLine();
				break;
			}
			System.out.print("\n                  Update error. Press Enter to try again: ");
			keyboard.nextLine();
		}
	}
	
	//***Level 4***
	public static void addClass(){
		while(true) {
			boolean flag = true;
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Add Class\n\n");
			Scanner keyboard = new Scanner(System.in);
			String crn = "-12";
			int crnInt = -12;
			while(flag == true){
				flag = false;
				System.out.print("           Please enter the class CRN and press Enter: ");
				crn = keyboard.next();
				if(crn.isEmpty()){
					flag = true;
				}
				try{
					crnInt = Integer.parseInt(crn);
				}
				catch (NumberFormatException e){
					flag = true;
					System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
					continue;
				}
				if(crnInt < 0){
					flag = true;
					System.out.println("           Invalid Entry: Please enter a positive value ");
				}
			}
			keyboard.nextLine();
			System.out.print("           Please enter the class Name and press Enter: ");
			String name = keyboard.nextLine();
			
			String hours = "-1";
			int intHours = -1;
			flag = true;
			while(flag == true){
				flag = false;
				System.out.print("           Please enter the class Hours and press Enter: ");
				hours = keyboard.next();
				if(hours.isEmpty()){
					flag = true;
				}
				try{
					intHours = Integer.parseInt(hours);
				}
				catch (NumberFormatException e){
					flag = true;
					System.out.println("           Invalid Entry: Please enter an integer value (EX:4) ");
					continue;
				}
				if(intHours < 0){
					flag = true;
					System.out.println("           Invalid Entry: Please enter a positive value ");
				}
			}
			System.out.println("           Would you like to keep the following entry? ");
			System.out.println("           CRN: " + crnInt + " | Name: " + name + " | Hours: " + hours);
			System.out.println("                          1) Yes");
			System.out.println("                          2) Yes, and add Students to Class");
			System.out.println("                          3) No and Exit");
			System.out.println("                          4) No and Try Again");
			System.out.print("\n\n                    Insert Selection and Press Enter: ");
			flag = false;
			while(flag == false){
				flag = false;
				keyboard.nextLine();
				String choice = keyboard.next();
				
				switch (choice) {
					case "1":
						writeToServer("INSERT INTO classes VALUES FROM (" + crnInt + ",\"" + name + "\",\"" + hours + "\");");
						if (response.equals("FAILURE!")) {
							System.out.print("         Insertion not successful. Press enter to try again: ");
							keyboard.nextLine();
							keyboard.nextLine();
							flag = true;
							break;
						}
						writeToServer("WRITE classes;");
						writeToServer("CREATE TABLE Roster"+crnInt+" (UIN INTEGER, Grade Integer) PRIMARY KEY (UIN);");
						writeToServer("WRITE Roster"+crnInt+";");
						System.out.print("                    Saved!  Press Enter to continue: ");
						keyboard.nextLine();
						keyboard.nextLine();
						return;
					case "2":
						writeToServer("INSERT INTO classes VALUES FROM (" + crnInt + ",\"" + name + "\",\"" + hours + "\");");
						if (response.equals("FAILURE!")) {
							System.out.print("         Insertion not successful. Press enter to try again: ");
							keyboard.nextLine();
							keyboard.nextLine();
							flag = true;
							break;
						}
						writeToServer("WRITE classes;");
						writeToServer("CREATE TABLE Roster"+crnInt+" (UIN INTEGER, Grade INTEGER) PRIMARY KEY (UIN);");
						writeToServer("WRITE Roster"+crnInt+";");
						addStudentsToClass(crnInt);
						return;
					case "3":
						return;
					case "4":
						flag = true;
						break;
					default:
						//Unexpected output
				}
			}
			
		}
	}
	
//***Level 4***
	public static void searchClass(){
		while(true) {
			System.out.print("\033[H\033[2J");
			System.out.flush();
			System.out.println();
			System.out.println("");
			System.out.println("                             Search Class\n\n");
			System.out.println("           How would you like to display class data?");
			System.out.println("                          1) Display all");
			System.out.println("                          2) Display based on name");
			System.out.println("                          3) Display based on hours");
			System.out.println("                          4) Display based on CRN");
			System.out.println("                          5) Display passing students in a class");
			System.out.println("                          6) Display failing students in a class");
			System.out.println("                          7) Back");
			System.out.print("\n\n                    Insert Selection and Press Enter: ");
			Scanner keyboard = new Scanner(System.in);
			String choice = keyboard.next();
			switch (choice) {
					case "1":
					displayAllClasses();
					return;
					case "2":
					displayNameClasses();
					return;
					case "3":
					displayHoursClasses();
					break;
					case "4":
					displayCrnClasses();
					break;
					case "5":
					displayPassStudents();
					break;
					case "6":
					displayFailStudents();
					break;
					case "7":
					return;
					default:
					//Unexpected output
			}
		}
	}
	
	//***Level 5***
	public static void displayAllClasses() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display All Classes\n\n");
		writeToServer("SHOW classes;");
		printTable(1, "CRN");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}
	
	//***Level 5***
	public static void displayNameClasses() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display by Name\n\n");
		System.out.print("              Please enter the name of the class: ");
		String name = keyboard.nextLine();
		writeToServer("SHOW (select (Name==\"" + name + "\") classes);");
		printTable(1, "CRN");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}
	
	//***Level 5***
	public static void displayHoursClasses() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display by Hours\n\n");
		String hours = "-1";
		int intHours = -1;
		boolean flag = true;
		while(flag == true){
			flag = false;
			System.out.print("           Please enter the class Hours and press Enter: ");
			hours = keyboard.next();
			if(hours.isEmpty()){
				flag = true;
			}
			try{
				intHours = Integer.parseInt(hours);
			}
			catch (NumberFormatException e){
				flag = true;
				System.out.println("           Invalid Entry: Please enter an integer value (EX:4) ");
				continue;
			}
			if(intHours < 0){
				flag = true;
				System.out.println("           Invalid Entry: Please enter a positive value ");
			}
		}
		keyboard.nextLine();
		writeToServer("SHOW (select (Hours==" + intHours + ") classes);");
		printTable(1, "CRN");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}
	
	//***Level 5***
	public static void displayCrnClasses() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display by CRN\n\n");
		String crn = "-12";
		int crnInt = -12;
		boolean flag = true;
		while(flag == true){
			flag = false;
			System.out.print("           Please enter the class CRN and press Enter: ");
			crn = keyboard.next();
			if(crn.isEmpty()){
				flag = true;
			}
			try{
				crnInt = Integer.parseInt(crn);
			}
			catch (NumberFormatException e){
				flag = true;
				System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
				continue;
			}
			if(crnInt < 0){
				flag = true;
				System.out.println("           Invalid Entry: Please enter a positive value ");
			}
		}
		keyboard.nextLine();
		writeToServer("SHOW (select (-1==" + crnInt + ") classes);");
		printTable(1, "CRN");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}
	
	//***Level 5***
	public static void displayPassStudents() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display Passing Students\n\n");
		String crn = "-12";
		int crnInt = -12;
		boolean flag = true;
		while(flag == true){
			flag = false;
			System.out.print("           Please enter the class CRN and press Enter: ");
			crn = keyboard.next();
			if(crn.isEmpty()){
				flag = true;
			}
			try{
				crnInt = Integer.parseInt(crn);
			}
			catch (NumberFormatException e){
				flag = true;
				System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
				continue;
			}
			if(crnInt < 0){
				flag = true;
				System.out.println("           Invalid Entry: Please enter a positive value ");
			}
		}
		keyboard.nextLine();
		writeToServer("OPEN Roster"+crnInt+";");
		writeToServer("SHOW (Roster"+crnInt+" - (select (Grade<70) Roster"+crnInt+"));");
		printTable(1, "UIN");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}
	
		//***Level 5***
	public static void displayFailStudents() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
		Scanner keyboard = new Scanner(System.in);
		System.out.println();
		System.out.println("");
		System.out.println("                             Display Passing Students\n\n");
		String crn = "-12";
		int crnInt = -12;
		boolean flag = true;
		while(flag == true){
			flag = false;
			System.out.print("           Please enter the class CRN and press Enter: ");
			crn = keyboard.next();
			if(crn.isEmpty()){
				flag = true;
			}
			try{
				crnInt = Integer.parseInt(crn);
			}
			catch (NumberFormatException e){
				flag = true;
				System.out.println("           Invalid Entry: Please enter an integer value (EX:822007959) ");
				continue;
			}
			if(crnInt < 0){
				flag = true;
				System.out.println("           Invalid Entry: Please enter a positive value ");
			}
		}
		keyboard.nextLine();
		writeToServer("OPEN Roster"+crnInt+";");
		writeToServer("SHOW (Roster"+crnInt+" - (select (Grade>=70) Roster"+crnInt+"));");
		printTable(1, "UIN");
		System.out.print("\n                          Press Enter to go back: ");
		keyboard.nextLine();
	}
	
	
//***Exit Level***
	public static void exitWindow(){
		System.out.print("\033[H\033[2J");
		System.out.flush();
		System.out.println();
		System.out.println();
		System.out.println("         Thank you for using the TAMU Database Management System!\n\n");
		System.out.println("                    Developer(s)     |     Team gitRekt");
		System.out.println("                         Version     |     1.0");
		System.out.println("                  Stable Release     |     October 2016");
		System.out.println("              Development Status     |     Active");
		System.out.println("                      Written In     |     Java\n\n");
		System.out.println("                       	      Have a Nice Day! \n");
		System.exit(0);
		}
}
