package CodeGeneration;

import java.util.ArrayList;
import java.util.List;

import CodeGeneration.Cgenerators.CByteExpression;
import General.CompilerException;
import General.Interface;
import General.Parameters;
import LexAnalysis.SuperToken;
import SemAnalysis.Function;
import SemAnalysis.Variable;

public class AsmCode {

	private List<String> osmCode;
	private List<Variable> variables;
	private List<Function> functions;
	private List<String> contexts;
	private List<String> jumpContexts;
	private List<String> fctContexts;
	private Interface interfa;
	private String separatorToIgnore = "";

	public String getSeparatorToIgnore() {
		return separatorToIgnore;
	}

	public void setSeparatorToIgnore(String separatorToIgnore) {
		this.separatorToIgnore = separatorToIgnore;
	}

	private int globVarAdr = Parameters.START_GLOB_VAR;
	private int uniqueId = 0;
	private int paramIndex = 0;

	public AsmCode(Interface interfa) {
		osmCode = new ArrayList<String>();
		variables = new ArrayList<Variable>();
		functions = new ArrayList<Function>();
		contexts = new ArrayList<String>();
		contexts.add("GLOBAL");
		jumpContexts = new ArrayList<String>();
		jumpContexts.add("GLOBAL");
		fctContexts = new ArrayList<String>();
		fctContexts.add("GLOBAL");
		this.interfa = interfa;
	}

	public void add(String line) {
		osmCode.add(line);
	}

	public void newVar(Variable var, SuperToken tk) throws CompilerException {
		var.setContextDad(getCurrentContext());
		var.setStorageDad(getCurrentFctContext());
		if (getCurrentContext().equals("GLOBAL")) {
			if (var.getSizeOf() <= 0)
				throw new CompilerException("The size of this global array : " + var.getName() + " is too small !", tk);
			var.setPointer(globVarAdr);
			globVarAdr += var.getSizeOf();
		} else if (var.getSizeOf() > 0) {
			Function dadFct = getFct(var.getStorageDad(), tk);
			var.setPointer(dadFct.getVarPointerCount());
			dadFct.setVarPointerCount(dadFct.getVarPointerCount() + var.getSizeOf());
		} else {
			interfa.warningMsg("Adding an unkown size array (less efficient) : " + var.getName());
			// on crée une case de redirection
			Variable redirvar = new Variable("@Redirect_" + var.getName(), "byte", 1);
			if (!var.getSizeInitTk().getType().equals("bExp")) {
				throw new CompilerException(
						"The size of this local array : " + var.getName() + " is not set by a byte expression", tk);
			}
			newVar(redirvar, tk);
			CByteExpression exp = new CByteExpression(var.getSizeInitTk(), interfa, 0);
			exp.generate(this);
			add("getStackSizeToT");
			add("writeStackTat(" + redirvar.getPointer() + ") ; T value should be not destroyed to work");
			add("ExpandStackByT");
			var.setRedirectPointer(redirvar.getPointer());
			// throw new CompilerException("jsp pour les tailles non déterminées!" + var,
			// tk);
		}
		if (variables.contains(var)) {
			throw new CompilerException("Redefinition of variable : " + var, tk);
		}
		variables.add(var);
		interfa.logMsg("Declaration of a new variable : " + var);
	}

	public void newParamVar(Variable var, SuperToken tk) throws CompilerException {
		var.setContextDad(getCurrentContext());
		var.setStorageDad(getCurrentFctContext());
		var.setisParameter(true);
		getFct(var.getStorageDad(), tk).newParamVar(var);
	}

	public Variable getVarParam(String name, SuperToken tk) throws CompilerException {
		return getFct(getCurrentContext(), tk).getVarParam(name, tk);
	}

	public void newFct(Function fct, SuperToken tk) throws CompilerException {
		if (functions.contains(fct)) {
			throw new CompilerException("Redefinition of a function : " + fct, tk);
		}
		functions.add(fct);
		interfa.logMsg("Declaration of a new function : " + fct);
	}

	public boolean isVarParamExist(String varname, SuperToken tk) throws CompilerException {
		if (getCurrentContext().equals("GLOBAL")) {
			return false;
		}
		return getFct(getCurrentFctContext(), tk).isVarParamExist(varname);
	}

	public boolean isVarExit(String varname, SuperToken tk) throws CompilerException {
		for (Variable v : variables) {
			if (v.getName().equals(varname)) {
				if (v.getStorageDad().equals("GLOBAL")) {
					return true;
				}
				if (v.getStorageDad().equals(getCurrentFctContext())) {
					return true;
				}
			}
		}
		return false;
	}

	private int getIdxInContext(String targetCtx) {
		for (int i = 0; i < contexts.size(); i++) {
			if (targetCtx.equals(contexts.get(i)))
				return i; // pas de redondance dans les contextes
		}
		return -1;
	}

	public Variable getVar(String varname, SuperToken tk) throws CompilerException {
		Variable vbest = null;
		int bestvIdx = -1; // dans la pile des contextes
		for (Variable v : variables) {
			if (v.getName().equals(varname)) {
				int idx = getIdxInContext(v.getContextDad());
				if (idx > bestvIdx) {
					bestvIdx = idx;
					vbest = v;
				}
			}
		}
		if (vbest != null) {
			return vbest;
		}
		throw new CompilerException("Unknow variable : " + varname + ". The current context is \"" + getCurrentContext()
				+ "\" and current function context is \"" + getCurrentFctContext() + "\".", tk);
	}

	public Function getFct(String fctname, SuperToken tk) throws CompilerException {
		for (Function f : functions) {
			if (f.getName().equals(fctname)) {
				return f;
			}
		}
		throw new CompilerException("Unknow function : " + fctname, tk);
	}

	public void printAllVar() {
		interfa.logMsg("All global variables detected :");
		for (Variable v : variables) {
			interfa.printlogMsg(v.getName() + "-");
		}
		interfa.logMsg("");
	}

	public String toString() {
		String out = "";
		for (String l : osmCode) {
			out += l + "\n";
		}
		return out;
	}

	public String getCurrentContext() {
		// revoie si on est dans une fonction, main, for...
		return contexts.get(contexts.size() - 1);
	}

	public String getCurrentJumpContext() {
		return jumpContexts.get(jumpContexts.size() - 1);
	}

	public String getCurrentFctContext() {
		return fctContexts.get(fctContexts.size() - 1);
	}

	public void newContext(String context) {
		contexts.add(context);
	}

	public void newFctContext(String context) {
		fctContexts.add(context);
	}

	public void newJumpContext(String context) {
		jumpContexts.add(context);
	}

	private void restore(List<String> ctx, String msg) throws CompilerException {
		if (ctx.size() > 0) {
			ctx.remove(ctx.size() - 1);
		} else {
			throw new CompilerException("No " + msg + " to restore error", null);
		}
	}

	public void restorePreviousContext() throws CompilerException {
		restore(contexts, "context");
	}

	public void restoreFctPreviousContext() throws CompilerException {
		restore(fctContexts, "function context");
	}

	public void restoreJumpPreviousContext() throws CompilerException {
		restore(jumpContexts, "jump context");
	}

	public String getUniqueId() {
		uniqueId++;
		return "#" + uniqueId;
	}

	public void resetParamIndex() {
		paramIndex = 0;
	}

	public int getParamIndex() {
		return paramIndex;
	}

	public void nextParamIndex() {
		paramIndex++;
	}

	public void computeHeader(String name) {
		Function startingFct = functions.get(functions.size() - 1);
		try {
			startingFct = getFct("main", null);
		} catch (CompilerException e) {
			interfa.warningMsg(
					"No \"main\" function detected. Starting the program with \"" + startingFct.getName() + "\"");
		}
		if (startingFct.getNbOfParams() != 0)
			interfa.warningMsg("Starting the program with no parameters but \"" + startingFct.getName() + "\" require "
					+ startingFct.getNbOfParams() + ".");
		osmCode.add(0, "Jump(@" + startingFct.getName() + ")		; lancement du programme");
		osmCode.add(0, "; -----------------------------");
		osmCode.add(0, "; C to ASM");
		osmCode.add(0, "; Compiled by " + Parameters.COMPILER_NAME);
		osmCode.add(0, "; Program : " + name);
		osmCode.add(0, "; -----------------------------");
	}

	public void computeSizeOfFct() {
		for (Function f : functions) {
			add("#macro BUF_ACC_@" + f.getName() + " " + (f.getVarPointerCount() + f.getMaxOffset()));
		}
		add("");
		for (Function f : functions) {
			add("#macro SIZE_STACK_ELM_OF_@" + f.getName() + " " + f.getVarPointerCount());
		}
	}

	public void shareOffset(int offset) throws CompilerException {
		if (getCurrentFctContext().equals("GLOBAL"))
			return;

		getFct(getCurrentFctContext(), null).shareOffset(offset);
	}
}
