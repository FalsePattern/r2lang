parser grammar R2Lang;

options {tokenVocab=R2LangLexer;}

program: function*;

function: Fun Identifier OpenP (Identifier (Comma Identifier)*)? CloseP statement;

statement
: OpenB statement* CloseB # compoundStatement
| If OpenP expression CloseP statement (Else statement) # conditionalStatement
| While OpenP expression CloseP statement # loopStatement
| Return expression Semicolon # returnStatement
| expression Semicolon # expressionStatement
| Var Identifier (Comma Identifier)* (Equal expr)? Semicolon # declarationStatement
| Semicolon # noopStatement
;

expression: expr;

expr
: IntegerLiteral # integerConstant
| StringLiteral # stringConstant
| Identifier # identifier
| OpenP expr CloseP # parentheses
| expr op=OpenP (expr (Comma expr)*)? CloseP # functionCall
| expr op=OpenBr expr CloseBr # indexing
| expr op=(Increment | Decrement) # postIncrementDecrement
| op=(Plus | Minus | Not | Increment | Decrement | And | Star) expr # unary
| expr op=(Star | Slash | Percent) expr # multiplicative
| expr op=(Plus | Minus) expr # additive
| expr op=(ShiftLeft | ShiftRight) expr # shifting
| expr op=(LessThan | LessOrEqual | GreaterThan | GreaterOrEqual) expr # comparative
| expr op=(EqualCompare | NotEqualCompare) expr # equality
| expr op=And expr # bitwiseAnd
| expr op=Caret expr # bitwiseXor
| expr op=Pipe expr # bitwiseOr
| expr op=AndAnd expr # logicalAnd
| expr op=PipePipe expr # logicalOr
| <assoc=right> expr op=Question expr Colon expr # conditional
| <assoc=right> expr op=(Equal | PlusEqual | MinusEqual | StarEqual | SlashEqual | PercentEqual
           | ShiftRightEqual | ShiftLeftEqual | AndEqual | CaretEqual | PipeEqual) expr # assignment
| expr op=Comma expr # comma
;
