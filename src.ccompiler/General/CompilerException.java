package General;

import LexAnalysis.SuperToken;

public class CompilerException extends Exception {

	private String msg;
	private SuperToken tk;

	public CompilerException(String msg, SuperToken tk) {
		super("");
		this.msg = msg;
		this.tk = tk;
	}

	public String getMsg() {
		if (tk != null) {
			return "\"" + tk + "\"\n(line " + (tk.getLine() + 1) + ") " + msg;
		}
		return "\"?\"\n(line ?) " + msg;
	}

	public int getLine() {
		if (tk != null)
			return tk.getLine();
		return 0;
	}

}
