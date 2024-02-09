package CodeGeneration.Cgenerators;

import CodeGeneration.AsmCode;
import General.CompilerException;
import General.Interface;
import LexAnalysis.SuperToken;
import SemAnalysis.Variable;

public class CExpAffectation extends CInstruction {

	public CExpAffectation(SuperToken token, Interface interfa) {
		super(token, interfa);
	}

	public void verifySize(Variable v) throws CompilerException {
		if (v.getSizeOf() == 1) {
			throw new CompilerException("You try to index a 1-byte variable.", token);
		}
	}

	@Override
	public void generate(AsmCode osmCode) throws CompilerException {
		SuperToken arrIdx = token.get(0).get(0);
		if ((token.get(0).size() != 1) || (!arrIdx.getType().equals("arrIdx"))) {
			throw new CompilerException("Left side assignement is prohibited.", token);
		}
		osmCode.add("; calcul de l'affectation " + token.getPerfectString());
		osmCode.add("ram<t, TAMP_INDEX_AFFECTATION ; save value to assign temporary");
		new CByteExpression(arrIdx.get("index").get(1), interfa, 0).generate(osmCode);
		Variable varToIdx;
		String id = osmCode.getUniqueId();

		if (osmCode.isVarParamExist(arrIdx.get("var").getPerfectString(), token)) {
			interfa.logMsg("wr idx to param array");
			varToIdx = osmCode.getVarParam(arrIdx.get("var").getPerfectString(), token);
			verifySize(varToIdx);
			osmCode.add("f<t");
			osmCode.add("ReadStackToT(" + varToIdx.getPointer() + ")	; sans toucher à f");
			osmCode.add("a<f");
			osmCode.add("b<t");
			osmCode.add("t<ALU(+)");
			osmCode.add("RW_SEC(" + id + ")");
			osmCode.add("ram<t, ADR(" + id + ")");
			osmCode.add("f<ram, TAMP_INDEX_AFFECTATION");
			osmCode.add(id + ":");
			osmCode.add("ram<f, 0	; fin de l'écriture");
			return;
		}

		// sinon variable classique

		varToIdx = osmCode.getVar(arrIdx.get("var").getPerfectString(), token);
		verifySize(varToIdx);

		if (varToIdx.getStorageDad().equals("GLOBAL")) {
			interfa.logMsg("wr idx to global array");
			osmCode.add("a<t");
			osmCode.add("b<rp, " + varToIdx.getPointer());
			osmCode.add("t<ALU(+)");
			osmCode.add("RW_SEC(" + id + ")");
			osmCode.add("ram<t, ADR(" + id + ")");
			osmCode.add("f<ram, TAMP_INDEX_AFFECTATION");
			osmCode.add(id + ":");
			osmCode.add("ram<f, 0	; fin de l'écriture");
			return;
		} else {
			interfa.logMsg("wr idx to local array...");

			if (varToIdx.getSizeOf() == -1) {
				interfa.logMsg("...with unknow size");
				osmCode.add("f<t");
				osmCode.add("ReadStackToT(" + varToIdx.getRedirectPointer() + ")	; sans toucher à f");
				osmCode.add("a<f");
				osmCode.add("b<t");
				osmCode.add("t<ALU(+)");
			} else {
				interfa.logMsg("... with know size");
				osmCode.add("a<t");
				osmCode.add("b<rp, " + varToIdx.getPointer());
				osmCode.add("t<ALU(+)");
			}
			osmCode.add("f<ram, TAMP_INDEX_AFFECTATION");
			osmCode.add("WriteStackFatT");
			return;
		}
	}

}
