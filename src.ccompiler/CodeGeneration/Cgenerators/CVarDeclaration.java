package CodeGeneration.Cgenerators;

import CodeGeneration.AsmCode;
import General.CompilerException;
import General.Interface;
import LexAnalysis.SuperToken;
import SemAnalysis.Variable;

public class CVarDeclaration extends CInstruction {

	public CVarDeclaration(SuperToken token, Interface interfa) {
		super(token, interfa);
	}

	@Override
	public void generate(AsmCode osmCode) throws CompilerException {
		if (token.get(0).getType().equals("bDeclInit") || token.get(0).getType().equals("bDecl")) {
			SuperToken initSeq;
			if (token.get(0).getType().equals("bDeclInit")) {
				initSeq = token.get(0).get("bDecl");
			} else {
				initSeq = token.get("bDecl");
			}
			Variable newVar = new Variable(initSeq.get("var").getPerfectString(),
					initSeq.get("typeToken").getPerfectString(), 1);
			if (!(token.getType().equals("declaParameter") || token.getType().equals("declaParameterInit"))) {
				// System.out.println("declarating a new var" + newVar);
				osmCode.newVar(newVar, token);
				// System.out.println("done");
				// PUIS CODE DE L'AFFECTATION (FACULTATIF)
				if (token.get(0).getType().equals("bDeclInit")) {
					CByteExpression exp = new CByteExpression(token.get(0).get("bExp"), interfa, 0);
					exp.generate(osmCode);
					CVarAffectation aff = new CVarAffectation(newVar, interfa, token);
					aff.generate(osmCode);
				}
				return;
			} else if (token.getType().equals("declaParameter")) {
				interfa.logMsg("declaParameter. Ajout du parametre " + newVar + " non initialisé ");
				osmCode.newParamVar(newVar, token);
				return;
			} else if (token.getType().equals("declaParameterInit")) {
				interfa.logMsg("declaParameterInit. Ajout du parametre " + newVar + " avec valeur par défault ");
				newVar.setDeflaultValue(token.get(0).get("bExp"));
				osmCode.newParamVar(newVar, token);
				return;
			}
		} else if (token.get(0).getType().equals("arrDeclInit") || token.get(0).getType().equals("arrDecl")) {
			// System.out.println("Declaration of an array");
			SuperToken initSeq;
			SuperToken expSetSize = null;
			if (token.get(0).getType().equals("arrDeclInit")) {
				initSeq = token.get(0).get("arrDecl");
			} else {
				initSeq = token.get(0);
			}
			// ArrayList<Integer> initValue = new ArrayList<>();
			// String varName = initSeq.get("var").getPerfectString();
			int sizeArrVar = -1;
			if (initSeq.get("index").size() == 3) {
				// on a la taille
				expSetSize = initSeq.get("index").get("bExp");
				// doit être constante si globale
				if ((initSeq.get("index").get("bExp").size() == 1)
						&& (initSeq.get("index").get("bExp").get(0).getType().equals("number"))) {
					sizeArrVar = initSeq.get("index").get("bExp").get(0).getNumericValue();
					// System.out.println("set size");
				}
				if (osmCode.getCurrentContext() == "GLOBAL") {
					if (sizeArrVar < 0) {
						throw new CompilerException(
								"The size of a global array must be a constant and not an expression.", token);
					}
				}
			} else if (initSeq.get("index").size() != 2) {
				throw new CompilerException("Invalid index.", token);
			}
			// FIN DE DECLARATION, ON A PEUT ETRE PAS LA BONNE TAILLE
			Variable newArrVar = new Variable(initSeq.get("var").getPerfectString(),
					initSeq.get("typeToken").getPerfectString(), sizeArrVar);
			// PUIS AFFECTATION
			if (token.getType().equals("declaParameter")) {
				interfa.logMsg("Ajout du ARRAY parametre " + newArrVar + " SZ=" + newArrVar.getSizeOf());
				// System.out.println("Context=" + osmCode.getCurrentContext());
				osmCode.newParamVar(newArrVar, token);
				return;
			} else if (token.get(0).getType().equals("arrDeclInit")) {
				CVarAffectation affNewArrVar = new CVarAffectation(newArrVar, interfa, token);
				affNewArrVar.generateArray(osmCode, true);
				return;
			} else if (token.get(0).getType().equals("arrDecl")) {
				// System.out.println("expSetSize=" + expSetSize);
				if ((expSetSize == null) || !expSetSize.getType().equals("bExp")) {
					throw new CompilerException(
							"The size of an local array should be a byte expression (and void is forbiden !)", token);
				}
				newArrVar.setSizeInitTk(expSetSize);
				osmCode.newVar(newArrVar, token);
				return;
			}
		}
		// System.out.println(token.get(0).getType() + " - " + token.get(0).getTypes());
		throw new CompilerException("Invalid declaration sequence.", token);
	}

}
