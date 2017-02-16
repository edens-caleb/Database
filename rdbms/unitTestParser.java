import org.junit.*;
import java.util.*;


public class unitTestParser extends Parser {
	
	@Test
	public void testTokenizing() {
		Parser p = new Parser(new Engine());
		String s = "  INSERT   INTO  students   VALUES FROM(10101, \"Joe Smith\",\"Computer Science\",    \"2018\");   ";
		ArrayList<String> answer = new ArrayList<String>(Arrays.asList("INSERT", "INTO", "students", "VALUES", "FROM", "(10101,\"Joe Smith\",\"Computer Science\",\"2018\")", ";"));
		
		Assert.assertEquals("Tokenizing 1 Failed:  ", answer, p.tokenize(s));
		
		s = "CREATE TABLE species(   kind VARCHAR( 10   )   )PRIMARY KEY (kind); # Hello There";
		answer = new ArrayList<String>(Arrays.asList("CREATE", "TABLE", "species", "(kind VARCHAR(10))", "PRIMARY", "KEY", "(kind)", ";"));
		
		Assert.assertEquals("Tokenizing 2 Failed:  ", answer, p.tokenize(s));
		
		s = "a<-rename(aname, akind)(project (  name, kind  )   animals) ;";
		answer = new ArrayList<String>(Arrays.asList("a", "<-", "rename(aname,akind)(project(name,kind)animals)", ";"));
		
		Assert.assertEquals("Tokenizing 3 Failed:  ", answer, p.tokenize(s));
	}
	
	@Test
	public void testParseOpen() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("table1", new ArrayList<String>(Arrays.asList("name", "kind")), false);
		e.saveTable("table1");		//save Table
		e.dropTable(1);
		
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("table1"));
		
		Assert.assertEquals("parseOpen 1 Failed:  ", new Table(""), p.parseOpen(args));
		
		e.createTable("table2", new ArrayList<String>(Arrays.asList("UIN", "Name")), false);
		e.saveTable("table2");		//save Table
		e.dropTable(2);
		String command = "OPEN table2;";
		
		Assert.assertEquals("parseOpen 2 Failed:  ", new Table(""), p.parse(command));
		
		//tear down
		e.deleteSaveFile("table1");
		e.deleteSaveFile("table2");
	}
	
	@Test
	public void testParseClose() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("table", new ArrayList<String>(Arrays.asList("name", "kind")), false);
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("table"));
		
		Assert.assertEquals("parseClose 1 Failed:  ", new Table(""), p.parseClose(args));
		
		e.createTable("table", new ArrayList<String>(Arrays.asList("name", "kind")), false);
		String command = "CLOSE table;";
		
		Assert.assertEquals("parseClose 2 Failed:  ", new Table(""), p.parse(command));
	}
	
	@Test
	public void testParseWrite() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("table3", new ArrayList<String>(Arrays.asList("att1")), false);
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("table3"));

		Assert.assertEquals("parseWrite 1 Failed:  ", new Table(""), p.parseWrite(args));
		
		e.createTable("table4", new ArrayList<String>(Arrays.asList("name", "kind")), false);
		String command = "WRITE table4;";
		
		Assert.assertEquals("parseWrite 2 Failed:  ", new Table(""), p.parse(command));
		
		//tear down
		e.deleteSaveFile("table3");
		e.deleteSaveFile("table4");
	}
	
	@Test
	public void testCreateTable() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("species", "(kind VARCHAR(10),name VARCHAR(20))", "PRIMARY", "KEY", "(kind)"));		
		
		Assert.assertEquals("Create 1 Failed:  ", new Table(""), p.parseCreateTable(args));
	}
	
	@Test
	public void testInsert() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("students", new ArrayList<String>(Arrays.asList("Name", "Major", "2018")), false);
		ArrayList<String> args = new ArrayList<String>(Arrays.asList("students", "VALUES", "FROM", "(10101,\"Joe Smith\",\"Computer Science\",\"2018\")"));
		
		Assert.assertEquals("Insert 1 Failed:  ", new Table(""), p.parseInsert(args));
		e.insertRecord("students", 10102, new ArrayList<String>(Arrays.asList("John Doe", "History", "2010")));
		
		e.createTable("majors", new ArrayList<String>(Arrays.asList("Major")), true);
		String command = "INSERT INTO majors VALUES FROM RELATION (project (Major) students);";
		Assert.assertEquals("Insert 2 Failed:  ", new Table(""), p.parse(command));
		
		e.createTable("students2", new ArrayList<String>(Arrays.asList("Name", "Major", "2018")), false);
		command = "INSERT INTO students2 VALUES FROM RELATION students;";
		Assert.assertEquals("Insert 3 Failed:  ", new Table(""), p.parse(command));
	}

	@Test
	public void testExit() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("table5", new ArrayList<String>(Arrays.asList("Name", "Major")), true);
		e.createTable("table6", new ArrayList<String>(Arrays.asList("Name", "Major")), false);
		e.createTable("table7", new ArrayList<String>(Arrays.asList("Name", "Major")), false);
		e.saveTable("table7");
		
		String command = "EXIT;";
		
		Assert.assertEquals( "Exit 1 Failed:  ", new Table(""), p.parse(command));
		
		//tear down
		e.deleteSaveFile("table5");
		e.deleteSaveFile("table7");
	}
	
	@Test
	public void testProject() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("table8", new ArrayList<String>(Arrays.asList("Name", "Major", "GPA")), false);
		e.insertRecord("table8", 1, new ArrayList<String>(Arrays.asList("Billy", "History", "3.5")));
		e.insertRecord("table8", 2, new ArrayList<String>(Arrays.asList("Jill", "Art", "3.7")));
		e.insertRecord("table8", 3, new ArrayList<String>(Arrays.asList("Nick", "Computer Science", "2.5")));
		
		Table answer = new Table("");
		answer.addAttribute(-1, "Name");
		answer.addEntity(1);
		answer.addEntity(2);
		answer.addEntity(3);
		
		answer.addAttribute(1, "Billy");
		answer.addAttribute(2, "Jill");
		answer.addAttribute(3, "Nick");
		
		Assert.assertEquals( "Project 1 Failed:  ", answer, p.parse("a <- project(Name) (project (Name, Major) table8);"));
	}
	
	@Test
	public void testRename() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("table8", new ArrayList<String>(Arrays.asList("Name", "Major", "GPA")), false);
		e.insertRecord("table8", 1, new ArrayList<String>(Arrays.asList("Billy", "History", "3.5")));
		e.insertRecord("table8", 2, new ArrayList<String>(Arrays.asList("Jill", "Art", "3.7")));
		e.insertRecord("table8", 3, new ArrayList<String>(Arrays.asList("Nick", "Computer Science", "2.5")));
		
		Table answer = new Table("");
		answer.addAttribute(-1, "Full Name");
		answer.addAttribute(-1, "Major");
		answer.addEntity(1);
		answer.addEntity(2);
		answer.addEntity(3);
		
		answer.addAttribute(1, "Billy");
		answer.addAttribute(1, "History");
		answer.addAttribute(2, "Jill");
		answer.addAttribute(2, "Art");
		answer.addAttribute(3, "Nick");
		answer.addAttribute(3, "Computer Science");
		
		Assert.assertEquals( "Rename 1 Failed:  ", answer, p.parse("a <- rename(Full Name, Major) (project (Name, Major) table8);"));
	}
	
	@Test
	public void testUnion() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("table8", new ArrayList<String>(Arrays.asList("Name", "Major", "GPA")), false);
		e.insertRecord("table8", 1, new ArrayList<String>(Arrays.asList("Billy", "History", "3.5")));
		e.insertRecord("table8", 2, new ArrayList<String>(Arrays.asList("Jill", "Art", "3.7")));
		e.insertRecord("table8", 3, new ArrayList<String>(Arrays.asList("Nick", "Computer Science", "2.5")));
		
		e.createTable("table9", new ArrayList<String>(Arrays.asList("Name", "Major", "GPA")), false);
		e.insertRecord("table9", 4, new ArrayList<String>(Arrays.asList("Kameron", "Computer Engineering", "4.0")));
		e.insertRecord("table9", 5, new ArrayList<String>(Arrays.asList("Neil", "Business", "3.25")));
		e.insertRecord("table9", 6, new ArrayList<String>(Arrays.asList("Jacob", "Computer Science", "4.0")));
		
		Table answer = new Table("");
		answer.hmap.put(-1, new ArrayList<String>(Arrays.asList("Name", "Major", "GPA")));
		answer.hmap.put(1, new ArrayList<String>(Arrays.asList("Billy", "History", "3.5")));
		answer.hmap.put(2, new ArrayList<String>(Arrays.asList("Jill", "Art", "3.7")));
		answer.hmap.put(3, new ArrayList<String>(Arrays.asList("Nick", "Computer Science", "2.5")));
		answer.hmap.put(4, new ArrayList<String>(Arrays.asList("Kameron", "Computer Engineering", "4.0")));
		answer.hmap.put(5, new ArrayList<String>(Arrays.asList("Neil", "Business", "3.25")));
		answer.hmap.put(6, new ArrayList<String>(Arrays.asList("Jacob", "Computer Science", "4.0")));
		
		Assert.assertEquals( "Union 1 Failed:  ", answer, p.parse("a <- table8 + table9;"));
	}
	
	@Test
	public void testDifference() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("table8", new ArrayList<String>(Arrays.asList("Name", "Major", "GPA")), false);
		e.insertRecord("table8", 1, new ArrayList<String>(Arrays.asList("Billy", "History", "3.5")));
		e.insertRecord("table8", 2, new ArrayList<String>(Arrays.asList("Jill", "Art", "3.7")));
		e.insertRecord("table8", 3, new ArrayList<String>(Arrays.asList("Nick", "Computer Science", "2.5")));
		
		e.createTable("table9", new ArrayList<String>(Arrays.asList("Name", "Major", "GPA")), false);
		e.insertRecord("table9", 1, new ArrayList<String>(Arrays.asList("Billy", "History", "3.5")));
		e.insertRecord("table9", 2, new ArrayList<String>(Arrays.asList("Jill", "Art", "3.7")));
		
		Table answer = new Table("");
		answer.addAttribute(-1, "Name");
		answer.addAttribute(-1, "Major");
		answer.addAttribute(-1, "GPA");
		
		answer.addEntity(3);
		answer.addAttribute(3, "Nick");
		answer.addAttribute(3, "Computer Science");
		answer.addAttribute(3, "2.5");
				
		Assert.assertEquals( "Difference 1 Failed:  ", answer, p.parse("a <- table8 - table9;"));
	}
	
	@Test
	public void testProduct() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("table8", new ArrayList<String>(Arrays.asList("Name")), false);
		e.insertRecord("table8", 1, new ArrayList<String>(Arrays.asList("Billy")));
		e.insertRecord("table8", 2, new ArrayList<String>(Arrays.asList("Jill")));
		
		e.createTable("table9", new ArrayList<String>(Arrays.asList("Name", "Major", "GPA")), false);
		e.insertRecord("table9", 1, new ArrayList<String>(Arrays.asList("Billy", "History", "3.5")));
		e.insertRecord("table9", 2, new ArrayList<String>(Arrays.asList("Jill", "Art", "3.7")));
		
		Table answer = new Table("");
		answer.hmap.put(-1, new ArrayList<String>(Arrays.asList("Name", "Major")));
		answer.hmap.put(0, new ArrayList<String>(Arrays.asList("Billy", "History")));
		answer.hmap.put(1, new ArrayList<String>(Arrays.asList("Billy", "Art")));
		answer.hmap.put(2, new ArrayList<String>(Arrays.asList("Jill", "History")));
		answer.hmap.put(3, new ArrayList<String>(Arrays.asList("Jill", "Art")));
				
		Assert.assertEquals( "Product 1 Failed:  ", answer, p.parse("a <- table8 * (project (Major) table9);"));
	}
	
	@Test
	public void testJoin() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("table8", new ArrayList<String>(Arrays.asList("Name", "Major", "GPA")), false);
		e.insertRecord("table8", 1, new ArrayList<String>(Arrays.asList("Billy", "History", "3.5")));
		e.insertRecord("table8", 2, new ArrayList<String>(Arrays.asList("Jill", "Art", "3.7")));
		e.insertRecord("table8", 3, new ArrayList<String>(Arrays.asList("Nick", "History", "2.5")));
		
		e.createTable("table9", new ArrayList<String>(Arrays.asList("Name", "Advisor")), false);
		e.insertRecord("table9", 1, new ArrayList<String>(Arrays.asList("Billy", "Dr. Furuta")));
		e.insertRecord("table9", 2, new ArrayList<String>(Arrays.asList("Jill", "Dr. Smith")));
		e.insertRecord("table9", 3, new ArrayList<String>(Arrays.asList("Nick", "Dr. Jones")));
		
		Table answer = new Table("");
		answer.hmap.put(-1, new ArrayList<String>(Arrays.asList("Name", "Major", "GPA", "Advisor")));
		answer.hmap.put(1, new ArrayList<String>(Arrays.asList("Billy", "History", "3.5", "Dr. Furuta")));
		answer.hmap.put(2, new ArrayList<String>(Arrays.asList("Jill", "Art", "3.7", "Dr. Smith")));
		answer.hmap.put(3, new ArrayList<String>(Arrays.asList("Nick", "History", "2.5", "Dr. Jones")));
				
		Assert.assertEquals( "Join 1 Failed:  ", answer, p.parse("a <- table8 JOIN table9;"));
	}
	
	@Test
	public void testShow() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("table8", new ArrayList<String>(Arrays.asList("Name", "Major", "GPA")), false);
		e.insertRecord("table8", 1, new ArrayList<String>(Arrays.asList("Billy", "History", "3.5")));
		e.insertRecord("table8", 2, new ArrayList<String>(Arrays.asList("Jill", "Art", "3.7")));
		e.insertRecord("table8", 3, new ArrayList<String>(Arrays.asList("Nick", "History", "2.5")));
		
		String command = "SHOW (project(Name) table8);";
		
		Table answer = new Table("");
		answer.addAttribute(-1, "Name");
		answer.addEntity(1);
		answer.addEntity(2);
		answer.addEntity(3);
		
		answer.addAttribute(1, "Billy");
		answer.addAttribute(2, "Jill");
		answer.addAttribute(3, "Nick");
		
		Assert.assertEquals( "Show 1 Failed:  ", answer, p.parse(command));
			
	}
	
	@Test
	public void testUpdate() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("table8", new ArrayList<String>(Arrays.asList("Name", "Major", "Credits")), false);
		e.insertRecord("table8", 1, new ArrayList<String>(Arrays.asList("Billy", "History", "30")));
		e.insertRecord("table8", 2, new ArrayList<String>(Arrays.asList("Jill", "Art", "40")));
		e.insertRecord("table8", 3, new ArrayList<String>(Arrays.asList("Nick", "History", "25")));
		
		String command = "UPDATE table8 SET (Major = \"CS\", Credits = 50) WHERE (Name == \"Billy\");";
	
		Assert.assertEquals( "Update 1 Failed:  ", new Table(""), p.parse(command));	
	}
	
	@Test
	public void testDelete() {
		Engine e = new Engine();
		Parser p = new Parser(e);
		
		e.createTable("table8", new ArrayList<String>(Arrays.asList("Name", "Major", "Credits")), false);
		e.insertRecord("table8", 1, new ArrayList<String>(Arrays.asList("Billy", "History", "30")));
		e.insertRecord("table8", 2, new ArrayList<String>(Arrays.asList("Jill", "Art", "40")));
		e.insertRecord("table8", 3, new ArrayList<String>(Arrays.asList("Nick", "History", "25")));
		
		String command = "DELETE FROM table8 WHERE (Major != \"History\" && Credits >= 30);";
		
		Assert.assertEquals( "Delete 1 Failed:  ", new Table(""),  p.parse(command));
	}
}
