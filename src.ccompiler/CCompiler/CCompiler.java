package CCompiler;

import java.util.ArrayList;

import CodeGeneration.AsmCode;
import CodeGeneration.CodeGenerator;
import General.CompilerException;
import General.Interface;
import General.Parameters;
import LexAnalysis.LexicalAnalysis;
import LexAnalysis.SuperToken;
import SynAnalysis.ListToken;
import SynAnalysis.SyntaxAnalysis;

public class CCompiler {

	public CCompiler() {

	}

	public void compileFile(Interface interfa, String codeTxt, String fileName) {
		try {
			compile(interfa, codeTxt, fileName);
			interfa.infoMsg("Compilation terminée sans erreur !");
		} catch (CompilerException e) {
			interfa.errorMsg(e.getLine(), e.getMsg());
		}
	}

	public void compile(Interface interfa, String codeTxt, String fileName) throws CompilerException {

		LexicalAnalysis lexicalAnalysis = new LexicalAnalysis(codeTxt, interfa);

		interfa.infoMsg("Analyse lexicale");

		ArrayList<SuperToken> tokens;

		tokens = lexicalAnalysis.buildTokens();

		SuperToken.printList(interfa, tokens);

		interfa.infoMsg("Analyse syntaxique");

		SyntaxAnalysis syntaxAnalysis = new SyntaxAnalysis(tokens, interfa);

		syntaxAnalysis.buildTree();

		tokens = syntaxAnalysis.getTree();

		interfa.logMsg("");
		interfa.infoMsg("Tokens Finaux :");

		SuperToken.printList(interfa, tokens);

		interfa.infoMsg("Analyse sémantique");
		/*
		 * SemanticAnalysis semanticAnalysis = new SemanticAnalysis(interfa); try {
		 * semanticAnalysis.sementicAnalysis(tokens); } catch (CompilerException e) {
		 * interfa.errorMsg(e.getMsg()); return Parameters.ERROR; }
		 */
		interfa.infoMsg("Generation de code");

		CodeGenerator codeGenerator = new CodeGenerator(interfa, new ListToken(Parameters.FINAL_TREE_TYPE, tokens));

		codeGenerator.generate();

		interfa.infoMsg("Compilation achevée");

		interfa.logMsg("Code généré : ");
		AsmCode code = codeGenerator.getAsmFile();
		code.printAllVar();

		code.computeHeader(fileName);

		System.out.println(
				"TODO : check type in expression + tester parametre de tailles variable + param avec valeur par default");
		System.out.println("TODO : calculer la taille des fct avec aussi la taille des buffer d'expression");
		interfa.codeMsg("" + code);

		interfa.logMsg("" + code);

		interfa.infoMsg(
				"Remember than the starting function should never end. You can use \"while(true)\" at the end of your program.");

		interfa.finishNoError();
	}

}
