package General;

import javax.swing.SwingUtilities;

import app.LogWindow;

public class Interface {

	private LogWindow window;

	public Interface(LogWindow window) {
		this.window = window;
	}

	public void finishNoError() {
		window.setCompileSelected();
	}

	public void errorMsg(int lineNumber, String str) {
		// System.out.println("\u001B[33m[Error] : " + str + "\u001B[0m");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.errorMsg(str);
				window.getSource().notifyError(lineNumber, str);
			}
		});
	}

	public void binErrorMsg(int lineNumber, String str) {
		// System.out.println("\u001B[33m[Error] : " + str + "\u001B[0m");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.errorMsg(str);
				window.getCompile().notifyError(lineNumber, str);
			}
		});
	}

	public void infoMsg(String str) {
		// System.out.println("\u001B[36m[Info] : " + str + "\u001B[0m");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.infoMsg(str);
			}
		});
	}

	public void warningMsg(String str) {
		// System.out.println("\u001B[33m[Warning] : " + str + "\u001B[0m");
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.warningMsg(str);
			}
		});
	}

	public void logMsg(String str) {
		// System.out.println(">" + str);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.logMsg(str);
			}
		});
	}

	public void printlogMsg(String str) {
		// System.out.print(str);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.printlogMsg(str);
			}
		});
	}

	public void codeMsg(String str) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.codeMsg(str);
			}
		});
	}

}
