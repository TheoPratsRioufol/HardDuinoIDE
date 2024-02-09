package LexAnalysis;

import java.util.ArrayList;

import General.CompilerException;
import General.Interface;
import General.Parameters;

public class SuperToken {

	public SuperToken() {

	}

	public static void flatten(SuperToken tree, ArrayList<SuperToken> treeflat) {
		if (tree.size() == 0) {
			treeflat.add(tree);
			return;
		}
		for (int i = 0; i < tree.size(); i++) {
			flatten(tree.get(i), treeflat);
		}
	}

	public String getString() {
		return "";
	}

	public String getType() {
		return "";
	}

	public int getLine() {
		return -1;
	}

	public static void printList(Interface interfa, ArrayList<SuperToken> tokens) {
		for (int i = 0; i < tokens.size(); i++) {
			interfa.printlogMsg("(" + tokens.get(i).getType() + "," + tokens.get(i) + ") - ");
		}
		interfa.logMsg("");
	}

	public int size() {
		return 0;
	}

	public SuperToken get(int i) {
		return new SuperToken();
	}

	public SuperToken get(String i) throws CompilerException {
		return new SuperToken();
	}

	public String getTypes() {
		return "";
	}

	public SuperToken getDeep(String string, int i) throws CompilerException {
		throw new CompilerException(Parameters.EMPTY_TOKEN_EXCEPTION, this);
	}

	public SuperToken getDeep(int i) throws CompilerException {
		// renvoie le ieme token de SuperToken
		throw new CompilerException(Parameters.EMPTY_TOKEN_EXCEPTION, this);
	}

	public boolean has(String string) {
		return false;
	}

	public String getPerfectString() {
		return "";
	}

	public int getNumericValue() throws CompilerException {
		throw new CompilerException("This token haven't any value", this);
	}

	public static void getNumsFromSet(SuperToken tk, ArrayList<Integer> setValue) throws CompilerException {
		if (tk.size() == 0) {
			if (tk.getType().equals("number")) {
				setValue.add(tk.getNumericValue());
			} else if (!tk.toString().equals(",")) {
				throw new CompilerException("A value in the set (" + tk
						+ ") is not a number. If you use variable, affect by indexing one by one.", tk);
			}
		} else {
			for (int i = 0; i < tk.size(); i++) {
				getNumsFromSet(tk.get(i), setValue);
			}
		}
	}

}
