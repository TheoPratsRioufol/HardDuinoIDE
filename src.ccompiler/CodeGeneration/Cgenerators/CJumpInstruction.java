package CodeGeneration.Cgenerators;

import CodeGeneration.AsmCode;
import General.CompilerException;
import General.Interface;
import LexAnalysis.SuperToken;

public class CJumpInstruction extends CInstruction {

	public CJumpInstruction(SuperToken token, Interface interfa) {
		super(token, interfa);
	}

	@Override
	public void generate(AsmCode asmCode) throws CompilerException {
		if (asmCode.getCurrentJumpContext().equals("GLOBAL"))
			throw new CompilerException("Yon can't use break or continue outside a loop.", token);

		if (token.get(0).getPerfectString().equals("break")) {
			asmCode.add("Jump(return_" + asmCode.getCurrentJumpContext() + ")   ; break");
			return;
		} else if (token.get(0).getPerfectString().equals("continue")) {
			asmCode.add("Jump(body_" + asmCode.getCurrentJumpContext() + ")   ; continue");
			return;
		}

		throw new CompilerException("Jump statement unkwow.", token);
	}

}
