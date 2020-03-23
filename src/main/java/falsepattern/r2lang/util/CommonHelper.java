package falsepattern.r2lang.util;

import falsepattern.r2lang.R2Lang.ExprContext;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

public final class CommonHelper {

  public static void replaceWithChild(ParserRuleContext ctx, ParserRuleContext child) {
    var parent = (ParserRuleContext)ctx.parent;
    var index = parent.children.indexOf(ctx);
    parent.children.set(index, child);
    ctx.parent = null;
    child.parent = parent;
    ctx.children.remove(child);
  }

  public static ExprContext createEmptySibling(ExprContext ctx) {
    return new ExprContext((ParserRuleContext) ctx.parent, 0);
  }

  public static ExprContext deepClone(ExprContext ctx) {
    ExprContext clone;
    try {
      clone = ctx.getClass().getConstructor(ExprContext.class).newInstance(new ExprContext((ParserRuleContext) ctx.parent, 0));
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
    clone.children = new ArrayList<>();
    ctx.children.forEach((child) -> {
      if (child instanceof ExprContext) {
        var childClone = deepClone((ExprContext) child);
        childClone.parent = clone;
        clone.children.add(childClone);
      } else if (child instanceof TerminalNode) {
        var newTerminal = cloneTerminal((TerminalNode) child);
        newTerminal.setParent(clone);
        clone.children.add(newTerminal);
      } else {
        throw new RuntimeException("Failed to clone expression: unknown type " + child.getClass().getName());
      }
    });
    try {
      clone.getClass().getField("op").set(clone, ctx.getClass().getField("op").get(ctx));
    } catch (IllegalAccessException | NoSuchFieldException ignored) {}
    return clone;
  }

  public static TerminalNode cloneTerminal(TerminalNode original) {
    return new TerminalNodeImpl(cloneToken(original.getSymbol()));
  }

  public static Token cloneToken(Token token) {
    return new CommonToken(0, token.getText());
  }
}
