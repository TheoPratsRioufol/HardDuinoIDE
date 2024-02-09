package CodeGeneration.Cgenerators;

import CodeGeneration.AsmCode;
import General.CompilerException;
import General.Interface;
import LexAnalysis.SuperToken;

public abstract class CInstruction {
	// TOKEN IS A EXPRESSION TYPE
	protected SuperToken token;
	protected Interface interfa;

	public CInstruction(SuperToken token, Interface interfa) {
		this.token = token;
		this.interfa = interfa;
	}

	public void generate(AsmCode osmCode) throws CompilerException {
		throw new CompilerException("Not implemented !", token);
	}

}
