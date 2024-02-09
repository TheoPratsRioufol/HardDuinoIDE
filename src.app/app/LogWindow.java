package app;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.text.BadLocationException;

import CCompiler.CCompiler;
import General.IconAtlas;
import General.Interface;
import General.Utils;
import bcompiler.BCompiler;

public class LogWindow extends JFrame {

	private IconAtlas iconAtlas;
	private Interface interfa;
	private Config config;

	private CodeTextPane sourceBox;
	private CodeTextPane compileBox;
	private CodeTextPane logBox;
	private CodeTextPane binaryBox;
	private JPanel simulateBox = new JPanel();

	private JTabbedPane tabs;

	private JScrollPane logTab;
	private JScrollPane sourceTab;
	private JScrollPane compileTab;
	private JScrollPane binaryTab;
	private JScrollPane simulateTab;

	private JButton compileButton, binButton;
	private boolean compileRunning = false;

	public LogWindow() {
		super("C-ASM HardDuino Compiler");
		iconAtlas = new IconAtlas();
		interfa = new Interface(this);
		config = new Config();
	}

	public void init() {

		sourceBox = new CodeTextPane(this, null);
		compileBox = new CodeTextPane(this, new AsmFormatter(this));
		logBox = new CodeTextPane(this, null);
		binaryBox = new CodeTextPane(this, null);

		JPanel mainpanel = new JPanel(new GridBagLayout());
		JPanel buttonPanel = new JPanel(new FlowLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = gbc.gridy = 0;
		gbc.gridheight = gbc.gridwidth = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.EAST;
		gbc.insets = new Insets(5, 5, 5, 5);

		compileButton = new JButton("Compiler");
		compileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!compileRunning) {
					compileRunning = true;
					resetBox();
					tabs.setSelectedComponent(logTab);
					Thread t1 = new Thread(new Runnable() {
						public void run() {
							new CCompiler().compileFile(interfa, getSourceTxt(), "");
						}
					});
					t1.start();
					compileRunning = false;
				}
			}
		});

		binButton = new JButton("Generate Binary");
		binButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!compileRunning) {
					compileRunning = true;
					// resetBox();
					tabs.setSelectedComponent(logTab);
					Thread t1 = new Thread(new Runnable() {
						public void run() {
							new BCompiler(interfa, compileBox.getText()).compile();
						}
					});
					t1.start();
					compileRunning = false;
				}
			}
		});

		buttonPanel.add(compileButton);
		buttonPanel.add(binButton);

		mainpanel.add(buttonPanel, gbc);

		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1;
		gbc.gridy++;
		mainpanel.add(mainPanel(), gbc);

		compileBox.setText(BCompiler.FAKE_ASM);

		sourceBox.setParentTab(sourceTab);
		compileBox.setParentTab(compileTab);

		setContentPane(mainpanel);
		setSize(400, 400);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setIconImage(iconAtlas.getImage("planet"));
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setVisible(true);
	}

	public JTabbedPane getTabs() {
		return tabs;
	}

	private JComponent mainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		logBox.setEditable(false);

		JPanel noWrapPanel = new JPanel(new BorderLayout());
		noWrapPanel.add(logBox);

		tabs = new JTabbedPane();

		sourceTab = new JScrollPane(sourceBox);
		sourceTab.getVerticalScrollBar().setUnitIncrement(20);

		logTab = new JScrollPane(noWrapPanel);
		logTab.getVerticalScrollBar().setUnitIncrement(20);

		compileTab = new JScrollPane(compileBox);
		compileTab.getVerticalScrollBar().setUnitIncrement(20);

		simulateTab = new JScrollPane(simulateBox);
		simulateTab.getVerticalScrollBar().setUnitIncrement(20);

		binaryTab = new JScrollPane(simulateBox);
		binaryTab.getVerticalScrollBar().setUnitIncrement(20);

		tabs.add(sourceTab);
		tabs.setTitleAt(tabs.indexOfComponent(sourceTab), "Source");
		tabs.setIconAt(tabs.indexOfComponent(sourceTab), iconAtlas.getIcon("planet", 16));

		tabs.add(logTab);
		tabs.setTitleAt(tabs.indexOfComponent(logTab), "Compiler logs");
		tabs.setIconAt(tabs.indexOfComponent(logTab), iconAtlas.getIcon("log", 16));

		tabs.add(compileTab);
		tabs.setTitleAt(tabs.indexOfComponent(compileTab), "Compiled code");
		tabs.setIconAt(tabs.indexOfComponent(compileTab), iconAtlas.getIcon("compile", 16));

		tabs.add(binaryTab);
		tabs.setTitleAt(tabs.indexOfComponent(binaryTab), "Binary code");
		tabs.setIconAt(tabs.indexOfComponent(binaryTab), iconAtlas.getIcon("binary", 16));

		tabs.add(simulateTab);
		tabs.setTitleAt(tabs.indexOfComponent(simulateTab), "Simulate");
		tabs.setIconAt(tabs.indexOfComponent(simulateTab), iconAtlas.getIcon("simulate", 16));

		panel.add(tabs);
		return panel;
	}

	public void open(File file) {
		sourceBox.setText(Utils.read(file));
	}

	public String getSourceTxt() {
		return sourceBox.getText();
	}

	public CodeTextPane getSource() {
		return sourceBox;
	}

	public CodeTextPane getCompile() {
		return compileBox;
	}

	public JScrollPane getSourceTab() {
		return sourceTab;
	}

	public void setCompileSelected() {
		tabs.setSelectedComponent(compileTab);
	}

	public Config getConfig() {
		return config;
	}

	public void resetBox() {
		logBox.setText("");
		compileBox.setText("");
	}

	public void requestFontSizeUp() {
		config.setFontSize(config.getFontSize() + 1);
		compileBox.setFontSize(config.getFontSize());
		logBox.setFontSize(config.getFontSize());
		sourceBox.setFontSize(config.getFontSize());
	}

	public void requestFontSizeDown() {
		if (config.getFontSize() - 1 < 5)
			return;
		config.setFontSize(config.getFontSize() - 1);
		compileBox.setFontSize(config.getFontSize());
		logBox.setFontSize(config.getFontSize());
		sourceBox.setFontSize(config.getFontSize());
	}

	public void infoMsg(String msg) {
		try {
			logBox.getTextDocument().insertString(logBox.getTextDocument().getLength(), msg + "\n",
					logBox.getInfoStyle());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void errorMsg(String msg) {
		try {
			logBox.getTextDocument().insertString(logBox.getTextDocument().getLength(), msg + "\n",
					logBox.getErrorStyle());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void warningMsg(String msg) {
		try {
			logBox.getTextDocument().insertString(logBox.getTextDocument().getLength(), msg + "\n",
					logBox.getWarningStyle());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void logMsg(String msg) {
		try {
			logBox.getTextDocument().insertString(logBox.getTextDocument().getLength(), msg + "\n",
					logBox.getPlainStyle());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void codeMsg(String str) {
		try {
			compileBox.getTextDocument().insertString(compileBox.getTextDocument().getLength(), str,
					compileBox.getPlainStyle());
			compileBox.format();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	public void printlogMsg(String msg) {
		try {
			logBox.getTextDocument().insertString(logBox.getTextDocument().getLength(), msg, logBox.getPlainStyle());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

}
