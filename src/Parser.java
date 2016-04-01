/* 
 * Auther1: Abhijet Deshpande
 * ID: 50168465
 * Author2: MD S Q Zulkar Nine  
 * ID : 50131471
 * CSE 505
 * Assignment 01
 */

/* 		OBJECT-ORIENTED PARSER FOR SIMPLE EXPRESSIONS
 * program -> decls stmts end
 * decls -> int idlist ';'
 * idlist -> id [',' idlist ]
 * stmts -> stmt [ stmts ]
 * stmt -> assign ';'| cmpd | cond | loop
 * assign -> id '=' expr
 * cmpd -> '{' stmts '}'
 * cond -> if '(' rexp ')' stmt [ else stmt ]
 * loop -> for '(' [assign] ';' [rexp] ';' [assign] ')' stmt

 * rexp -> expr ('<' | '>' | '==' | '!= ') expr
 * expr -> term [ ('+' | '-') expr ]
 * term -> factor [ ('*' | '/') term ]
 * factor -> int_lit | id | '(' expr ')'

 */



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;



public class Parser {
	public static void main(String[] args)  {
		System.out.println("Enter program and terminate with 'end'!\n");

		// Starts program parsing here 
		new Program();

		// Print the byte code 
		Code.output();
	}
}

class ByteCodes{
	public static final String CODE_RETURN = "return";
	public static final String CODE_ISTORE = "istore";
	public static final String CODE_ISTORE_ = "istore_";
	public static final String CODE_ILOAD = "iload";
	public static final String CODE_ILOAD_ = "iload_";
	public static final String CODE_GOTO = "goto";
	public static final String CODE_BIPUSH = "bipush";
	public static final String CODE_SIPUSH = "sipush";
	public static final String CODE_ICONST_ = "iconst_";

}


/*
 * Grammer : 
 * program -> decls stmts end
 */
class Program {
	Decls d;
	Stmts s;

	public Program() {

		// This lex is getting 'int'
		Lexer.lex();

		// Parsing the declarations 
		if (Lexer.nextToken == Token.KEY_INT){
			d = new Decls();
		}

		// Parsing the statements 
		if (Lexer.nextToken == Token.ID ||
				Lexer.nextToken == Token.KEY_IF ||
				Lexer.nextToken == Token.LEFT_BRACE ||
				Lexer.nextToken == Token.KEY_FOR ){

			s = new Stmts();
		}


		// This is generating byte code for 'return'
		if (Lexer.nextToken == Token.KEY_END) {
			Code.bytes = 1;
			Code.gen(ByteCodes.CODE_RETURN);
		}
	}
}


//added: 9/21/15:9:11pm
//construct Decls class
// Grammer: 
class Decls {
	Idlist i;

	public Decls() {
		if (Lexer.nextToken == Token.KEY_INT) {

			// Getting the id
			Lexer.lex();

			// Parsing the Idlist 
			i = new Idlist();

			// Skip over the semicolon 
			if (Lexer.nextToken == Token.SEMICOLON) {
				Lexer.lex();
			}
		}
	} // end of Constructor Decls
}  // end of Decls 



//added: 9/21/15:9:11pm
//construct Idlist class
//commented: 9/23/15:10:29pm
// Grammer : idlist -> id [',' idlist ]
class Idlist {

	Idlist i;
	public Idlist() {

		if (Lexer.nextToken == Token.ID) {

			// Storing values to hash map
			Code.storeVariable(Lexer.ident);

			// consuming the comma or semicolon
			Lexer.lex();  

			if (Lexer.nextToken == Token.COMMA) {
				Lexer.lex();
				i = new Idlist();
			}	 

		}
	}//end of constructor - Idlist
} // end of class Idlist


//added: 9/21/15:7:17pm
//construct Stmts class
class Stmts {
	Stmt s;
	Stmts ss;

	public Stmts() {
		s = new Stmt();
		if (Lexer.nextToken == Token.ID || 
				Lexer.nextToken == Token.KEY_FOR ||
				Lexer.nextToken == Token.KEY_IF || 
				Lexer.nextToken == Token.LEFT_BRACE ) {
			ss = new Stmts();
		}
	} // end of Stmts() - constructor 
} // end of Stmt class


//added: 9/21/15:4:10pm
//construct Statement class
//Grammer: stmt -> assign ';'| cmpd | cond | loop
class Stmt {
	Assign a;
	Loop l;
	Cond c;
	Cmpd cm;

	public Stmt() {
		
		if (Lexer.nextToken == Token.ID){
			a = new Assign();
			if (Lexer.nextToken == Token.SEMICOLON) {
				Lexer.lex();
			}
		}
		else if (Lexer.nextToken == Token.KEY_FOR){
			l = new Loop();
		}
		else if (Lexer.nextToken == Token.KEY_IF){
			c = new Cond();
		}
		else if (Lexer.nextToken == Token.LEFT_BRACE){
			cm = new Cmpd(); 
		}

	} // end of Stmt() - constructor 
}  // end of class Stmt


//added: 9/21/15: 10:40pm 
//VERSION : 02 (Latest)
//Grammer : assign -> id '=' expr
class Assign {
	Character c;
	Expr e;

	public Assign() {
		if (Lexer.nextToken == Token.ID) {
			c = Lexer.ident;
			Lexer.lex();
			if (Lexer.nextToken == Token.ASSIGN_OP) {
				Lexer.lex();
				e = new Expr();

				if ( Code.getIndex(c) > 3 ){
					Code.bytes = 2;
					Code.gen(ByteCodes.CODE_ISTORE + " " + Code.getIndex(c));
				}
				else{
					Code.bytes = 1;
					Code.gen(ByteCodes.CODE_ISTORE_ + Code.getIndex(c));
				}

			}
		}
	} // end of Assign() - constructor 
} // end of Assign class


//added : 9/21/15:9:41pm 
//Construct Compound Statement class
//Grammer : cmpd -> '{' stmts '}'
class Cmpd {
	Stmts s;

	public Cmpd() {
		if (Lexer.nextToken == Token.LEFT_BRACE) {
			Lexer.lex();
			s = new Stmts();
			if (Lexer.nextToken == Token.RIGHT_BRACE)
				Lexer.lex();
		}
	}
}


//added : 9/20/15 : 9:57pm
//Construct the Conditional statement
// Grammer : cond -> if '(' rexp ')' cmpd [ else cmpd ]
class Cond {
	Rexpr rexp;
	Stmt stmt1,stmt2;

	
	public Cond() {
		int jumpAddress;
		if (Lexer.nextToken == Token.KEY_IF) {
			Lexer.lex();
			if (Lexer.nextToken == Token.LEFT_PAREN) {
				Lexer.lex();
				rexp = new Rexpr();
				if (Lexer.nextToken == Token.RIGHT_PAREN) {
					Lexer.lex();
					stmt1 = new Stmt();
					if (Lexer.nextToken == Token.KEY_ELSE) {
						Lexer.lex();
						jumpAddress = Code.getCodeInstCounter();
						Code.bytes = 3;
						Code.gen(ByteCodes.CODE_GOTO);
						Code.appendAddress(Code.getInstCounterValue(), rexp.offsetAdr);
						stmt2 = new Stmt();
						Code.appendAddress(Code.getInstCounterValue(), jumpAddress);
					}
					else
						Code.appendAddress(Code.getInstCounterValue(), rexp.offsetAdr);
				}
			}
		}
	}// end of Cond() - constructor
}// end of Cond class


//added : 9/21/15:4:10pm 
//Construct Loop class
//Grammer : loop -> for '(' [assign] ';' [rexp] ';' [assign] ')' stmt
class  Loop{
	Rexpr r;
	Assign a1,a2;
	Stmt s;
	int instptr;
	boolean assignExecuted = false;
	public Loop() {
		if (Lexer.nextToken == Token.KEY_FOR) {

			// consume parenthesis
			Lexer.lex();

			if (Lexer.nextToken == Token.LEFT_PAREN) {
				Lexer.lex();   // id or semicolon 

				if(Lexer.nextToken != Token.SEMICOLON){   // lex got id
					a1 = new Assign();
				}

				Lexer.lex(); // rexp or semicolon
				if (Lexer.nextToken != Token.SEMICOLON) {  // rexp

					r = new Rexpr();
				}

				Lexer.lex();  // assignment or right paren


				if (Lexer.nextToken != Token.RIGHT_PAREN){
					Code.sendLoopCodeToTempStack = true;					
					Code.tempStackInstCount = 0;
					a2 = new Assign();

					Code.pointerArray.push(Code.tempStackInstCount);

					Code.sendLoopCodeToTempStack = false;
					assignExecuted = true;
				}


				Lexer.lex();   // stmt 
				s = new Stmt();
				if (assignExecuted){
					Code.transferCode = true;
					Code.gen("");
					Code.transferCode = false;
				}

				Code.bytes = 3;
				Code.gen(ByteCodes.CODE_GOTO + " " + r.compareAdr);
				Code.appendAddress(Code.getInstCounterValue(), r.offsetAdr);

			}
		}
	}
}


//added : zulkar : 9/21/15 : 10:30pm
//VERSION: 02 (Latest)
// Grammer :// rexp -> expr ('<' | '>' | '==' | '!= ') expr
class Rexpr {
	Expr e1, e2;
	char operand;
	int offsetAdr, compareAdr;

	public Rexpr() {
		compareAdr = Code.getInstCounterValue();
		e1 = new Expr();
		if (Lexer.nextToken == Token.LESSER_OP ||
				Lexer.nextToken == Token.GREATER_OP ||
				Lexer.nextToken == Token.EQ_OP ||
				Lexer.nextToken == Token.NOT_EQ) {
			operand = Lexer.nextChar;
			Lexer.lex();
			e2 = new Expr();
			offsetAdr = Code.getCodeInstCounter();
			Code.bytes = 3;
			Code.gen(Code.opcode(operand));
		}
	}
}

class Expr {
	Term t;
	Expr e;
	char op;

	public Expr() {
		t = new Term();
		if (Lexer.nextToken == Token.ADD_OP || Lexer.nextToken == Token.SUB_OP) {
			op = Lexer.nextChar;
			Lexer.lex();
			e = new Expr();
			Code.bytes = 1;
			Code.gen(Code.opcode(op));
		}
	}
}

class Term {  
	Factor f;
	Term t;
	char op;

	public Term() {
		f = new Factor();
		if (Lexer.nextToken == Token.MULT_OP || Lexer.nextToken == Token.DIV_OP) {
			op = Lexer.nextChar;
			Lexer.lex();
			t = new Term();
			Code.bytes = 1;
			Code.gen(Code.opcode(op));
		}
	}
}

class Factor {  
	Expr e;
	int i;
	char ch;

	public Factor() {
		switch (Lexer.nextToken) {
		case Token.INT_LIT:
			i = Lexer.intValue;
			Code.gen(Code.intcode(i));

			Lexer.lex();
			break;
		case Token.ID:
			ch = Lexer.ident;
			Code.bytes = 1;
			if (Code.getIndex(ch) > 3 ){
				Code.bytes = 2;
				Code.gen(ByteCodes.CODE_ILOAD + " " + Code.getIndex(ch));
			}
			else {
				Code.bytes = 1;
				Code.gen(ByteCodes.CODE_ILOAD_ + Code.getIndex(ch));

			}

			Lexer.lex();
			break;
		case Token.LEFT_PAREN: // '('
			Lexer.lex();
			e = new Expr();
			Lexer.lex(); // skip over ')'
			break;
		default:
			break;
		}
	}
}

class Code {
	public static int bytes = 0;
	static String[] code = new String[1000];
	static int codeptr = 0;
	// Hash map to store the variables and index
	static Map<Character, Integer> variableMap = new HashMap<Character, Integer>();

	// index for next instruction
	static int nextIndex = 1;

	// counter to measure the bytecode length
	static int instCounter = 0;

	public static boolean sendLoopCodeToTempStack = false;
	public static Stack<String> loopCode = new Stack<String>(); //Instructions for loop counter
	public static Stack<Integer> pointerArray = new Stack<Integer>(); //No of Instructions to be appended
	public static boolean transferCode = false;
	public static int tempStackInstCount = 0;
	public static List<String> tempStringStore = new ArrayList<String>();


	public static void gen(String s) {

		if( !sendLoopCodeToTempStack ){  // writing the main byte code
			if ( !s.isEmpty() ){
				code[codeptr] = instCounter + ": " + s;   // insert new instruction
				codeptr++;   // increment array offset
				instCounter += bytes;    // increment IP according to the instruction's size
			}

			if(transferCode)
			{		

				// poping Assign code of for loop to a temp Array
				tempStackInstCount = Code.pointerArray.pop();
				int i;
				for( i=0; i<tempStackInstCount; i++)
				{
					tempStringStore.add(loopCode.pop());
				}
				// reversing the array 
				Collections.reverse(tempStringStore);

				for(int j=0; j<tempStackInstCount; j++)
				{
					code[codeptr] = instCounter + ": "+tempStringStore.get(j);
					codeptr++;
					// call function
					bytes = getByteForTempStack(tempStringStore.get(j));
					instCounter += bytes; 
				}
			}

		}
		else{   // saving loop assign code to temp loop stack
			loopCode.push(s);
			tempStackInstCount++;
		}
	}

	// Get the length of instruction in bytes from the stack
	public static int getByteForTempStack(String a){
		String[] tokens = a.split(" ");
		if (tokens.length == 1) {
			return 1;
		}
		else {
			if (tokens[0].contains(ByteCodes.CODE_ILOAD)){				
				if (Integer.parseInt(tokens[1]) > 3 ){
					return 2;
				}				
			}
			if (tokens[0].contains(ByteCodes.CODE_ISTORE)){				
				if (Integer.parseInt(tokens[1]) > 3 ){
					return 2;
				}				
			}			
			if (tokens[0].contains(ByteCodes.CODE_SIPUSH)){				
				return 3;				
			}
			if (tokens[0].contains(ByteCodes.CODE_BIPUSH)){				
				return 2;				
			}
		}
		return 0;
	}

	//modified the existing code to accomodate byte values
	public static String intcode(int i) {
		if (i > 127) {
			bytes = 3;
			return ByteCodes.CODE_SIPUSH + " " + i;
		}
		if (i > 5) {
			bytes = 2;
			return ByteCodes.CODE_BIPUSH + " "+ i;
		}
		bytes = 1;
		return ByteCodes.CODE_ICONST_ + i;
	}
	

	// Returns offset for code counter
	public static int getCodeInstCounter() {
		return codeptr;
	}

	// Gives current value of Inst Counter 
	public static int getInstCounterValue() {
		return instCounter;
	}

	// Gives the index of the variable from Variable Map
	public static int getIndex(Character variable) {
		return variableMap.get(variable);
	}

	// Get address value for GOTO jump
	public static void appendAddress(int adr, int ptr) {
		code[ptr] = code[ptr] + " " + adr;
	}


	// Adds a new value to the Variable Map
	public static void storeVariable(Character c) {
		variableMap.put(c, nextIndex);
		nextIndex++;
	}
	
	public static String opcode(char op) {
		switch(op) {
		case '+' : return "iadd";
		case '-':  return "isub";
		case '*':  return "imul";
		case '/':  return "idiv";
		// added: 9/20/15 : 3:22
		case '<':  return "if_icmpge";
		case '>':  return "if_icmple";
		case '=':  return "if_icmpne";
		case '!':  return "if_icmpeq";
		default: return "";
		}
	}
	
	public static void output() {
		for (int i=0; i<codeptr; i++)
			System.out.println(code[i]);
	}
}


