package CodeGeneration.Cgenerators;

import CodeGeneration.AsmCode;
import General.CompilerException;
import General.Interface;
import LexAnalysis.SuperToken;
import SemAnalysis.DataType;
import SemAnalysis.Function;
import SemAnalysis.Variable;

public class CParamAffectation extends CInstruction {

	private int offset;
	private Function fctCall;
	private DataType dataType;

	public CParamAffectation(SuperToken token, Interface interfa, int offset) {
		super(token, interfa);
		this.offset = offset;
	}

	private void generateParams(AsmCode asmCode, SuperToken tk) throws CompilerException {
		interfa.logMsg("==>" + tk.getType());
		// ON DOIT VERIFIER LA COMPATIBILITE DES TYPES
		if (tk.getType().equals("bExp")) {
			interfa.logMsg("Expression ! : " + tk + "*" + tk.getTypes());
			DataType dataType;

			if ((tk.size() == 1) && (tk.get(0).getType().equals("var"))) {
				String varName = tk.get(0).getPerfectString();
				Variable varParamValue;
				if (asmCode.isVarExit(varName, tk)) {
					varParamValue = asmCode.getVar(varName, tk);
				} else if (asmCode.isVarParamExist(varName, tk)) {
					varParamValue = asmCode.getVarParam(varName, tk);
				} else
					throw new CompilerException("Unknow variable : " + varName, token);
				dataType = varParamValue.getDataType();
				if (varParamValue.getSizeOf() != 1) {
					// on gère, il ne faut passer que le pointeur
					if (varParamValue.getStorageDad().equals("GLOBAL")) {
						asmCode.add(
								"t<rp, " + varParamValue.getPointer() + "  ; pointer of " + varParamValue.getName());
					} else if (varParamValue.getSizeOf() > 1) {
						// on connait sa taille donc son emplacement aussi
						asmCode.add("GetRealAdressFromStack(" + varParamValue.getPointer()
								+ ")  ; pointer of the local function " + varParamValue.getName());
					} else {
						// on ne connait que le redirecteur
						asmCode.add("ReadStackToT(" + varParamValue.getRedirectPointer()
								+ ")  ; pointer of the local function with redirector " + varParamValue.getName());
					}
				} else {
					new CByteExpression(tk, interfa, offset).generate(asmCode); // on calcule le parametre, on l'a dans
																				// t
				}
			} else {
				CByteExpression exp = new CByteExpression(tk, interfa, offset); // on calcule le parametre, on l'a dans
																				// t
				exp.generate(asmCode);
				dataType = exp.getDataType();
			}

			// la valeur est dans le registre t
			asmCode.add("WriteStackToFuturNewPushT(@" + fctCall.getName() + " + " + 2 + asmCode.getParamIndex()
					+ ")   ; affectation du " + asmCode.getParamIndex() + "parametre de l'appel de fonction");
			interfa.logMsg("Paramètre généré");
			fctCall.verifyTypeOfParam(asmCode.getParamIndex(), dataType, token);
			asmCode.nextParamIndex();
			return;
		} else if (tk.getPerfectString().equals(",")) {
			return;
		} else if (tk.size() == 0) {
			throw new CompilerException(
					"Invalid function call parameter (Array declared explicitly are forbidens). Only variables (array or not) and byte expressions are valids.",
					token);
		}
		for (int i = 0; i < tk.size(); i++) {
			generateParams(asmCode, tk.get(i));
		}
	}

	@Override
	public void generate(AsmCode asmCode) throws CompilerException {
		System.out.println("Genration appel de fonction " + token.getTypes());

		fctCall = asmCode.getFct(token.get("var").getPerfectString(), token);
		dataType = fctCall.getDataType();
		String id = asmCode.getUniqueId();
		asmCode.resetParamIndex(); // pour compter les params

		if (token.has("evalParameterArray")) {
			SuperToken params = token.get("evalParameterArray");
			// puis les parametres
			generateParams(asmCode, params);
		}

		// on ne fait le push qu'après pour ne pas perdre l'adresse des variables
		// locales
		if (asmCode.getParamIndex() != fctCall.getNbOfParams()) {
			// on complète
			if (asmCode.getParamIndex() < fctCall.getNbOfParams()) {
				interfa.warningMsg("Not enougth parameter for \"" + fctCall.getName()
						+ "\" looking for preset variables (at the end)");
				for (int i = asmCode.getParamIndex(); i < fctCall.getNbOfParams(); i++) {
					Variable v = fctCall.getVarParam(i);
					if (v.getDeflaultValue() == null)
						break;
					// sinon on ajoute ce presset
					interfa.logMsg("initial value : " + v.getDeflaultValue());
					generateParams(asmCode, v.getDeflaultValue());
				}
			}
			// on reteste après avoir chargé les constantes
			if (asmCode.getParamIndex() != fctCall.getNbOfParams())
				throw new CompilerException(
						"The function \"" + fctCall.getName() + "\" did not receive the correct number of arguments",
						token);
		}

		asmCode.add("StackPush(SIZE_STACK_ELM_OF_@" + fctCall.getName() + ") ; appel de " + fctCall.getName());
		asmCode.add("PrepareFctReturn(return_fct_call_" + id + ")");
		asmCode.add("Jump(@" + fctCall.getName() + ")");
		asmCode.add("$return_fct_call_" + id + " ; --- fin de l'appel");
	}

	public DataType getDataType() {
		return dataType;
	}

}
