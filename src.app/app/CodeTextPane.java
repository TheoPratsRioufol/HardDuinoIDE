package app;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

@SuppressWarnings("serial")
public class CodeTextPane extends JPanel implements DocumentListener {

	private JTextPane text;
	private JTextPane lineNumberPane;
	private LogWindow app;
	private JScrollPane myTab;

	private Style lineNumberStyle;
	private StyledDocument lineNumberDoc;
	private StyledDocument textDoc;

	private Style plainStyle, errorStyle, warningStyle, infoStyle;

	private Formatter formatter;

	private JPopupMenu errorPopup = null;

	public CodeTextPane(LogWindow app, Formatter formatter) {
		super();
		this.app = app;
		this.formatter = formatter;

		text = new JTextPane();
		// text.setBackground(Color.DARK_GRAY);
		textDoc = new DefaultStyledDocument();
		text.setDocument(textDoc);

		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_F && e.isControlDown()) {
					format();
					return;
				}
				if ((e.getKeyCode() == KeyEvent.VK_PLUS || e.getKeyCode() == KeyEvent.VK_EQUALS) && e.isControlDown()
						&& !e.isAltGraphDown()) {
					app.requestFontSizeUp();
					return;
				}
				if ((e.getKeyCode() == KeyEvent.VK_MINUS || e.getKeyCode() == 54) && e.isControlDown()) {
					app.requestFontSizeDown();
					return;
				}
			}
		});

		lineNumberPane = new JTextPane();
		lineNumberPane.setEditable(false);
		lineNumberPane.setFocusable(false);
		lineNumberPane.setFont(app.getConfig().getFont());
		DefaultCaret caret = (DefaultCaret) lineNumberPane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

		lineNumberDoc = new DefaultStyledDocument();
		lineNumberStyle = lineNumberDoc.addStyle("lineNumber", null);
		StyleConstants.setForeground(lineNumberStyle, app.getConfig().getLineNumberColor());
		lineNumberPane.setDocument(lineNumberDoc);

		textDoc.addDocumentListener(this);

		buildStyles();

		// text.setFont(textFont);
		// lineNumber.setFont(textFont);

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = gbc.gridy = 0;
		gbc.gridheight = gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.EAST;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.insets = new Insets(5, 5, 5, 5);

		add(lineNumberPane, gbc);

		gbc.weightx = 1;
		gbc.gridx++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(text, gbc);
	}

	public void setParentTab(JScrollPane myTab) {
		this.myTab = myTab;
	}

	public void buildStyles() {
		plainStyle = textDoc.addStyle("plain", null);
		errorStyle = textDoc.addStyle("error", null);
		warningStyle = textDoc.addStyle("warning", null);
		infoStyle = textDoc.addStyle("info", null);

		StyleConstants.setForeground(errorStyle, Color.red);
		StyleConstants.setForeground(warningStyle, Color.orange);
		StyleConstants.setForeground(infoStyle, Color.blue);

		text.setFont(app.getConfig().getFont());
	}

	public void format() {
		if (formatter == null)
			return;
		DefaultCaret caret = (DefaultCaret) text.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		int pos = text.getCaretPosition();
		textDoc = formatter.format(textDoc);
		text.setDocument(textDoc);
		caret.setUpdatePolicy(DefaultCaret.UPDATE_WHEN_ON_EDT);
		text.setCaretPosition(pos);
	}

	private void updateLinesNumber() {
		lineNumberPane.setText("");
		String lines = "";
		String txt = text.getText();
		int lineN = 0;
		for (int i = 0; i < txt.length(); i++) {
			if (txt.charAt(i) == '\n') {
				lineN++;
				lines += lineN + "\n";
			}
		}

		try {
			lineNumberDoc.insertString(0, lines, lineNumberStyle);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void setText(String txt) {
		text.setText(txt);
		updateLinesNumber();
	}

	public StyledDocument getTextDocument() {
		return textDoc;
	}

	public Style getPlainStyle() {
		return plainStyle;
	}

	public Style getInfoStyle() {
		return infoStyle;
	}

	public Style getErrorStyle() {
		return errorStyle;
	}

	public Style getWarningStyle() {
		return warningStyle;
	}

	public String getText() {
		return text.getText();
	}

	public void setEditable(boolean editable) {
		text.setEditable(editable);
	}

	public void setFontSize(int size) {
		text.setFont(app.getConfig().getFontBySize(size));
		lineNumberPane.setFont(app.getConfig().getFontBySize(size));
	}

	private int getPositionFromLine(int line) {
		String txt = text.getText();
		int lineReached = 0;
		for (int i = 0; i < txt.length(); i++) {
			if (txt.charAt(i) == '\n') {
				lineReached++;
				if (lineReached == line) {
					return i - lineReached + 1;
				}
			}
		}
		return 0;
	}

	public void notifyError(int line, String txt) {
		System.out.println("NOTIFY " + myTab.getComponentCount());
		app.getTabs().setSelectedComponent(myTab);

		JScrollBar vertical = app.getSourceTab().getVerticalScrollBar();

		int pos = getPositionFromLine(line);

		int offY = 0;
		try {
			offY = (int) text.modelToView2D(pos).getY();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		vertical.setValue(offY - 50);

		JLabel errLabel = new JLabel(txt);
		errLabel.setFont(app.getConfig().getFont());

		errorPopup = new JPopupMenu();
		errorPopup.add(errLabel);
		errLabel.setBackground(new Color(255, 204, 153));
		errLabel.setOpaque(true);
		errorPopup.setOpaque(false);

		int off = 0;
		try {
			off = (int) (text.modelToView2D(pos).getBounds().getLocation().getY()
					- app.getSourceTab().getVerticalScrollBar().getValue() + 30);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		errorPopup.show(app.getSourceTab(), (int) text.getLocation().getX() + 10, off);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateLinesNumber();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateLinesNumber();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateLinesNumber();
	}

}
