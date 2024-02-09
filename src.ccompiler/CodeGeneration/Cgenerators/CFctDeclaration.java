package CodeGeneration.Cgenerators;

import CodeGeneration.AsmCode;
import CodeGeneration.CodeGenerator;
import General.CompilerException;
import General.Interface;
import LexAnalysis.SuperToken;
import SemAnalysis.Function;

public class CFctDeclaration extends CInstruction {

	public CFctDeclaration(SuperToken token, Interface interfa) {
		super(token, interfa);
	}

	public void generate(AsmCode osmCode) throws CompilerException {
		// System.out.println("GENERATION D'UNE FCT, " + token.getTypes());
		String fctName = token.get("bDecl").get("var").getPerfectString();
		Function fctDecl = new Function(fctName, token.get("bDecl").get("typeToken").getPerfectString());
		osmCode.newFct(fctDecl, token);
		if (!osmCode.getCurrentContext().equals("GLOBAL")) {
			throw new CompilerException("It's illegal to define a function inside a function", token);
		}
		osmCode.newContext(fctName);
		osmCode.newFctContext(fctName);
		osmCode.add("@" + fctName + "_fct:   ; Zone de définition de cette fonction");
		osmCode.setSeparatorToIgnore(",");
		// PUIS GENERATION DU CODE
		// GESTION DES PARAMETRES D'APPEL
		if (token.has("declaParameterArray")) {
			SuperToken parameters = token.get("declaParameterArray");
			// System.out.println("params=" + parameters + " SIZE = " + parameters.size());
			// System.out.println(parameters.get(0).getTypes());
			CodeGenerator.generateToken(parameters, osmCode, interfa);
		}
		osmCode.setSeparatorToIgnore("");
		// GESTION DU CODE A L'INTERIEUR
		CodeGenerator.generateToken(token.get("instruction"), osmCode, interfa);
		// GESTION DU RETOUR
		osmCode.add("$return_" + fctName + ":");
		osmCode.add("ReturnFct   ; ------ fin de " + fctName);
		osmCode.restorePreviousContext();
		osmCode.restoreFctPreviousContext();
	}

}
