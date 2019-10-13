grammar Expression;

// Lexer tokens:

PLUS : '+' ;
MINUS : '-' ;
TIMES : '*' ;
DIVIDE : '/' ;
MODULO : '%' ;
POWER : '^' | '**' ;
LEFT_SHIFT : '<<' ;
RIGHT_SHIFT : '>>' ;
ASSIGN : '=' ;
COMPLEMENT : '~' ;

PLUS_ASSIGN : '+=' ;
MINUS_ASSIGN : '-=' ;
TIMES_ASSIGN : '*=' ;
DIVIDE_ASSIGN : '/=' ;
MODULO_ASSIGN : '%=' ;
POWER_ASSIGN : '^=' ;

EQUAL : '==' ;
NOT_EQUAL : '!=' ;
NEAR : '~=' ;
LESS_THAN : '<' ;
LESS_THAN_OR_EQUAL : '<=' ;
GREATER_THAN : '>' ;
GREATER_THAN_OR_EQUAL : '>=' ;
// SC = "Short Circuit"
// Non-SC variants not currently implemented.
AND_SC : '&&' ;
OR_SC : '||' ;

INCREMENT : '++' ;
DECREMENT : '--' ;

COMMA : ',' ;
OPEN_PAREN : '(' ;
CLOSE_PAREN : ')' ;
OPEN_BRACKET : '{' ;
CLOSE_BRACKET : '}' ;
SEMI_COLON : ';' ;
QUESTION_MARK : '?' ;
COLON : ':' ;
EXCLAMATION_MARK : '!' ;

IF : 'if' ;
ELSE : 'else' ;
WHILE : 'while' ;
DO : 'do' ;
FOR : 'for' ;
BREAK : 'break' ;
CONTINUE : 'continue' ;
RETURN : 'return' ;
SWITCH : 'switch' ;
CASE : 'case' ;
DEFAULT : 'default' ;

fragment DIGIT : [0-9] ;
fragment SIGN : [+-] ;
fragment EXP_CHAR : [eE] ;
fragment DECIMAL : '.' DIGIT+ ( EXP_CHAR SIGN? DIGIT+ )? ;

// All numbers are treated the same. No int/dec divide.
NUMBER : SIGN? ( DIGIT+ DECIMAL? | DECIMAL ) ;

ID : [A-Za-z] [0-9A-Za-z_]* ;

WS  :  [ \t\r\n\u000C]+ -> skip ;

// Parser rules:

/**
 * All statements parseable from the input. Forces consumption of EOF token.
 */
allStatements : statements EOF ;

statements : statement+ ;

statement
    : block # BlockStmt
    | ifStatement # IfStmt
    | whileStatement # WhileStmt
    | doStatement # DoStmt
    | forStatement # ForStmt
    | breakStatement # BreakStmt
    | continueStatement # ContinueStmt
    | returnStatement # ReturnStmt
    | switchStatement # SwitchStmt
    | expressionStatement # ExpressionStmt
    | SEMI_COLON # EmptyStmt
    ;

block : '{' statements '}' ;

ifStatement : IF '(' expression ')' statement ( ELSE statement ) ;

whileStatement : WHILE '(' expression ')' statement ;

doStatement : DO statement WHILE '(' expression ')' SEMI_COLON ;

forStatement
    : FOR '('
        // C-style for loop
        ( expression ';' expression ';' expression
        // Range for loop
        | ID ASSIGN ID ',' ID
        )
      ')' statement ;

breakStatement : BREAK ;

continueStatement : CONTINUE ;

returnStatement : RETURN expression? ;

switchStatement : SWITCH '(' expression ')' '{' (switchLabel ':' statements )+ '}' ;

switchLabel
    : CASE constantExpression # Case
    | DEFAULT # Default
    ;

expressionStatement : expression SEMI_COLON ;

expression
    : unaryOp expression # UnaryExpr
    | expression binaryOp expression # BinaryExpr
    | expression postUnaryOp # PostUnaryExpr
    | ID binaryAssignOp expression # AssignExpr
    | expression '?' expression ':' expression # TernaryExpr
    | functionCall # FunctionCallExpr
    | constantExpression # ConstantExpr
    | ID # IdExpr
    | '(' expression ')' # WrappedExpr
    ;

constantExpression : NUMBER ;

functionCall : ID '(' (expression ( ',' expression )*)? ')' ;

unaryOp
    : MINUS
    | EXCLAMATION_MARK
    | COMPLEMENT
    | INCREMENT
    | DECREMENT
    ;

postUnaryOp
    : INCREMENT
    | DECREMENT
    | EXCLAMATION_MARK
    ;

binaryOp
    : POWER
    | TIMES
    | DIVIDE
    | MODULO
    | PLUS
    | MINUS
    | LEFT_SHIFT
    | RIGHT_SHIFT
    | LESS_THAN
    | GREATER_THAN
    | LESS_THAN_OR_EQUAL
    | GREATER_THAN_OR_EQUAL
    | EQUAL
    | NOT_EQUAL
    | NEAR
    | AND_SC
    | OR_SC
    ;

binaryAssignOp
    : ASSIGN
    | PLUS_ASSIGN
    | MINUS_ASSIGN
    | TIMES_ASSIGN
    | DIVIDE_ASSIGN
    | MODULO_ASSIGN
    | POWER_ASSIGN
    ;
