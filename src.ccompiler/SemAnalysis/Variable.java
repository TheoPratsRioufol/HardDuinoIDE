package SemAnalysis;

import java.util.Objects;

import General.CompilerException;
import General.Parameters;
import LexAnalysis.SuperToken;

public class Variable {

	private String name;
	private String type;
	private String contextDad = "NOTDEFINED";
	private String storageDad = "NOTDEFINED";
	private int size = -1;
	private int pointer = -1;
	private int redirectPointer = -1;
	private SuperToken defaultValue = null;
	private SuperToken initTk = null;
	private DataType dType;
	private boolean isParam;

	public Variable(String name, String type, int size) throws CompilerException {
		this.name = name;
		this.type = type;
		this.size = size;
		computeDataType();
		this.isParam = false;
	}

	private void computeDataType() {
		dType = new DataType(type, size);
	}

	public void setSizeInitTk(SuperToken tk) {
		// pour les arrays qui sont alloué dynamiquement
		this.initTk = tk;
	}

	public SuperToken getSizeInitTk() {
		// pour les arrays qui sont alloué dynamiquement
		return initTk;
	}

	public void setRedirectPointer(int value) {
		redirectPointer = value;
	}

	public int getRedirectPointer() throws CompilerException {
		if (redirectPointer < 0)
			throw new CompilerException("The redirection pointer of " + getName() + " is not initialized", null);
		return redirectPointer;
	}

	public String toString() {
		return "(" + name + "," + type + "," + contextDad + ",stored:" + storageDad + "," + size + "@" + pointer + ")";
	}

	public String getName() {
		return name;
	}

	public String getContextDad() {
		return contextDad;
	}

	public String getStorageDad() {
		return storageDad;
	}

	public void setContextDad(String contextDad) {
		this.contextDad = contextDad;
	}

	public void setStorageDad(String storageDad) {
		this.storageDad = storageDad;
	}

	public int getPointer() throws CompilerException {
		if (Parameters.isByteBound(pointer, "Pointor of " + this + " is out of bounds.")) {
			return pointer;
		}
		return -1;
	}

	public void setPointer(int pointer) {
		this.pointer = pointer;
	}

	public void setSizeOf(int size) {
		this.size = size;
		computeDataType();
	}

	public int getSizeOf() throws CompilerException {
		/*
		 * if (size > 0) { return size; } throw new
		 * CompilerException("The size of "+name+" is invalid.", null);
		 */
		return size;
	}

	public void setDeflaultValue(SuperToken tk) {
		defaultValue = tk;
	}

	public SuperToken getDeflaultValue() {
		return defaultValue;
	}

	public boolean isParameter() {
		return isParam;
	}

	public void setisParameter(boolean value) {
		isParam = value;
	}

	public DataType getDataType() {
		return dType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(contextDad, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variable other = (Variable) obj;
		return Objects.equals(contextDad, other.contextDad) && Objects.equals(name, other.name);
	}
}
