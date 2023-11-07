package java;
import static java.lang.System.*;

import java.util.Arrays;
import java.util.Scanner;
import java.io.IOException;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaCompiler.CompilationTask;

public class BooleanWork {
	/**
	 * gimme a clean string version of a boolean
	 * 
	 * @param x - boolean to convert
	 * @return String
	 */
	public static String S(boolean x) {
		return x ? "T" : "F";
	}
	
	/**
	 * creates a java file from a string, use this to make java execute the boolean expression for us
	 * 
	 * @return SimpleJavaFileObject
	 */
	public static SimpleJavaFileObject getJavaFileContentAsString(String exp) {
		// code to execute
		StringBuilder javaFileContents = new StringBuilder(
			"public class ExpressionEvaluator {" +
			"	public static void main(String[] args) {" +
			"		boolean result = " + exp + ";" +
			"		System.out.println(result);" +
			"	}" +
			"}"
		);
		
		JavaObjectFromString javaFileObject = null;
		
		// try to convert it, the only 2 reasons this will fail is if the expression is invalid
		// or if there's something i don't know about how other operating systems run java
		try {
			javaFileObject = new JavaObjectFromString("ExpressionEvaluator.java", javaFileContents.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return javaFileObject;
	}
	
	/**
	 * pretty print for boolean 2d array specifically as a truth table
	 * 
	 * @param table - boolean 2d array to print
	 * @param numVars - number of variables in the boolean expression
	 */
	public static void printBooleanTable(boolean [][] table, int numVars) {
		// print the variables out, convenience ideal dictates using the index to convert
		// the rest is just string formatting
		for (int i = 0; i < numVars; i++) {
			out.print((i == 0 ? "\n " : "") + (char) (i + 65) + (i != numVars - 1 ? " " : " |\n"));
		}
		
		out.println("-" + "--".repeat(numVars + 2)); // separator
		
		// go through each row
		for (int r = 0; r < table.length; r++) {
			// go through each column in the current row, simple string formatting
			for (int c = 0; c < numVars; c++) {
				out.print((c == 0 ? " " : "") + S(table[r][c]) + (c != numVars - 1 ? " " : (" | " + S(table[r][numVars]) + "\n")));
			}
		}
		out.println();
	}
	
	/**
	 * create all possible combinations of true/false values for the given number of variables
	 * 
	 * @param numVars - number of boolean variables
	 * @return boolean[][] 
	 */
	public static boolean[][] getAllCombos(int numVars) {
		// need 2^numVars rows bc that's how many ways there are to
		// combine 2 elements (true, false) in `numVar` ways
		int numRows = (int) Math.pow(2, numVars);
		
		// need a column for each variable and one extra to store each result
		boolean [][] table = new boolean[numRows][numVars + 1];
		
		for (int r = 0; r < numRows; r++) {
			// converting each row index to binary will provide each possible combination in order,
			// use bitwise OR with (1 << numVars) in order to create a binary string that is
			// padded to `numVar` digits so that we have a value for each variable even if the
			// number itself is too small
			String binString = Integer.toBinaryString((1 << numVars) | r).substring(1);
			
			// zero represents true and one represents false
			// so just check if each character is equal to zero
			for (int var = 0; var < numVars; var++) {
				table[r][var] = binString.charAt(var) == '0';
			}
		}
		
		return table;
	}
	
	/**
	 * this is cheating, flat out (but plz parsing boolean expressions is so harddddd).
	 * creates a java file using the expression as essentially the main line of the file.
	 * compiles the file, runs it, and retrieves the output.
	 * 
	 * @param exp - the boolean expression
	 * @return String
	 * @throws Exception
	 */
	public static String executeProcess(String exp) throws Exception {
		// daddy oracle was kind enough to give us a compiler tool
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
		
		Iterable<? extends JavaFileObject> fileObjects = Arrays.asList(getJavaFileContentAsString(exp));
		
		// use a decoy StringWriter so that the standard output isn't cluttered with compilation errors
		CompilationTask task = compiler.getTask(new StringWriter(), fileManager, null, null, null, fileObjects);
		
		// the magic of compilation
		Boolean result = task.call();
		
		if (result) {
			try {
				// the magic of execution
				Process pro = Runtime.getRuntime().exec(new String[] {"java", "ExpressionEvaluator"});
				
				// there's so much involved in reading an inputstream for no reason
				// literally all this does is unpack the output from the previous execution
				return new BufferedReader(new InputStreamReader(pro.getInputStream())).readLine();
			} catch (IOException ioe) {
				// not sure exactly what could go wrong to get us here, my gut says it'd
				// be something close to a file not found error
				throw new Exception("something else went wrong, oh well");
			}
		} else {
			// if result is false then we actually got an error by calling the task
			// which can ONLY mean the boolean expression couldn't be compiled
			throw new Exception("boolean expression is invalid");
		}
	}
	
	/**
	 * evaluate the boolean expressions row by row in the truth table, modifying as we go
	 * 
	 * @param table - 2d boolean array that holds the truth table
	 * @param exp - boolean expression to evaluate
	 * @return boolean[][]
	 */
	public static boolean[][] evaluate(boolean[][] table, String exp) throws Exception {
		for (int row = 0; row < table.length; row++) {
			String rowCalc = exp;
			
			// set up the string for execution
			// replace all the variables relative to their position in the truth table
			for (int elIndex = 0; elIndex < table[row].length - 1; elIndex++) {
				rowCalc = rowCalc.replace("" + (char)(elIndex + 65), table[row][elIndex] ? "true" : "false");
			}
			
			try {
				String res = executeProcess(rowCalc);
				// check if the output of the execution is "true"
				table[row][table[row].length - 1] = res.equals("true");
			} catch (Exception e) {
				// the largest possible reason for this exception to have occurred is that
				// the expression was invalid so just say that and hope for the best
				throw new Exception("boolean expression is invalid");
			}
		}
		
		return table;
	}
	
	public static void main (String [] args) {
		Scanner scan = new Scanner(System.in); // "sum of the forces equals m a"
		
		int numVars; // store the number of boolean variables in here
		String expression; // store the boolean algebra expression in here
		boolean [][] table; // store the boolean table in here
		boolean isFirst = true;
		
		main: while (true) {
			// reset all the variables without creating new instances, saves memory (i think)
			numVars = 0;
			expression = "";
			table = null;
			
			if (isFirst) {
				out.println("use an integer greater than 0 for the number of boolean variables");
				out.println("enter \"q\" at any time to quit");
			}
			// get the number of variables for the problem
			numVarLoop: while (true) {
				out.print("enter the number of variables: ");
				String tempNumVars = scan.nextLine();
				
				// we're quitting so break out of main
				if (tempNumVars.equals("q")) {
					break main;
				}
				
				// number of variables should be an integer greater than 0
				try {
					numVars = Integer.parseInt(tempNumVars);
					if (numVars < 1) throw new NumberFormatException();
					break numVarLoop;
				} catch (NumberFormatException nfe) {
					out.println("please enter a valid integer greater than 0");
				}
			}
			
			// set up the table
			table = getAllCombos(numVars);
			
			if (isFirst) {
				out.println("\nuse the capital letters A through Z as variable names");
				out.println("enter \"b\" to go back");
			}
			// get the boolean algebra expression
			out.print("enter the boolean expression: ");
			expression = scan.nextLine();
			
			// the back command will just let us input a different number of variables
			if (expression.equals("b")) {
				continue;
			} else if (expression.equals("q")) {
				break main;
			}
			
			// catch any errors, just let the user know and restart
			try {
				table = evaluate(table, expression);
			} catch (Exception e) {
				out.println("\nthe boolean expression is invalid or something else went wrong, try again\n\n" + "-".repeat(30) + "\n");
				continue;
			}
			
			// printy boi
			printBooleanTable(table, numVars);
			
			// separator
			out.println("-".repeat(30) + "\n");
			
			// no longer on the first iteration, don't want instructions again
			if (isFirst) isFirst = false;
		}
		
		out.println("\n----------\n\nbuh bye");
		scan.close();
	}
}