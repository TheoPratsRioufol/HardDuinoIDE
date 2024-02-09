package SemAnalysis;

import java.util.ArrayList;
import java.util.HashMap;

import General.CompilerException;
import General.Parameters;
import LexAnalysis.SuperToken;

public class SymbolTable {
	
	private ArrayList<CanBeSymbolyse> symbols = new ArrayList<CanBeSymbolyse>();
	
	public SymbolTable() {
		
	}
	
	public void reset() {
		symbols = new ArrayList<CanBeSymbolyse>();
	}
	
	public void add(CanBeSymbolyse sym) throws CompilerException {
		if (has(sym)) {
			throw new CompilerException(Parameters.REDEFINITION, new SuperToken());
		}
		symbols.add(sym);
	}
	
	public boolean has(CanBeSymbolyse sym) {
		for (int i = 0; i < symbols.size(); i++) {
			if (symbols.get(i).getName().equals(sym.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public CanBeSymbolyse get(int idx) {
		return symbols.get(idx);
	}
	
	public int  size() {
		return symbols.size();
	}

}
