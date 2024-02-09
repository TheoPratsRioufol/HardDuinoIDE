package topanalysis;

import java.util.List;

public class AsmAffectation extends AsmInst {

	public AsmAffectation(List<String> parameters, int lineNumber) {
		super(parameters, lineNumber);
		System.out.println("created with " + parameters);
	}

}
