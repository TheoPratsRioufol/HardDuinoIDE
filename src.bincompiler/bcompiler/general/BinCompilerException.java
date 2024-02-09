package bcompiler.general;

@SuppressWarnings("serial")
public class BinCompilerException extends Exception {

	private String msg;
	private int lineNumber;

	public BinCompilerException(String msg, int lineNumber) {
		super("");
		this.msg = msg;
		this.lineNumber = lineNumber;
	}

	public String getMsg() {
		return "(line " + lineNumber + ") : " + msg;
	}

	public int getLineNumber() {
		return lineNumber;
	}

}
