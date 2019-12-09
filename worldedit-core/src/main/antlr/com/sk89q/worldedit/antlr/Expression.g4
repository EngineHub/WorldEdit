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
NUMBER : ( DIGIT+ DECIMAL? | DECIMAL ) ;

ID : [A-Za-z] [0-9A-Za-z_]* ;

WS  :  [ \t\r\n\u000C]+ -> skip ;

// Parser rules:

/**
 * All statements parseable from the input. Forces consumption of EOF token.
 */
allStatements : statements EOF ;

statements : statement+ ;

statement
    : ( block
      | ifStatement
      | whileStatement
      | doStatement
      | forStatement
      | simpleForStatement
      | breakStatement
      | continueStatement
      | returnStatement
      | switchStatement
      | expressionStatement
      | emptyStatement
      ) SEMI_COLON?
    ;

block : '{' statements '}' ;

ifStatement : IF '(' condition=expression ')' trueBranch=statement ( ELSE falseBranch=statement )? ;

whileStatement : WHILE '(' condition=expression ')' body=statement ;

doStatement : DO body=statement WHILE '(' condition=expression ')' ;

// C-Style for loop
forStatement
    : FOR '(' init=expression ';' condition=expression ';' update=expression ')' body=statement ;

// Range for loop
simpleForStatement
    : FOR '(' counter=ID ASSIGN first=expression ',' last=expression ')' body=statement ;

breakStatement : BREAK ;

continueStatement : CONTINUE ;

returnStatement : RETURN value=expression? ;

switchStatement : SWITCH '(' target=expression ')' '{' (labels+=switchLabel ':' bodies+=statements )+ '}' ;

switchLabel
    : CASE constant=constantExpression # Case
    | DEFAULT # Default
    ;

expressionStatement : expression ;

emptyStatement: SEMI_COLON ;

expression : assignmentExpression ;

assignmentExpression
    : conditionalExpression
    | assignment
    ;

assignment
    : target=ID assignmentOperator source=expression
    ;

assignmentOperator
    : ASSIGN
    | POWER_ASSIGN
    | TIMES_ASSIGN
    | DIVIDE_ASSIGN
    | MODULO_ASSIGN
    | PLUS_ASSIGN
    | MINUS_ASSIGN
    ;

conditionalExpression
    : conditionalOrExpression # CEFallthrough
    | condition=conditionalOrExpression QUESTION_MARK
      trueBranch=expression COLON falseBranch=conditionalExpression # TernaryExpr
    ;

conditionalOrExpression
    : conditionalAndExpression # COFallthrough
    | left=conditionalOrExpression OR_SC right=conditionalAndExpression # ConditionalOrExpr
    ;

conditionalAndExpression
    : equalityExpression # CAFallthrough
    | left=conditionalAndExpression AND_SC right=equalityExpression # ConditionalAndExpr
    ;

equalityExpression
    : relationalExpression # EqFallthrough
    | left=equalityExpression
      op=
      ( EQUAL
      | NOT_EQUAL
      | NEAR
      ) right=relationalExpression # EqualityExpr
    ;

relationalExpression
    : shiftExpression # ReFallthrough
    | left=relationalExpression
      op=
      ( LESS_THAN
      | GREATER_THAN
      | LESS_THAN_OR_EQUAL
      | GREATER_THAN_OR_EQUAL
      ) right=shiftExpression # RelationalExpr
    ;

shiftExpression
    : additiveExpression # ShFallthrough
    | left=shiftExpression op=( LEFT_SHIFT | RIGHT_SHIFT ) right=additiveExpression # ShiftExpr
    ;

additiveExpression
    : multiplicativeExpression # AdFallthrough
    | left=additiveExpression op=( PLUS | MINUS ) right=multiplicativeExpression # AddExpr
    ;

multiplicativeExpression
    : powerExpression # MuFallthrough
    | left=multiplicativeExpression
      op=
      ( TIMES
      | DIVIDE
      | MODULO
      ) right=powerExpression # MultiplicativeExpr
    ;

powerExpression
    : unaryExpression # PwFallthrough
    | left=powerExpression POWER right=unaryExpression # PowerExpr
    ;

unaryExpression
    : op=( INCREMENT | DECREMENT ) target=ID # PreCrementExpr
    | op=( PLUS | MINUS ) expr=unaryExpression # PlusMinusExpr
    | postfixExpression # UaFallthrough
    | COMPLEMENT expr=unaryExpression # ComplementExpr
    | EXCLAMATION_MARK expr=unaryExpression # NotExpr
    ;

postfixExpression
    : unprioritizedExpression # PoFallthrough
    | target=ID op=( INCREMENT | DECREMENT) # PostCrementExpr
    | expr=postfixExpression op=EXCLAMATION_MARK # PostfixExpr
    ;

unprioritizedExpression
    : functionCall # FunctionCallExpr
    | constantExpression # ConstantExpr
    | source=ID # IdExpr
    | '(' expression ')' # WrappedExpr
    ;

constantExpression : NUMBER ;

functionCall : name=ID '(' (args+=expression ( ',' args+=expression )*)? ')' ;
