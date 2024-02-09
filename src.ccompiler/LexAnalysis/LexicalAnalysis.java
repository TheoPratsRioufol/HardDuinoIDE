package LexAnalysis;

import java.util.ArrayList;

import General.CompilerException;
import General.Interface;
import General.Parameters;

public class LexicalAnalysis {

	private String codeTxt;
	private Interface interfa;

	public LexicalAnalysis(String codeTxt, Interface interfa) {
		this.codeTxt = codeTxt;
		this.interfa = interfa;
	}

	private void replaceDefineInCode(String oldStr, String newStr) {
		String newCode = "";
		for (int i = 0; i < codeTxt.length(); i++) {
			if ((i < codeTxt.length() - 1 - oldStr.length()) && (isSeparator(codeTxt.charAt(oldStr.length())))
					&& codeTxt.substring(i, oldStr.length()).equals(oldStr))
				i += oldStr.length();
			else
				newCode += codeTxt.charAt(i);
		}
		codeTxt = newCode;
	}

	private void executeDefine(int index) throws CompilerException {
		String buffer = "";
		String[] params = { "", "" };
		int idxParam = 0;
		int endIdx = -1;
		boolean previousGoInBuf = false;
		for (int i = index + Parameters.DEFINE.length(); i < codeTxt.length() - Parameters.DEFINE.length(); i++) {
			if (((codeTxt.charAt(i) == ' ') && (idxParam == 0)) || (codeTxt.charAt(i) == '\n')) {
				if (previousGoInBuf) {
					params[idxParam] += buffer;
					idxParam = 1;
					buffer = "";
				}
				if (codeTxt.charAt(i) == '\n') {
					endIdx = i;
					break;
				}
			} else {
				previousGoInBuf = true;
				buffer += codeTxt.charAt(i);
			}
		}
		interfa.logMsg("#define executed by replacing all " + params[0] + " with " + params[1]);
		if (!Token.isString(params[0]) || (endIdx < 0) || (params[1].length() == 0)) {
			throw new CompilerException(Parameters.INCORRECT_DEFINE_ARGS, null);
		}
		codeTxt = codeTxt.substring(0, index) + codeTxt.substring(endIdx);
		replaceDefineInCode(params[0], params[1]);
		// codeTxt = codeTxt.replaceAll(params[0], params[1]);
		// this.codeTxt = buffer;
	}

	private void executeInclude(int index) throws CompilerException {
		interfa.errorMsg(0, "Not implemented yet");
	}

	private int getIndexOfFirstStr(String str) {
		for (int i = 0; i < codeTxt.length() - str.length(); i++) {
			if (codeTxt.substring(i, i + str.length()).equals(str)) {
				return i;
			}
		}
		return -1;
	}

	public void lookPreprocessors() throws CompilerException {
		// EXECUTE PREPROCESSOR
		for (int k = 0; k < Parameters.MAX_PREPROCESSOR_COUNT; k++) {
			int idx = getIndexOfFirstStr(Parameters.DEFINE);
			if (idx < 0) {
				break;
			} else {
				executeDefine(idx);
			}
		}
		for (int k = 0; k < Parameters.MAX_PREPROCESSOR_COUNT; k++) {
			int idx = getIndexOfFirstStr(Parameters.INCLUDE);
			if (idx < 0) {
				break;
			} else {
				executeInclude(idx);
			}
		}
	}

	private void clearComments() { // on fait aussi le trie dans les espaces
		String codeTxtPur = "";
		boolean multiLineComment = false;
		boolean singleLineComment = false;

		for (int i = 0; i < codeTxt.length(); i++) {
			// ON GARDE LE NOMBRE DE LIGNE
			// filtered coms :
			if (codeTxt.charAt(i) == '/') {
				if (i < codeTxt.length() - 1) {
					if (codeTxt.charAt(i + 1) == '*') {
						multiLineComment = true;
					} else if (codeTxt.charAt(i + 1) == '/') {
						singleLineComment = true;
					}
				}
				if ((i > 0) && codeTxt.charAt(i - 1) == '*') {
					multiLineComment = false;
					continue;
				}
			} else if ((codeTxt.charAt(i) == '\n') && singleLineComment) {
				singleLineComment = false;
			}

			if (((codeTxt.charAt(i) == '\n') || !(singleLineComment || multiLineComment))
					&& isAllowed(codeTxt.charAt(i))) {
				codeTxtPur += codeTxt.charAt(i);
			}
		}
		this.codeTxt = codeTxtPur;
	}

	public ArrayList<SuperToken> buildTokens() throws CompilerException {
		// we tokennize
		// making string packet and filtered comments
		clearComments();
		lookPreprocessors();

		String buffer = "";
		boolean stringDefinition = false;
		int lineNumber = 0;
		ArrayList<SuperToken> tokens = new ArrayList<SuperToken>();

		for (int i = 0; i < codeTxt.length(); i++) {

			if (codeTxt.charAt(i) == '\n') {
				lineNumber++;
			}

			if (codeTxt.charAt(i) == '"') {
				stringDefinition = !stringDefinition;
			} else if (codeTxt.charAt(i) == '\n') {
				stringDefinition = false;
			}

			if ((i < codeTxt.length() - 3) && (codeTxt.charAt(i) == '\'') && (codeTxt.charAt(i + 2) == '\'')) {
				// char
				buffer += "'" + codeTxt.charAt(i + 1) + "'";
				i += 3;
			}

			if ((stringDefinition || ((codeTxt.charAt(i) != ' ') && (codeTxt.charAt(i) != '\n')))) {
				// clear spaces
				buffer += codeTxt.charAt(i);
			}

			if (((i == codeTxt.length() - 1) || isSeparator(codeTxt.charAt(i))
					|| (((codeTxt.charAt(i) == ' ') && !stringDefinition))) && (buffer.length() > 0)) {
				if (isSeparator(codeTxt.charAt(i)) && !stringDefinition) {
					if (buffer.length() > 1) {
						if (codeTxt.charAt(i) != '\n') {
							buffer = buffer.substring(0, buffer.length() - 1);
						}
						tokens.add(tokenFromStr(buffer, lineNumber));
					}
					if (codeTxt.charAt(i) != '\n') {
						tokens.add(tokenFromStr("" + codeTxt.charAt(i), lineNumber));
					}
					buffer = "";
				} else {
					tokens.add(tokenFromStr(buffer, lineNumber));
					buffer = "";
				}
			}
		}

		return tokens;
	}

	private SuperToken tokenFromStr(String str, int lineNumber) {
		return new Token(str, lineNumber);
	}

	private boolean isSeparator(char c) {
		return (Parameters.SEPARATOR_LIST.indexOf(c) >= 0);
	}

	private boolean isAllowed(char c) {
		return (Parameters.ALLOWED_CHAR.indexOf(c) >= 0);
	}

}
