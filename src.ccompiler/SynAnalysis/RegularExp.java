package SynAnalysis;

import java.util.ArrayList;

import General.Parameters;
import LexAnalysis.SuperToken;

public class RegularExp {
	private String type;
	private ArrayList<String> patern = new ArrayList<String>();
	
	public RegularExp(String str) {
		// on construit le bloc
		if (str.charAt(0) == Parameters.COMMAND_CHAR) {
			// s'il s'agit d'une commande
			type = str;
		} else {
			String[] sequence = str.split(":");
			this.type = sequence[0];
			//System.out.println("Type="+type);
			// puis plit de la serconde partie en faisant attention aux '-'
			patern = splitByChr(sequence[1], '-');
		}
	}
	
	public static ArrayList<String> splitByChr(String source, char delimiter) {
		String buffer = "";
		boolean bracketOpen = false;
		ArrayList<String> splitedOutput = new ArrayList<String>();
		for (int i = 0; i < source.length(); i++) {
			if (source.charAt(i) == '\'') {bracketOpen = !bracketOpen;}
			if (((source.charAt(i) == delimiter) || (i == source.length()-1)) && !(bracketOpen)) {
				if (i == source.length()-1) {buffer += source.charAt(i);}
				splitedOutput.add(buffer);
				buffer = "";
			} else {
				buffer += source.charAt(i);
			}
		}
		return splitedOutput;
	}
	
	public boolean elmcompatible(SuperToken token, String paternCond) {
		// test si ces deux éléments sont compatibles
		// on regarde s'il y a des OU :
		if (paternCond.equals("*")) {
			return true;
		}
		if (paternCond.indexOf(Parameters.OR_CHAR) >= 0) {
			ArrayList<String> params = splitByChr(paternCond, Parameters.OR_CHAR);
			if (params.size() > 1) {
			for (int i = 0; i < params.size(); i++) {
				if (elmcompatible(token, params.get(i))) {
					return true;
				}
			}
			return false;
			}
		}
		if (paternCond.indexOf(Parameters.AND_CHAR) >= 0) {
			ArrayList<String> params = splitByChr(paternCond, Parameters.AND_CHAR);
			if (params.size() > 1) {
			for (int i = 0; i < params.size(); i++) {
				if (!elmcompatible(token, params.get(i))) {
					return false;
				}
			}
			return true;
			}
		}
		if (paternCond.charAt(0) == '\'') {
			// il faut vérifier le contenue string
			return token.getString().equals(paternCond.substring(1,paternCond.length()-1));
		} else if (paternCond.charAt(0) == '!') {
			// il faut faire la négation
			return !elmcompatible(token, paternCond.substring(1,paternCond.length()));
		} else if (paternCond.charAt(0) == '(') {
			// il faut vérifier l'intérieur des parenthèses
			return elmcompatible(token, paternCond.substring(1,paternCond.length()-1));
		} else {
			// on vérifie le type
			return token.getType().equals(paternCond);
		}
		}
	
	
	public boolean compatible(ArrayList<SuperToken> thetree,int idxTree ,int rang) {
		return elmcompatible(thetree.get(idxTree + rang), patern.get(rang));
	}
	
	public int size() {
		return patern.size();
	}
	
	public String getType() {
		return type;
	}
	
	public int addExpression(ArrayList<SuperToken> newTree, ArrayList<SuperToken> thetree, int idxTree) {
		ArrayList<SuperToken> newToken = new ArrayList<SuperToken>();
		int nbToSkip = 0;
		boolean firstaffectation = false;
		int idxToReplace = -1;
		for (int i = 0; i < patern.size(); i++) {
			nbToSkip++;
			if (patern.get(i).charAt(0) != '(') {
				newToken.add(thetree.get(idxTree + i));
				if (!firstaffectation) {
					newTree.add(thetree.get(idxTree + i));
				}
				idxToReplace = newTree.size()-1;
				firstaffectation = true;
			} else {
				if (!firstaffectation && ((i < patern.size()-1) && (patern.get(i+1).charAt(0) != '('))) {
					// il faut l'insérer après
					newTree.add(thetree.get(idxTree + i));
					newTree.add(thetree.get(idxTree + i));
					idxToReplace = newTree.size()-1;
					firstaffectation = true;
				} else {
					newTree.add(thetree.get(idxTree + i));
				}
			}
		}
		ListToken nt = new ListToken(type, newToken);
		//System.out.println("tk = "+nt);
		if (idxToReplace == -1) {
			newTree.add(new ListToken(type, newToken));
		} else {
			newTree.set(idxToReplace, new ListToken(type, newToken));
		}
		return nbToSkip-1;
	}

}
