package topanalysis;

import java.util.ArrayList;
import java.util.List;

import bcompiler.general.BinCompilerException;

public abstract class AsmInst {

	protected List<String> parameters;
	protected int lineNumber;
	protected List<List<String>> args = null;

	public AsmInst(List<String> parameters, int lineNumber) {
		this.parameters = parameters;
		this.lineNumber = lineNumber;
	}

	public String toString() {
		return parameters.toString();
	}

	public boolean containsMacro(Macro macro) {
		String macroName = macro.getName();
		for (String param : parameters) {
			if (param.equals(macroName))
				return true;
		}
		return false;
	}

	public void replaceMacro(Macro macro) {
		System.out.println("Replacing..." + parameters);
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public boolean isOnlyMacro() {

		args = new ArrayList<>();
		List<String> arg = new ArrayList<>();

		if (parameters.size() == 1)
			return true;
		if ((parameters.size() < 3) || (parameters.get(1) != "("))
			return false;

		int parenthesisOpen = 1;
		String elm = "";
		for (int i = 2; i < parameters.size(); i++) {
			elm = parameters.get(i);
			if (elm.equals("("))
				parenthesisOpen++;
			else if (elm.equals(")"))
				parenthesisOpen--;
			else if (elm.equals(",") || (i == parameters.size() - 1)) {
				args.add(arg);
				arg = new ArrayList<>();
			} else {
				arg.add(elm);
			}
		}
		if (!parameters.get(parameters.size() - 1).equals(")"))
			return false;
		return true;
	}

	public List<List<String>> getArgs() throws BinCompilerException {
		if (args == null)
			throw new BinCompilerException("[intern] Arg should be extracted before used", lineNumber);
		return args;
	}

}
