package SemAnalysis;

import java.util.Objects;

public class DataType {

	public static int UNDEFINED = 0;

	private String type;
	private int dim;

	@Override
	public int hashCode() {
		return Objects.hash(dim, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataType other = (DataType) obj;
		return ((dim == other.dim) || ((dim == -1) && (other.dim != 1)) || (other.dim == -1) && (dim != 1))
				&& (Objects.equals(type, other.type) || type.equals("any") || other.type.equals("any"));
	}

	@Override
	public String toString() {
		if (dim == 1)
			return type;
		if (dim == 0)
			return "undefined";
		if (dim > 1)
			return type + "[" + dim + "]";
		return type + "[(unknow size at the compilation)]";
	}

	public DataType mergeDataType(DataType other) {
		int dim_ = (dim != 0) ? dim : other.getDim();

		DataType res = new DataType(type, dim_);
		if (type.equals("any")) {
			res.setType(other.getType());
		}
		return res;
	}

	public int getDim() {
		return dim;
	}

	public void setDim(int dim) {
		this.dim = dim;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isUndefined() {
		return dim == 0;
	}

	public DataType(String type, int dim) {
		this.type = type;
		this.dim = dim;
	}

}
