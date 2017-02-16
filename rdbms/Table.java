import java.lang.*;
import java.util.*;

public class Table{

	HashMap<Integer, ArrayList<String>> hmap;
	//NEW: save state and table title data fields
	boolean save;
	String name;
	final int TITLE_KEY;

	//NEW: Takes in the table name and save state
	public Table(String tableName){
		hmap = new HashMap<Integer,ArrayList<String>>();
		TITLE_KEY = -1;
		addEntity(TITLE_KEY);
		save = false;
		name = tableName;
	}
	
	/*
	Parameters: Table t
	Summary...
	 Copies t into new Table object
	 NEW: Takes in the table name and save state
	*/	
	public Table(Table t, String tableName) {
		this.hmap = new HashMap<Integer,ArrayList<String>>(t.hmap);
		TITLE_KEY = -1;
		save = false;
		name = tableName;
	}

	//NEW: returns the value of the saved state (True = it will save)
	boolean getSave(){
		return save;
	}

	//NEW: returns the name of the table
	String getName(){
		return name;
	}

	//NEW: sets the save state to the passed in value
	void setSave(boolean tableSave){
		save = tableSave;
	}

	//NEW: sets the table name to the passed in value
	void setName(String tableName){
		name = tableName;
	}
	

	/*
	Parameters: int entity key
	Return: void
	Summary...
	 makes a new entity with a the passed in key value
	*/
	void addEntity(int key){
		ArrayList<String> vec = new ArrayList<String>();
		hmap.put(key,vec);
	}



	/*
	Parameters: int entity key, String attribute value
	Return: void
	Summary...
	Adds the value to the end of the attribute list of entity key
	*/
	void addAttribute(int key, String value){
		if(hmap.containsKey(key)){
			hmap.get(key).add(value);
		}
		else{
			System.out.println("ERROR1: Key not found!");
		}
	}

	/*
	Parameters: N/A
	Return: String 
	Summary...
	 This takes all of the data of the table and makes a text representation
	 of it for a .txt file
	 NEW:It now outputs ALL of the table info
	*/
	String tableToText(){
		StringBuilder str = new StringBuilder();
		//This makes an array of all of the entity keys
	
		ArrayList<Integer> keyArray = keyList();
		str.append("*--*");
		str.append(System.getProperty("line.separator"));
		str.append(name);
		str.append(System.getProperty("line.separator"));
		//now we use that array to iterate through all of the entities 

		/*
		This section builds a string that is a text representation of the table. 
		This only includes the lists of attributes which have the format:
		//
		attributeValue
		attributeValue
		//
		attributeValue
		attributeValue
		The table title and begining/ end delimiters are added in the save function
		that will call this since it is the only one with access to the table title.
		*/

		for(int i=0; i<keyArray.size();i++){
			str.append("//");
			str.append(System.getProperty("line.separator"));
			str.append(keyArray.get(i));
			str.append(System.getProperty("line.separator"));
			for(int j=0; j<hmap.get(keyArray.get(i)).size();j++){
				str.append(hmap.get(keyArray.get(i)).get(j));
				str.append(System.getProperty("line.separator"));
			}
		}
		str.append("*--*");
		return str.toString();
	}
	
	/*
	Parameters: N/A
	Return:int
	Summary...
	 Returns the number of attributes in the attribute table (based on title row)
	*/
	int attributeCount(){
		return hmap.get(TITLE_KEY).size();
	}
	
	/*
	Parameters: N/A
	Return: ArrayList<Integer>
	Summary...
	 Returns an ArrayList of all of the Primary Keys in this table
	*/
	ArrayList<Integer> keyList(){
		Object[] keyArrayBuffer = hmap.keySet().toArray(); 
		ArrayList<Integer> keyArray = new ArrayList<Integer>();
		for(int i=0; i<keyArrayBuffer.length;i++){
			keyArray.add(Integer.parseInt(keyArrayBuffer[i].toString()));
		}
		return keyArray;
	}
	
	/*
	Parameters: int entity key
	Return: ArrayList<String>
	Summary...
	 Returns an ArrayList of all of the attribute values of a particular entity
	*/
	ArrayList<String> attributeList(int key){
		return hmap.get(key);
	}
	
	/*
	Parameters: int entity key, int attribute index, String attribute value
	Return: void
	Summary...
	 changes the attribute value stored in a particular position specified by 
	 the entity key and attribute index. 
	*/
	void updateElement(int key, int attrIndex, String newValue){
		if (!hmap.containsKey(key)) {
			hmap.put(key, new ArrayList<String>());
			for (int i = 0; i < hmap.get(-1).size(); i++)
				addAttribute(key, "N/A");
		}
		hmap.get(key).set(attrIndex, newValue);
	}
	
	/*
	Parameters: Table t2
	Return: boolean
	Summary...
	 This checks to see if two tables are compatible. This is mostly used for functions like
	 union that need this to be true.
	*/
	boolean attributeCheck(Table t2){
		if (attributeCount() != t2.attributeCount()){
			return false;
		}
		boolean flag = true; 
		for(int i=0; i < attributeCount();i++){
			if(attributeList(TITLE_KEY).get(i).equals(t2.attributeList(TITLE_KEY).get(i))==false){
				flag = false;
			}
		}
		return flag;
	}
	
	/*
	Parameters: Table t1, Table t2
	Return: void
	Summary...
	Turns the current table into a Set Union between Table t1 and Table t2.
	WARNING: Only call this is the current table has nothing in it at the 
	start. If it has content already, it will simply add the Set Union to the 
	existing table. 
	*/
	void unite(Table t1, Table t2){
		if(t1.attributeCheck(t2)==false){
			System.out.println("ERROR: Tables not compatible, no union made");
		}
		
		else{
			ArrayList<Integer> keyArrayT1 = t1.keyList();
			ArrayList<Integer> keyArrayT2 = t2.keyList();
			
			for(int i=0; i < keyArrayT1.size();i++){
				if(keyArrayT2.contains(keyArrayT1.get(i))){
					keyArrayT2.remove(keyArrayT1.get(i));
				}
			}
			int key;
			for(int i=0; i < keyArrayT1.size();i++){
				key = keyArrayT1.get(i);
				addEntity(key);
				for(int j=0;j<t1.attributeList(key).size();j++){
					addAttribute(key,t1.attributeList(key).get(j));
				}
			}
			for(int i=0; i < keyArrayT2.size();i++){
				key = keyArrayT2.get(i);
				addEntity(key);
				for(int j=0;j<t2.attributeList(key).size();j++){
					addAttribute(key,t2.attributeList(key).get(j));
				}
			}
		}
	}
	
	/*
	Parameters: Table t1, Table t2
	Return: void
	Summary...
	Turns the current table into a Set Intersect between Table t1 and Table t2.
	WARNING: Only call this is the current table has nothing in it at the 
	start. If it has content already, it will simply add the Set Intersect to the 
	existing table. 
	*/
	void intersect(Table t1, Table t2){
		if(t1.attributeCheck(t2)==false){
			System.out.println("ERROR: Tables not compatible, no union made");
		}
		
		else{
			ArrayList<Integer> keyArrayT1 = t1.keyList();
			ArrayList<Integer> keyArrayT2 = t2.keyList();
			
			for(int i=0; i < keyArrayT1.size();i++){
				if(keyArrayT2.contains(keyArrayT1.get(i))==false){
					keyArrayT1.remove(keyArrayT1.get(i));
				}
			}
			for(int i=0; i < keyArrayT2.size();i++){
				if(keyArrayT1.contains(keyArrayT2.get(i))==false){
					keyArrayT2.remove(keyArrayT2.get(i));
				}
			}
			int key;
			for(int i=0; i < keyArrayT1.size();i++){
				key = keyArrayT1.get(i);
				addEntity(key);
				for(int j=0;j<t1.attributeList(key).size();j++){
					addAttribute(key,t1.attributeList(key).get(j));
				}
			}
			for(int i=0; i < keyArrayT2.size();i++){
				key = keyArrayT2.get(i);
				addEntity(key);
				for(int j=0;j<t2.attributeList(key).size();j++){
					addAttribute(key,t2.attributeList(key).get(j));
				}
			}
		}
	}
	
	/*
	Parameters: Table t1, Table t2
	Return: void
	Summary...
	Turns the current table into a Set Difference between Table t1 and Table t2.
	WARNING: Only call this is the current table has nothing in it at the 
	start. If it has content already, it will simply add the Set Difference to the 
	existing table. 
	*/
	void difference(Table t1, Table t2){
		if(t1.attributeCheck(t2)==false){
			System.out.println("ERROR: Tables not compatible, no difference made");
		}
		
		else{
			ArrayList<Integer> keyArrayT1 = t1.keyList();
			ArrayList<Integer> keyArrayT1dup = t1.keyList();
			ArrayList<Integer> keyArrayT2 = t2.keyList();
			for(int j=0; j < keyArrayT1dup.size(); j++){
				for(int i=0; i < keyArrayT1.size();i++){
				
					if(keyArrayT2.contains(keyArrayT1.get(i))){
						if(keyArrayT1.get(i) != -1){
							keyArrayT1.remove(i);
						}
						
					}
					
					
				}
			}
			
			
			
			int key;
			for(int i=0; i < keyArrayT1.size();i++){
				key = keyArrayT1.get(i);
				addEntity(key);
				for(int j=0;j<t1.attributeList(key).size();j++){
					addAttribute(key,t1.attributeList(key).get(j));
				}
			}
			
		}
	}
	
	/*
	Parameters: Table t1, String attribute type, String attribute value
	Return: void
	Summary...
	Turns the current table into a select Equals where it only adds an 
	entity if the attrValue equals its stored value in the attrType column.
	WARNING: Only call this is the current table has nothing in it at the 
	start. If it has content already, it will simply add the Select Equals to the 
	existing table. 
	*/
	void selectEQ(Table t1, String attrType, String attrValue){
		if(attrType.equals("-1")){
			ArrayList<Integer> tableKeys = t1.keyList();
			int value = -100;
			try{
				value = Integer.parseInt(attrValue);
			}
			catch(NumberFormatException e){
				System.err.println("Passed in attrValue is not an int in selectEQ");
			}
			if(tableKeys.contains(value)){
				addEntity(value);
				ArrayList<String> t1Attributes = t1.attributeList(TITLE_KEY);
			
				addEntity(TITLE_KEY);
				for(int j=0;j<t1Attributes.size();j++){
					addAttribute(TITLE_KEY,t1Attributes.get(j));
				}
				t1Attributes = t1.attributeList(value);
				addEntity(value);
				for(int j=0;j<t1Attributes.size();j++){
					addAttribute(value,t1Attributes.get(j));
				}
			}
		}
		else{
			ArrayList<String> t1Attributes = t1.attributeList(TITLE_KEY);
			ArrayList<Integer> tableKeys = t1.keyList();
			int attributeKeyIndex = -1;
			//Finds  the index of the attribute we care about
			for(int i=0; i<t1Attributes.size();i++){
				if(t1Attributes.get(i).equals(attrType)){
					attributeKeyIndex = i;
				}
			}
			//checks all of the values at that element to see if they meet
			//the requirement. 
			for(int i=0; i < tableKeys.size();i++){
				t1Attributes = t1.attributeList(tableKeys.get(i));
				if (tableKeys.get(i) == TITLE_KEY){
					addEntity(TITLE_KEY);
					for(int j=0;j<t1Attributes.size();j++){
						addAttribute(TITLE_KEY,t1Attributes.get(j));
					}
				}
				else{
					if(t1Attributes.get(attributeKeyIndex).equals(attrValue)){
						addEntity(tableKeys.get(i));
						for(int j=0;j<t1Attributes.size();j++){
							addAttribute(tableKeys.get(i),t1Attributes.get(j));
						}
					}
				}
				
			}
		}
		
	}
	
	/*
	Parameters: Table t1, String attribute type, String attribute value
	Return: void
	Summary...
	Turns the current table into a select NOT Equals where it only adds an 
	entity if the attrValue does not equal its stored value in the attrType column.
	WARNING: Only call this is the current table has nothing in it at the 
	start. If it has content already, it will simply add the Select NOT Equals 
	to the existing table. 
	*/	
	void selectNTEQ(Table t1, String attrType, String attrValue){
		if(attrType.equals("-1")){
			ArrayList<Integer> tableKeys = t1.keyList();
			int value = -100;
			try{
				value = Integer.parseInt(attrValue);
			}
			catch(NumberFormatException e){
				System.err.println("Passed in attrValue is not an int in selectEQ");
			}
			//Title row
			ArrayList<String> t1Attributes = t1.attributeList(TITLE_KEY);
			addEntity(TITLE_KEY);
			for(int j=0;j<t1Attributes.size();j++){
				addAttribute(TITLE_KEY,t1Attributes.get(j));
			}
			//content
			for(int i=0; i < tableKeys.size(); i++){
				if(tableKeys.get(i) != value){
					addEntity(tableKeys.get(i));
					
					t1Attributes = t1.attributeList(tableKeys.get(i));
					addEntity(tableKeys.get(i));
					for(int j=0;j<t1Attributes.size();j++){
						addAttribute(tableKeys.get(i),t1Attributes.get(j));
					}
				}
			}
			
			
		}
		else{
			ArrayList<String> t1Attributes = t1.attributeList(TITLE_KEY);
			ArrayList<Integer> tableKeys = t1.keyList();
			int attributeKeyIndex = -1;
			//Finds  the index of the attribute we care about
			for(int i=0; i<t1Attributes.size();i++){
				if(t1Attributes.get(i).equals(attrType)){
					attributeKeyIndex = i;
				}
			}
			//checks all of the values at that element to see if they meet
			//the requirement. 
			for(int i=0; i < tableKeys.size();i++){
				t1Attributes = t1.attributeList(tableKeys.get(i));
				if (tableKeys.get(i) == TITLE_KEY){
					addEntity(TITLE_KEY);
					for(int j=0;j<t1Attributes.size();j++){
						addAttribute(TITLE_KEY,t1Attributes.get(j));
					}
				}
				else{
					if(t1Attributes.get(attributeKeyIndex).equals(attrValue)==false){
						addEntity(tableKeys.get(i));
						for(int j=0;j<t1Attributes.size();j++){
							addAttribute(tableKeys.get(i),t1Attributes.get(j));
						}
					}
				}
				
			}
		}
		
	}
		
	/*
	Parameters: Table t1, String attribute type, String attribute value
	Return: void
	Summary...
	Turns the current table into a select Greater Than where it only adds an 
	entity if the attrValue is greater than its stored value in the attrType 
	column.
	WARNING: Only call this is the current table has nothing in it at the 
	start. If it has content already, it will simply add the Select Greater 
	Than to the existing table. 
	*/		
	void selectGT(Table t1, String attrType, String attrValue){
		if(attrType.equals("-1")){
			ArrayList<Integer> tableKeys = t1.keyList();
			int value = -100;
			try{
				value = Integer.parseInt(attrValue);
			}
			catch(NumberFormatException e){
				System.err.println("Passed in attrValue is not an int in selectEQ");
			}
			//Title row
			ArrayList<String> t1Attributes = t1.attributeList(TITLE_KEY);
			addEntity(TITLE_KEY);
			for(int j=0;j<t1Attributes.size();j++){
				addAttribute(TITLE_KEY,t1Attributes.get(j));
			}
			//content
			for(int i=0; i < tableKeys.size(); i++){
				if(tableKeys.get(i) > value){
					addEntity(tableKeys.get(i));
					
					t1Attributes = t1.attributeList(tableKeys.get(i));
					addEntity(tableKeys.get(i));
					for(int j=0;j<t1Attributes.size();j++){
						addAttribute(tableKeys.get(i),t1Attributes.get(j));
					}
				}
			}
			
			
		}
		else{
			ArrayList<String> t1Attributes = t1.attributeList(TITLE_KEY);
			ArrayList<Integer> tableKeys = t1.keyList();
			int attributeKeyIndex = -1;
			//Finds  the index of the attribute we care about
			for(int i=0; i<t1Attributes.size();i++){
				if(t1Attributes.get(i).equals(attrType)){
					attributeKeyIndex = i;
				}
			}
			//checks all of the values at that element to see if they meet
			//the requirement. 
			for(int i=0; i < tableKeys.size();i++){
				t1Attributes = t1.attributeList(tableKeys.get(i));
				if (tableKeys.get(i) == TITLE_KEY){
					addEntity(TITLE_KEY);
					for(int j=0;j<t1Attributes.size();j++){
						addAttribute(TITLE_KEY,t1Attributes.get(j));
					}
				}
				else{
					if(Integer.parseInt(t1Attributes.get(attributeKeyIndex))>Integer.parseInt(attrValue)){
						addEntity(tableKeys.get(i));
						for(int j=0;j<t1Attributes.size();j++){
							addAttribute(tableKeys.get(i),t1Attributes.get(j));
						}
					}
				}
				
			}
		}
		
	}
		
	/*
	Parameters: Table t1, String attribute type, String attribute value
	Return: void
	Summary...
	Turns the current table into a select Less Than where it only adds an 
	entity if the attrValue is less than its stored value in the attrType 
	column.
	WARNING: Only call this is the current table has nothing in it at the 
	start. If it has content already, it will simply add the Select Less
	Than to the existing table. 
	*/		
	void selectLT(Table t1, String attrType, String attrValue){
		if(attrType.equals("-1")){
			ArrayList<Integer> tableKeys = t1.keyList();
			int value = -100;
			try{
				value = Integer.parseInt(attrValue);
			}
			catch(NumberFormatException e){
				System.err.println("Passed in attrValue is not an int in selectEQ");
			}
			//Title row
			ArrayList<String> t1Attributes = t1.attributeList(TITLE_KEY);
			addEntity(TITLE_KEY);
			for(int j=0;j<t1Attributes.size();j++){
				addAttribute(TITLE_KEY,t1Attributes.get(j));
			}
			//content
			for(int i=0; i < tableKeys.size(); i++){
				if(tableKeys.get(i) < value){
					addEntity(tableKeys.get(i));
					
					t1Attributes = t1.attributeList(tableKeys.get(i));
					addEntity(tableKeys.get(i));
					for(int j=0;j<t1Attributes.size();j++){
						addAttribute(tableKeys.get(i),t1Attributes.get(j));
					}
				}
			}
			
			
		}
		else{
			ArrayList<String> t1Attributes = t1.attributeList(TITLE_KEY);
			ArrayList<Integer> tableKeys = t1.keyList();
			int attributeKeyIndex = -1;
			//Finds  the index of the attribute we care about
			for(int i=0; i<t1Attributes.size();i++){
				if(t1Attributes.get(i).equals(attrType)){
					attributeKeyIndex = i;
				}
			}
			//checks all of the values at that element to see if they meet
			//the requirement. 
			for(int i=0; i < tableKeys.size();i++){
				t1Attributes = t1.attributeList(tableKeys.get(i));
				if (tableKeys.get(i) == TITLE_KEY){
					addEntity(TITLE_KEY);
					for(int j=0;j<t1Attributes.size();j++){
						addAttribute(TITLE_KEY,t1Attributes.get(j));
					}
				}
				else{
					if(Integer.parseInt(t1Attributes.get(attributeKeyIndex))<Integer.parseInt(attrValue)){
						addEntity(tableKeys.get(i));
						for(int j=0;j<t1Attributes.size();j++){
							addAttribute(tableKeys.get(i),t1Attributes.get(j));
						}
					}
				}
				
			}
		}
	}	
		
	/*
	Parameters: Table t1, String attribute type, String attribute value
	Return: void
	Summary...
	Turns the current table into a select Greater Than Equal To where it 
	only adds an entity if the attrValue is greater than or equal to its 
	stored value in the attrType column.
	WARNING: Only call this is the current table has nothing in it at the 
	start. If it has content already, it will simply add the Select Greater 
	Than Equal To to the existing table. 
	*/	
	void selectGTET(Table t1, String attrType, String attrValue){
		if(attrType.equals("-1")){
			ArrayList<Integer> tableKeys = t1.keyList();
			int value = -100;
			try{
				value = Integer.parseInt(attrValue);
			}
			catch(NumberFormatException e){
				System.err.println("Passed in attrValue is not an int in selectEQ");
			}
			//Title row
			ArrayList<String> t1Attributes = t1.attributeList(TITLE_KEY);
			addEntity(TITLE_KEY);
			for(int j=0;j<t1Attributes.size();j++){
				addAttribute(TITLE_KEY,t1Attributes.get(j));
			}
			//content
			for(int i=0; i < tableKeys.size(); i++){
				if(tableKeys.get(i) >= value){
					addEntity(tableKeys.get(i));
					
					t1Attributes = t1.attributeList(tableKeys.get(i));
					addEntity(tableKeys.get(i));
					for(int j=0;j<t1Attributes.size();j++){
						addAttribute(tableKeys.get(i),t1Attributes.get(j));
					}
				}
			}
			
			
		}
		else{
			ArrayList<String> t1Attributes = t1.attributeList(TITLE_KEY);
			ArrayList<Integer> tableKeys = t1.keyList();
			int attributeKeyIndex = -1;
			//Finds  the index of the attribute we care about
			for(int i=0; i<t1Attributes.size();i++){
				if(t1Attributes.get(i).equals(attrType)){
					attributeKeyIndex = i;
				}
			}
			//checks all of the values at that element to see if they meet
			//the requirement. 
			for(int i=0; i < tableKeys.size();i++){
				t1Attributes = t1.attributeList(tableKeys.get(i));
				if (tableKeys.get(i) == TITLE_KEY){
					addEntity(TITLE_KEY);
					for(int j=0;j<t1Attributes.size();j++){
						addAttribute(TITLE_KEY,t1Attributes.get(j));
					}
				}
				else{
					if(Integer.parseInt(t1Attributes.get(attributeKeyIndex))>=Integer.parseInt(attrValue)){
						addEntity(tableKeys.get(i));
						for(int j=0;j<t1Attributes.size();j++){
							addAttribute(tableKeys.get(i),t1Attributes.get(j));
						}
					}
				}
				
			}
		}
	}		
		
	/*
	Parameters: Table t1, String attribute type, String attribute value
	Return: void
	Summary...
	Turns the current table into a select Greater Than Equal To where it 
	only adds an entity if the attrValue is greater than or equal to its 
	stored value in the attrType column.
	WARNING: Only call this is the current table has nothing in it at the 
	start. If it has content already, it will simply add the Select Greater 
	Than Equal To to the existing table. 
	*/		
	void selectLTET(Table t1, String attrType, String attrValue){
		if(attrType.equals("-1")){
			ArrayList<Integer> tableKeys = t1.keyList();
			int value = -100;
			try{
				value = Integer.parseInt(attrValue);
			}
			catch(NumberFormatException e){
				System.err.println("Passed in attrValue is not an int in selectEQ");
			}
			//Title row
			ArrayList<String> t1Attributes = t1.attributeList(TITLE_KEY);
			addEntity(TITLE_KEY);
			for(int j=0;j<t1Attributes.size();j++){
				addAttribute(TITLE_KEY,t1Attributes.get(j));
			}
			//content
			for(int i=0; i < tableKeys.size(); i++){
				if(tableKeys.get(i) <= value){
					addEntity(tableKeys.get(i));
					
					t1Attributes = t1.attributeList(tableKeys.get(i));
					addEntity(tableKeys.get(i));
					for(int j=0;j<t1Attributes.size();j++){
						addAttribute(tableKeys.get(i),t1Attributes.get(j));
					}
				}
			}
			
			
		}
		else{
			ArrayList<String> t1Attributes = t1.attributeList(TITLE_KEY);
			ArrayList<Integer> tableKeys = t1.keyList();
			int attributeKeyIndex = -1;
			//Finds  the index of the attribute we care about
			for(int i=0; i<t1Attributes.size();i++){
				if(t1Attributes.get(i).equals(attrType)){
					attributeKeyIndex = i;
				}
			}
			//checks all of the values at that element to see if they meet
			//the requirement. 
			for(int i=0; i < tableKeys.size();i++){
				t1Attributes = t1.attributeList(tableKeys.get(i));
				if (tableKeys.get(i) == TITLE_KEY){
					addEntity(TITLE_KEY);
					for(int j=0;j<t1Attributes.size();j++){
						addAttribute(TITLE_KEY,t1Attributes.get(j));
					}
				}
				else{
					if(Integer.parseInt(t1Attributes.get(attributeKeyIndex))<=Integer.parseInt(attrValue)){
						addEntity(tableKeys.get(i));
						for(int j=0;j<t1Attributes.size();j++){
							addAttribute(tableKeys.get(i),t1Attributes.get(j));
						}
					}
				}
				
			}
		}
	}		
	
	/*
	Parameters: N/A
	Return: String
	Summary...
	Simply returns a String representation of the table data. This can be used for neat 
	and ordered output. 
	NOTE: You do not have to type toString for this to work. Simply output the table itself.
	*/		
	public String toString(){
		ArrayList<String> t1Attributes = attributeList(TITLE_KEY);
		ArrayList<Integer> tableKeys = keyList();	
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		
		/////Print Header Line/////
		String s0 = String.format("%-16.16s", "Primary Key");
		sb.append(s0);

		for(int i = 0; i < attributeCount(); i++){
			String s1 = String.format("%-16.16s", "Attribute");
			sb.append(s1);
			sb.append("\t");
		}

		sb.append("\n");

		/////Print Keys and Attribute values/////
		for(int i = 0; i < tableKeys.size(); i++){
			String s2 = String.format("%-16.16s", tableKeys.get(i));
			sb.append(s2);

			t1Attributes = attributeList(tableKeys.get(i));
			for(int j = 0; j < attributeCount(); j++){
				String s3 = String.format("%-16.16s", t1Attributes.get(j));
				sb.append(s3);
				sb.append("\t");
			}
			sb.append("\n");
		}

		sb.append("\n");

		
		/////Print out Whole Table/////
		return sb.toString();
	}
	
	/*
	Parameters: Object t2
	Return: Boolean
	Summary...
	Determines of the current table is equal (by value) to Object t2
	Used with assertEquals()
	*/	
	public boolean equals(Object t2){
		if(t2.toString().equals(this.toString())){
			return true;
		}
		return false;
	}
		
}