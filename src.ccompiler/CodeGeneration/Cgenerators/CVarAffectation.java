package CodeGeneration.Cgenerators;

import java.util.ArrayList;

import CodeGeneration.AsmCode;
import General.CompilerException;
import General.Interface;
import LexAnalysis.SuperToken;
import SemAnalysis.Variable;

public class CVarAffectation extends CInstruction {

	private Variable var = null;
	private SuperToken token;
	private SuperToken varTk = null;
	private boolean isStandardVar;

	public CVarAffectation(Variable var, Interface interfa, SuperToken token) {
		super(null, interfa);
		this.var = var;
		this.token = token;
		isStandardVar = true;
	}

	public CVarAffectation(SuperToken varTk, AsmCode osmCode, Interface interfa, SuperToken token)
			throws CompilerException {
		super(null, interfa);
		this.varTk = varTk;
		this.token = token;
		isStandardVar = true;
		if (!varTk.getType().equals("var"))
			throw new CompilerException("Can't do affectation on type different to variable", token);

		if (varTk.getPerfectString().equals("PORTB") || varTk.getPerfectString().equals("PORTC")) {
			isStandardVar = false;
			return;
		}
		if (varTk.getPerfectString().equals("PORTD")) {
			throw new CompilerException("PORTD is a input port. It can't be set to any value", token);
		}
		var = osmCode.getVar(varTk.getPerfectString(), token);

	}

	@Override
	public void generate(AsmCode osmCode) throws CompilerException {
		if (isStandardVar) {
			generate_normal(osmCode);
			return;
		}
		interfa.logMsg("generating non standard variable affectation");
		// sinon on gère spécialement
		if (varTk.getPerfectString().equals("PORTB")) {
			osmCode.add("portb<t");
			return;
		}
		if (varTk.getPerfectString().equals("PORTC")) {
			osmCode.add("portc<t");
			return;
		}
		throw new CompilerException("Non standard variable error", token);
	}

	public void generate_normal(AsmCode osmCode) throws CompilerException {
		// FOR BYTE VALUES
		// on doit affecter la variable à la valeur contenue dans le registre t
		if (var.getSizeOf() == 1) {
			CByteExpression exp;
			if (token.has("bDeclInit")) {
				exp = new CByteExpression(token.get("bDeclInit").get("bExp"), interfa, 0);
			} else {
				exp = new CByteExpression(token.get("bExp"), interfa, 0);
			}
			exp.generate(osmCode);
			if (!exp.getDataType().equals(var.getDataType()))
				throw new CompilerException("Conflicting type between " + var + " and the affectation value ("
						+ var.getDataType() + " vs " + exp.getDataType() + ").", token);
			if (var.getStorageDad().equals("GLOBAL")) {
				osmCode.add("ram<t, " + var.getPointer() + " ; affectation de " + var.getName());
				return;
			} else {
				// variable locale
				osmCode.add("WriteStackTat(" + var.getPointer() + ") ; affectation de " + var.getName());
				return;
			}
		} else if (var.getSizeOf() > 0) {
			generateArray(osmCode, false);
			return;
		}
		throw new CompilerException("The variable " + var + " have a wrong size.", token);
	}

	public void generateArray(AsmCode osmCode, boolean newVar) throws CompilerException {
		// FOR BYTE VALUES
		// on doit affecter la variable à la valeur contenue dans le registre t
		// System.out.println("ARRAY affectation on " + var + " <> " +
		// token.getTypes());
		int sizeArrVar = var.getSizeOf();
		String varName = var.getName();
		ArrayList<Integer> initValue = new ArrayList<>();
		// System.out.println("TYPE=" + token.get(0).getType() + "**" +
		// token.getPerfectString());
		if (token.get(0).getType().equals("arrDeclInit") || token.getType().equals("affectation")) {
			// System.out.println("Loading Constant to be affected with");
			SuperToken expAff;
			if (token.get(0).getType().equals("arrDeclInit")) {
				expAff = token.get(0).get("arrayExp");
			} else {
				expAff = token.get("arrayExp");
			}
			// System.out.println("exp of Affectation = " + expAff);
			if (expAff.size() == 1) {
				if (expAff.get(0).getType().equals("strValue")) {
					String strValue = expAff.get(0).get(1).getPerfectString();
					for (int i = 0; i < strValue.length(); i++) {
						initValue.add((int) strValue.charAt(i));
					}
					if (sizeArrVar < 0) {
						interfa.logMsg("The size of " + varName + " has been deducted from his constant.");
						sizeArrVar = initValue.size();
					}
				} else if (expAff.get(0).getType().equals("setValue")) {
					if (expAff.get(0).size() == 3) {
						SuperToken.getNumsFromSet(expAff.get(0).get(1), initValue);
						// Cas particulier : si de longeur 1, s'addapte à la dimention de l'array
						// System.out.println("affectation to a set :" + initValue);
						if (sizeArrVar < 0) {
							sizeArrVar = initValue.size();
						}
						if (sizeArrVar == 1) {
							throw new CompilerException("You try to declare a byte as a array. It's bad !", token);
						} else if (initValue.size() == 1) {
							// on s'addapte
							interfa.logMsg("varName is set for an array of the same value : " + initValue.get(0));
							for (int i = 1; i < sizeArrVar; i++) {
								initValue.add(initValue.get(0));
							}
						}
					} else {
						throw new CompilerException("Illegal set.", expAff);
					}
				} else {
					throw new CompilerException("The affectation value isn't supported.", token);
				}
			} else {
				throw new CompilerException("Operations on array are prohibited.", token);
			}

			var.setSizeOf(sizeArrVar);
			if (newVar) {
				osmCode.newVar(var, token);
			}
			// if (token.get(0).getType().equals("arrDeclInit")) {
			CVarAffectation arrAff = new CVarAffectation(var, interfa, token);
			arrAff.generateConstantAffectation(osmCode, initValue);
			// }
			return;
		} else if (sizeArrVar < 0) {
			throw new CompilerException("The size of " + varName + " cann't be determined.", token);
		} else {
			var.setSizeOf(sizeArrVar);
			if (newVar) {
				osmCode.newVar(var, token);
			}
			return;
		}
	}

	public void generateConstantAffectation(AsmCode osmCode, ArrayList<Integer> initValue) throws CompilerException {
		// FOR ARRAY VALUE
		// TEST IF SIZE IS COMPATIBLE
		if (var.getSizeOf() != initValue.size()) {
			throw new CompilerException("The size of " + var.getName() + " and his affected value are incompatibles ("
					+ var.getSizeOf() + " vs " + initValue.size() + ").", token);
		}
		osmCode.add(" ; affectation de " + var.getName());
		for (int i = 0; i < initValue.size(); i++) {
			if (var.getStorageDad().equals("GLOBAL")) {
				osmCode.add("t<rp, " + initValue.get(i));
				osmCode.add("ram<t, " + (var.getPointer() + i));
			} else {
				throw new CompilerException(
						"The variable " + var + " can't be affected to any value because it's an non global array.",
						token);
			}
		}
	}

}
