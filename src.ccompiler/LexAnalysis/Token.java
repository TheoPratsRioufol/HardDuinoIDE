package LexAnalysis;

import General.CompilerException;
import General.Parameters;

public class Token extends SuperToken {

	private String str;
	private String type;
	private int lineNumber;
	private int numericValue;
	
	static int getNumFromBinStr(String str) {
		if ((str.length() > 2) && (str.charAt(0) == '0') && (str.charAt(1) == 'b')) {
			char ch;
			int num = 0;
			for (int i = 2; i < str.length(); i++) {
				ch = str.charAt(i);
				if (ch == '1') {
					num += 1 << (str.length()-1-i);
				} else if (ch != '0') {
					return -1;
				}
			}
			return num;
		}
		return -1;
	}
	
	static int getNumFromHexStr(String str) {
		if ((str.length() > 2) && (str.charAt(0) == '0') && (str.charAt(1) == 'x')) {
			char ch;
			int num = 0;
			for (int i = 2; i < str.length(); i++) {
				ch = str.charAt(i);
				if ((ch >= '0') && (ch <= '9')) {
					num += ((int) ch - (int) '0') * (int) Math.pow(16,(str.length()-1-i));
				} else if ((ch >= 'a') && (ch <= 'f')) {
					num += ((int) ch - (int) 'a' + 10) * (int) Math.pow(16,(str.length()-1-i));
				} else if ((ch >= 'A') && (ch <= 'F')) {
					num += ((int) ch - (int) 'A' + 10) * (int) Math.pow(16,(str.length()-1-i));
				} else {
					return -1;
				}
			}
			return num;
		}
		return -1;
	}
	
	public int size() {
		return 0;
	}
	
	public boolean isNumeric(String str) { 
		if (str.equals("true")) {
			numericValue = 1;
			return true;
		} else if (str.equals("false")) {
			numericValue = 0;
			return true;
		} else if ((str.length() == 3) && (str.charAt(0) == '\'') && (str.charAt(2) == '\'')) {
			numericValue = (int) str.charAt(1);
			return true; // c'est un char
		} else if (getNumFromBinStr(str) >= 0) {
			numericValue = getNumFromBinStr(str);
			return true;
		}else if (getNumFromHexStr(str) >= 0) {
			numericValue = getNumFromHexStr(str);
			return true;
		}
		  try {  
			  numericValue = Integer.parseInt(str);  
		    return true;
		  } catch(NumberFormatException e){  
		    return false;  
		  }  
	}
	
	public static boolean isString(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (Parameters.STRING_CHR.indexOf(str.charAt(i)) < 0) {
				return false;
			}
		}
		return true;
	}
	
	public Token(String str, int lineNumber) {
		super();
		this.str = str;
		this.lineNumber = lineNumber;
		numericValue = 0;
		// compute primitive type
		if (isNumeric(str)) {
			type = "number";
		} else if (isString(str)) {
			type = "string";
		} else {
			type = "null";
		}
	}
	
	public String toString() {
		//return "(" + str + "," + lineNumber + "," + type + ")";
		return str;
	}
	
	public String getString() {
		return str;
	}
	
	@Override
	public String getPerfectString() {
		return str;
	}
	
	public String getType() {
		return type;
	} 
	
	public String getTypes() {
		return type;
	}
	
	public int getLine() {
		return lineNumber;
	}
	
	public int getNumericValue() throws CompilerException {
		return numericValue;
	}
}
