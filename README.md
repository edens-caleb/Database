# gitRektP1D4
* Jacob Hostler
* Neil Markund
* Caleb Edens
* Kameron Goodman

RDBMS Deliverable 4

Under the "More" Tab drop down, there are links to the Development Log and Final Report.

The full API of engine and it's helper classes are located in the gitHub wiki of this repository. These wiki pages give a brief explanation of the classes as well as exlaining the use of every function within them. 

Instructions for sockets:
1.  Open two separate puTTY windows both connected to compute.cse.tamu.edu 
2.  On one, run Server.java using "java -cp .:junit-4.12.jar:hamcrest-core-1.3.jar Server" in the rdbms folder (This MUST be done first for the socket to connect properly!)
3.  On the other, run Gradebook.java using "java Gradebook".
4.  Type commands on Gradebook and you should see output appear on your screen.
5.  Type "EXIT;" on Gradebook to close both programs.


HOW TO COMPILE SERVER:
* javac -cp .:junit-4.12.jar *.java

HOW TO RUN:
* java -cp .:junit-4.12.jar:hamcrest-core-1.3.jar Server <(optional) portNumber>
* Note: If address in use error then pass integer argument after the above command. (Default is 36,000)

HOW TO COMPILE INTERACTIVE SYSTEM
* javac Gradebook.java

HOW TO RUN:
* java Gradebook <(optional) portNumber>
* Note: port has to be the same as the server. Pass integer argument after the above command if you get address in use error. (Default is 36,000) 

