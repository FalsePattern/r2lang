grammar R2Inter;
program: statement*;

statement: (funcDef | paramDef | varDef | operation) '\n';

funcDef: 'function' type IDENTIFIER;

paramDef: 'param' type IDENTIFIER;

varDef: 'var' type IDENTIFIER;
operation: zeroOperation | singleOperation | dualOperation;

zeroOperation: 'ret';
singleOperation: singleOperator argSink;
dualOperation: dualOperator argSink ',' argSource;

singleOperator: 'neg' | 'not';
dualOperator: 'add' | 'sub' | 'mul' | 'div' | 'mod' | 'shl' | 'shr' | 'lor' | 'lxor' | 'land'
| 'or' | 'xor' | 'and' | 'eq' | 'neq' | 'lt' | 'gt' | 'leq' | 'geq' | 'ldv' | 'ldi';
argSink: REGISTER | MEMCELL | IDENTIFIER;

argSource: REGISTER | MEMCELL | NUMBER | IDENTIFIER;

type: 'int' | 'void';

MEMCELL: '[' NUMBER ']';
NUMBER: DIGIT+ | ('0x' DIGIT+);
IDENTIFIER: LETTER WORDCHAR*;
WHITESPACE: [ \r\t] -> skip;
REGISTER: 'r' DIGIT+;

fragment LOWERCASE: [a-z];
fragment UPPERCASE: [A-Z];
fragment DIGIT: [0-9];
fragment HEX: [a-fA-F];
fragment LETTER: LOWERCASE | UPPERCASE;
fragment ALPHANUM: LETTER | DIGIT;
fragment WORDCHAR: ALPHANUM | '_';