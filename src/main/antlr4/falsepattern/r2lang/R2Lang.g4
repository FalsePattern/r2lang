grammar R2Lang;
program: function*;

function: typeDeclaration IDENTIFIER functionParams block;

functionParams: '(' (variableDeclaration (',' variableDeclaration)*)? ')';

block: '{' rootStatement* '}';

rootStatement: ((statement) ';') | (block ';'?);

statement:expressionStatement | variableDeclaration | jumpStatement;

jumpStatement: returnStatement;

returnStatement: 'return' expression;

expressionStatement: expression;

expression: assignmentExpression;

assignmentExpression
: logicalOrExpression
| IDENTIFIER ASSIGNMENTOPERATOR assignmentExpression
;

ASSIGNMENTOPERATOR
: '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|='
| '&&=' | '^^=' | '||=';


logicalOrExpression
: logicalXorExpression
| logicalOrExpression '||' logicalXorExpression
;
logicalXorExpression
: logicalAndExpression
| logicalXorExpression '^^' logicalAndExpression
;
logicalAndExpression
: inclusiveOrExpression
| logicalAndExpression '&&' inclusiveOrExpression
;
inclusiveOrExpression
: exclusiveOrExpression
| inclusiveOrExpression '|' exclusiveOrExpression
;
exclusiveOrExpression
: andExpression
| exclusiveOrExpression '^' andExpression
;
andExpression
: equalityExpression
| andExpression '&' equalityExpression
;
equalityExpression
: relationalExpression
| equalityExpression '==' relationalExpression
| equalityExpression '!=' relationalExpression
;
relationalExpression
: shiftExpression
| relationalExpression '<' shiftExpression
| relationalExpression '>' shiftExpression
| relationalExpression '<=' shiftExpression
| relationalExpression '>=' shiftExpression
;
shiftExpression
: additiveExpression
| shiftExpression '<<' additiveExpression
| shiftExpression '>>' additiveExpression
;
additiveExpression
: multiplicativeExpression
| additiveExpression '+' multiplicativeExpression
| additiveExpression '-' multiplicativeExpression
;
multiplicativeExpression
: unaryExpression
| multiplicativeExpression '*' unaryExpression
| multiplicativeExpression '/' unaryExpression
| multiplicativeExpression '%' unaryExpression
;
unaryExpression
: primaryExpression
| unaryOperator unaryExpression
;
unaryOperator: '+' | '-' | '~' | '!';

primaryExpression
: IDENTIFIER
| NUMBER
| '(' expression ')'
;

variableDeclaration: typeDeclaration IDENTIFIER;
typeDeclaration: 'int' | 'void';

NUMBER: DIGIT+ | ('0x' DIGIT+);
IDENTIFIER: LETTER WORDCHAR*;
WHITESPACE: [ \r\n\t] -> skip;

fragment LOWERCASE: [a-z];
fragment UPPERCASE: [A-Z];
fragment DIGIT: [0-9];
fragment HEX: [a-fA-F];
fragment LETTER: LOWERCASE | UPPERCASE;
fragment ALPHANUM: LETTER | DIGIT;
fragment WORDCHAR: ALPHANUM | '_';

