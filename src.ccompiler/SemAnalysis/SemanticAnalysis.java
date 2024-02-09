package SemAnalysis;

import java.util.ArrayList;
import java.util.HashMap;

import General.CompilerException;
import General.Interface;
import General.Parameters;
import LexAnalysis.SuperToken;
import SynAnalysis.ListToken;

public class SemanticAnalysis {
	
	private SymbolTable symbolTable;
	private Interface interfa;
	
	public SemanticAnalysis(Interface interfa) {
		this.interfa = interfa;
		this.symbolTable = new SymbolTable();
	}
	
	public boolean buildSymbolTableFromToken(SuperToken tree, Context ctx) throws CompilerException {
		if (tree.getType().equals(Parameters.TYPE_VAR_DECLARATION)) {
			System.out.println("sel token = "+tree);
			symbolTable.add(new Variable(tree, ctx));
			return true;
		}
		 if (tree.getType().equals(Parameters.TYPE_FUNC_DECLARATION)) {
			if (ctx.include(Parameters.FUNCTION_CTX)) {
				// error
				throw new CompilerException(Parameters.ERROR_FUNCD_IN_FUNCD, tree);
			}
			Function thisFct = new Function(tree, ctx);
			symbolTable.add(thisFct);
			ctx.addContext(Parameters.FUNCTION_CTX,thisFct);
			for (int i = 1; i < tree.size(); i++) { // on skip le 1er
				if (!buildSymbolTableFromToken(tree.get(i), ctx)) {
					// si onpouvait rien faire avec l'expression elle même, on regarde ses enfants
					buildSymbolTable(tree.get(i), ctx);
				}
			}
			ctx.removeContext(Parameters.FUNCTION_CTX);
			return true;
		} 
		return false;
	}
	
	public int buildSymbolTable(SuperToken tree, Context ctx) throws CompilerException {
		// contexte : variable locales ou non !
		for (int i = 0; i < tree.size(); i++) {
			if (!buildSymbolTableFromToken(tree.get(i), ctx)) {
				buildSymbolTable(tree.get(i), ctx);
			}
		}
		return Parameters.OK;
	}
	
	private void printSymbolTable() {
		for (int i = 0; i < symbolTable.size(); i++) {
			interfa.printlogMsg(symbolTable.get(i)+"\n");	
		}
		interfa.logMsg("");
	}
	
	public int sementicAnalysis(ArrayList<SuperToken> tree) throws CompilerException {
		// reset :
		symbolTable.reset();
		buildSymbolTable(new ListToken(Parameters.FINAL_TREE_TYPE, tree), new Context());
		
		interfa.logMsg("Table des symboles :");
		printSymbolTable();
		return Parameters.OK;
	}

}
