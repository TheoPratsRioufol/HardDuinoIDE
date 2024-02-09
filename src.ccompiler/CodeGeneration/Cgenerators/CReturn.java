package CodeGeneration.Cgenerators;

import CodeGeneration.AsmCode;
import General.CompilerException;
import General.Interface;
import LexAnalysis.SuperToken;

public class CReturn extends CInstruction {

	public CReturn(SuperToken token, Interface interfa) {
		super(token, interfa);
	}

	@Override
	public void generate(AsmCode asmCode) throws CompilerException {

		if (asmCode.getCurrentContext().equals("GLOBAL"))
			throw new CompilerException("Yon can't return in a global area. You must be in a function", token);

		if (token.get(0).getType().equals("returnNoValue")) {
			asmCode.add("Jump(return_" + asmCode.getCurrentFctContext() + ")	; " + token.getPerfectString());
			return;
		} else if (token.get(0).getType().equals("returnValue")) {
			new CByteExpression(token.get(0).get("bExp"), interfa, 0);
			asmCode.add("ram<t, BUF_RETURN_FCT");
			asmCode.add("Jump(return_" + asmCode.getCurrentFctContext() + ")	; " + token.getPerfectString());
			return;
		}
		throw new CompilerException("Invalid return statement", token);
	}

}
