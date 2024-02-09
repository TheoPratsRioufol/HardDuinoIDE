package SemAnalysis;

import java.util.ArrayList;

import General.CompilerException;
import General.Parameters;
import LexAnalysis.SuperToken;
import LexAnalysis.Token;

public class Expression {
	
	private SuperToken token;
	
	public Expression(SuperToken token) {
		this.token = token;
	}
	
	public boolean isConstant() throws CompilerException {
		System.out.println(token.getTypes());
		if ((token.has("strValue") || token.has("setValue"))) {
			if (token.size() == 1) {
				return true;
			} else {
				throw new CompilerException(Parameters.ILLEGAL_OP_ARRAY, token);
			}
		}
		return false;
	}
	
	public int sizeof() throws CompilerException {
		if (isConstant()) {
			if (token.has("strValue")) {
				return token.get("strValue").get(1).toString().length();
			} else if (token.has("setValue")) {
				ArrayList<SuperToken> treeflat = new ArrayList<SuperToken>();
				Token.flatten(token.get("setValue"), treeflat);
				int numberOfnumber = 0;
				for (int i = 0; i < treeflat.size(); i++) {
					if (treeflat.get(i).getType().equals("number")) {
						numberOfnumber++;
					}
				}
				return numberOfnumber;
			}
		}
		throw new CompilerException(Parameters.UNKOWN_SIZE, token);
	}
	
	public int computeStaticFromToken(SuperToken tk) throws CompilerException {
		if (tk.size() == 0) {
			if (tk.getType().equals("number")) {
				return Integer.parseInt(tk.toString());
			}
		} else if (tk.size() == 1) {
			return computeStaticFromToken(tk.get(0));
		} else if (tk.size() == 2) {
			if (tk.get(0).equals("operator")) {
				return applyOperator(tk.get(0), computeStaticFromToken(tk.get(1)));
			} 
		} else if (tk.size() == 3) {
			if (tk.get(1).equals("operator")) {
				return applyOperator2(tk.get(1), computeStaticFromToken(tk.get(0)),computeStaticFromToken(tk.get(2)));
			} 
		}
		throw new CompilerException(Parameters.CANT_COMPUTE_STATIC+tk,tk);
	}
	
	private int applyOperator(SuperToken op, int value) throws CompilerException {
		throw new CompilerException(Parameters.NOT_IMPLEMENTED,op);
	}
	
	private int applyOperator2(SuperToken op, int value, int value2) throws CompilerException {
		throw new CompilerException(Parameters.NOT_IMPLEMENTED,op);
	}
	
	public int computeStatic() throws CompilerException {
		return computeStaticFromToken(token);
	}

}
