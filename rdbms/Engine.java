import java.lang.*;
import java.util.*;
import java.io.*;

public class Engine{

	private HashMap<Integer,Table> dictionary;

	/*
	Parameters: none
	Summary...
	Creates new Engine with empty dictionary.
	*/
	public Engine(){
		dictionary = new HashMap<Integer,Table>();	
	}
	
	private class TableFileFilter implements FileFilter {
		public boolean accept(File file) {
		if (file.getName().endsWith(".db"))
			return true;
		return false;
		}
	}
	
	/*
	Parameters: none
	Return: boolean
	Summary...
	Loads all *.db file in current directory to Engine.  Returns false if none found.
	*/
	public boolean loadAllTables() {
		File dir = new File(".");
		File[] files = dir.listFiles(new TableFileFilter());
		if (files.length == 0)
			return false;
		for (File f : files) {
			int i = f.getName().indexOf('.');
			String tableName = f.getName().substring(0, i);
			loadTable(tableName);
		}
		return true;
	}

	/*
	Parameters: Table t, Condition c
	Return: Table of selected data
	Summary...
	Prints data from Table t that matches Condition c.
	Selection is done by a recursive cycle of three functions:
	 * evalCondition
	 * evalConjunction
	 * evalComparison
	*/
	public Table select(Table t, Condition c) {
		return evalCondition(t, c);
	}
	
	private Table evalCondition(Table t, Condition cond) {
		if(cond.getConj2() == null) {
			t = evalConjunction(t, cond.getConj1());
		}
		else {
			Table temp = new Table("tempName");
			temp.unite(evalConjunction(t, cond.getConj1()), evalConjunction(t, cond.getConj2()));
			t = temp;
		}
		return t;
	}
	
	private Table evalConjunction(Table t, Conjunction conj) {
		if(conj.getComp2() == null) {
			t = evalComparison(t, conj.getComp1());
		}
		else {
			Table temp = new Table("tempName");
			temp.intersect(evalComparison(t, conj.getComp1()), evalComparison(t, conj.getComp2()));
			t = temp;
		}
		return t;
	}

	private Table evalComparison(Table t, Comparison comp) {
		if(comp.getCondition() == null) {
			Table temp = new Table("tempName");
			switch(comp.getOperator()) {
				case "==":
					temp.selectEQ(t, comp.getOperand1(), comp.getOperand2());
					
					break;
				case "!=":
					temp.selectNTEQ(t, comp.getOperand1(), comp.getOperand2());
					break;
				case ">":
					temp.selectGT(t, comp.getOperand1(), comp.getOperand2());
					break;
				case "<":
					temp.selectLT(t, comp.getOperand1(), comp.getOperand2());
					break;
				case ">=":
					temp.selectGTET(t, comp.getOperand1(), comp.getOperand2());
					break;
				case "<=":
					temp.selectLTET(t, comp.getOperand1(), comp.getOperand2());
					break;
				default:
					System.err.println("Switch block failed");
			}
			t = temp;
		}
		else
		{
			t = evalCondition(t, comp.getCondition());
		}
		return t;
	}

	/*
	Parameters: Table t, AttributeList<String> a 
	Return: Table of projected data
	Summary...
	Prints the columns in the AttributeList a of Table t. Note: Does not save this table. 
	*/
	public Table projection(Table t, ArrayList<String> a){
		ArrayList<Integer> keyArray = t.keyList();
		ArrayList<String> currAttributeArray;
		ArrayList<Integer> attributesIndex = new ArrayList<Integer>();
		int attributeKey = -2;
		//Stores the index of the attributes we care about in attributesIndex
		currAttributeArray = t.attributeList(-1);
		for(int i=0;i<currAttributeArray.size();i++){
			if(a.contains(currAttributeArray.get(i))){
				attributesIndex.add(i);
			}
		}
		
		Table result = new Table("Projection");
		//Finds the attributes and stores them in result: 
		for(int i=0; i < keyArray.size();i++){
			result.addEntity(keyArray.get(i));
			currAttributeArray = t.attributeList(keyArray.get(i));
			for(int j=0; j < currAttributeArray.size();j++){
				if(attributesIndex.contains(j)){
					result.addAttribute(keyArray.get(i),currAttributeArray.get(j));
				}
					
			}
		}
		
		return result;
	}


	/*
	Parameters: Table t, ArrayList<String> names
	Return: boolean success status
	Summary...
	Replaces Table t's TitleRow with AttributeList names. Returns true 
	if the operation completed successfully. 
	
	NOTE:  names must be same length as the t's TitleRow
	*/
	public boolean rename(Table t, ArrayList<String> names){
		int attLength = t.hmap.get(-1).size();
		if (names.size() != attLength) {
			System.err.println("Argument list not equal to table width");
			return false;
		}
		for (int i = 0; i < names.size(); i++) {
			t.hmap.get(-1).set(i, names.get(i));
		}
		return true;
	}


	/*
	Parameters: Table t1, Table t2
	Return: Table
	
	Summary...
	Consolidates data from Table t1 and t2 into one Table
	NOTE:  Keys of t2 must be different from t1 to work properly. 
	*/
	public Table setUnion(Table t1, Table t2){
		Table union = new Table("Union");
		union.unite(t1,t2);

		return union;
	}
	
	/*
	Parameters: Table t1, Table t2
	Return: Table
	
	Summary...
	returns a tables of the common data between Table t1 and t2 into one Table
	*/
	public Table setIntersect(Table t1, Table t2){
		Table intersect = new Table("Intersect");
		intersect.intersect(t1,t2);
		
		return intersect;
	}


	/*
	Parameters: Table t1, Table t2
	Return: Table
	Summary...
	Displays data in Table t1 that is not in Table t2
	*/
	public Table setDifference(Table t1, Table t2){
		Table diff = new Table("Difference");
		diff.difference(t1,t2);
		
		return diff;
	}


	/*
	Parameters: Table t1, Table t2
	Return: Table
	Summary...
	Displays Cartestian Product of Table t1 and Table t2
	*/
	public Table crossProduct(Table t1, Table t2){
		Table result = new Table("Cross");
		ArrayList<Integer> entitiesT1 = t1.keyList();
		ArrayList<Integer> entitiesT2 = t2.keyList();
		ArrayList<String> attributesT1 = t1.attributeList(-1);
		ArrayList<String> attributesT2 = t2.attributeList(-1);
		//Add title row:
		result.addEntity(-1);
		for(int i=0; i<attributesT1.size(); i++){
			result.addAttribute(-1,attributesT1.get(i));
		}
		for(int i=0; i<attributesT2.size(); i++){
			result.addAttribute(-1,attributesT2.get(i));
		}
		//entitiesT1.remove(-1);
		//entitiesT2.remove(-1);
		
		//Add content:
		int index = 0;
		int currEntityT1;
		int currEntityT2;
		for(int i=0; i<entitiesT1.size(); i++){
			if(entitiesT1.get(i) != -1){
				currEntityT1 = entitiesT1.get(i);
				attributesT1 = t1.attributeList(currEntityT1);
				for(int j=0; j<entitiesT2.size(); j++){
					if(entitiesT2.get(j) != -1){
						currEntityT2 = entitiesT2.get(j);
						attributesT2 = t2.attributeList(currEntityT2);
						result.addEntity(index);
						for(int k=0; k<attributesT1.size(); k++){
							result.addAttribute(index,attributesT1.get(k));
						}
						for(int l=0; l<attributesT2.size(); l++){
							result.addAttribute(index,attributesT2.get(l));
						}
						index++;
					}
					
				}
			}
			
		}
		
		return result;
	}


/*
	Parameters: Table t1, Table t2
	Return: Table
	Summary...
	Merges data of Table t1 and Table t2
	*/
	public Table naturalJoin(Table t1, Table t2){
		Table result = new Table("Natural Join");
		ArrayList<Integer> commonT1Index = new ArrayList<Integer>();
		ArrayList<Integer> commonT2Index = new ArrayList<Integer>();
		ArrayList<Integer> unCommonT2Index = new ArrayList<Integer>();
		ArrayList<Integer> entitiesT1 = t1.keyList();
		ArrayList<Integer> entitiesT2 = t2.keyList();
		ArrayList<String> attributesT1 = t1.attributeList(-1);	
		ArrayList<String> attributesT2 = t2.attributeList(-1);
		
		//Fills result with all of the content in table 1 (serves as a starting point):
		for(int i=0; i<entitiesT1.size();i++){
			result.addEntity(entitiesT1.get(i));
			attributesT1 = t1.attributeList(entitiesT1.get(i));
			for(int j=0; j<attributesT1.size();j++){
				
				result.addAttribute(entitiesT1.get(i),attributesT1.get(j));
			}
		}
		attributesT1 = t1.attributeList(-1);
		
		//Finds the index of all of the common attributes:
		for(int i=0; i< attributesT1.size();i++){
			for(int j=0; j< attributesT2.size(); j++){
				if(attributesT1.get(i).equals(attributesT2.get(j))){
					
					commonT1Index.add(i);
					commonT2Index.add(j);
				}
			}
		}
		
		//Finds the index of all of the uncommon attributes in table 2:
		for(int i=0; i< attributesT2.size();i++){
			if(commonT2Index.contains(i)==false){
				unCommonT2Index.add(i);
			}
		}
		
		//Appends all of the "uncommon" data to result.
		boolean flag;
		for(int i=0; i< entitiesT1.size();i++){
			attributesT1 = t1.attributeList(entitiesT1.get(i));
			for(int j=0; j< entitiesT2.size();j++){
				attributesT2 = t2.attributeList(entitiesT2.get(j));
				flag = true;
				//This if handles the title row:
				if(entitiesT1.get(i)==-1 && entitiesT2.get(j)==-1){ 
					for(int k=0; k<unCommonT2Index.size();k++){
						result.addAttribute(entitiesT1.get(i),attributesT2.get(unCommonT2Index.get(k)));
					}
				}
				//This else handles all of the non title rows:
				else{
					//This for determines of we meet the conditions to match uncommon values
					//to the existing table
					for(int iT1=0; iT1<commonT1Index.size();iT1++){
						if(attributesT1.get(commonT1Index.get(iT1)).equals(attributesT2.get(commonT2Index.get(iT1)))==false){
							flag = false;
						}
					}
					if(flag == true){
						for(int k=0; k<unCommonT2Index.size();k++){
							result.addAttribute(entitiesT1.get(i),attributesT2.get(unCommonT2Index.get(k)));
						}
					}
				}
			}
		}	
		
		return result;
	}


	/*
	Parameters: String name,  ArrayList<String> attributeList, boolean state
	Return: void
	Summary...
	Adds new table to dictionary with name and a title Row containing
	the strings in attributeList.  Table's save state is set to state.  
	*/
	public void createTable(String name,  ArrayList<String> attributeList, boolean state) {
		int i = dictionary.size() + 1;
		while (containsTable(i))
			i++;
		//name check
		ArrayList<Integer> keyArray = keyList();
		int index;
		for(int j=0; j<keyArray.size();j++){
			index = keyArray.get(j);
			if(dictionary.get(index).getName().equals(name)){
				System.err.println("Database already has name");
				return;
			}
		}
		
		dictionary.put(i, new Table(name));
		dictionary.get(i).setSave(state);
		for(int c = 0; c < attributeList.size(); c++) {
			insertRecord(i, -1, attributeList.get(c), attributeList.get(c));	
		}
	}


	/*
	Parameters: String table name
	Return: int
	Summary...
	Finds the key of the table in dictionary with the passed in name. Returns -1 if
	not found. 
	*/
	public int keyFinder(String tableName){
		ArrayList<Integer> keyArray = keyList();
		int index;
		for(int i=0; i<keyArray.size();i++){
			index = keyArray.get(i);
			if(dictionary.get(index).getName().equals(tableName)){
				return index;
			}
		}
		return -1;
	}

	/*
	Parameters: int key
	Return: boolean
	Summary...
	Removes Table mapped to key from dictionary
	Returns false if key doesn't exist, otherwise true
	*/
	public boolean dropTable(int key){
		if(dictionary.containsKey(key)){
			dictionary.remove(key);
			return true;
		}
		return false;
	}


	/*
	Parameters: int tableKey, int entity key, String attribute type, String attribute value
	Return: boolean
	Summary...
	Adds entry to Table t. If something was already in that slot, it acts like an update and overwrites it. 
	If the attribute is a new type, we will make a new column for that type, fill in the value for the 
	matching entity, and put "N/A" in that column for all of the other entities.
	*/
	public boolean insertRecord(int tableKey, int key, String atType, String atValue) {
		ArrayList<String> attributeArray;
		boolean flag = false;
		int attributeKey = -2;
		ArrayList<Integer> keyArray;
		
		//Check for attribute type::
		attributeArray = dictionary.get(tableKey).attributeList(-1);
		if (attributeArray != null) {
			for(int i=0; i<attributeArray.size(); i++){
				if(attributeArray.get(i).equals(atType)){
					flag = true; 
					attributeKey = i;
				}
			}
		}
		//If column exists, treat as an updateRecord. Else, make new column
		if(flag == true){
			dictionary.get(tableKey).updateElement(key, attributeKey, atValue);
		}
		else{
			keyArray = dictionary.get(tableKey).keyList();
			for(int i=0;i<keyArray.size();i++){
				if(keyArray.get(i)==-1){
					dictionary.get(tableKey).addAttribute(-1,atType);
				}
				else if(keyArray.get(i)==key){
					dictionary.get(tableKey).addAttribute(key,atValue);
				}
				else{
					dictionary.get(tableKey).addAttribute(keyArray.get(i),"N/A");
				}
			}
		}
		return true;
	}
	
	/*
	Parameters: String tableName, int key, ArrayList<String> row
	Return: boolean
	Summary...
	Adds row to table with tableName.  The argument must be <= length of
	the titleRow of the table.
	*/
	public boolean insertRecord(String tableName, int key, ArrayList<String> row) {
		int tableKey = keyFinder(tableName);
		if (tableKey == -1) {
			System.err.println("Table not found");
			return false;
		}
		Table t = dictionary.get(tableKey);
		int attLength = t.hmap.get(-1).size();
		if (row.size() > attLength) {
			System.err.println("Argument longer than width.");
			return false;
		}
		int primaryKey = key;
		if (t.hmap.get(primaryKey) != null) {
			System.err.println("Key already exists.");
			return false;
		}
		t.addEntity(primaryKey);
		while (row.size() < attLength)
			row.add("N/A");
		for (String ent : row) {
			t.addAttribute(primaryKey, ent);
		}
		return true;
	}

	/*
	Parameters: Table t, boolean state
	Return: none
	Summary...
	Adds Table t to dictionary with save state set to state
	*/
	public void addTable(Table t, boolean state) {
		int i = dictionary.size() + 1;
		while (containsTable(i))
			i++;
		//name check
		ArrayList<Integer> keyArray = keyList();
		int index;
		for(int j=0; j<keyArray.size();j++){
			index = keyArray.get(j);
			if(dictionary.get(index).getName().equals(t.getName())){
				System.err.println("Database already has name");
				return;
			}
		}
		dictionary.put(i, t);
		t.setSave(state);
	}
	
	/*
	Parameters: String tableName, ArrayList<String> attNames, ArrayList<String> attValues, Condition c
	Return: boolean
	Summary...
	In table with tableName, it takes list of attributeNames in attNames that want to be changed
	and the list of new values for those attributes and edits rows that match Condition c
	*/
	public boolean updateRecord(String tableName, ArrayList<String> attNames, ArrayList<String> attValues, Condition c) {
		int tableKey = keyFinder(tableName);
		if (tableKey == -1) {
			System.err.println("Table not found.");
			return false;
		}
		Table t = dictionary.get(tableKey);
		for (String x : attNames) {
			if (!t.hmap.get(-1).contains(x)) {
				System.err.println(x + "not found in attribute list.");
				return false;
			}
		}
		Table result = select(t, c);
		for (int i = 0; i < attNames.size(); i++) {
			int arrayIndex = t.hmap.get(-1).indexOf(attNames.get(i));
			for (int key : result.hmap.keySet()) {
				if (key != -1)
					t.updateElement(key, arrayIndex, attValues.get(i));
			}
		}
		
		setTable(tableKey, t, true);
		
		return true;
	}


	/*
	Parameters: String tableName, Condition c
	Return: boolean
	Summary...
	Deletes all rows in Table with tableName that match Condition c.
	*/
	public boolean deleteRecord(String tableName, Condition c) {
		int tableKey = keyFinder(tableName);
		if (tableKey == -1)
			System.err.println("Table not found.");
		Table t = dictionary.get(tableKey);
		Table result = select(t, c);
		for (int key : result.hmap.keySet()) {
			if (key != -1)
				t.hmap.remove(key);
		}
		setTable(tableKey, t, true);
		
		return true;
	}


	/*
	Parameters: int key
	Return: void
	Summary...
	Prints graphical representation of Table with key
	*/
	public void showTable(int key){
		System.out.println(dictionary.get(key));
	}
	
	/*
	Parameters: String relationName
	Return: void
	Summary...
	Prints graphical representation of Table with relationName
	*/
	public void showTable(String relationName) {
		int tableKey = keyFinder(relationName);
		if (tableKey != -1)
			System.out.println(dictionary.get(tableKey));
	}
	
	/*
	Parameters: Table t
	Return: void
	Summary...
	Prints graphical representation of Table t
	*/
	public void showTable(Table t) {
		System.out.println(t);
	}

	/*
	Parameters: None
	Return: boolean
	Summary...
	Loads table stored in .db file by searching for tableName.db in current directory  and adds 
	it to dictionary.  Only one table is stored in a .db file.  Sets save state of loaded table to true.
	*/
	public boolean loadTable(String tableName){
		String fileName = tableName + ".db";
		try{
			String line = null;
			Vector<String> commands = new Vector<String>();
			int index  = 0;
			int currentKey = 0;
			int currentEntityKey = 0;

			//This section reads each line of the file and stores them in a vector "commands"
			FileReader fileReader = new FileReader(fileName);

			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while((line = bufferedReader.readLine()) != null) {
				commands.add(line.trim());
            }   

            bufferedReader.close(); 

            //This section takes the Vector "cammands" and uses it to build the tables
            while(index < commands.size()){

            	if(commands.elementAt(index).equals("*--*")){ //finds start of new table
            		index++;
            		int i = dictionary.size() + 1;
					while (containsTable(i))
						i++;
            		currentKey = i;
            		tableName = commands.elementAt(index); //finds the table name
					Table t = new Table(tableName);
            		dictionary.put(currentKey, t);
					dictionary.get(currentKey).setSave(true);
            		index++;
            		while(commands.elementAt(index).equals("*--*")==false){ //adds attributes
            			if(commands.elementAt(index).equals("//")){
            				index++;
            				currentEntityKey = Integer.parseInt(commands.elementAt(index));
            				dictionary.get(currentKey).addEntity(currentEntityKey);
            				index++;
            				while(commands.elementAt(index).equals("//")==false &&
							commands.elementAt(index).equals("*--*")==false){
            					dictionary.get(currentKey).addAttribute(currentEntityKey,commands.elementAt(index));
            					index++;
            				}
            			}
            		}//end adding attributes
					
            	} // end building table

            	index++;
            } // end looping through commands

		} //end try block


		catch(FileNotFoundException ex) {
            System.err.println(
                "Unable to open file '" + 
	                fileName + "'");  
			return false;
        }
        catch(IOException ex) {
            System.err.println(
                "Error reading file '" 
                + fileName + "'");
			return false;
        }
		return true;
	}

	/*
	Parameters: String tableName
	Return: boolean
	Summary...
	Saves table in tableName.db in current directory.
	Only one table is stored in a .db file.  Sets save state of saved table to true.  
	*/
	public boolean saveTable(String tableName){
		
		String fileName = tableName + ".db";
		StringBuilder str = new StringBuilder();
		int key = keyFinder(tableName);
		if(key < 0){
			System.out.println("ERROR: Bad table index!");
			return false;
		}
		else{
			str.append(dictionary.get(key).tableToText());
			dictionary.get(key).setSave(true);
		}
		
		//this section takes the string we built above and writes it to the file
		try{
			PrintWriter writer = new PrintWriter(fileName, "UTF-8");
			writer.println(str.toString());
			writer.close();
		}
		catch(FileNotFoundException ex) {
            System.err.println(
                "Unable to open file '" + fileName + "'"); 
			return false;
        }
        catch(IOException ex) {
            System.err.println(
                "Error reading file '" + fileName + "'");
			return false;
        }
		return true;
		
	}
	
	/*
	Parameters: String tableName
	Return: boolean
	Summary...
	Saves all tables in dictionary with save state = true.
	Each table is stored in their own tableName.db file.  Used with EXIT command. 
	*/
	public void saveAllTables() {
		for (Table t : dictionary.values()) {
			if (t.getSave()) {
				saveTable(t.name);
			}
		}
	}
	
	/*
	Parameters: String tableName
	Return: boolean
	Summary...
	Deletes file in currentDirectory named tableName.db.  Used for testing.
	*/
	public boolean deleteSaveFile(String tableName){
		String fileName = tableName + ".db";
		int key = keyFinder(tableName);
		if(key < 0){
			System.out.println("ERROR: Table not found!");
			return false;
		}
		try{

    		File file = new File(fileName);

    		if(file.delete()){
    			dictionary.get(key).setSave(false);
    			return true;
    		}
    		else{
    			System.out.println("Delete operation is failed.");
    			return false;
    		}


    	}catch(Exception e){

    		e.printStackTrace();

    	}
    	return false;
	}
	
	/*
	Parameters: int key
	Return: Table
	Summary...
	Returns the table at the given dictionary key
	*/
	public Table getTable(int key){
		if (containsTable(key))
			return dictionary.get(key);
		return null;
	}
	
	/*
	Parameters: String tableName
	Return: Table
	Summary...
	Returns the table with tableName
	*/
	public Table getTable(String tableName) {
		int tableKey = keyFinder(tableName);
		if (tableKey != -1)
			return dictionary.get(keyFinder(tableName));
		return null;
	}
	
	/*
	Parameters: int key
	Return: boolean
	Summary...
	Returns if the table is in dictionary
	*/	
	public boolean containsTable(int key) {
		return dictionary.containsKey(key);
	}
	
	/*
	Parameters: none
	Return: void
	Summary...
	Clears dictionary of all tables
	*/	
	public void clearEngine() {
		dictionary.clear();
	}
	
	/*
	Parameters: none
	Return: int
	Summary...
	Returns number of tables in dictionary
	*/		
	public int numOfTables() {
		return dictionary.size();
	}
	
	/*
	Parameters: int key, Table t, boolean state
	Return: none
	Summary...
	Changes Table key maps to in dictionary.  Sets state of Table to state.
	*/	
	public void setTable(int key, Table t, boolean state) {
		dictionary.replace(key, t);
		dictionary.get(key).setSave(state);
	}
	
	/*
	Parameters: N/A
	Return: ArrayList<Integer>
	Summary...
	Returns an ArrayList of all of the table keys stored in dictionary.
	*/
	ArrayList<Integer> keyList(){
		Object[] keyArrayBuffer = dictionary.keySet().toArray(); 
		ArrayList<Integer> keyArray = new ArrayList<Integer>();
		for(int i=0; i<keyArrayBuffer.length;i++){
			keyArray.add(Integer.parseInt(keyArrayBuffer[i].toString()));
		}
		return keyArray;
	}
}
