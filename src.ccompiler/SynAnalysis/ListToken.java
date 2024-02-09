package SynAnalysis;

import java.util.ArrayList;

import General.CompilerException;
import General.Parameters;
import LexAnalysis.SuperToken;

public class ListToken extends SuperToken {
	
	private String type;
	private ArrayList<SuperToken> tokens;
	private ArrayList<SuperToken> flattree;

	public ListToken (String type, ArrayList<SuperToken> tokens) {
		super();
		this.tokens = tokens;
		this.type = type;
		flattree = new ArrayList<SuperToken>();
		SuperToken.flatten(this, flattree);
	}
	
	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		String str = "";
		for (int i = 0; i < tokens.size(); i++) {
			str += tokens.get(i).toString() + ".";
		}
		return str;
	}
	
	@Override
	public String getPerfectString() {
		String str = "";
		for (int i = 0; i < tokens.size(); i++) {
			str += tokens.get(i).toString();
		}
		return str;
	}
	
	public String getString() { // return none
		if (tokens.size() == 1) {
			return tokens.get(0).getString();
		}
		return "";
	}
	
	public int size() {
		return tokens.size();
	}
	
	public SuperToken get(int i) {
		return tokens.get(i);
	}
	
	public SuperToken get(String type_) throws CompilerException { // renvoie la première occurence
		// get by type
		if (type_.indexOf('|') > 0) {
			String[] types = type_.split("\\|");
			for (int i = 0; i < types.length; i++) {
				try {
					return get(types[i]);
				} catch (CompilerException e) {}
			}
		} else {
			for (int i = 0; i < tokens.size(); i++) {
				if (tokens.get(i).getType().equals(type_)) {
					return tokens.get(i);
				}
			}
		}
		throw new CompilerException(Parameters.TOKEN_NOT_FOUND[0] + type_ + Parameters.TOKEN_NOT_FOUND[1] + this.type + Parameters.TOKEN_NOT_FOUND[2]+ this + ", tree = [" + this.getTypes() + "]",this);
	}
	
	public SuperToken getDeep(String type_, int rank) throws CompilerException { // renvoie la première occurence
		// get by type and rank-th occurence
		// fatenize the tree :
		int rankfound = 0;
		for (int i = 0; i < flattree.size(); i++) {
			if (flattree.get(i).getType().equals(type_)) {
				if (rankfound == rank) {
					return flattree.get(i);
				}
				rankfound++;
			}
		}
		throw new CompilerException(Parameters.TOKEN_NOT_FOUND[0] + type_ + Parameters.TOKEN_NOT_FOUND[1] + this.type + Parameters.TOKEN_NOT_FOUND[2]+ this,this);
	}
	
	public SuperToken getDeep(int i) throws CompilerException {
		// renvoie le ieme token de SuperToken
		if (flattree.size() > i) {
			return flattree.get(i);
		}
		throw new CompilerException(Parameters.EMPTY_TOKEN_EXCEPTION, this);
	}
	
	public boolean has(String type_) {
		for (int i = 0; i < tokens.size(); i++) {
			if (tokens.get(i).getType().equals(type_)) {
				return true;
			}
		}
		return false;
	}
	
	public int getLine() {
		return tokens.get(0).getLine();
	}
	
	public String getTypes() {
		String out = "";
		for (int i = 0; i < tokens.size(); i++) {
			out += tokens.get(i).getType() + ".";
		}
		return out;
	}
	
}
