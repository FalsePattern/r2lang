package falsepattern.r2lang;

import falsepattern.r2lang.R2Lang.AssignmentContext;
import falsepattern.r2lang.R2Lang.IdentifierContext;
import falsepattern.r2lang.R2Lang.IntegerConstantContext;
import java.util.HashMap;
import org.antlr.v4.runtime.ParserRuleContext;

public class Analyzer extends R2LangBaseListener {


  private HashMap<ParserRuleContext, Boolean> isPure;
  private HashMap<ParserRuleContext, Boolean> noSideEffects;
  private HashMap<ParserRuleContext, Integer> pureValue;

  public void init() {
    if (isPure == null) {
      isPure = new HashMap<>();
      noSideEffects = new HashMap<>();
      pureValue = new HashMap<>();
    } else {
      isPure.clear();
      noSideEffects.clear();
      pureValue.clear();
    }
  }




  
  public void setPureWithoutSideEffects(ParserRuleContext ctx, int value) {
    isPure.put(ctx, true);
    noSideEffects.put(ctx, true);
    pureValue.put(ctx, value);
  }

  public void setImpureWithoutSideEffects(ParserRuleContext ctx) {
    isPure.put(ctx, false);
    noSideEffects.put(ctx, true);
  }

  public void setPureWithSideEffects(ParserRuleContext ctx, int value) {
    isPure.put(ctx, true);
    noSideEffects.put(ctx, false);
    pureValue.put(ctx, value);
  }

  public void setImpureWithSideEffects(ParserRuleContext ctx) {
    isPure.put(ctx, false);
    noSideEffects.put(ctx, false);
  }

  public boolean getIsPure(ParserRuleContext ctx) {
    return isPure.getOrDefault(ctx, false);
  }

  public boolean getIsImpure(ParserRuleContext ctx) {
    return !getIsPure(ctx);
  }

  public boolean getIsWithoutSideEffects(ParserRuleContext ctx) {
    return noSideEffects.getOrDefault(ctx, false);
  }

  public boolean getIsWithSideEffects(ParserRuleContext ctx) {
    return !getIsWithoutSideEffects(ctx);
  }

  public boolean getIsPureWithoutSideEffects(ParserRuleContext ctx) {
    return getIsPure(ctx) && getIsWithoutSideEffects(ctx);
  }

  public boolean getIsImpureWithoutSideEffects(ParserRuleContext ctx) {
    return getIsImpure(ctx) && getIsWithoutSideEffects(ctx);
  }

  public boolean getIsPureWithSideEffects(ParserRuleContext ctx) {
    return getIsPure(ctx) && getIsWithSideEffects(ctx);
  }

  public boolean getIsImpureWithSideEffects(ParserRuleContext ctx) {
    return getIsImpure(ctx) && getIsWithSideEffects(ctx);
  }

  public int getPureValue(ParserRuleContext ctx) {
    if (getIsPure(ctx)) {
      return pureValue.getOrDefault(ctx, 0);
    } else {
      throw new RuntimeException("Tried to get pure value of impure rule");
    }
  }


}
