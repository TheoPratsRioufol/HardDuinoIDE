package topanalysis;

import java.util.ArrayList;
import java.util.List;

import bcompiler.general.BinCompilerException;
import bcompiler.general.Parameters;

public class TopAnalysis {

	private String asmTxt;
	private List<AsmInst> instructions;
	private List<Macro> macros;

	public TopAnalysis(String asmTxt) {
		this.asmTxt = asmTxt;
	}

	public void generateInsts() throws BinCompilerException {
		String buffer = "";
		char c = '\0';
		char prevC = '\0';
		boolean comment = false;
		int lineNumber = 1;
		boolean accolade = false;

		instructions = new ArrayList<>();
		macros = new ArrayList<>();
		List<String> parameters = new ArrayList<>();

		for (int i = 0; i < asmTxt.length(); i++) {
			// au maximum une instruction par ligne
			c = asmTxt.charAt(i);
			if (c == '{')
				accolade = true;
			else if (c == '}')
				accolade = false;
			if (c == ';') {
				comment = true;
			} else if ((c == '\n') || (i == asmTxt.length() - 1)) {
				if ((i == asmTxt.length() - 1) && !isSpace(c))
					buffer += c;
				if (buffer.length() > 0)
					parameters.add(buffer);
				if ((parameters.size() > 0) && !accolade) {
					generateInstFromLine(parameters, macros, instructions, lineNumber);
					parameters = new ArrayList<>();
				}
				if (accolade) {
					parameters.add(Parameters.NEW_LINE);
				}
				buffer = "";
				comment = false;
				lineNumber++;
			} else if (!isSpace(c) && !comment) {
				if (isSpace(prevC) || isSeparator(c)) {
					if (buffer.length() > 0)
						parameters.add(buffer);
					buffer = "";
					if (isSeparator(c))
						parameters.add("" + c);
					else
						buffer += c;
				} else
					buffer += c;
			}
			prevC = c;
		}

	}

	public void replaceMacro() throws BinCompilerException {
		for (Macro m : macros) {
			for (int i = 0; i < instructions.size(); i++) {
				if (instructions.get(i).containsMacro(m)) {
					if (m.isMultipleInst()) { // vérifier qu'il y a bien que la macro (éventuelkement avec parametres),
												// qui doit avoir les parametre qui commencent en idx
						if (!instructions.get(i).isOnlyMacro())
							throw new BinCompilerException("Multilines macro can only be used alone",
									instructions.get(i).getLineNumber());
						for (AsmInst inst : m.getInsts(instructions.get(i).getArgs())) {
							instructions.add(inst);
						}
						System.out.println("multi inst" + instructions.get(i));
					} else {
						System.out.println("rp" + m.getName());
						instructions.get(i).replaceMacro(m);
					}
				}
			}
		}
	}

	public static void generateInstFromLine(List<String> parameters, List<Macro> macros, List<AsmInst> instructions,
			int lineNumber) throws BinCompilerException {
		if (parameters.get(0).equals("#macro")) {
			if (parameters.size() < 3)
				throw new BinCompilerException("Invalid number of argument for macro", lineNumber);
			System.out.println("aDDD" + parameters);
			macros.add(new Macro(parameters, lineNumber));
			return;
		}
		instructions.add(new AsmAffectation(parameters, lineNumber));
	}

	public static boolean isSpace(char c) {
		return (c == ' ') || (c == '	') || (c == '\r');
	}

	public static boolean isSeparator(char c) {
		return (c == ',') || (c == '<') || (c == '(') || (c == ')');
	}

}
