package CodeGeneration.Cgenerators;

import CodeGeneration.AsmCode;
import CodeGeneration.CodeGenerator;
import General.CompilerException;
import General.Interface;
import LexAnalysis.SuperToken;

public class CWhileLoop extends CInstruction {

	public CWhileLoop(SuperToken token, Interface interfa) {
		super(token, interfa);
	}

	@Override
	public void generate(AsmCode asmCode) throws CompilerException {
		interfa.logMsg("GENERATING WHILE LOOP. Element of while = " + token.getTypes());
		String id = "while_" + asmCode.getUniqueId();

		// while (do_something(), do_something_else(), i < n);
		// Because statements connected with the comma operator evaluate to the
		// rightmost statement,

		asmCode.add(id + ":	; boucle while(" + token.get("evalParameterArray").getPerfectString() + ")");
		asmCode.newJumpContext(id);
		// ON COMMENCE PAR VERIFIER LA CONDITION
		// QUI EST LA DERNIERE DE L'ARRAY D'EXPRESSION
		// IL SUFFIT DE CALCULER TT LES EXP ET PRENDRE LA VALEUR DU REG T A LA FIN
		asmCode.setSeparatorToIgnore(",");
		CodeGenerator.generateToken(token.get("evalParameterArray"), asmCode, interfa);
		asmCode.setSeparatorToIgnore("");

		// PUIS TEST DE T
		generateJumpIf(asmCode, id);

		if (token.has("instruction"))
			CodeGenerator.generateToken(token.get("instruction"), asmCode, interfa);

		asmCode.add("$return_" + id + ":");
		asmCode.restoreJumpPreviousContext();
	}

	public static void generateJumpIf(AsmCode asmCode, String id) {
		asmCode.add("a<t	; Test de la condition du while");
		asmCode.add("f<ALU(NON_ZERO)");
		asmCode.add("section<rp, SEC(return_jumper_" + id + ")");
		asmCode.add("a<rp, ADR(body_" + id + ")");
		asmCode.add("b<rp, ADR(return_" + id + ")");
		asmCode.add("&BEGIN ; same section constrains");
		asmCode.add("buf<if, NON_ZERO_MASK");
		asmCode.add("$return_jumper_" + id + ":");
		asmCode.add("Jump(return_" + id + ")");
		asmCode.add("$body_" + id + ":");
		asmCode.add("&END ; same section constrains end");
	}

}
