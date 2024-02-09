package General;

import java.util.Arrays;
import java.util.List;

public class Parameters {
	
	public static final String COMPILER_NAME = "Anubis-v-1.0, Théo Prats Rioufol, Février 2023";
	
	public static final int ERROR = -1;
	public static final int OK = 0;
	
	
	public static final String SEPARATOR_LIST = "\"';,:/*+=-.<>\\#&|%}{()[]!?\n";
	public static final String ALLOWED_CHAR = SEPARATOR_LIST + "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_#&$~¨^µèéàù ";
	public static final String STRING_CHR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
	
	
	public static final char OR_CHAR = '|';
	public static final char AND_CHAR = '&';
	public static final char COMMAND_CHAR = '/';
	public static final int NB_CYCLE_REGULAR_EXP = 100;
	public static final int NB_CYCLE_EXP = NB_CYCLE_REGULAR_EXP;
	public static final String ERROR_MAX_CYCLE_REACH = "Nombre maximal de cycle atteint !";
	public static final String CREATION_REGULAR_EXP = "Création des expressions régulières";
	
	public static final String DEFINE = "#define";
	public static final String INCLUDE = "#include";
	public static final int MAX_PREPROCESSOR_COUNT = 10;
	
	public static final String RegularExpressions[][] = {//plus de préprocesseur a cette etape !
			{"typeToken:'void'&!typeToken|'unsigned'&!typeToken|'char'&!typeToken|'byte'&!typeToken", // on commence par isoler les mots clées
			 "loopToken:'for'&!loopToken|'while'&!loopToken|'do'&!loopToken",
			 "escapeToken:'break'&!escapeToken|'continue'&!escapeToken",
			 "typeToken:typeToken-typeToken",
			 "operator:'>'-'='",
			 "operator:'<'-'='",
			 "operator:'!'-'='",
			 "operator:'='-'='",
			 "operator:'+'-'+'",
			 "operator:'&'-'&'",
			 "operator:'>'-'>'",
			 "operator:'<'-'<'",
			 "operator:'|'-'|'",
			 "operator:'!'&!operator|'+'&!operator|'-'&!operator|'<'&!operator|'>'&!operator|'&'&!operator",
			 "PP:(string)-'('&!PP", // protect parameter parentesis
			 "terminator:';'&!terminator|','&!terminator|')'&!terminator|'}'&!terminator|'='&!terminator",
			 }, // IL FAUT REMPLACER LES DEFINES ICI "/replacedefine:_"
			
			//EXP_RULES, // puis les expressions
			 
			{
				 
			// PARTIE 1 : TOUT CE QUI EST NUMERIQUE
		    "bExp:number|arrIdx|pbExp|functionEval",
		    "pbExp:'('&!PP-bExp|var-')'",
		    "arrIdx:(!typeToken)-var-index",
		    "bExp:bExp|var-bExp|var",
		    "bExp:bExp|var-operator-bExp|var-(!'[')&(!index)&(!PP)",
		    "bExp:operator-bExp|var-(!'[')&(!index)&(!PP)",
		    "bExp:bExp|var-operator",
		    "bExp:('[')|(',')|('(')|('{')-var-(']')|(',')|(')')|('}')",
		    "bExp:('=')-var-(terminator)",
		    "index:'['-bExp-']'",
		    "index:'['-']'",
		    
		    "arrayExp:strValue|setValue",
		    "arrayExp:arrayExp-operator-arrayExp",
		    "strValue:'\"'-*-'\"'",
		    "setValue:'{'-evalParameterArray-'}'",
		    "setValue:(!')')-'{'-'}'",
		    
		    "var:string-(terminator)|(operator)|('[')|(']')|('(')",
		    
		    // PARTIE 2 : TOUTES LES DECLARATIONS 
		 	"bDecl:typeToken-var-(!'[')&(!index)",
		 	"arrDecl:typeToken-var-index",
		 	"bDeclInit:bDecl-'='-bExp-(terminator)",
		 	"arrayExp:(arrDecl)-('=')-var",
		 	"arrDeclInit:arrDecl-'='-arrayExp-(terminator)",
				 
			"functionEval:var-PP-evalParameterArray-')'",
			"functionEval:var-PP-')'",
			
			// PARTIE 2 : TOUTES LES DECLARATIONS 
			"functionDeclaration:bDecl-PP-declaParameterArray-')'-'{'-instruction-'}'",
			"functionDeclaration:bDecl-PP-declaParameterArray-')'-'{'-'}'",
			"functionDeclaration:bDecl-PP-')'-'{'-instruction-'}'",
			"functionDeclaration:bDecl-PP-')'-'{'-'}'",
						
			// PARTIE 3 : PARAMETRES DES DECLARATION DE FONCTIONS
			// POUR LA DECLARATION
			"declaParameter:(PP)|(',')-bDecl|arrDecl-(',')|(')')",
			"declaParameterInit:(PP)|(',')-bDeclInit|arrDeclInit-(',')|(')')",
			"declaParameterArray:declaParameter|declaParameterInit|declaParameterArray-','-declaParameter|declaParameterInit|declaParameterArray",
			"declaParameterArray:(PP)-declaParameter|declaParameterInit|'void'-(')')",
			// POUR L'EVALUATION
			"evalParameterInit:(PP)|(',')|('{')-var-'='-bExp|arrayExp-(',')|('}')|(')')",
			"evalParameter:(PP)|(',')|('{')-bExp|arrayExp-(',')|('}')|(')')",
			"evalParameterArray:evalParameterArray|evalParameter|evalParameterInit-','-evalParameterArray|evalParameter|evalParameterInit",
			"evalParameterArray:(PP)|('{')-evalParameter|evalParameterInit-(')')|('}')",
			
			// PARTIE 4 : CODE AUTONOME
			"affectationWithDec:bDecl|bDeclInit|arrDecl|arrDeclInit-';'",
			"affectation:var|bExp-'='-bExp|arrayExp-';'",
			"returnValue:'return'-var|bExp|arrayExp-';'",
			"returnNoValue:'return'-';'",
			"escape:escapeToken-';'",
			"return:returnValue|returnNoValue",
			"evaluation:(instruction)|('{')-bExp|arrExp-';'",
			"whileLoop:'while'-PP-evalParameterArray-')'-'{'-instruction-'}'",
			"whileLoop:'while'-PP-evalParameterArray-')'-'{'-'}'",
			"whileLoop:'while'-PP-evalParameterArray-')'-';'",
			"forLoop:'for'-PP-instruction-bExp-')'-'{'-instruction-'}'",
			"forLoop:'for'-PP-instruction-bExp-')'-'{'-'}'",
			"forLoop:'for'-PP-instruction-bExp-')'-';'",
			
			// PARTIE 5 : LES INSTRUCTIONS = LES CODES AUTONOMES
			"instruction:affectationWithDec|affectation|evaluation|whileLoop|functionDeclaration|return|escape|forLoop",
			"instruction:instruction-instruction"
			}
	};
	
	public static final String FINAL_TREE_TYPE = "GLOBAL";
	public static final String TYPE_VAR_DECLARATION = "affectationWithDec";
	public static final String TYPE_FUNC_DECLARATION = "functionDeclaration";
	public static final String FUNC_DECLA_NAME = "functionDeclaration";
	public static final String FUNCTION_CTX = "fct_ctx";
	public static final String INTR_TYPE = "instruction";
	
	public static final String NO_DAD = "NO_DAD";
	
	public static final String ERROR_FUNCD_IN_FUNCD = "Illegal to define a function in a function";
	public static final String[] TOKEN_NOT_FOUND = {"Type \"","\" not found for token \"","\" : "};
	public static final String EMPTY_TOKEN_EXCEPTION = "EMPTY_TOKEN_EXCEPTION";
	public static final String WRONG_AFFECTATION = "Wrong token after the declaration of a variable";
	public static final String INCORRECT_DEFINE_ARGS = "#define have invalid(s) argument(s). Functional define isn't supported";
	public static final String TREE_NOT_CONVERGE = "The syntaxic analysis don't finish. Some tokens are unknowns, like : ";
	public static final String ARRAY_DIM_UNKNOW = "The size of the array could not be determined at the compilation. Make the size more explicit. Array declaration can only be with constant, or {constant value} : ";
	public static final String ILLEGAL_OP_ARRAY = "You do operation on array, it's forbiden (even at compilation) ! Uses pointers";
	public static final String ARRAY_DIM_NON_CST = "It seems than the size of the array is not constant. If it's normal, fix the size on the left (Arr[SIZE]). Maybe "+ILLEGAL_OP_ARRAY+" : ";
	public static final String UNKOWN_SIZE = "Unable to get the size on : ";
	public static final String CANT_COMPUTE_STATIC = "CANT_COMPUTE_STATIC : ";
	public static final String NOT_IMPLEMENTED = "flemme de coder";
	public static final String REDEFINITION = "Redefinition of a variable or a function : ";
	
	
	public static final String[] ORDERED_OP_STR = {
			"'!'",
			"type",
			"'&'",
			"'sizeof'",
			"'+'|'-'",
			"'<<'|'>>'",
			"'<'|'>'|'<='|'=>'",
			"'=='|'!='",
			"'&'|'|'",
			"'&&'|'||'",
			"'+='|'-='",
			"'++'|'--'" // opérande à gauche
	};
	
	public static final int[] ORDERED_OP_NB_OP = {
			1,
			1,
			1,
			1,
			2,
			2,
			2,
			2,
			2,
			2,
			2,
			-1,
	};
	
	public static final boolean[] ORDERED_OP_AB = {
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
			true,
	};
	
	
	public static final List<String> ORDERED_OP_ARRAY = Arrays.asList(ORDERED_OP_STR);
	
	public static final String[] BYTE_TYPES_STR = {
			"number",
			"var",
			"pbExp",
			"functionEval",
			"arrIdx"
	};
	
	public static final List<String> BYTE_TYPES_ARRAY = Arrays.asList(BYTE_TYPES_STR);
	
	public static final boolean isByteBound(int number, String errMsg) throws CompilerException {
		if ((number < 0) || (number > 255)) {
			throw new CompilerException(errMsg, null);
		}
		return true;
	}
	
	public static boolean isABForOp(String op) {
		return true;
	}
	
	public static final int START_GLOB_VAR = 10;
}
