package app;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class AsmFormatter extends Formatter {

	public static final String[] KEYWORDS = { "ram", "rp", "a", "portc", "t", "portd", "portb", "ALU", "&BEGIN", "&END",
			"#macro", "b", "f", "buf", "section" };
	public static final String KEYWORDS_SEPARATOR = " \n<,(	";

	private Map<String, Color> highlightColor;

	public static final int commentSpacing = 20;
	private boolean macroPrev = false;

	private LogWindow app;

	private Style plainStyle, keywordsStyle, adrStyle, commentStyle, macroStyle;

	public AsmFormatter(LogWindow app) {
		this.app = app;
		highlightColor = app.getConfig().getHighlightColor();
	}

	@Override
	public StyledDocument format(StyledDocument doc) {
		buildStyles(doc);
		try {
			return SecondFormat(Fastformat(doc));
		} catch (BadLocationException e) {
			e.printStackTrace();
			return doc;
		}
	}

	public static boolean fit(int idx, String str1, String str2) {
		int bound = idx + str1.length();
		if (bound > str2.length())
			return false;
		for (int i = idx; i < bound; i++) {
			if (str1.charAt(i - idx) != str2.charAt(i))
				return false;
		}
		return true;
	}

	public static boolean isSeparator(char c) {
		return KEYWORDS_SEPARATOR.indexOf(c) >= 0;
	}

	public static boolean isSpace(char c) {
		return " 	".indexOf(c) >= 0;
	}

	public static String indent(int nb) {
		String buffer = "";
		for (int q = 0; q < nb; q++) {
			buffer += "   ";
		}
		return buffer;
	}

	public static String removeFirstIndentations(String str) {
		String buffer = "";
		boolean fistChr = false;
		for (int q = 0; q < str.length(); q++) {
			if (fistChr)
				buffer += str.charAt(q);
			else if ((str.charAt(q) != '	') && (str.charAt(q) != '\n') && (str.charAt(q) != ' ')
					&& (str.charAt(q) != '\r')) {
				fistChr = true;
				buffer += str.charAt(q);
			}
		}
		return buffer;
	}

	public int isKeyword(String str) {
		String sub = "";
		for (int k = 0; k < KEYWORDS.length; k++) {
			if (KEYWORDS[k].length() > str.length())
				continue;
			sub = str.substring(str.length() - KEYWORDS[k].length(), str.length());
			if (((str.length() == KEYWORDS[k].length())
					|| isSeparator(str.charAt(str.length() - 1 - KEYWORDS[k].length()))) && KEYWORDS[k].equals(sub)) {
				return str.length() - KEYWORDS[k].length();
			}
		}
		return -1;
	}

	public void buildStyles(StyledDocument doc) {
		plainStyle = doc.addStyle("plain", null);
		keywordsStyle = doc.addStyle("keywords", null);
		adrStyle = doc.addStyle("adr", null);
		commentStyle = doc.addStyle("comment", null);
		macroStyle = doc.addStyle("macro", null);

		StyleConstants.setForeground(keywordsStyle, highlightColor.get("keyword"));
		StyleConstants.setForeground(adrStyle, highlightColor.get("adress"));
		StyleConstants.setForeground(commentStyle, highlightColor.get("comment"));
		StyleConstants.setForeground(macroStyle, highlightColor.get("macro"));

		StyleConstants.setBold(keywordsStyle, true);
	}

	public int formatLine(AsmFormatter form, StyledDocument doc, String line, int intentationStack)
			throws BadLocationException {
		String buffer = "";
		boolean firstChr = false;
		boolean comment = false;
		int idx = 0;
		int LireStartidx = doc.getLength();
		char c = ' ';
		char prevC = '\0';
		Style endStyle = plainStyle;

		for (int q = 0; q < line.length(); q++) {

			c = line.charAt(q);

			if (!isSpace(c) && !firstChr) {
				firstChr = true;
				buffer = indent(intentationStack);
			}
			if (firstChr) {
				if (comment || !(isSpace(c) && isSpace(prevC))) {
					if (isSpace(c) && !comment)
						buffer += " ";
					else
						buffer += c;
				}
				idx = isKeyword(buffer);
				if ((c == ';') && !comment) {
					comment = true;
					if (buffer != "")
						doc.insertString(doc.getLength(), buffer.substring(0, buffer.length() - 1), endStyle);
					endStyle = commentStyle;
					buffer = "" + c;
				} else if (!comment) {
					if (((q == line.length() - 1) || isSeparator(line.charAt(q + 1))) && (idx >= 0)) {
						doc.insertString(doc.getLength(), buffer.substring(0, idx), plainStyle);
						doc.insertString(doc.getLength(), buffer.substring(idx), keywordsStyle);
						buffer = "";
					} else if ((c == '#') && (prevC == '\0')) {
						buffer = removeFirstIndentations(buffer);
						form.macroPrev = true;
						intentationStack = 0;
					} else if (c == ':') {
						intentationStack++;
						buffer = removeFirstIndentations(buffer);
						if (buffer.charAt(0) != '$') {
							intentationStack = 1;
							buffer = "\n" + indent(intentationStack - 1) + buffer;
						} else
							buffer = indent(intentationStack - 1) + buffer;
						doc.insertString(doc.getLength(), buffer, adrStyle);
						buffer = "";
					}
				}
				prevC = c;
			}
		}

		if (comment) {
			int insertIndex = LireStartidx + commentSpacing;
			if (doc.getLength() - LireStartidx > commentSpacing)
				insertIndex = doc.getLength();
			else {
				String b2 = "";
				for (int i = 0; i < commentSpacing - (doc.getLength() - LireStartidx); i++) {
					b2 += " ";
				}
				doc.insertString(doc.getLength(), b2, endStyle);
			}

			doc.insertString(insertIndex, buffer + "\n", endStyle);
		} else {
			doc.insertString(doc.getLength(), buffer + "\n", endStyle);
		}

		return intentationStack;
	}

	public StyledDocument Fastformat(StyledDocument doc) throws BadLocationException {

		String docTxt = doc.getText(0, doc.getLength());

		doc.remove(0, doc.getLength());
		int intentationStack = 0;

		String buffer = "";

		for (int i = 0; i < docTxt.length(); i++) {
			if ((docTxt.charAt(i) == '\n') || (i == docTxt.length() - 1)) {
				if (removeFirstIndentations(buffer).length() == 0) {
					if (macroPrev) {
						doc.insertString(doc.getLength(), "\n", plainStyle);
					}
				} else {
					macroPrev = false;
					intentationStack = formatLine(this, doc, buffer, intentationStack);
				}
				buffer = "";
			} else {
				buffer += docTxt.charAt(i);
			}

		}

		return doc;
	}

	public void loadAdresses(StyledDocument doc, List<String> adresses) throws BadLocationException {
		String docTxt = doc.getText(0, doc.getLength());
		String buffer = "";
		boolean comment = false;

		for (int i = 0; i < docTxt.length(); i++) {
			if (docTxt.charAt(i) == '\n') {
				buffer = "";
				comment = false;
			} else if (docTxt.charAt(i) == ';') {
				comment = true;
			} else if ((docTxt.charAt(i) == ':') && !comment) {
				if (buffer.charAt(0) == '$')
					adresses.add(buffer.substring(1, buffer.length()));
				else
					adresses.add(buffer);
			}
			if (!isSpace(docTxt.charAt(i)) && (docTxt.charAt(i) != '\n')) {
				buffer += docTxt.charAt(i);
			}
		}
	}

	public void loadMacros(StyledDocument doc, List<String> macros) throws BadLocationException {
		String docTxt = doc.getText(0, doc.getLength());
		String buffer = "";
		String Macbuffer = "";
		boolean comment = false;
		int macroState = 0;
		char prevChr = '\0';

		for (int i = 0; i < docTxt.length(); i++) {
			if (docTxt.charAt(i) == '\n') {
				buffer = "";
				comment = false;
				macroState = 0;
			} else if (docTxt.charAt(i) == ';') {
				comment = true;
			} else if ((docTxt.charAt(i) == '#') && !comment) {
				macroState = 1;
			}
			if (!isSpace(docTxt.charAt(i)) && (docTxt.charAt(i) != '\n')) {
				buffer += docTxt.charAt(i);
				if (!comment) {
					if ((macroState != 0) && isSpace(prevChr)) {
						macroState++;
					}
					if (macroState == 2)
						Macbuffer += docTxt.charAt(i);
					if (macroState == 3) {
						macros.add(Macbuffer);
						Macbuffer = "";
					}
				}
			}
			prevChr = docTxt.charAt(i);
		}
	}

	public void replaceColor(StyledDocument doc, List<String> adresses, Style style) throws BadLocationException {
		String docTxt = doc.getText(0, doc.getLength());

		for (int i = 0; i < docTxt.length(); i++) {
			for (int k = 0; k < adresses.size(); k++) {
				if (fit(i, adresses.get(k), docTxt)) {
					doc.setCharacterAttributes(i, adresses.get(k).length(), style, true);
					i += adresses.get(k).length();
					break;
				}
			}
		}
	}

	public StyledDocument SecondFormat(StyledDocument doc) throws BadLocationException {

		List<String> adresses = new ArrayList<>();
		List<String> macros = new ArrayList<>();

		loadAdresses(doc, adresses);

		loadMacros(doc, macros);

		replaceColor(doc, adresses, adrStyle);

		replaceColor(doc, macros, macroStyle);

		return doc;
	}

}
