package bcompiler;

import General.Interface;
import bcompiler.general.BinCompilerException;
import topanalysis.TopAnalysis;

public class BCompiler {

	public static final String FAKE_ASM = "#macro CST 25\r\n" + "\r\n" + "Jump(@main+1) \r\n" + "t<rp, 111\r\n"
			+ "ram<t, 10\r\n" + "t<rp, 110\r\n" + "ram<t, 11\r\n" + "\r\n" + "@main:\r\n" + "   a<b\r\n"
			+ "   f < t\r\n" + "   a < rp, 15 + 12\r\n" + "   a < rp, CST + 12\r\n" + "   f < t + 85\r\n"
			+ "   $main_loc:\r\n" + "      a < rp, 0b1001011\r\n" + "      a < rp, 0xff\r\n" + "      a<rp, 1110\r\n"
			+ "      &BEGIN\r\n" + "      section <rp, SEC(@main)\r\n" + "      &END\r\n" + "#macro Jump(u, k) {\r\n"
			+ "section < SEC(u)\r\n" + "buf < rp, ADR(u)\r\n" + "}" + " \r\n \r\n#macro bobi 12";

	private String fileTxt;
	private Interface interfa;

	public BCompiler(Interface interfa, String fileTxt) {
		this.fileTxt = fileTxt;
		this.interfa = interfa;
	}

	public void compile() {
		try {
			compileThrow(fileTxt);
		} catch (BinCompilerException e) {
			// System.out.println(e.getMsg());
			interfa.binErrorMsg(e.getLineNumber(), e.getMsg());
		}
	}

	public void compileThrow(String fileTxt) throws BinCompilerException {
		interfa.infoMsg("Binary generation started...");

		TopAnalysis topAnalysis = new TopAnalysis(fileTxt);

		topAnalysis.generateInsts();
		topAnalysis.replaceMacro();
	}

}
