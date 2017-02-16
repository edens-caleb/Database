import org.junit.*;
import java.util.*;

// Run by running Main.java

//javac -cp .:junit-4.12.jar unitTest.java
//java -cp .:junit-4.12.jar:hamcrest-core-1.3.jar Main

public class unitTest {	
	
	@Ignore
	public Table initializeStudents() {
		Table students = new Table("students");
		
		students.addEntity(10101);
		students.addEntity(10102);
		
		students.addAttribute(-1, "Name");
		students.addAttribute(-1, "Major");
		students.addAttribute(-1, "Graduating Year");
		students.addAttribute(-1, "Advisor");
		students.addAttribute(-1, "Favorite Language");
		
		students.addAttribute(10101, "Kameron Goodman");
		students.addAttribute(10101, "Computer Engineering");
		students.addAttribute(10101, "2017");
		students.addAttribute(10101, "Chuck Chucker");
		students.addAttribute(10101, "German");
		
		students.addAttribute(10102, "Mike Wazowski");
		students.addAttribute(10102, "Scare");
		students.addAttribute(10102, "2013");
		students.addAttribute(10102, "Dr. Hardscrabble");
		students.addAttribute(10102, "Rawr");
		
		return students;
	}
	
	@Ignore
	public Table initializeStudents2() {
		Table students2 = new Table("students2");
		
		students2.addEntity(10103);
		students2.addEntity(10104);
		
		students2.addAttribute(-1, "Name");
		students2.addAttribute(-1, "Major");
		students2.addAttribute(-1, "Graduating Year");
		students2.addAttribute(-1, "Advisor");
		students2.addAttribute(-1, "Favorite Language");
		
		students2.addAttribute(10103, "Caleb Edens");
		students2.addAttribute(10103, "Computer Science");
		students2.addAttribute(10103, "2018");
		students2.addAttribute(10103, "Bradley Hopps");
		students2.addAttribute(10103, "Spanish");

		students2.addAttribute(10104, "Jacob Hostler");
		students2.addAttribute(10104, "Computer Science");
		students2.addAttribute(10104, "2018");
		students2.addAttribute(10104, "Barack Hussain");
		students2.addAttribute(10104, "Pig Latin");
		
		return students2;
	}
	
	@Test
	public void testCreateTable() {
		Engine testEngine = new Engine();
		testEngine.clearEngine();
		Table t = new Table("animals");
		
		testEngine.createTable("animals", new ArrayList<String>(Arrays.asList("name", "kind")), false);
		
		boolean flag1 = false;
		
		t.addAttribute(-1, "name");
		t.addAttribute(-1, "kind");
		
		Assert.assertEquals("Create Table Failed: ", t, testEngine.getTable(1));
	}
	
	@Test
	public void testDropTable() {
		Engine testEngine = new Engine();
		testEngine.clearEngine();
		testEngine.createTable("name", new ArrayList<String>(), false);
		testEngine.createTable("hello", new ArrayList<String>(), false);
		testEngine.dropTable(1);
		
		boolean flag1 = false;
		boolean flag2 = false;
		boolean flag3 = false;
		boolean flag4 = false;
		
		if (!testEngine.containsTable(1))
			flag1 = true;
		if (testEngine.containsTable(2))
			flag2 = true;
		if (testEngine.numOfTables() == 1)
			flag3 = true;
		if (testEngine.keyFinder("name") == -1) 
			flag4 = true;
		
		Assert.assertEquals("Drop Table Failed: ", true, flag1 && flag2 && flag3 && flag4);
	}
	
	@Test
	public void testInsertRecord() {
		Engine testEngine = new Engine();
		testEngine.clearEngine();
		testEngine.createTable("name", new ArrayList<String>(Arrays.asList("Name", "Major", "Graduating Year", "Advisor")), false);
		Table result = testEngine.getTable(1);
		Table answer = new Table("name");
		
		answer.addAttribute(-1, "Name");
		answer.addAttribute(-1, "Major");
		answer.addAttribute(-1, "Graduating Year");
		answer.addAttribute(-1, "Advisor");
		
		answer.addEntity(10101);
		
		answer.addAttribute(10101, "Neil Marklund");
		answer.addAttribute(10101, "Computer Science");
		answer.addAttribute(10101, "2018");
		answer.addAttribute(10101, "Dr. Furuta");
		
		testEngine.insertRecord("name", 10101, new ArrayList<String>(Arrays.asList("Neil Marklund", "Computer Science", "2018", "Dr. Furuta")));	
					
		Assert.assertEquals("Insert Failed: ", answer, result);
	}
	
	@Test
	public void testUpdate() {
		Engine testEngine = new Engine();
		testEngine.clearEngine();
		testEngine.createTable("name", new ArrayList<String>(), false);
		Table students = testEngine.getTable(1);

		testEngine.insertRecord(1, -1, "Name", "Name");
		testEngine.insertRecord(1, -1, "Major", "Major");
		testEngine.insertRecord(1, -1, "Graduating Year", "Graduating Year");
		testEngine.insertRecord(1, -1, "Advisor", "Advisor");
		testEngine.insertRecord(1, -1, "Favorite Language", "Favorite Language");

		testEngine.insertRecord(1, 10101, "Name", "Neil Marklund");
		testEngine.insertRecord(1, 10101, "Major", "Computer Science");
		testEngine.insertRecord(1, 10101, "Graduating Year", "2018");
		testEngine.insertRecord(1, 10101, "Advisor", "Dr. Furuta");
		testEngine.insertRecord(1, 10101, "Favorite Language", "C++");
		
		Table answer = new Table("name");
		answer.addAttribute(-1, "Name");
		answer.addAttribute(-1, "Major");
		answer.addAttribute(-1, "Graduating Year");
		answer.addAttribute(-1, "Advisor");
		answer.addAttribute(-1, "Favorite Language");
		answer.addEntity(10101);
		answer.addAttribute(10101, "Neil Marklund");
		answer.addAttribute(10101, "History");
		answer.addAttribute(10101, "2019");
		answer.addAttribute(10101, "Dr. Furuta");
		answer.addAttribute(10101, "C++");
		
		testEngine.updateRecord("name", new ArrayList<String>(Arrays.asList("Major", "Graduating Year")), new ArrayList<String>(Arrays.asList("History", "2019")),
								new Condition(new Conjunction(new Comparison("Name", "==", "Neil Marklund"))));
		
		Assert.assertEquals("Update Failed: ", answer, students);
	}
	
	@Test
	public void testDelete() {
		Engine testEngine = new Engine();
		testEngine.clearEngine();
		testEngine.createTable("name", new ArrayList<String>(), false);
		Table students = testEngine.getTable(1);

		testEngine.insertRecord(1, -1, "Name", "Name");
		testEngine.insertRecord(1, -1, "Major", "Major");
		testEngine.insertRecord(1, -1, "Graduating Year", "Graduating Year");
		testEngine.insertRecord(1, -1, "Advisor", "Advisor");
		testEngine.insertRecord(1, -1, "Favorite Language", "Favorite Language");

		testEngine.insertRecord(1, 10101, "Name", "Neil Marklund");
		testEngine.insertRecord(1, 10101, "Major", "Computer Science");
		testEngine.insertRecord(1, 10101, "Graduating Year", "2018");
		testEngine.insertRecord(1, 10101, "Advisor", "Dr. Furuta");
		testEngine.insertRecord(1, 10101, "Favorite Language", "C++");
		
		Table answer = new Table("name");
		answer.addAttribute(-1, "Name");
		answer.addAttribute(-1, "Major");
		answer.addAttribute(-1, "Graduating Year");
		answer.addAttribute(-1, "Advisor");
		answer.addAttribute(-1, "Favorite Language");
		
		testEngine.deleteRecord("name", new Condition(new Conjunction(new Comparison("Major", "==", "Computer Science"))));
		Assert.assertEquals("Delete Failed: ", answer, testEngine.getTable(1));

	}
		
	@Test
	public void testSelection() {
		Engine testEngine = new Engine();
		Table students = initializeStudents();
		
		//Major == Scare
		Table result = testEngine.select(students, new Condition(new Conjunction(new Comparison("Major", "==", "Scare"))));
		
		Table answer = new Table("tempName");
		
		answer.addEntity(10102);
		
		answer.addAttribute(-1, "Name");
		answer.addAttribute(-1, "Major");
		answer.addAttribute(-1, "Graduating Year");
		answer.addAttribute(-1, "Advisor");
		answer.addAttribute(-1, "Favorite Language");
		
		answer.addAttribute(10102, "Mike Wazowski");
		answer.addAttribute(10102, "Scare");
		answer.addAttribute(10102, "2013");
		answer.addAttribute(10102, "Dr. Hardscrabble");
		answer.addAttribute(10102, "Rawr");
		
		Assert.assertEquals("Select 1 Failed:  ", answer, result);
		
		answer = new Table(students, "tempName");
		
		//Graduating Year == 2017 || Advisor == Dr. Hardscrabble
		result = 	testEngine.select(students, new Condition(new Conjunction(new Comparison("Graduating Year", "==", "2017")),
					new Conjunction(new Comparison("Advisor", "==", "Dr. Hardscrabble"))));
		
		Assert.assertEquals("Select 2 Failed:  ", answer, result);
	}
	
	@Test
	public void testProjection() {
		Engine testEngine = new Engine();
		Table students = initializeStudents();
		
		Table result = testEngine.projection(students, new ArrayList<String>(Arrays.asList("Major")));
		Table answer = new Table("student");
		answer.addAttribute(-1, "Major");
		
		answer.addEntity(10101);
		answer.addAttribute(10101, "Computer Engineering");
		
		answer.addEntity(10102);
		answer.addAttribute(10102, "Scare");

		Assert.assertEquals("Projection Failed: ", answer, result);
	}
	
	@Test
	public void testRenaming() {
		Engine testEngine = new Engine();
		testEngine.clearEngine();
		testEngine.createTable("name", new ArrayList<String>(Arrays.asList("Name", "Major", "Graduating Year", "Advisor", "Favorite Language")), false);
		Table result = testEngine.getTable(1);
		Table answer = new Table("name");
		
		answer.addAttribute(-1, "Full Name");
		answer.addAttribute(-1, "Hobby");
		answer.addAttribute(-1, "Birth Year");
		answer.addAttribute(-1, "Friend");
		answer.addAttribute(-1, "Misc.");
		
		testEngine.rename(result, new ArrayList<String>(Arrays.asList("Full Name", "Hobby", "Birth Year", "Friend", "Misc.")));

		Assert.assertEquals("Rename Failed: ", answer, result);
	}
	
	@Test
	public void testSetUnion() {
		Engine testEngine = new Engine();
		Table t1 = initializeStudents();
		Table t2 = initializeStudents2();
		Table answer = new Table("Union");
		
		answer.addEntity(10101);
		answer.addEntity(10102);
		answer.addEntity(10103);
		answer.addEntity(10104);

		answer.addAttribute(-1, "Name");
		answer.addAttribute(-1, "Major");
		answer.addAttribute(-1, "Graduating Year");
		answer.addAttribute(-1, "Advisor");
		answer.addAttribute(-1, "Favorite Language");
		
		answer.addAttribute(10101, "Kameron Goodman");
		answer.addAttribute(10101, "Computer Engineering");
		answer.addAttribute(10101, "2017");
		answer.addAttribute(10101, "Chuck Chucker");
		answer.addAttribute(10101, "German");
		
		answer.addAttribute(10102, "Mike Wazowski");
		answer.addAttribute(10102, "Scare");
		answer.addAttribute(10102, "2013");
		answer.addAttribute(10102, "Dr. Hardscrabble");
		answer.addAttribute(10102, "Rawr");

		answer.addAttribute(10103, "Caleb Edens");
		answer.addAttribute(10103, "Computer Science");
		answer.addAttribute(10103, "2018");
		answer.addAttribute(10103, "Bradley Hopps");
		answer.addAttribute(10103, "Spanish");

		answer.addAttribute(10104, "Jacob Hostler");
		answer.addAttribute(10104, "Computer Science");
		answer.addAttribute(10104, "2018");
		answer.addAttribute(10104, "Barack Hussain");
		answer.addAttribute(10104, "Pig Latin");
		
		Table result = testEngine.setUnion(t1, t2);

		Assert.assertEquals("Set Union Failed: ", answer, result);
	}
	
	@Test
	public void testSetDifference() {
		Engine testEngine = new Engine();
		Table t1 = initializeStudents();
		Table t2 = new Table(t1, "students");
		
		t2.addEntity(10103);
		t2.addAttribute(10103, "Saul Navarro");
		t2.addAttribute(10103, "History");
		t2.addAttribute(10103, "2020");
		t2.addAttribute(10103, "Darth Vader");
		t2.addAttribute(10103, "French");
				
		Table result = testEngine.setDifference(t2, t1);
		
		Table answer = new Table("Difference");
		answer.addAttribute(-1, "Name");
		answer.addAttribute(-1, "Major");
		answer.addAttribute(-1, "Graduating Year");
		answer.addAttribute(-1, "Advisor");
		answer.addAttribute(-1, "Favorite Language");
		
		answer.addEntity(10103);
		answer.addAttribute(10103, "Saul Navarro");
		answer.addAttribute(10103, "History");
		answer.addAttribute(10103, "2020");
		answer.addAttribute(10103, "Darth Vader");
		answer.addAttribute(10103, "French");
		
		Assert.assertEquals("Set Difference Failed: ", answer, result);
	}
	
	@Test
	public void testCrossProduct() {
		Engine testEngine = new Engine();
		
		Table testTable1 = new Table("one");
		Table testTable2 = new Table("two");
		Table testTable3 = new Table("Cross");
		
		testTable1.addEntity(1);
		testTable1.addEntity(2);
		
		testTable1.addAttribute(-1, "Name");
		testTable1.addAttribute(1, "Joe");
		testTable1.addAttribute(2, "Bill");
		
		testTable2.addEntity(1);
		testTable2.addEntity(2);
		
		testTable2.addAttribute(-1, "Major");
		testTable2.addAttribute(1, "Math");
		testTable2.addAttribute(2, "Science");
		
		testTable3.addEntity(0);
		testTable3.addEntity(1);
		testTable3.addEntity(2);
		testTable3.addEntity(3);
		
		testTable3.addAttribute(-1, "Name");
		testTable3.addAttribute(-1, "Major");
		
		testTable3.addAttribute(0, "Joe");
		testTable3.addAttribute(0, "Math");
		testTable3.addAttribute(1, "Joe");
		testTable3.addAttribute(1, "Science");
		
		testTable3.addAttribute(2, "Bill");
		testTable3.addAttribute(2, "Math");
		testTable3.addAttribute(3, "Bill");
		testTable3.addAttribute(3, "Science");
		
		Table answer = testTable3;

		Assert.assertEquals("Cross Product Failed: ", testEngine.crossProduct(testTable1, testTable2), answer);
	}
	
	@Test
	public void testNaturalJoin() {
		Engine testEngine = new Engine();
		testEngine.clearEngine();
		testEngine.createTable("name", new ArrayList<String>(), false);
		Table t1 = initializeStudents();
		Table t2 = testEngine.getTable(1);
		
		testEngine.insertRecord(1, -1, "Name", "Name");
		testEngine.insertRecord(1, -1, "GPA", "GPA");
		
		testEngine.insertRecord(1, 10101, "Name", "Kameron Goodman");
		testEngine.insertRecord(1, 10101, "GPA", "3.48");
		
		testEngine.insertRecord(1, 10102, "Name", "Mike Wazowski");
		testEngine.insertRecord(1, 10102, "GPA", "3.95");
		testEngine.insertRecord(1, 10102, "Credits", "85");

		Table result = testEngine.naturalJoin(t1, t2);
				
		Table answer = new Table("Natural Join");

		answer.addAttribute(-1, "Name");
		answer.addAttribute(-1, "Major");
		answer.addAttribute(-1, "Graduating Year");
		answer.addAttribute(-1, "Advisor");
		answer.addAttribute(-1, "Favorite Language");
		answer.addAttribute(-1, "GPA");
		answer.addAttribute(-1, "Credits");
				
		answer.addEntity(10101);
		
		answer.addAttribute(10101, "Kameron Goodman");
		answer.addAttribute(10101, "Computer Engineering");
		answer.addAttribute(10101, "2017");
		answer.addAttribute(10101, "Chuck Chucker");
		answer.addAttribute(10101, "German");
		answer.addAttribute(10101, "3.48");
		answer.addAttribute(10101, "N/A");
		
		answer.addEntity(10102);
		
		answer.addAttribute(10102, "Mike Wazowski");
		answer.addAttribute(10102, "Scare");
		answer.addAttribute(10102, "2013");
		answer.addAttribute(10102, "Dr. Hardscrabble");
		answer.addAttribute(10102, "Rawr");
		answer.addAttribute(10102, "3.95");
		answer.addAttribute(10102, "85");
		
		Assert.assertEquals("Natural Join failed: ", answer, result);
	}
	
}
