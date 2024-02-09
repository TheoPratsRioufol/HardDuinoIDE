package CodeGeneration;

import CodeGeneration.Cgenerators.CByteExpression;
import CodeGeneration.Cgenerators.CExpAffectation;
import CodeGeneration.Cgenerators.CFctDeclaration;
import CodeGeneration.Cgenerators.CForLoop;
import CodeGeneration.Cgenerators.CJumpInstruction;
import CodeGeneration.Cgenerators.CReturn;
import CodeGeneration.Cgenerators.CVarAffectation;
import CodeGeneration.Cgenerators.CVarDeclaration;
import CodeGeneration.Cgenerators.CWhileLoop;
import General.CompilerException;
import General.Interface;
import LexAnalysis.SuperToken;

public class CodeGenerator {

	private Interface interfa;
	private SuperToken mainToken;
	private AsmCode asmFile;

	public CodeGenerator(Interface interfa, SuperToken mainToken) {
		this.mainToken = mainToken;
		this.interfa = interfa;
		asmFile = new AsmCode(interfa);
	}

	private static void canIGenerate(SuperToken tk, AsmCode asmFile) throws CompilerException {
		if (asmFile.getCurrentContext().equals("GLOBAL")
				&& !(tk.getType().equals("affectationWithDec") || tk.getType().equals("functionDeclaration")))
			throw new CompilerException("In a global zone, on are only authorised to declare variables and functions.",
					tk);

	}

	private static boolean isTokenHasGenerated(SuperToken tk, AsmCode asmFile, Interface interfa)
			throws CompilerException {
		if (tk.getType().equals("bExp")) {
			new CByteExpression(tk, interfa, 0).generate(asmFile);
			return true;
		} else if (tk.getType().equals("affectationWithDec")) {
			new CVarDeclaration(tk, interfa).generate(asmFile);
			return true;
		} else if (tk.getType().equals("affectation")) {
			if (tk.get(0).getType().equals("var")) {
				new CVarAffectation(tk.get("var"), asmFile, interfa, tk).generate(asmFile);
				return true;
			}
			new CExpAffectation(tk, interfa).generate(asmFile);
			return true;
		} else if (tk.getType().equals("functionDeclaration")) {
			new CFctDeclaration(tk, interfa).generate(asmFile);
			return true;
		} else if (tk.getType().equals("declaParameter") || tk.getType().equals("declaParameterInit")) {
			// DECLARATION AU SEIN D'UNE FONCTION
			interfa.logMsg("PPPARAMETER" + tk + "//" + tk.get(0).getType());
			new CVarDeclaration(tk, interfa).generate(asmFile);
			return true;
		} else if (tk.getType().equals("return")) {
			new CReturn(tk, interfa).generate(asmFile);
			return true;
		} else if (tk.getType().equals("whileLoop")) {
			new CWhileLoop(tk, interfa).generate(asmFile);
			return true;
		} else if (tk.getType().equals("forLoop")) {
			new CForLoop(tk, interfa).generate(asmFile);
			return true;
		} else if (tk.getType().equals("evaluation")) {
			asmFile.add("; evaluation of " + tk.getPerfectString());
			new CByteExpression(tk.get("bExp"), interfa, 0).generate(asmFile);
			return true;
		} else if (tk.getPerfectString().equals(asmFile.getSeparatorToIgnore())) {
			interfa.warningMsg("separator ignored : " + tk.getPerfectString());
			return true;
		} else if (tk.getType().equals("escape")) {
			new CJumpInstruction(tk, interfa).generate(asmFile);
			return true;
		}
		return false;
	}

	public static void generateToken(SuperToken tk, AsmCode asmFile, Interface interfa) throws CompilerException {
		// ON REGARDE SI ON PEUT TRAITER DIRECTEMENT
		if (isTokenHasGenerated(tk, asmFile, interfa)) {
			canIGenerate(tk, asmFile);
			return;
		}
		// ON REGARDE A L'ECHELLE EN DESSOUS
		for (int i = 0; i < tk.size(); i++) {
			generateToken(tk.get(i), asmFile, interfa);
		}
		if (tk.size() == 0) {
			throw new CompilerException(
					"This token haven't been generated. Context is " + asmFile.getCurrentContext() + ".", tk);
			// interfa.errorMsg("This token haven't been generated"+tk);
		}
	}

	public void generate() throws CompilerException {
		generateToken(mainToken, asmFile, interfa);
		generateEndOperations(); // like size of the function, marcro, titles ...
	}

	public void generateEndOperations() {
		asmFile.add("; ================== End =============");
		asmFile.computeSizeOfFct();
	}

	public AsmCode getAsmFile() {
		return asmFile;
	}

}
