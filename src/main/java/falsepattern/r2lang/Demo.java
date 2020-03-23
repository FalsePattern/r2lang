package falsepattern.r2lang;

import static falsepattern.r2lang.util.ReflectionHelper.getOpText;

import falsepattern.r2lang.R2Lang.ExprContext;
import falsepattern.r2lang.R2Lang.IdentifierContext;
import falsepattern.r2lang.R2Lang.IntegerConstantContext;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Demo {
  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Accepted parameters: <source file path> [optimization passes]");
      System.exit(0);
    }
    int passes = 0;
    if (args.length == 2) {
      passes = Integer.parseInt(args[1]);
    }
    String filePath = args[0];
    try {
      var parseLog = optimizeCode(Files.readString(Path.of(filePath)), passes);
      System.out.println(parseLog);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static String optimizeCode(String code, int passes) {

    var lexer = new R2LangLexer(CharStreams.fromString(code));
    var tokenStream = new CommonTokenStream(lexer);
    var parser = new R2Lang(tokenStream);
    var analyzer = new Analyzer();
    var optimizer = new Optimizer();
    optimizer.a = analyzer;
    var program = parser.program();
    StringBuilder result = new StringBuilder("Unoptimized:\n" + stringifyRule(program, " "));
    for (int i = 0; i < passes - 1; i++) {
      analyzer.init();
      ParseTreeWalker.DEFAULT.walk(optimizer, program);
      result.append("\n\nPass").append(i).append(":\n").append(stringifyRule(program, " "));
    }
    return result.toString();
  }

  private static String stringifyRule(ParserRuleContext ctx, String indent) {
    var result = new StringBuilder();
    var ind = indent + " ";
    result.append(ctx.getClass().getSimpleName());
    if (ctx instanceof ExprContext) {
      try {
        result.append(getOpText((ExprContext) ctx));
      } catch (IllegalAccessException | NoSuchFieldException ignored) {

      }
    }
    result.append('\n');
    ctx.children.stream().filter((tree) -> !(tree instanceof TerminalNode)).map((child) -> {
      if (child instanceof IntegerConstantContext) {
        return "INTEGER " + child.getText();
      } else if (child instanceof IdentifierContext) {
        return "IDENT " + child.getText();
      } else if (child instanceof R2Lang.FunctionContext) {
        var fun = (R2Lang.FunctionContext)child;
        var builder = new StringBuilder();
        builder.append("FUNCTION ").append(fun.Identifier(0)).append("; ARGS: ");
        var args = fun.Identifier().size() - 1;
        for (int i = 0; i < args; i++) {
          builder.append(fun.Identifier(i + 1)).append(" ");
        }
        return builder.append('\n').append(ind).append(" ").append(stringifyRule(fun.statement(), ind + " ")).toString();
      } else if (child instanceof ParserRuleContext) {
        return stringifyRule((ParserRuleContext) child, ind);
      } else {
        return child.getText();
      }
    }).forEach((rule -> result.append(ind).append(rule).append('\n')));
    result.deleteCharAt(result.length() - 1);
    return result.toString();
  }
}
