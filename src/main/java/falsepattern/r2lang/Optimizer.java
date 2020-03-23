package falsepattern.r2lang;

import static falsepattern.r2lang.util.CommonHelper.createEmptySibling;
import static falsepattern.r2lang.util.CommonHelper.replaceWithChild;
import static falsepattern.r2lang.util.ReflectionHelper.getExpr;
import static falsepattern.r2lang.util.ReflectionHelper.getExprCount;

import falsepattern.r2lang.R2Lang.AdditiveContext;
import falsepattern.r2lang.R2Lang.AssignmentContext;
import falsepattern.r2lang.R2Lang.BitwiseAndContext;
import falsepattern.r2lang.R2Lang.BitwiseOrContext;
import falsepattern.r2lang.R2Lang.BitwiseXorContext;
import falsepattern.r2lang.R2Lang.CommaContext;
import falsepattern.r2lang.R2Lang.ComparativeContext;
import falsepattern.r2lang.R2Lang.EqualityContext;
import falsepattern.r2lang.R2Lang.ExprContext;
import falsepattern.r2lang.R2Lang.IdentifierContext;
import falsepattern.r2lang.R2Lang.IntegerConstantContext;
import falsepattern.r2lang.R2Lang.LogicalAndContext;
import falsepattern.r2lang.R2Lang.LogicalOrContext;
import falsepattern.r2lang.R2Lang.MultiplicativeContext;
import falsepattern.r2lang.R2Lang.ParenthesesContext;
import falsepattern.r2lang.R2Lang.ShiftingContext;
import falsepattern.r2lang.util.CommonHelper;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

public class Optimizer extends R2LangBaseListener {
  public Analyzer a;

  @Override
  public void exitMultiplicative(MultiplicativeContext ctx) {
    basicBinary(ctx);
  }

  @Override
  public void exitAdditive(AdditiveContext ctx) {
    basicBinary(ctx);
  }

  @Override
  public void exitShifting(ShiftingContext ctx) {
    basicBinary(ctx);
  }

  @Override
  public void exitComparative(ComparativeContext ctx) {
    basicBinary(ctx);
  }

  @Override
  public void exitEquality(EqualityContext ctx) {
    basicBinary(ctx);
  }

  @Override
  public void exitBitwiseAnd(BitwiseAndContext ctx) {
    basicBinary(ctx);
  }

  @Override
  public void exitBitwiseXor(BitwiseXorContext ctx) {
    basicBinary(ctx);
  }

  @Override
  public void exitBitwiseOr(BitwiseOrContext ctx) {
    basicBinary(ctx);
  }

  @Override
  public void exitLogicalAnd(LogicalAndContext ctx) {
    var left = ctx.expr(0);
    if (a.getIsPure(left) && a.getPureValue(left) == 0) {
      if (a.getIsWithoutSideEffects(left)) {
        replaceWithInteger(ctx, 0);
      } else {
        replaceWithChild(ctx, left);
      }
    } else {
      basicBinary(ctx);
    }
  }

  @Override
  public void exitLogicalOr(LogicalOrContext ctx) {
    var left = ctx.expr(0);
    if (a.getIsPure(left) && a.getPureValue(left) == 1) {
      if (a.getIsWithoutSideEffects(left)) {
        replaceWithInteger(ctx, 1);
      } else {
        replaceWithChild(ctx, left);
      }
    } else {
      basicBinary(ctx);
    }
  }

  private void basicBinary(ExprContext ctx) {
    ExprContext left;
    ExprContext right;
    try {
      left = getExpr(ctx, 0);
      right = getExpr(ctx, 1);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    var leftPure = a.getIsPure(left);
    var rightPure = a.getIsPure(right);
    var leftNoSideEffect = a.getIsWithoutSideEffects(left);
    var rightNoSideEffect = a.getIsWithoutSideEffects(right);
    if (leftPure && rightPure) {
      var pureVal = calcPure(ctx, left, right);
      if (leftNoSideEffect && rightNoSideEffect) {
        replaceWithInteger(ctx, pureVal);
      } else {
        replaceWithComma(ctx, pureVal);
      }
    } else {
      if (leftNoSideEffect && rightNoSideEffect) {
        a.setImpureWithoutSideEffects(ctx);
      } else {
        a.setImpureWithSideEffects(ctx);
      }
    }
  }

  private int calcPure(ExprContext ctx, ExprContext left, ExprContext right) {
    var x = a.getPureValue(left);
    var y = a.getPureValue(right);
    try {
      return switch (((Token)ctx.getClass().getField("op").get(ctx)).getText()) {
        case "*" -> x * y;
        case "/" -> x / y;
        case "%" -> x % y;
        case "+" -> x + y;
        case "-" -> x - y;
        case "<<" -> x << y;
        case ">>" -> x >> y;
        case "<" -> x < y ? 1 : 0;
        case ">" -> x > y ? 1 : 0;
        case "<=" -> x <= y ? 1 : 0;
        case ">=" -> x >= y ? 1 : 0;
        case "==" -> x == y ? 1 : 0;
        case "!=" -> x != y ? 1 : 0;
        case "&" -> x & y;
        case "^" -> x ^ y;
        case "|" -> x | y;
        case "&&" -> (x != 0) && (y != 0) ? 1 : 0;
        case "||" -> (x != 0) || (y != 0) ? 1 : 0;
        default -> throw new RuntimeException();
      };
    } catch (IllegalAccessException | NoSuchFieldException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void exitIntegerConstant(IntegerConstantContext ctx) {
    a.setPureWithoutSideEffects(ctx, Integer.parseInt(ctx.getText()));
  }

  @Override
  public void exitIdentifier(IdentifierContext ctx) {
    a.setImpureWithoutSideEffects(ctx);
  }

  @Override
  public void exitAssignment(AssignmentContext ctx) {
    var op = ctx.op.getText();
    if (op.equals("=")) {
      return;
    }
    extractAssignmentOperation(ctx, (Class<? extends ExprContext>)switch(op) {
      default -> throw new RuntimeException("Unimplemented operation: " + op);
      case "+=", "-=" -> AdditiveContext.class;
      case "*=", "/=", "%=" -> MultiplicativeContext.class;
      case "<<=", ">>=" -> ShiftingContext.class;
      case "&=" -> BitwiseAndContext.class;
      case "^=" -> BitwiseXorContext.class;
      case "|=" -> BitwiseOrContext.class;
    });
  }

  private <T extends ExprContext> void extractAssignmentOperation(AssignmentContext ctx, Class<T> operationClass) {
    var op = ctx.op.getText().substring(0, ctx.op.getText().length() - 1);
    var left = ctx.expr(0);
    var right = ctx.expr(1);
    if (a.getIsWithoutSideEffects(left)) {
      var leftClone = CommonHelper.deepClone(left);
      T rightOperation;
      try {
        rightOperation = operationClass.getConstructor(ExprContext.class).newInstance(new ExprContext(ctx, 0));
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
      rightOperation.children = new ArrayList<>();
      rightOperation.addChild(leftClone);
      leftClone.parent = rightOperation;
      try {
        operationClass.getField("op").set(rightOperation, new CommonToken(0, op));
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (NoSuchFieldException ignored) {
      }
      rightOperation.addChild(right);
      right.parent = rightOperation;
      ctx.children.set(ctx.children.indexOf(right), rightOperation);
      ctx.op = new CommonToken(0, "=");
    } else {
      throw new RuntimeException("Unimplemented!");
    }
  }

  @Override
  public void exitComma(CommaContext ctx) {
    var exprCount = ctx.expr().size() - 1;
    for (int i = 0; i < exprCount; i++) {
      var expr = ctx.expr(i);
      if (a.getIsWithoutSideEffects(expr)) {
        ctx.children.remove(expr);
        expr.parent = null;
        i--;
        exprCount--;
      } else {
        var sideEffects = getChildrenWithSideEffect(expr);
        if (sideEffects.size() > 0) {
          expr.children.removeAll(sideEffects);
          var start = ctx.children.indexOf(expr);
          ctx.children.remove(start);
          for (int j = 0; j < sideEffects.size(); j++) {
            ctx.children.add(j + start, sideEffects.get(j));
          }
        }
      }
    }
    if (exprCount == 0) {
      replaceWithChild(ctx, ctx.expr(0));
    } else {
      var last = ctx.expr(ctx.expr().size() - 1);
      if (a.getIsPure(last)) {
        a.setPureWithSideEffects(ctx, a.getPureValue(last));
      }
    }
  }

  private IntegerConstantContext createIntegerConstant(ParserRuleContext parent, int value) {
    var integerTerminal = createIntegerTerminal(value);
    var result = new IntegerConstantContext(new ExprContext(parent, 0));
    result.children = new ArrayList<>();
    result.children.add(integerTerminal);
    return result;
  }


  private void replaceWithInteger(ParserRuleContext ctx, int value) {
    var sibling = createIntegerConstant((ParserRuleContext) ctx.parent, value);
    ((ParserRuleContext) ctx.parent).children.set(((ParserRuleContext) ctx.parent).children.indexOf(ctx), sibling);
    a.setPureWithoutSideEffects(sibling, value);
    ctx.parent = null;
  }

  private void replaceWithComma(ExprContext ctx, int pureValue) {
    int exprCount;
    try {
      exprCount = getExprCount(ctx);
      var sibling = new CommaContext(createEmptySibling(ctx));
      var parent = (ParserRuleContext)ctx.parent;
      sibling.children = new ArrayList<>();
      sibling.parent = parent;
      boolean sideEffect = false;
      for (int i = 0; i < exprCount; i++) {
        var expr = getExpr(ctx, i);
        if (!a.getIsWithoutSideEffects(ctx)) {
          sibling.children.add(expr);
          expr.parent = sibling;
          sideEffect = true;
        }
      }
      if (sideEffect) {
        a.setPureWithSideEffects(sibling, pureValue);
      } else {
        a.setPureWithoutSideEffects(sibling, pureValue);
      }
      sibling.children.add(createIntegerConstant(sibling, pureValue));
      parent.children.set(parent.children.indexOf(ctx), sibling);
      ctx.parent = null;
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void exitParentheses(ParenthesesContext ctx) {
    replaceWithChild(ctx, ctx.expr());
  }

  private List<ExprContext> getChildrenWithSideEffect(ExprContext ctx) {
    var result = new ArrayList<ExprContext>();
    try {
      var exprCount = getExprCount(ctx);
      for (int i = 0; i < exprCount; i++) {
        var expr = getExpr(ctx, i);
        if (!a.getIsWithoutSideEffects(expr)) {
          result.add(expr);
        }
      }
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  private TerminalNodeImpl createIntegerTerminal(int value) {
    return new TerminalNodeImpl(new CommonToken(0, Integer.toString(value)));
  }

}
