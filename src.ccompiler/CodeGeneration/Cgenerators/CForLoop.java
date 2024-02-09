package CodeGeneration.Cgenerators;

import CodeGeneration.AsmCode;
import CodeGeneration.CodeGenerator;
import General.CompilerException;
import General.Interface;
import LexAnalysis.SuperToken;

public class CForLoop extends CInstruction {

	public CForLoop(SuperToken token, Interface interfa) {
		super(token, interfa);
	}

	private static boolean isCorrectForArg(SuperToken inst) {
		return inst.getType().equals("affectation") || inst.getType().equals("affectationWithDec")
				|| inst.getType().equals("evaluation");
	}

	@Override
	public void generate(AsmCode asmCode) throws CompilerException {
		interfa.logMsg("Generating for loop");

		SuperToken inst = token.get(2);

		if (inst.size() != 2)
			throw new CompilerException("Invalid number of argument in \"for\" loop", token);
		SuperToken instA = inst.get(0).get(0);
		SuperToken instB = inst.get(1).get(0);
		SuperToken instC = token.get(3);
		SuperToken body = null;

		if (token.size() > 6)
			body = token.get(6);

		if (!isCorrectForArg(instA) || !isCorrectForArg(instB) || !instC.getType().equals("bExp"))
			throw new CompilerException("Invalid argument in \"for\" loop." + instA.getType(), token);

		// Initializing the for loop
		String id = "for_" + asmCode.getUniqueId();
		asmCode.newJumpContext(id);
		asmCode.newContext(id);
		CodeGenerator.generateToken(instA, asmCode, interfa);

		// Compute condition
		asmCode.add("$body_" + id);
		CodeGenerator.generateToken(instB, asmCode, interfa);
		CWhileLoop.generateJumpIf(asmCode, id);

		// Compute iterator at the end
		CodeGenerator.generateToken(instC, asmCode, interfa);

		// loop body
		interfa.logMsg("for loop tks = " + token.getTypes());
		if ((body != null) && body.getType().equals("instruction")) {
			CodeGenerator.generateToken(body, asmCode, interfa);
		}

		asmCode.restoreJumpPreviousContext();
		asmCode.restorePreviousContext();
		asmCode.add("Jump(body_" + id + ")");
		asmCode.add("$return_" + id + ":");

	}

}
