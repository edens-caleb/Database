/*
----------------------------------------------------------------------------
Condition.java
Handles the formatting of query conditions, using the given condition grammar:
condition ::= conjunction { || conjunction }
conjunction ::= comparison { && comparison }
comparison ::= operand op operand
                         | ( condition )
op ::= == | != | < | > | <= | >=
operand ::= attribute-name | literal
attribute-name ::= identifier
literal ::= integer | " string-literal "
integer ::= digit { digit }
----------------------------------------------------------------------------
There are three classes that work together to form the condition for a query operation:
 * Condition - Holds a Conjunction, or two Conjunctions (represents logical OR)
 * Conjunction - Holds a Comparison, or two Comparisons (represents logical AND)
 * Comparison - Holds an operator and its two operands, or a Condition (in case logic needs to be nested or is out of this order)
 
A few things to note:
 1. ALWAYS start with a Condition, and work your way down as needed. The expression will be uglier, but most Engine commands will require a Condition object initially
 2. Similarly, ensure that every Condition ends with Comparisons
 3. Be careful with nested logic, make sure you are properly cycling through Condition -> Conjunction -> Comparison -> Condition -> etc...
 4. Currently, Condition can only handle Strings, handling generics (or at the very least integers) is a TODO
Finally a few examples:
If we wanted to represent the condition:
	(x == 5) OR (y <= 3)
We could do represent it like so:
	new Condition( new Conjunction(new Comparison("x", "==", "5") ), new Conjunction( new Comparison("y", "<=", "3") ) )
However, things get a bit uglier when an OR (Condition) is nested below an AND (Conjunction)...
For example:
	((x == 5) AND ((y > 4) OR (z <= 3)))
Would be:
	new Condition( new Conjunction( new Comparison("x", "==", "5") , new Comparison( new Condition( new Conjunction( new Comparison("y", ">", "4") ), new Conjunction( new Comparison("z", "<=", "3") ) ) ) ) )
*/



class Condition {
	
	//Data Members
	private Conjunction conj1, conj2;
	
	//Constructors
	public Condition(Conjunction c1) {
		this.conj1 = c1;
		this.conj2 = null;
	}
	
	public Condition(Conjunction c1, Conjunction c2) {
		this.conj1 = c1;
		this.conj2 = c2;
	}
	
	//Getters
	public Conjunction getConj1() {
		return this.conj1;
	}
	
	public Conjunction getConj2() {
		return this.conj2;
	}
	
	//toString
	@Override
	public String toString() {
		//if the second conjunction is null, we are not performing an OR operation
		if(conj2 == null)
			return this.conj1.toString();
		else
			return "(" + this.conj1.toString() + " OR " + this.conj2.toString()+ ")";
	}
	
	//main, for testing purposes
	public static void main(String[] args)
	{
		Comparison comp1 = new Comparison("major", "==", "computer science"); //x
		Comparison comp2 = new Comparison("age", ">=", "18"); //y
		
		Conjunction conj1 = new Conjunction(comp1);
		Conjunction conj2 = new Conjunction(comp2);
		
		Condition c1 = new Condition(conj1, conj2);
		
		Comparison comp3 = new Comparison("age", ">=", "21"); //z
		
		//Conjunction conj3 = new Conjunction(comp3);
		Conjunction conj3 = new Conjunction(comp3);
		
		Condition c2 = new Condition(conj1, conj3);
		
		Comparison comp4 = new Comparison(c1);
		Comparison comp5 = new Comparison(c2);
		
		Conjunction conj5 = new Conjunction(comp4,comp5);
		
		Condition c3 = new Condition(conj5);
		Condition c4 = new Condition(new Conjunction(new Comparison("school","==","texas a&m")), conj5);
		
		//System.out.println(c1);
		//System.out.println(c2);
		//System.out.println(c3);
		//((school == texas a&m) OR (((major == computer science) OR (age >= 18)) AND ((major == computer science) OR (age >= 21))))
		System.out.println(c4);
		
		//((x == 5) OR (y <= 3))
		System.out.println(new Condition( new Conjunction(new Comparison("x", "==", "5") ), new Conjunction( new Comparison("y", "<=", "3") ) ));
		
		//((x == 5) AND ((y > 4) OR (z <= 3)))
		System.out.println(new Condition( new Conjunction( new Comparison("x", "==", "5") , new Comparison( new Condition( new Conjunction( new Comparison("y", ">", "4") ), new Conjunction( new Comparison("z", "<=", "3") ) ) ) ) ) );
		
		
		
	}
	
	
}
 
class Conjunction {
	//Data Members
	private Comparison comp1, comp2;

	
	//Constructors
	public Conjunction(Comparison c1) {
		this.comp1 = c1;
		this.comp2 = null;
	}
	
	public Conjunction(Comparison c1, Comparison c2) {
		this.comp1 = c1;
		this.comp2 = c2;
	}
	
	
	//Getters
	public Comparison getComp1() {
		return this.comp1;
	}
	
	public Comparison getComp2() {
		return this.comp2;
	}
	
	//toString
	@Override
	public String toString() {
		//if the second conjunction is null, we are not performing an AND operation
		if(comp2 == null)
			return this.comp1.toString();
		else
			return "(" + this.comp1.toString() + " AND " + this.comp2.toString() + ")";
	}
} 

class Comparison {
	//Data Members
	private String operator;
	private String operand1, operand2;
	private Condition cond;
	//contains all valid operators for Comparison objects
	public static String[] valid_ops = {"==", "!=", "<", ">", "<=", ">="};
	
	//Constructors
	public Comparison(String oprnd1, String op, String oprnd2) {
		//determine if operator is valid
		boolean valid_op = false;
		for (String s : Comparison.valid_ops) {
			if(op.equals(s)) {
				valid_op = true;
				break;
			}
		}
		if(!valid_op){
			System.err.println("Comparison constructor failed!\nInvalid operator: " + op);
			return;
		}
		else {
			this.operand1 = oprnd1;
			this.operator = op;
			this.operand2 = oprnd2;
			this.cond = null;
		}
	}
	
	public Comparison(Condition c)
	{
		this.operator = this.operand1 = this.operand2 = null;
		this.cond = c;
	}
	
	//Getters
	public String getOperand1() {
		return this.operand1;
	}
	
	public String getOperand2() {
		return this.operand2;
	}
	
	public String getOperator() {
		return this.operator;
	}
	
	public Condition getCondition() {
		return this.cond;
	}
	
	//toString
	@Override
	public String toString() {
		if(this.cond == null)
			return "(" + this.operand1.toString() + " " + this.operator.toString() + " " + this.operand2.toString() + ")";
		else
			return this.cond.toString();
	}
}
