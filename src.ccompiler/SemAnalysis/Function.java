package SemAnalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import General.CompilerException;
import LexAnalysis.SuperToken;

public class Function {

	private String name;
	private String type;
	private int varPointerCount = 3;
	private List<Variable> parameters;
	private DataType dType;
	private List<DataType> paramsDataType;
	private int maxOffset = 0;

	public Function(String name, String type) {
		this.name = name;
		this.type = type;
		dType = new DataType(type, 1);
		parameters = new ArrayList<>();
		paramsDataType = new ArrayList<>();
	}

	public String toString() {
		return "(" + name + "," + type + ")";
	}

	public String getName() {
		return name;
	}

	public int getVarPointerCount() {
		return varPointerCount;
	}

	public void setVarPointerCount(int varPointerCount) {
		this.varPointerCount = varPointerCount;
	}

	public void newParamVar(Variable var) throws CompilerException {
		if (isVarParamExist(var.getName())) {
			throw new CompilerException("Redefinition of a parameter of " + name + " : " + var.getName(), null);
		}
		var.setPointer(varPointerCount); // sizeof = -1 si on doit chercher le pointeur
		parameters.add(var);
		paramsDataType.add(var.getDataType());
		varPointerCount++;
	}

	public Variable getVarParam(String name, SuperToken tk) throws CompilerException {
		for (Variable v : parameters) {
			if (v.getName().equals(name)) {
				return v;
			}
		}
		throw new CompilerException("The variable " + name + " isn't a parameter of " + name, tk);
	}

	public Variable getVarParam(int index) {
		return parameters.get(index);
	}

	public boolean isVarParamExist(String name) {
		for (Variable v : parameters) {
			if (v.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	public DataType getDataType() {
		return dType;
	}

	public void verifyTypeOfParam(int index, DataType dtype, SuperToken tk) throws CompilerException {
		if (index >= paramsDataType.size())
			throw new CompilerException("Too much parameters for " + getName(), tk);
		if (!dtype.equals(paramsDataType.get(index)))
			throw new CompilerException("The type of the " + (index + 1) + "-th argument is wrong (it's \"" + dtype
					+ "\" vs the correct one \"" + paramsDataType.get(index) + "\")", tk);
	}

	public int getNbOfParams() {
		return parameters.size();
	}

	public void shareOffset(int offset) {
		if (offset + 1 > maxOffset)
			maxOffset = offset + 1;
	}

	public final int getMaxOffset() {
		return maxOffset;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Function other = (Function) obj;
		return Objects.equals(name, other.name); // attetnion aux differentes signatures !
	}

}
