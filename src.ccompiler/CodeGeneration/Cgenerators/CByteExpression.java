package CodeGeneration.Cgenerators;

import java.util.ArrayList;
import java.util.List;

import CodeGeneration.AsmCode;
import General.CompilerException;
import General.Interface;
import General.Parameters;
import LexAnalysis.SuperToken;
import SemAnalysis.DataType;
import SynAnalysis.ListToken;
import SynAnalysis.RegularExp;

public class CByteExpression extends CInstruction {

	private int accOffset;
	private DataType dataType = new DataType("any", DataType.UNDEFINED);

	public int getAccOffset() {
		return accOffset;
	}

	public CByteExpression(SuperToken token, Interface interfa, int accOffset) {
		super(token, interfa);
		System.out.println("Generation expression : " + token + "/" + token.getTypes());
		this.accOffset = accOffset;
	}

	@Override
	public void generate(AsmCode osmCode) throws CompilerException {
		osmCode.shareOffset(accOffset);
		osmCode.add("; Calcul de l'expression \"" + token.getPerfectString() + "\"");
		generateFromOrderedTree(generateOrderedExp().get(0), osmCode, accOffset);
	}

	private static boolean checkExactOrTypeSimple(SuperToken tk, String regOp) {
		return (((regOp.charAt(0) == '\'') && (tk.getPerfectString().equals(regOp.substring(1, regOp.length() - 1))))
				|| ((regOp.charAt(0) != '\'') && (regOp.equals(tk.getPerfectString()))));
	}

	private static boolean isSameOp(SuperToken tk, String regOp) {
		if (regOp.indexOf(Parameters.OR_CHAR) >= 0) {
			ArrayList<String> params = RegularExp.splitByChr(regOp, Parameters.OR_CHAR);
			if (params.size() > 0) {
				for (String i : params) {
					if (checkExactOrTypeSimple(tk, i)) {
						return true;
					}
				}
				return false;
			}
		}
		return checkExactOrTypeSimple(tk, regOp);
	}

	private SuperToken generateOrderedExp() throws CompilerException {
		// GENERE LA LISTE LA VALEUR INITIALE DE L'ACCUMULATEUR
		// ET LA LISTE DES OPERATIONS A LUI APPLIQUER
		// ON COMMENCE PAR FLAT ENTRE OPERATEUR ET BYTES
		ArrayList<SuperToken> flattenOperations = new ArrayList<>();
		ArrayList<SuperToken> opTree = new ArrayList<SuperToken>();
		flattenizeExp(token, flattenOperations);
		for (int opIdx = 0; opIdx < Parameters.ORDERED_OP_STR.length; opIdx++) {
			// puis ordre de gauche à droite
			for (int iter = 0; iter < Parameters.NB_CYCLE_EXP; iter++) {
				opTree = new ArrayList<SuperToken>();
				for (int tkIdx = 0; tkIdx < flattenOperations.size(); tkIdx++) {
					if (isSameOp(flattenOperations.get(tkIdx), Parameters.ORDERED_OP_STR[opIdx])) {
						// on le prend
						ArrayList<SuperToken> op = new ArrayList<>();
						if (Parameters.ORDERED_OP_NB_OP[opIdx] == 2) { // LEFT OP RIGHT
							if ((tkIdx + 1 > flattenOperations.size() - 1) || (tkIdx - 1 < 0)) {
								throw new CompilerException("An operand is missing with this operator.",
										flattenOperations.get(tkIdx));
							}
							opTree.remove(opTree.size() - 1);
							op.add(flattenOperations.get(tkIdx - 1));
							op.add(flattenOperations.get(tkIdx));
							op.add(flattenOperations.get(tkIdx + 1));
							tkIdx += 1;
							opTree.add(new ListToken("AOB", op));
						} else if (Parameters.ORDERED_OP_NB_OP[opIdx] == 1) { // OP RIGHT
							if (tkIdx + 1 > flattenOperations.size() - 1) {
								throw new CompilerException("An operand is missing with this operator.",
										flattenOperations.get(tkIdx));
							}
							op.add(flattenOperations.get(tkIdx));
							op.add(flattenOperations.get(tkIdx + 1));
							opTree.add(new ListToken("OA", op));
							tkIdx += 1;
						} else if (Parameters.ORDERED_OP_NB_OP[opIdx] == -1) { // LEFT OP RIGHT
							if (tkIdx - 1 < 0) {
								throw new CompilerException("An operand is missing with this operator.",
										flattenOperations.get(tkIdx));
							}
							opTree.remove(opTree.size() - 1);
							op.add(flattenOperations.get(tkIdx - 1));
							op.add(flattenOperations.get(tkIdx));
							opTree.add(new ListToken("AO", op));
							System.out.println("l for op tk =" + new ListToken("AO", op).size());
						}

						// System.out.println("OP = "+flattenOperations.get(tkIdx)+" -
						// "+Parameters.ORDERED_OP_NB_OP[opIdx]+"//"+flattenOperations.size());
					} else {
						opTree.add(flattenOperations.get(tkIdx));
					}
				}
				if (flattenOperations.size() == opTree.size()) {
					break;
				}
				flattenOperations = opTree;
			}
		}
		SuperToken opTreeTk = new ListToken("mainParent", opTree);
		if (flattenOperations.size() != 1) {
			// il y a erreur
			throw new CompilerException("The expression have non linked operations (maybe you forgot an operator?).",
					opTreeTk);
		}
		// on a maintenant un arbre d'opération hierarchisé
		interfa.logMsg("Expression tree done for " + opTreeTk.getPerfectString());
		// System.out.println("Tree=="+opTreeTk.getPerfectString()+"*"+opTreeTk.getTypes()+"**"+flattenOperations.size());
		// ON DOIT GERER LE PARENTHESAGE EN PREMIER
		// for
		return opTreeTk;
	}

	private void generateFromOrderedTree(SuperToken tree, AsmCode osmCode, int accOffset) throws CompilerException {

		// LA PREMIERE OPERANDE C4EST TOUJOUR LA STACK ?

		if (isByte(tree)) {
			CByteNumber nb = new CByteNumber(tree, interfa, this);
			nb.generate(osmCode);
			return;
			// cas ou l'expression a un seul elment : on return calcul
		} else if (tree.size() == 2) {
			// de la
			if (tree.getType().equals("OA")) {
				// opérateur puis opérande
				generateFromOrderedTree(tree.get(1), osmCode, accOffset);
				osmCode.add("a<t");
				osmCode.add("t<ALU(" + tree.get(0).getPerfectString() + ")");
			} else {
				// opérande puis opérateur AO : une seule opérande donc tj registre a
				generateFromOrderedTree(tree.get(0), osmCode, accOffset);
				osmCode.add("a<t");
				osmCode.add("t<ALU(" + tree.get(1).getPerfectString() + ")");
			}
		} else if (tree.size() == 3) {
			/// IDEE:
			// 1 calcul op 1 -> t
			// sauvegarde de t
			// calcul op 2
			// lecture de t
			// a = t
			// b = save(local)
			int idxA = 0;
			int idxB = 2;
			if (!Parameters.isABForOp(tree.get(1).getPerfectString())) {
				idxA = 2;
				idxB = 0;
			}
			generateFromOrderedTree(tree.get(idxA), osmCode, accOffset);
			// ICI ON A LE RESULTAT DANS T POUR SUR, ON SAUVEGARDE
			osmCode.add("WriteStackTat(ADR_ACC_@" + osmCode.getCurrentFctContext() + " + " + accOffset + ")");
			// ON CALCULE LA DEUXIEME OPERANDE
			generateFromOrderedTree(tree.get(idxB), osmCode, accOffset);
			// PUIS ON CALCULE
			osmCode.add("a<t");
			osmCode.add("ReadStackToB(ADR_ACC_@" + osmCode.getCurrentFctContext() + " + " + accOffset + ")");
			osmCode.add("t<ALU(" + tree.get(1).getPerfectString() + ")");
		}
	}

	public DataType getDataType() {
		return dataType;
	}

	public void setDataType(DataType dataType) {
		this.dataType = dataType;
	}

	private static boolean isByte(SuperToken tk) {
		return Parameters.BYTE_TYPES_ARRAY.contains(tk.getType());
	}

	private static void flattenizeExp(SuperToken dadToken, List<SuperToken> flattenOperations)
			throws CompilerException {

		if (dadToken.getType().equals("operator") || isByte(dadToken)) {
			flattenOperations.add(dadToken);
		} else if (dadToken.size() == 0) { // terminal token unused
			throw new CompilerException("A value in a 8 bit expression is not a byte.", dadToken);
		} else {
			for (int i = 0; i < dadToken.size(); i++) {
				flattenizeExp(dadToken.get(i), flattenOperations);
			}
		}
	}

}
