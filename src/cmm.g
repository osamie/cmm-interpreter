Program -> FunctionDefinition*

FunctionDefinition -> Type id ParameterList Block

ParameterList -> lparen (Parameter (listsep Parameter)*)? rparen

Parameter -> Type id

Block -> bb ExpressionList be

ExpressionList -> Statement*

Statement -> Declaration | WhileLoop | DoLoop | IfStatement | SimpleStatement

Declaration -> Type id (listsep id)* eol

Type -> number_t | string_t | boolean_t 

WhileLoop -> while Condition Block

DoLoop -> do Block while Condition eol

IfStatement -> if Condition Block (elsif Condition Block)* (else Block)?

#Condval ->  lparen Assignment rparen
Condition ->  lparen Assignment rparen


SimpleStatement -> Assignment eol

Assignment -> Negatedlogical (gets Negatedlogical)?

Logical -> Tenary ((and|or) Tenary)*  [>1]


Comparison -> Sum ((lt|gt|eq|le|ge|ne) Sum)?  [>1]

Sum -> Term ((plus|minus) Term)*  [>1]
Term -> Exp ((multiply|divide|mod) Exp)* [>1]  
Exp -> Element (exp Element)*  [>1] 
Element -> String | lparen Logical rparen

ElementPlus -> id ArgumentList?

Constant -> string | boolean | number

String -> (Constant|ElementPlus) (concat (Constant|ElementPlus))*  [>1]
 
ConcatString -> string

ArgumentList -> lparen (Assignment (listsep Assignment)*)? rparen

Negatedlogical -> negate_l* Logical [>1]

Tenary -> Comparison (tenary_op1 Comparison tenary_op2 Comparison)? [>1] 

#Assignment -> Logical (gets Logical)? 