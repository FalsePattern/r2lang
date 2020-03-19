package falsepattern.r2lang;

import falsepattern.r2lang.R2LangParser.AdditiveExpressionContext;
import falsepattern.r2lang.R2LangParser.AndExpressionContext;
import falsepattern.r2lang.R2LangParser.AssignmentExpressionContext;
import falsepattern.r2lang.R2LangParser.EqualityExpressionContext;
import falsepattern.r2lang.R2LangParser.ExclusiveOrExpressionContext;
import falsepattern.r2lang.R2LangParser.ExpressionContext;
import falsepattern.r2lang.R2LangParser.ExpressionStatementContext;
import falsepattern.r2lang.R2LangParser.FunctionContext;
import falsepattern.r2lang.R2LangParser.FunctionParamsContext;
import falsepattern.r2lang.R2LangParser.InclusiveOrExpressionContext;
import falsepattern.r2lang.R2LangParser.LogicalAndExpressionContext;
import falsepattern.r2lang.R2LangParser.LogicalOrExpressionContext;
import falsepattern.r2lang.R2LangParser.LogicalXorExpressionContext;
import falsepattern.r2lang.R2LangParser.MultiplicativeExpressionContext;
import falsepattern.r2lang.R2LangParser.PrimaryExpressionContext;
import falsepattern.r2lang.R2LangParser.RelationalExpressionContext;
import falsepattern.r2lang.R2LangParser.ReturnStatementContext;
import falsepattern.r2lang.R2LangParser.ShiftExpressionContext;
import falsepattern.r2lang.R2LangParser.UnaryExpressionContext;
import falsepattern.r2lang.R2LangParser.VariableDeclarationContext;

public class R2LangToR2Inter extends R2LangBaseListener {
  private StringBuilder output = new StringBuilder();
  private RegisterManager registerManager = new RegisterManager();

  @Override
  public void enterFunction(FunctionContext ctx) {
    addLine("function", ctx.typeDeclaration().getText(), ctx.IDENTIFIER().getText());
  }

  @Override
  public void exitFunction(FunctionContext ctx) {
    addLine("endfunc", ctx.IDENTIFIER().getText());
  }

  @Override
  public void enterVariableDeclaration(VariableDeclarationContext ctx) {
    if (ctx.parent instanceof FunctionParamsContext) {
      addLine("param", ctx.typeDeclaration().getText(), ctx.IDENTIFIER().getText());
    } else {
      addLine("var", ctx.typeDeclaration().getText(), ctx.IDENTIFIER().getText());
    }
  }

  @Override
  public void enterExpressionStatement(ExpressionStatementContext ctx) {
    registerManager.clear();
  }

  @Override
  public void exitAssignmentExpression(AssignmentExpressionContext ctx) {
    if (ctx.ASSIGNMENTOPERATOR() != null) {
      var varName = ctx.IDENTIFIER().getText();
      var operator = ctx.ASSIGNMENTOPERATOR().getText();
      if (!operator.equals("=")) {
        loadVar(varName);
        //: '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|='
        //| '&&=' | '^^=' | '||='
        dualOpReg(switch (operator) {
          case "*=" -> "mul";
          case "/=" -> "div";
          case "%=" -> "mod";
          case "+=" -> "add";
          case "-=" -> "sub";
          case "<<=" -> "shl";
          case ">>=" -> "shr";
          case "&=" -> "and";
          case "^=" -> "xor";
          case "|=" -> "or";
          case "&&=" -> "land";
          case "^^=" -> "lxor";
          case "||=" -> "lor";
          default -> {
            throw new RuntimeException("Unimplemented assignment operator " + operator + "at line"
                + ctx.ASSIGNMENTOPERATOR().getSymbol().getLine() + " pos " + ctx.ASSIGNMENTOPERATOR().getSymbol().getCharPositionInLine());
          }
        }, true);
      }
      storeVar(varName);
    }
  }

  @Override
  public void exitLogicalOrExpression(LogicalOrExpressionContext ctx) {
    if (ctx.logicalOrExpression() != null)
      dualOpReg("lor");
  }

  @Override
  public void exitLogicalXorExpression(LogicalXorExpressionContext ctx) {
    if (ctx.logicalXorExpression() != null)
      dualOpReg("lxor");
  }

  @Override
  public void exitLogicalAndExpression(LogicalAndExpressionContext ctx) {
    if (ctx.logicalAndExpression() != null)
      dualOpReg("land");
  }

  @Override
  public void exitInclusiveOrExpression(InclusiveOrExpressionContext ctx) {
    if (ctx.inclusiveOrExpression() != null)
      dualOpReg("or");
  }

  @Override
  public void exitExclusiveOrExpression(ExclusiveOrExpressionContext ctx) {
    if (ctx.exclusiveOrExpression() != null)
      dualOpReg("xor");
  }

  @Override
  public void exitAndExpression(AndExpressionContext ctx) {
    if (ctx.andExpression() != null)
      dualOpReg("and");
  }

  @Override
  public void exitEqualityExpression(EqualityExpressionContext ctx) {
    if (ctx.equalityExpression() != null) {
      switch (ctx.children.get(1).getText()) {
        case "==":
          dualOpReg("eq");
          break;
        case "!=":
          dualOpReg("neq");
          break;
      }
    }
  }

  @Override
  public void exitRelationalExpression(RelationalExpressionContext ctx) {
    if (ctx.relationalExpression() != null) {
      switch (ctx.children.get(1).getText()) {
        case "<":
          dualOpReg("lt");
          break;
        case ">":
          dualOpReg("gt");
          break;
        case "<=":
          dualOpReg("leq");
          break;
        case ">=":
          dualOpReg("geq");
      }
    }
  }

  @Override
  public void exitShiftExpression(ShiftExpressionContext ctx) {
    if (ctx.shiftExpression() != null) {
      switch (ctx.children.get(1).getText()) {
        case "<<":
          dualOpReg("shl");
          break;
        case ">>":
          dualOpReg("shr");
          break;
      }
    }
  }

  @Override
  public void exitAdditiveExpression(AdditiveExpressionContext ctx) {
    if (ctx.additiveExpression() != null) {
      switch (ctx.children.get(1).getText()) {
        case "+":
          dualOpReg("add");
          break;
        case "-":
          dualOpReg("sub");
      }
    }
  }

  @Override
  public void exitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
    if (ctx.multiplicativeExpression() != null) {
      switch (ctx.children.get(1).getText()) {
        case "*":
          dualOpReg("mul");
          break;
        case "/":
          dualOpReg("div");
          break;
        case "%":
          dualOpReg("mod");
      }
    }
  }

  @Override
  public void exitUnaryExpression(UnaryExpressionContext ctx) {
    if (ctx.unaryOperator() != null) {
      switch (ctx.unaryOperator().getText()) {
        case "+":
          break; //unary + is no-op
        case "-":
          singleOpReg("neg");
          break;
        case "~":
          singleOpReg("not");
          break;
        case "!":
          singleOpReg("lnot");
      }
    }
  }

  @Override
  public void exitPrimaryExpression(PrimaryExpressionContext ctx) {
    if (ctx.IDENTIFIER() != null) {
      loadVar(ctx.IDENTIFIER().getText());
    } else if (ctx.NUMBER() != null) {
      loadImmediate(ctx.NUMBER().getText());
    }
  }

  private void dualOpReg(String opCode) {
    dualOpReg(opCode, false);
  }

  private void dualOpReg(String opCode, boolean swapRegs) {
    int r1;
    int r2;
    if (swapRegs) {
      r1 = registerManager.getTopRegister();
      r2 = registerManager.getSecondRegister();
    } else {
      r2 = registerManager.getTopRegister();
      r1 = registerManager.getSecondRegister();
    }
    addLine(opCode, "r" + r1,  "r" + r2);
    registerManager.freeRegister(r2);
  }

  private void singleOpReg(String opCode) {
    int r = registerManager.getTopRegister();
    addLine(opCode, "r" + r);
  }

  private void loadVar(String varName) {
    addLine("loadvar", "r" + registerManager.allocateNew(), varName);
  }

  private void loadImmediate(String value) {
    addLine("loadint", "r" + registerManager.allocateNew(), value);
  }

  private void storeVar(String varName) {
    addLine("storevar", varName, "r" + registerManager.getTopRegister());
  }

  @Override
  public void exitReturnStatement(ReturnStatementContext ctx) {
    addLine("ret");
  }



  private void addLine(String... words) {
    output.append(words[0]);
    for (int i = 1; i < words.length; i++) {
      output.append(' ').append(words[i]);
    }
    output.append('\n');
  }

  public String toString() {
    return output.toString();
  }
}
