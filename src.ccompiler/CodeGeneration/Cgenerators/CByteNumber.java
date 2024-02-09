package CodeGeneration.Cgenerators;

import CodeGeneration.AsmCode;
import General.CompilerException;
import General.Interface;
import LexAnalysis.SuperToken;
import SemAnalysis.DataType;
import SemAnalysis.Variable;

public class CByteNumber extends CInstruction {

	// COMPUTE THE RESULT OF A 8 BIT TERM IN EXPRESSION
	CByteExpression dadExp;

	public CByteNumber(SuperToken token, Interface interfa, CByteExpression dadExp) {
		super(token, interfa);
		this.dadExp = dadExp;
	}

	public void isDataTypeCompatible(DataType dataType) throws CompilerException {
		// si c'est le premier terme de l'expression, l'expression devient son type
		if (dadExp.getDataType().getType().equals("any") || dadExp.getDataType().isUndefined()) {
			dadExp.setDataType(dadExp.getDataType().mergeDataType(dataType));
			return;
		}
		if (!dadExp.getDataType().equals(dataType))
			throw new CompilerException(
					"Types incompatibles, exepted " + dadExp.getDataType() + " vs " + dataType + ".", token);

	}

	@Override
	public void generate(AsmCode osmCode) throws CompilerException {
		interfa.logMsg("Generating CByte for " + token.getType());
		if (token.getType().equals("number")) {
			if ((token.getNumericValue() >= 0) && (token.getNumericValue() < 256)) {
				osmCode.add("t<rp, " + token.getNumericValue());
				isDataTypeCompatible(new DataType("any", 1));
				return;
			} else {
				throw new CompilerException("Out of bound numeric value. Use only BYTE : B-Y-T-E !", token);
			}
		} else if (token.getType().equals("var")) {
			if (token.getPerfectString().equals("PORTD")) {
				isDataTypeCompatible(new DataType("any", 1));
				osmCode.add("t<portd");
				return;
			}
			if (token.getPerfectString().equals("PORTB") || token.getPerfectString().equals("PORTC"))
				throw new CompilerException("The value of PORTB or PORTC can't by used because there are output ports.",
						token);
			if (osmCode.isVarParamExist(token.getPerfectString(), token)) {
				interfa.logMsg("generating Param var");
				System.out.println(token.getPerfectString() + " is a parameter of a fct");
				Variable varToReadFct = osmCode.getVarParam(token.getPerfectString(), token);
				System.out.println("from var = " + varToReadFct);
				isDataTypeCompatible(varToReadFct.getDataType());
				if (varToReadFct.getSizeOf() == 1) {
					// c'est une variable classique locale préchargée
					osmCode.add("ReadStack(" + varToReadFct.getStorageDad() + "_fct + " + varToReadFct.getPointer()
							+ ") ; lecture de " + varToReadFct);
					return;
				} else if (varToReadFct.getSizeOf() == -1) {
					// c'est le pointeur d'un array
					throw new CompilerException(
							"This expressions isn't homegenous : " + varToReadFct.getName() + " is an array", token);
				}
			} else if (osmCode.isVarExit(token.getPerfectString(), token)) {
				interfa.logMsg("generating global or local var");
				Variable varToRead = osmCode.getVar(token.getPerfectString(), token);
				isDataTypeCompatible(varToRead.getDataType());
				if (varToRead.getSizeOf() != 1)
					throw new CompilerException(
							"The size of " + varToRead.getName() + " is incompatible with a byte expression", token);
				if (varToRead.getStorageDad().equals("GLOBAL")) {
					osmCode.add("t<ram, " + varToRead.getPointer() + " ; lecture de " + varToRead);
					return;
				} else {
					osmCode.add("ReadStack(" + varToRead.getStorageDad() + "_fct + " + varToRead.getPointer()
							+ ") ; lecture de " + varToRead);
					return;
				}
			} else {
				System.out.println(osmCode.isVarExit(token.getPerfectString(), token) + " => "
						+ token.getPerfectString() + osmCode.getCurrentContext());
				throw new CompilerException("Unknow variable : " + token.getPerfectString(), token);
			}
		} else if (token.getType().equals("arrIdx")) {
			// gerer les u[] ou u est un parametre (donc il faut lire son index)
			Variable varToIdx;
			if (osmCode.isVarExit(token.get(0).getPerfectString(), token))
				varToIdx = osmCode.getVar(token.get(0).getPerfectString(), token);
			else if (osmCode.isVarParamExist(token.get(0).getPerfectString(), token))
				varToIdx = osmCode.getVarParam(token.get(0).getPerfectString(), token);
			else
				throw new CompilerException("Unknow variable : " + token.getPerfectString(), token);
			System.out.println("ARR EXPI INDX TYPE:" + token.get(1).get("bExp").getTypes());
			if (varToIdx.getSizeOf() == 1) {
				throw new CompilerException("Illegal to index a byte (which is not an array)", token);
			}
			isDataTypeCompatible(new DataType(varToIdx.getDataType().getType(), 1));
			SuperToken expTk = token.get(1).get("bExp");
			String id = osmCode.getUniqueId();
			CByteExpression exp = new CByteExpression(expTk, interfa, dadExp.getAccOffset() + 1);
			exp.generate(osmCode);
			if (varToIdx.getStorageDad().equals("GLOBAL")) {
				if (expTk.get(0).getType().equals("number") && expTk.size() == 1) {
					interfa.logMsg("variable " + varToIdx.getName() + " indexed by a constant");
					osmCode.add("t<ram, " + (int) (varToIdx.getPointer() + expTk.get(0).getNumericValue())
							+ "; lecture de " + varToIdx.getName());
					return;
				} else {
					interfa.logMsg("variable " + varToIdx.getName() + " indexed by a expression");
					osmCode.add("a<t");
					osmCode.add("b<rp, " + varToIdx.getPointer());
					osmCode.add("t<ALU(+)");
					osmCode.add("RWSEC(wr_idx_" + id + ")");
					osmCode.add("ram<t, wr_idx_" + id);
					osmCode.add("$wr_idx_" + id);
					osmCode.add("t<ram, 0; lecture de " + token.getPerfectString());
					return;
				}
			} else if (!varToIdx.isParameter()) {
				interfa.logMsg(varToIdx.getName() + " is a local variable index");
				if (varToIdx.getSizeOf() < 0) {
					interfa.logMsg("his size is not know, we use redirector");
					// a ce state on a t qui a notre index
					osmCode.add("f<t");
					osmCode.add("ReadStackToT(" + varToIdx.getRedirectPointer() + ") ; FAUT PAS MODIFIER f");
					// t a l'adresse de début de l'array
					osmCode.add("a<t");
					osmCode.add("b<f");
					osmCode.add("t<ALU(+)");
					osmCode.add("ReadStackToTFromT" + varToIdx.getRedirectPointer() + ") ; lecture de "
							+ token.getPerfectString());
					return;
				} else {
					interfa.logMsg("his size is know, we go direct");
					osmCode.add("a<t");
					osmCode.add("b<rp, " + varToIdx.getPointer());
					osmCode.add("t<ALU(+)");
					osmCode.add("ReadStackToTFromT  ; lecture de " + token.getPerfectString());
					return;
				}
			} else if (varToIdx.isParameter()) {
				interfa.logMsg(varToIdx.getName() + " is a param index");
				osmCode.add("f<t");
				osmCode.add("ReadStackToT(" + varToIdx.getPointer() + ") ; FAUT PAS MODIFIER f");
				osmCode.add("a<t");
				osmCode.add("b<f");
				osmCode.add("t<ALU(+)");
				osmCode.add("RWSEC(wr_idx_" + id + ")");
				osmCode.add("ram<t, wr_idx_" + id);
				osmCode.add("$wr_idx_" + id);
				osmCode.add("t<ram, 0; lecture de " + token.getPerfectString());
				return;
			}
		} else if (token.getType().equals("functionEval")) {
			// il va faloir vérifier que l'on a le bon nombre de paramettre
			// et preset ceux par défaut
			CParamAffectation paramAff = new CParamAffectation(token, interfa, dadExp.getAccOffset() + 1);
			paramAff.generate(osmCode);
			osmCode.add("t<ram, BUF_RETURN_FCT");
			isDataTypeCompatible(paramAff.getDataType());
			return;
		} else if (token.getType().equals("pbExp")) {
			CByteExpression exp = new CByteExpression(token.get(1), interfa, dadExp.getAccOffset() + 1);
			exp.generate(osmCode);
			isDataTypeCompatible(exp.getDataType());
			return;
		}
		System.out.println(token.getType());
		throw new CompilerException("CByteNumber - This token haven't any numeric value associed.", token);

	}

}
