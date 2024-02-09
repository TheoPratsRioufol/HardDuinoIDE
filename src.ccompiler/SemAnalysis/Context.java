package SemAnalysis;

import java.util.ArrayList;

public class Context {
	
	private ArrayList<String> str = new ArrayList<String>();
	private ArrayList<CanBeSymbolyse> strValue = new ArrayList<CanBeSymbolyse>();
	
	public Context() {
	}
	
	public String toString() {
		String out = "";
		for (int i = 0; i < str.size(); i++) {
			out += "," + str.get(i);
		}
		return out;
	}
	
	public boolean include(String ctxstr) {
		return str.contains(ctxstr);
	}
	
	public void addContext(String ctxstr, CanBeSymbolyse value) {
		str.add(ctxstr);
		strValue.add(value);
	}
	
	public void removeContext(String ctxstr) {
		int idx = str.indexOf(ctxstr);
		str.remove(idx);
		strValue.remove(idx);
	}
	
	public CanBeSymbolyse getContextByType(String ctxstr) {
		int idx = str.indexOf(ctxstr);
		return strValue.get(idx);
	}

}
