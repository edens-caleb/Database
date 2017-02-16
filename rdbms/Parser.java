import java.util.*;
import java.io.*;

// Class that will parse strings into commands the Engine understands.
// The only public class will be parse(String s).
// All other classes will be protected so testing class has access to it still

public class Parser {
	
	private Engine e;
	
	public Parser(Engine eng) {
		e = eng;
	}
	
	public Parser() {
		e = new Engine();
	}
	
	/*
	Parameters: String t
	Return: If query, returns resulting table,
		if command returns nameless table (w/ exception of SHOW), null otherwise.
	Summary...
		*Parses through input and determines if input is valid.
	*/
	public Table parse(String s) {
		try {
			ArrayList<String> tokenList = tokenize(s);
			if (tokenList == null) {
				return null;
			}
			tokenList.remove(";");
			Table result = null;
			
			if (tokenList.contains("<-")) {
				//divide list by the stuff to the left of the arrow and right of the arrow
				if (tokenList.indexOf("<-") != 1) {
					System.err.println("Parse Error:  Too many tokens to left of arrow.");
					return null;
				}
				String relationName = tokenList.get(0);
				if (!isAlphaNum(relationName)) {
					System.err.println("Parse Error:  \"" + relationName + "\" has non-alphanumeric characters.");
					return null;
				}
				tokenList = new ArrayList<String>(tokenList.subList(2, tokenList.size()));		//All stuff to the right of the arrow
				Table t = parseView(relationName, tokenList.get(0));
				if (t.name.equals(""))
					return null;
				e.addTable(t, false);
				result = t;
			}
			
			else {		// It is command
				if (tokenList.get(0).equals("OPEN")) {
					ArrayList<String> arguments = new ArrayList<String>(tokenList.subList(1, tokenList.size()));
					result = parseOpen(arguments);
				}
				
				else if ((tokenList.get(0).equals("CLOSE")) || (tokenList.get(0).equals("DROP") && tokenList.get(1).equals("TABLE"))) {
					ArrayList<String> arguments = new ArrayList<String>();
					if (tokenList.get(0).equals("CLOSE"))
						arguments = new ArrayList<String>(tokenList.subList(1, tokenList.size()));
					else
						arguments = new ArrayList<String>(tokenList.subList(2, tokenList.size()));
					result = parseClose(arguments);
				}
				
				else if (tokenList.get(0).equals("WRITE")) {
					ArrayList<String> arguments = new ArrayList<String>(tokenList.subList(1, tokenList.size()));
					result = parseWrite(arguments);
				}
				
				else if (tokenList.get(0).equals("EXIT")) {
					if (tokenList.size() > 1) {
						System.err.println("Parse Error: Bad EXIT Command");
						return null;
					}
					result = parseExit();
				}
				
				else if (tokenList.get(0).equals("SHOW")) {
					ArrayList<String> arguments = new ArrayList<String>(tokenList.subList(1, tokenList.size()));
					result = parseShow(arguments);
				}
				
				else if (tokenList.get(0).equals("CREATE") && tokenList.get(1).equals("TABLE")) {
					ArrayList<String> arguments = new ArrayList<String>(tokenList.subList(2, tokenList.size()));
					result = parseCreateTable(arguments);
				}
				
				else if (tokenList.get(0).equals("INSERT") && tokenList.get(1).equals("INTO")) {
					ArrayList<String> arguments = new ArrayList<String>(tokenList.subList(2, tokenList.size()));
					result = parseInsert(arguments);
				}
				
				else if (tokenList.get(0).equals("UPDATE")) {
					ArrayList<String> arguments = new ArrayList<String>(tokenList.subList(1, tokenList.size()));
					result = parseUpdate(arguments);
				}
				
				else if (tokenList.get(0).equals("DELETE") && tokenList.get(1).equals("FROM")) {
					ArrayList<String> arguments = new ArrayList<String>(tokenList.subList(2, tokenList.size()));
					result = parseDelete(arguments);
				}
				
				else
					return null;
			}
			return result;
		}
		catch (Exception e) {
			System.err.println("Parse Error:  " + e.getMessage());
			return null;
		}
	}
	
	/*
	Parameters: ArrayList<String> tokenList, String t
	Return: True if successful, false otherwise.
	Summary...
		*Looks through tokenList and finds t.
		*If found, returns true.
	*/
	private boolean containsToken(ArrayList<String> tokenList, String t) {
		for (String tok : tokenList) {
			if (tok.contains(t))
				return true;
		}
		return false;
	}
	
	/*
	Parameters: String s
	Return: True if successful, false otherwise.
	Summary...
		*Determines if s is an Alpha-Numeric value.
		*If does, returns true.
	*/
	private boolean isAlphaNum(String s) {
		return s.matches("^[a-zA-Z0-9_]*$");
	}
	
	/*
	Parameters: String s, String unwant, String want
	Return: String without unwanted String with wanted String
	Summary...
		*Returns String without unwanted String with wanted String
	*/
	private String replaceLast(String s, String unwant, String want) {
		int i = s.lastIndexOf(unwant);
		if (i == -1)
			return s;
		return s.substring(0, i) + want + s.substring(i + unwant.length());
		
	}
	
	/*
	Parameters: String s, char c
	Return: Counter.
	Summary...
		*Counts number of chars in String s.
		*Returns int counter.
	*/
	private int countChar(String s, char c) {
		int counter = 0;
		for( int i=0; i<s.length(); i++ ) {
			if( s.charAt(i) == c ) {
				counter++;
			} 
		}
		return counter;
	}
	
	/*
	Parameters: String s
	Return: Array list that is parsed.
	Summary...
		*Parses input.
	*/
	protected ArrayList<String> parseList(String s) {
		s = removeOuterParen(s);
		return new ArrayList<String>(Arrays.asList(s.split(",")));
	}
	
	/*
	Parameters: String s
	Return: A string that has had outer parentheses removed.
	Summary...
		*Takes in a string and removes outer parentheses.
	*/
	private String removeOuterParen(String s) {
		s = s.replaceFirst("\\(", "");
		s = replaceLast(s, ")", "");
		return s;
	}
	
	/*
	Parameters: String s
	Return: A string that has had quotes removed.
	Summary...
		*Removes quotes.
	*/
	private String removeQuotes(String s) {
		return s.replace("\"", "");
	}
	
	/*
	Parameters: String s
	Return: True if successful, false otherwise.
	Summary...
		*If s is an integer, returns true.
	*/
	private boolean isInteger(String s) {
		return s.matches("^\\d+$");
	}
	
	/*
	Parameters: String s
	Return: ArrayList<String> named result.
	Summary...
		*Parses s.
	*/
	protected ArrayList<String> tokenize(String s) {
		int comments = s.indexOf('#');
		if (comments != -1)
			s = s.substring(0, comments);
		s = s.trim().replaceAll(" +", " ");		//Make one space max between tokens
		s = s.replaceAll(", ", ",");			//Remove spaces between commas
		s = s.replaceAll("\\( ", "\\(");		//Remove spaces after (
		s = s.replaceAll(" \\(", "\\(");		//Remove spaces before (
		s = s.replaceAll("\\) ", "\\)");		//Remove spaces after )
		s = s.replaceAll(" \\)", "\\)");		//Remove spaces before )
		s = s.replaceAll(" =", "=");
		s = s.replaceAll("= ", "=");
		s = s.replaceAll(" ==", "==");
		s = s.replaceAll("== ", "==");
		s = s.replaceAll(" >=", ">=");
		s = s.replaceAll(">= ", ">=");
		s = s.replaceAll(" !=", "!=");
		s = s.replaceAll("!= ", "!=");
		s = s.replaceAll(" <", "<");
		s = s.replaceAll("< ", "<");
		s = s.replaceAll(" >", ">");
		s = s.replaceAll("> ", ">");
		s = s.replaceAll(" <=", "<=");
		s = s.replaceAll("<= ", "<=");
		s = s.replaceAll(" &&", "&&");
		s = s.replaceAll("&& ", "&&");
		s = s.replaceAll(" \\|\\|", "\\|\\|");
		s = s.replaceAll("\\|\\| ", "\\|\\|");
		
		char[] input = s.toCharArray();
		if (input[s.length()-1] != ';') {
			System.err.println("Parse Error:  No semi-colon closer.");
			return null;
		}
		ArrayList<String> result = new ArrayList<String>();
		String next = "";
		int depth = 0;					//Keeps track of whether token is in parentheses or not
		
		boolean query = false;
		
		for(int i = 0; i < input.length; i++) {
			if (input[i] == '(' && !query) {
				depth++;
				if (!next.equals("") && depth == 1) {
					result.add(next);
					next = "(";
				}
				else
					next = next + '(';
			}
			else if (input[i] == ' '&& !query) {
				if (!next.equals("")) {
					if (depth == 0) {
						result.add(next);
						next = "";
					}
					else
						next = next + " ";	
				}
			}
			else if (input[i] == ')'&& !query) {
				depth--;
				if (depth == 0) {
					result.add(next + ')');
					next = "";
				}
				else
					next = next + ')';
			}
			else if (input[i] == ';') {
				if (!next.equals(""))
					result.add(next);
				result.add(";");
			}
			else if (input[i] == '<'&& !query) {				//Handles case of "<-" in queries
				if (i != input.length - 1) {
					if (input[i+1] == '-') {
						query = true;
						if (!next.equals(""))
							result.add(next);
						result.add("<-");
						next = "";
						i++;
					}
					else
						next = next + '<';
				}
				else
					result.add(next + '<');
			}
			else
				next = next + input[i];
		}
		
		if (depth != 0) {
			System.err.println("Parse Error:  unpaired parentheses");
			return null;
		}

		return result;
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: Table with no name if successful, null otherwise.
	Summary...
		*Parses OPEN command.
		*Loads a relation.
	*/
	protected Table parseOpen(ArrayList<String> tokenList){
		if (tokenList.size() > 1){
			System.err.println("Parse Error:  Bad OPEN Command");
			return null;
		}
		String relationName = tokenList.get(0);
		if (!isAlphaNum(relationName)) {
			System.err.println("Parse Error:  \"" + relationName + "\" has non-alphanumeric characters.");
			return null;
		}
		if (!(new File(relationName + ".db").isFile())) {
			System.err.println("ERROR:  \"" + relationName + ".db\" not found in directory");
			return null;
		}
		if (e.loadTable(relationName))
			return new Table("");
		System.err.println("ERROR:  \"" + relationName + "\" not successfully open.");
		return null;
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: Table with no name if successful, null otherwise.
	Summary...
		*Parses CLOSE command.
		*Drops a relation.
	*/
	protected Table parseClose(ArrayList<String> tokenList) {
		if (tokenList.size() > 1) {
			System.err.println("Parse Error:  Bad CLOSE/DROP TABLE command.");
			return null;
		}
		String relationName = tokenList.get(0);
		if (!isAlphaNum(relationName)) {
			System.err.println("Parse Error:  \"" + relationName + "\" has non-alphanumeric characters.");
			return null;
		}
		int index = e.keyFinder(relationName);
		if (index == -1) {
			System.err.println("ERROR:  \"" + relationName + "\" not found in database");
			return null;
		}
		e.dropTable(index);
		return new Table("");
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: Table with no name if successful, null otherwise.
	Summary...
		*Parses WRITE command.
		*Save a relation.
	*/
	protected Table parseWrite(ArrayList<String> tokenList){
		if(tokenList.size() > 1){
			System.err.println("Parse Error: Bad WRITE Command");
			return null;
		}
		String relationName = tokenList.get(0);
		if (!isAlphaNum(relationName)) {
			System.err.println("Parse Error:  \"" + relationName + "\" has non-alphanumeric characters.");
			return null;
		}
		int index = e.keyFinder(relationName);
		if (index == -1) {
			System.err.println("ERROR:  \"" + relationName + "\" not found in database");
			return null;
		}
		if (e.saveTable(relationName))
			return new Table("");
		System.err.println("ERROR:  \"" + relationName + "\" not successfully saved.");
		return null;
	}
	
	/*
	Parameters: NULL
	Return: Table with no name.
	Summary...
		*Parses EXIT command.
		*Saves all tables.
	*/
	protected Table parseExit(){
		e.saveAllTables();
		return new Table("");
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: Table printed out.
	Summary...
		*Parses SHOW command.
		*Prints all tables.
	*/
	protected Table parseShow(ArrayList<String> tokenList){
		if(tokenList.size() > 1){
			System.err.println("Parse Error: Bad SHOW Command");
			return null;
		}
		String expr = tokenList.get(0);
		if (expr.charAt(0) == '(')
			expr = removeOuterParen(expr);
		Table exprResult = parseExpr(tokenizeExpr(expr));
		if (exprResult.name.equals("")) {
			System.err.println("Parse Error:  Expression is invalid");
			return null;
		}
		e.showTable(exprResult);
		return exprResult;
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: Table with no name if successful, null otherwise.
	Summary...
		*Parses CREATE TABLE command. 
		*Creates a relation.
	*/
	protected Table parseCreateTable(ArrayList<String> tokenList){
		if (tokenList.size() != 5 || !tokenList.get(2).equals("PRIMARY") || !tokenList.get(3).equals("KEY")) {
			System.err.println("Parse Error:  Bad CREATE TABLE command.");
			return null;
		}
		String relationName = tokenList.get(0);
		if (!isAlphaNum(relationName)) {
			System.err.println("Parse Error:  \"" + relationName + "\" has non-alphanumeric characters.");
			return null;
		}
		if (countChar(tokenList.get(1), '(') < 1 || countChar(tokenList.get(4), '(') < 1 || countChar(tokenList.get(4), '(') > 2) {
			System.err.println("Parse Error:  Bad CREATE TABLE command.");
			return null;
		}
		
		ArrayList<String> arguments = parseList(tokenList.get(1));

		for(int i = 0; i < arguments.size(); i++) {
			arguments.set(i, arguments.get(i).split(" ")[0]);
		}
		String primaryKey = arguments.get(0);
		arguments.remove(0);
		
		ArrayList<String> primaryKeys = parseList(tokenList.get(4));
		
		if (!primaryKey.equals(primaryKeys.get(0))) {
			System.err.println("Parse Error:  Invalid Primary Key");
		} 
			
		e.createTable(relationName, arguments, true);
		
		return new Table("");

	}
	
	/*
	Parameters: NULL
	Return: Table with no name if successful, null otherwise.
	Summary...
		* Parses UPDATE command.
		* Updates a table by searching for attributes name and updating information within that attribute.
	*/
	protected Table parseUpdate(ArrayList<String> tokenList) {
		if (tokenList.size() != 5 || !tokenList.get(1).equals("SET") ||
			countChar(tokenList.get(2), '(') < 1 || !tokenList.get(3).equals("WHERE") || countChar(tokenList.get(4), '(') < 1) {
			System.err.println("Parse Error:  Bad DELETE command.");
			return null;
		}
		String relationName = tokenList.get(0);
		if (!isAlphaNum(relationName)) {
			System.err.println("Parse Error:  \"" + relationName + "\" has non-alphanumeric characters.");
			return null;
		}
		if (e.keyFinder(relationName) == -1) {
			System.err.println("ERROR:  \"" + relationName + "\" not found in database");
			return null;
		}
		ArrayList<String> aList = parseList(tokenList.get(2));
		ArrayList<String> attNames = new ArrayList<String>();
		ArrayList<String> attValues = new ArrayList<String>();
		for (String s : aList) {
			String[] str = s.split("=");
			if (str.length > 2) {
				System.err.println("Invalid Attribute List");
				return null;
			}
			if (countChar(str[0], '\"') > 1 || countChar(str[1], '\"') > 2) {
				System.err.println("Invalid Attribute List");
				return null;
			}
			attNames.add(removeQuotes(str[0]));
			attValues.add(removeQuotes(str[1]));
		}
		String condition = removeOuterParen(tokenList.get(4));
		Condition c = parseCondition(tokenizeCondition(condition));
		if (c == null)
			return null;
		e.updateRecord(relationName, attNames, attValues, c);
		return new Table("");
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: Table with no name if successful, null otherwise.
	Summary...
		*Parses DELETE command.
		*Finds relation and deletes a condition
	*/
	protected Table parseDelete(ArrayList<String> tokenList) {
		if (tokenList.size() != 3 || !tokenList.get(1).equals("WHERE") || countChar(tokenList.get(2), '(') < 1) {
			System.err.println("Parse Error:  Bad DELETE command.");
			return null;
		}
		String relationName = tokenList.get(0);
		if (!isAlphaNum(relationName)) {
			System.err.println("Parse Error:  \"" + relationName + "\" has non-alphanumeric characters.");
			return null;
		}
		if (e.keyFinder(relationName) == -1) {
			System.err.println("ERROR:  \"" + relationName + "\" not found in database");
			return null;
		}
		String condition = removeOuterParen(tokenList.get(2));
		Condition c = parseCondition(tokenizeCondition(condition));
		if (c == null)
			return null;
		e.deleteRecord(relationName, c);
		return new Table("");
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: Table with no name if successful, null otherwise.
	Summary...
		*Parses INSERT command.
		*It either inserts literal values into the relation or it inserts from other relations
	*/
	protected Table parseInsert(ArrayList<String> tokenList){
		if ((tokenList.size() != 4 && tokenList.size() != 5) || !tokenList.get(1).equals("VALUES") || !tokenList.get(2).equals("FROM")) {
			System.err.println("Parse Error:  Bad INSERT INTO command.");
			return null;
		}
		String relationName = tokenList.get(0);
		if (!isAlphaNum(relationName)) {
			System.err.println("Parse Error:  \"" + relationName + "\" has non-alphanumeric characters.");
			return null;
		}
		if (e.keyFinder(relationName) == -1) {
			System.err.println("ERROR:  \"" + relationName + "\" not found in database");
			return null;
		}

		//this is first insert case
		if (!tokenList.get(3).equals("RELATION")) {
			if (countChar(tokenList.get(3), '(') < 1 || countChar(tokenList.get(3), '(') > 2) {
				System.err.println("Parse Error:  Bad INSERT command.");
				return null;
			}
			ArrayList<String> arguments = parseList(tokenList.get(3));
			
			for (int i = 0; i < arguments.size(); i++) {
				String s = arguments.get(i);
				if (!isAlphaNum(s)) {
					if (countChar(s, '\"') == 2) {
						if (s.charAt(0) != '\"' || s.charAt(s.length()-1) != '\"') {
							System.err.println("Parse Error:  Unpaired quotations.");
							return null;
						}
						arguments.set(i, removeQuotes(s));
					}
					else {
						System.err.println("Parse Error:  Unpaired quotations.");
						return null;
					} 
				}
			}
			for (int i = 0; i < arguments.size(); i++) {
				if (!isAlphaNum(arguments.get(0))) {
					System.err.println("Parse Error:  literal-list contains non-alphanumeric characters.");
					return null;
				}
			}
			if (!isInteger(arguments.get(0))) {
				System.err.println("Parse Error:  First element of literal-list is not right type.");
				return null;
			}
			int primaryKey = Integer.parseInt(arguments.get(0));
			arguments = new ArrayList<String>(arguments.subList(1, arguments.size()));
			if(e.insertRecord(relationName, primaryKey, arguments))
				return new Table("");
			return null;
		}
		
		//this is for 2nd insert case
		else if(tokenList.get(3).equals("RELATION")) { 
			String expr = tokenList.get(4);
			if (expr.charAt(0) == '(')
				expr = removeOuterParen(expr);
			Table exprResult = parseExpr(tokenizeExpr(expr));
			if (exprResult.attributeCount() != e.getTable(relationName).attributeCount()) {
				System.err.println("Parse Error:  Argument is different width from Table");
				return null;
			}
			if (exprResult.name.equals("")) {
				System.err.println("Parse Error:  Expression is invalid");
				return null;
			}
			e.setTable(e.keyFinder(relationName), exprResult, true);			
			return new Table("");
		}
		
		return null;
	}
	
	/*
	Parameters: String relationName, ArrayList<String> tokenList
	Return: Table of selected data
	Summary...
		*Parses VIEW command.
		*Prints out relation.
	*/
	//call this function, all others are helpers
	protected Table parseView(String relationName, String tokens) {
		ArrayList<String> tokenList = tokenizeExpr(tokens);
		Table t = parseExpr(tokenList);
		if (t.name.equals(""))
			return new Table("");
		return new Table(t, relationName);
	}

	/*
	Parameters: String str
	Return: ArrayList<String>(Arrays.asList(str.split(",")));
	Summary...
		*Parses str and returns a parsed list of attributes.
	*/
	protected static ArrayList<String> parseAttributeList(String str) {
		return new ArrayList<String>(Arrays.asList(str.split(",")));
	}

	/*
	Parameters: String expr
	Return: ArrayList<String> finalList
	Summary...
		*Parses expr and returns list of tokens
	*/
	protected static ArrayList<String> tokenizeExpr(String expr) {
		String withDelimiter = "((?<=%1$s)|(?=%1$s))";
		char[] input = expr.toCharArray();
		
		//split based on parentheses
		ArrayList<String> result = new ArrayList<String>();
		int depth = 0;
		String next = "";
		for(int i = 0; i < input.length; i++) {
			if (input[i] == '(') {
				depth++;
				if (!next.equals("") && depth == 1) {
					result.add(next);
					next = "(";
				}
				else
					next = next + '(';
			}
			else if (input[i] == ')') {
				depth--;
				if (depth == 0) {
					result.add(next + ')');
					next = "";
				}
				else
					next = next + ')';
			}
			else if (input[i] == ' ') {
				if (!next.equals("")) {
					if (depth == 0) {
						result.add(next);
						next = "";
					}
					else
						next = next + " ";	
				}
			}
			else if (i == input.length-1) {
				if (!next.equals("") || i == 0)
					result.add(next + input[i]);
			}
			else
				next = next + input[i];
		}
		
		ArrayList<String> finalList = new ArrayList<String>();
		for(String str : result) {
			if(str.charAt(0) != '(') {
				String[] list = str.split(String.format(withDelimiter, "\\+|\\-|\\*|JOIN"));
				for(String s : list) {
					finalList.add(s);
				}
			}
			else {
				finalList.add(str);
			}
				
		}
		return finalList;
			
	}

	/*
	Parameters: ArrayList<String> tokenList
	Return: null
	Summary...
		*Parses tokenList and determines expr.
	*/
	protected Table parseExpr(ArrayList<String> tokenList) {
		if(tokenList.get(0).equals("select")) {
			return parseSelect(tokenList);
		}
		else if (tokenList.get(0).equals("project")) {
			return parseProject(tokenList);
		}
	
		else if (tokenList.get(0).equals("rename")) {
			return parseRename(tokenList);
		}
		else if (tokenList.size() == 3){
			if(tokenList.get(1).equals("+")) {
				return parseUnion(tokenList);
			}
			else if(tokenList.get(1).equals("-")) {
				return parseDifference(tokenList);
			}
			else if(tokenList.get(1).equals("*")) {
				return parseProduct(tokenList);
			}
			else if(tokenList.get(1).equals("JOIN")) {
				return parseNatJoin(tokenList);
			}
		}
		else {
			//parse atomic expr
			return parseAtomicExpr(tokenList.get(0));
		}
		return new Table("");
	}

	/*
	Parameters: String token
	Return: parseExpr expr or Table based on key
	Summary...
		*Parses atomic-expr.
		*Either the expression will parse a relation-name or expr
	*/
	protected Table parseAtomicExpr(String token) {
		//must parse an additional expression
		if(token.charAt(0) == '(') { 
			ArrayList<String> newTokenList = tokenizeExpr(removeOuterParen(token));
			return parseExpr(newTokenList);
		}
		//find the table in memory, end case
		else {
			int key = e.keyFinder(token);
			if (key == -1)
				return new Table("");
			return e.getTable(key);
		}
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: e.select(table, condition)
	Summary...
		*Parses Selection based on condition and atomic-expr.
		*Selects tuples in a table that satisfies given condition.
	*/
	protected Table parseSelect(ArrayList<String> tokenList) {
		//select (condition) atomic-expr
		
		Condition c = parseCondition(tokenizeCondition(removeOuterParen(tokenList.get(1))));
		Table t = parseAtomicExpr(tokenList.get(2));
		return e.select(t, c);
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: Projected Table 
	Summary...
		*Parses Projection based on attribute-list and atomic-expr.
		*Selects a subset of attributes in a table.
	*/
	protected Table parseProject(ArrayList<String> tokenList) {
		//project (attribute-list) atomic-expr
		ArrayList<String> attributeList = parseAttributeList(removeOuterParen(tokenList.get(1)));
		return e.projection(parseAtomicExpr(tokenList.get(2)), attributeList);
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: Renamed table.
	Summary...
		*Parses Rename based on attribute-list and atomic-expr.
	*/
	protected Table parseRename(ArrayList<String> tokenList) {
		//rename (attribute-list) atomic-expr
		ArrayList<String> attributeList = parseAttributeList(removeOuterParen(tokenList.get(1)));
		Table t = parseAtomicExpr(tokenList.get(2));
		if(!e.rename(t, attributeList))
			System.err.println("Rename Failed");
		return t;
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: Unioned Table.
	Summary...
		*Parse Union based on atomic-expr and atomic-expr.
	*/
	protected Table parseUnion(ArrayList<String> tokenList) {
		//atomic-expr + atomic-expr
		return e.setUnion(parseAtomicExpr(tokenList.get(0)), parseAtomicExpr(tokenList.get(2)));
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: Table after difference.
	Summary...
		*computes the difference between two tables.
	*/
	protected Table parseDifference(ArrayList<String> tokenList) {
		//atomic-expr - atomic-expr
		return e.setDifference(parseAtomicExpr(tokenList.get(0)), parseAtomicExpr(tokenList.get(2)));
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: Table that contains cartesean product.
	Summary...
		*Parses Product and returns cartesean product table.
	*/
	protected Table parseProduct(ArrayList<String> tokenList) {
		//atomic-expr * atomic-expr
		return e.crossProduct(parseAtomicExpr(tokenList.get(0)), parseAtomicExpr(tokenList.get(2)));
	}
	
	/*
	Parameters: ArrayList<String> tokenList
	Return: natural joined Table.
	Summary...
		*Parses natural-join
		*Returns a table including all combinations of tuples in two tables that are equal on their common attribute names.
	*/
	protected Table parseNatJoin(ArrayList<String> tokenList) {
		//atomic-expr JOIN atomic-expr
		return e.naturalJoin(parseAtomicExpr(tokenList.get(0)), parseAtomicExpr(tokenList.get(2)));
	}
	
	//parseCondition

	protected Condition parseCondition(ArrayList<String> tokenList) {
		boolean foundOr = false;
		ArrayList<String> left = new ArrayList<String>();
		ArrayList<String> right = new ArrayList<String>(tokenList);
		for(String t : tokenList) {
			if(t.equals("||")) {
				right.remove(0);
				foundOr = true;
				return new Condition(parseConjunction(left), parseConjunction(right));
			}
			else {
				//move first elem of right to end of left, "iterating" through the token list
				left.add(t);
				right.remove(0);
			}
		}
		if(!foundOr) {
			return new Condition(parseConjunction(tokenList));
		}
		return null;
	}

	protected Conjunction parseConjunction(ArrayList<String> tokenList){
		boolean foundAnd = false;
		ArrayList<String> left = new ArrayList<String>();
		ArrayList<String> right = new ArrayList<String>(tokenList);
		for(String t : tokenList) {
			if(t.equals("&&")) {
				foundAnd = true;
				right.remove(0);
				return new Conjunction(parseComparison(left), parseComparison(right));
			}
			else {
				//move first elem of right to end of left, "iterating" through the token list
				left.add(t);
				right.remove(0);
			}
		}
		if(!foundAnd) {
			return new Conjunction(parseComparison(tokenList));
		}
		return null;
	}

	protected Comparison parseComparison(ArrayList<String> tokenList){
		if(tokenList.size() == 3)
			return new Comparison(tokenList.get(0), tokenList.get(1), tokenList.get(2));
		else if(containsToken(tokenList, "||") || containsToken(tokenList, "&&")) {
			return new Comparison(parseCondition(tokenList));
		}
		else {
			System.err.println("Invalid Condition, badly formed input");
			return null;
		}
	}
	
	protected ArrayList<String> tokenizeCondition(String cond) {
		String withDelimiter = "((?<=%1$s)|(?=%1$s))";
		ArrayList<String> tokenList = tokenQuote(cond);
		ArrayList<String> finalList = new ArrayList<String>();
		for(String str : tokenList) {
			String[] list = str.split(String.format(withDelimiter, "(==)|(!=)|(>=)|(<=)|(&&)|(\\|\\|)|<(?!=)|>(?!=)"));
			for(String s : list) {
				finalList.add(s);
			}
		}
		return finalList;
	}

	protected ArrayList<String> tokenQuote(String str) {
		char[] charArray = str.toCharArray();
		ArrayList<String> tokenList = new ArrayList<String>();
		String token = "";
		for(int i = 0; i < charArray.length; i++) {
			char tokenChar = charArray[i];
			if(tokenChar == '"') {
				if(token!="")
					tokenList.add(token);
				token = "";
			}
			else {
				token += Character.toString(tokenChar);
			}
		}
		if(token!="")
			tokenList.add(token);
		
		return tokenList;
	}
}
