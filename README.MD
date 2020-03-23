#R2Lang

##Dependencies
JDK 14<br>
maven 3.6.3 or higher

##Building
To generate sources from the Antlr4 grammar files, run:<br>
`mvn clean antlr4:antlr4`

To build a jar with dependencies (also generates sources), run the following in the root folder:<br>
`mvn clean package`<br>
and then you can find two jar files in the target folder: one without dependencies, and one with every dependency included

##Running
`r2lang-x.y.z.jar <source file path> [optimization passes]`<br>
`source file path` is a relative or absolute path to a file containing R2Lang source code<br>
`optimization passes` specifies how many times you want the optimizer to pass over the code before exiting (recommended: at least `2`)