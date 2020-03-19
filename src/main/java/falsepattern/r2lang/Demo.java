package falsepattern.r2lang;

import java.math.BigInteger;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Demo {
  public static void main(String[] args) {
    String text = "int demo(int a, int b) {\n"
        + "int c;\n"
        + "c = 1 + 2 + 3 + a + b;\n"
        + "c += 1 << 12;\n"
        + "return c;\n}";
    var lexer = new R2LangLexer(CharStreams.fromString(text));
    var tokenStream = new CommonTokenStream(lexer);
    var parser = new R2LangParser(tokenStream);
    var translator = new R2LangToR2Inter();
    ParseTreeWalker.DEFAULT.walk(translator, parser.program());
    var code = translator.toString();
    System.out.println(code);
    int prevLength;
    do {
      prevLength = code.length();
      code = R2InterOptimizer.optimizeIntMath(code, BigInteger.valueOf(65536));
    } while (code.length() < prevLength);
    System.out.println(code);
  }
}
