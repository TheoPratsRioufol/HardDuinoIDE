package topanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bcompiler.general.BinCompilerException;
import bcompiler.general.Parameters;

public class Macro {

	private List<String> parameters;
	private int lineNumber;

	public Macro(List<String> parameters, int lineNumber) throws BinCompilerException {
		this.parameters = parameters;
		this.lineNumber = lineNumber;
		System.out.println("macro : " + parameters);
	}

	public List<AsmInst> getInsts(List<List<String>> args) throws BinCompilerException {
		List<AsmInst> insts = new ArrayList<AsmInst>();
		Map<String, List<String>> paramSubsti = new HashMap<>();

		// ETAPE 1 ON RECUPERE LES PARAMETRES A REMPLACER
		boolean parentesis = parameters.get(2).equals("(");
		int argNub = 0;
		int idxEnd = 2;

		for (int i = 3; i < parameters.size(); i++) {
			if (parameters.get(2).equals(")")) {
				parentesis = false;
				idxEnd = i;
				break;
			} else {
				if (argNub > args.size())
					throw new BinCompilerException("Not enought macro parameters", lineNumber);
				paramSubsti.put(parameters.get(2), args.get(argNub));
				argNub++;
			}
		}
		if (parentesis)
			throw new BinCompilerException("Parenthesis never closed", lineNumber);
		if (argNub != args.size())
			throw new BinCompilerException("Too much parameters for the macro", lineNumber);
		if (idxEnd + 1 > parameters.size() + 1)
			throw new BinCompilerException("Macro body invalid", lineNumber);

		// ETAPE 2 : GENERATION
		List<String> instbuffer = new ArrayList<>();
		for (int i = idxEnd + 1; i < parameters.size(); i++) {
			List<String> instFromArgs = paramSubsti.get(parameters.get(i));
			if (instFromArgs != null)
				instbuffer.addAll(instFromArgs);
			if (parameters.get(i) == Parameters.NEW_LINE) {
				TopAnalysis.generateInstFromLine(instbuffer, null, insts, lineNumber);
				instbuffer = new ArrayList<>();
			} else if (parameters.get(i) == "#macro") {
				throw new BinCompilerException("Illegal to define macro inside macro", lineNumber);
			}
		}

		return insts;
	}

	public String getName() {
		return parameters.get(1);
	}

	public boolean isMultipleInst() {
		return parameters.contains(Parameters.NEW_LINE);
	}

}
