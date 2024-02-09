package General;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Utils {

	public static final BufferedImage readImage(File stream) {
		try {
			return ImageIO.read(stream);
		} catch (IOException e) {
			System.out.println("fail to read " + stream.getAbsolutePath());
			e.printStackTrace();
			return null;
		}
	}

	public static String read(File file) {
		BufferedReader br;
		String content = "";
		try {
			br = new BufferedReader(new FileReader(file));
			StringBuilder sb = new StringBuilder();
			String line = "";
			line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			content = sb.toString();
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return content;
	}

	public static final File resolveRes(String path) {
		return new File(System.getProperty("user.dir") + "\\res\\" + path);
	}

	public static final String removeExtension(String name) {
		String[] split = name.split("\\.");
		return split[0];
	}

}
