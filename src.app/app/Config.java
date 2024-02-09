package app;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

public class Config {

	public int getFontSize() {
		return fontSize;
	}

	public Font getFontBySize(int size) {
		return new Font("Monospaced", Font.PLAIN, size);
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	private int fontSize = 11;
	private Color lineNumberColor = Color.gray;

	public Config() {

	}

	public Font getFont() {
		return getFontBySize(fontSize);
	}

	public Color getLineNumberColor() {
		return lineNumberColor;
	}

	public Map<String, Color> getHighlightColor() {

		Map<String, Color> colMap = new HashMap<>();

		colMap.put("keyword", new Color(0, 76, 135));
		colMap.put("macro", new Color(153, 0, 153));
		colMap.put("adress", new Color(255, 76, 135));
		colMap.put("comment", Color.gray);

		return colMap;
	}

}
