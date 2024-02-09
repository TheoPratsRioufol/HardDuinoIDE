package main;

import java.io.File;

import javax.swing.UIManager;

import LexAnalysis.EasyFile;
import app.LogWindow;

public class TestMain {

	private static String path = "C:\\Users\\alber\\Desktop\\TestCompilateurOsm\\TestCompilateurOsm.ino";

	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Lecture du fichier");

		EasyFile fileReader = new EasyFile(path);
		LogWindow window = new LogWindow();
		// Interface interfa = new Interface(window);

		window.init();
		window.open(new File(path));

	}
}
