package LexAnalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class EasyFile {
	
	private String path;
	public static final String EXTENSION_MACHINE_CODE = "machinecode";
	public static final String EXTENSION_ASSEMBLEUR = "osm";
	
	public EasyFile(String path) {
		this.path = path;
	}

	public void write(String data) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(this.path, "UTF-8");
			writer.print(data);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public String read() {
		BufferedReader br;
		String content = "";
		try {
			br = new BufferedReader(new FileReader(this.path));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return content;
	}
	
	public static String getExtension(String Path) {
		if (Path.lastIndexOf('.') >= 0) {
			return Path.substring(Path.lastIndexOf('.')+1);
		}
		return "";
	}
	
	public static boolean CorrectExtension(String Path) {
		String Extension = getExtension(Path);
		if (Extension.equals("osm") || Extension.equals(EXTENSION_MACHINE_CODE) || Extension.equals("cpp")) {
			return true;
		}
		return false;
	}
	
	public static ArrayList <String> getFileListOfDirectory(String path) {
		ArrayList <String> out = new ArrayList <String>();
		final File folder = new File(path);
		if (folder.listFiles() != null) {
		for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory() == false) {
	        	out.add(fileEntry.getName());
	        }
	    }
		}
		return out;
	}
	
	public static String getName(String Path) {
		if (Path.lastIndexOf('\\') >= 0) {
			return Path.substring(Path.lastIndexOf('\\')+1);
		}
		return "";
	}
}
