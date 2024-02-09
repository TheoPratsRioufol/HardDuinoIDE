package SynAnalysis;

import java.util.ArrayList;

import General.CompilerException;
import General.Interface;
import General.Parameters;
import LexAnalysis.SuperToken;

public class SyntaxAnalysis {

	ArrayList<SuperToken> tree;
	ArrayList<ArrayList<RegularExp>> regularExps = new ArrayList<ArrayList<RegularExp>>();
	Interface interfa;

	private void buildRegularExps() {
		for (int pGroup = 0; pGroup < Parameters.RegularExpressions.length; pGroup++) {
			regularExps.add(new ArrayList<RegularExp>());
			for (int idx = 0; idx < Parameters.RegularExpressions[pGroup].length; idx++) {
				regularExps.get(pGroup).add(new RegularExp(Parameters.RegularExpressions[pGroup][idx]));
			}
		}
	}

	public SyntaxAnalysis(ArrayList<SuperToken> tokens, Interface interfa) {
		this.tree = tokens;
		this.interfa = interfa;
		buildRegularExps();
		interfa.infoMsg(Parameters.CREATION_REGULAR_EXP);
	}

	public int buildTree() throws CompilerException {

		ArrayList<SuperToken> nextTree = new ArrayList<SuperToken>();

		for (int pGroup = 0; pGroup < Parameters.RegularExpressions.length; pGroup++) {
			// on va tester tous les groupes de paterns
			// on réitère k fois

			for (int cycle = 0; cycle < Parameters.NB_CYCLE_REGULAR_EXP; cycle++) {
				nextTree = new ArrayList<SuperToken>();
				boolean expFoundDuringCycle = false;

				for (int idxTree = 0; idxTree < tree.size(); idxTree++) {
					// on boucle sur chaque élément
					// on récupère la meilleure chaine (cad la plus longue)

					int idxBestExp = getBestExpression(tree, idxTree, pGroup);

					if (idxBestExp > -1) {
						// on a trouvé un partern
						int nbIdxToJump = addExpression(nextTree, tree, pGroup, idxBestExp, idxTree);
						idxTree += nbIdxToJump;
						expFoundDuringCycle = true;
					} else {
						// on ajoute le token pour le gérer plus tard
						nextTree.add(tree.get(idxTree));
					}

				}
				interfa.logMsg("End Cycle, (Tree size = " + tree.size() + "), TREE = ");
				SuperToken.printList(interfa, tree);
				if (expFoundDuringCycle == false) {
					// on peut s'arrèter ici pour cette étape
					break;
				} else if (cycle == Parameters.NB_CYCLE_REGULAR_EXP - 1) {
					// si c'était le dernier cycle et qu'on venait de faire un update, il y a
					// probablement une erreur
					interfa.errorMsg(0, Parameters.ERROR_MAX_CYCLE_REACH);
					return Parameters.ERROR;
				}
				// on fait l'échange d'arbre
				tree = nextTree;
			}
		}

		if (tree.size() > 1) {
			for (int i = 0; i < tree.size(); i++) {
				if (!tree.get(i).getType().equals(Parameters.INTR_TYPE)) {
					throw new CompilerException(Parameters.TREE_NOT_CONVERGE + tree.get(i), tree.get(i));
				}
			}
		}

		return Parameters.OK;

	}

	public ArrayList<SuperToken> getTree() {
		return tree;
	}

	private int getBestExpression(ArrayList<SuperToken> thetree, int idxTree, int pGroup) {
		int bestExpNumber = -1;
		int bestSize = -1;
		// System.out.println("LOOKING FOR A BEST EXPRESSION");
		for (int idx = 0; idx < Parameters.RegularExpressions[pGroup].length; idx++) {

			RegularExp regularExp = regularExps.get(pGroup).get(idx);

			if ((regularExp.size() > thetree.size() - idxTree) || (regularExp.size() <= bestSize)) {
				// this one don't fit or is too small
				continue;
			}
			boolean fit = true;
			for (int idxT = 0; idxT < regularExp.size(); idxT++) {
				// System.out.println("check for "+thetree.get(idxT+idxTree).getString());
				if (!regularExp.compatible(thetree, idxTree, idxT)) {
					fit = false;// don't fit
					break;
				}

			}
			if (fit && (regularExp.size() > bestSize)) {
				// System.out.println("FOUND!");
				bestSize = regularExp.size();
				bestExpNumber = idx;
			}

		}
		return bestExpNumber;
	}

	private int addExpression(ArrayList<SuperToken> newTree, ArrayList<SuperToken> thetree, int pGroup, int idxBestExp,
			int idxTree) {
		return regularExps.get(pGroup).get(idxBestExp).addExpression(newTree, thetree, idxTree);
	}

}
