package falsepattern.r2lang.util;

import falsepattern.r2lang.R2Lang.ExprContext;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.antlr.v4.runtime.Token;

public final class ReflectionHelper {
  public static boolean operationsEqual(ExprContext a, ExprContext b)
      throws NoSuchFieldException, IllegalAccessException {
    return a.getClass().equals(b.getClass()) && getOpText(a).equals(getOpText(b));
  }

  public static ExprContext getExpr(ExprContext ctx, int index)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    try {
      return (ExprContext) ctx.getClass().getMethod("expr", int.class).invoke(ctx, index);
    } catch (NoSuchMethodException e) {
      return (ExprContext) ctx.getClass().getMethod("expr").invoke(ctx);
    }
  }

  @SuppressWarnings("rawtypes")
  public static int getExprCount(ExprContext ctx)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    try {
      return ((List) ctx.getClass().getMethod("expr").invoke(ctx)).size();
    } catch (ClassCastException e) {
      return 1;
    }
  }

  public static String getOpText(ExprContext ctx)
      throws NoSuchFieldException, IllegalAccessException {
    try {
      return ((Token) ctx.getClass().getField("op").get(ctx)).getText();
    } catch (NullPointerException e) {
      return "";
    }
  }


}
