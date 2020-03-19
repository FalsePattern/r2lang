package falsepattern.r2lang;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class R2InterOptimizer {
  private static final List<String> OPT = Arrays.asList("add", "sub", "mul", "div", "mod", "shl",
      "shr", "lor", "lxor", "land", "or", "xor", "and", "eq", "neq", "lt", "gt", "leq", "geq");
  public static String optimizeIntMath(String code, BigInteger maxConstantValue) {
    boolean wasPreviousLoadIntA = false;
    boolean wasPreviousLoadIntB = false;
    LinkedBlockingDeque<String> tmp = new LinkedBlockingDeque<>();
    var result = new StringBuilder();
    var lines = code.split("\n");
    for (var line: lines) {
      boolean isLoadInt = false;
      boolean prev2Int = wasPreviousLoadIntA && wasPreviousLoadIntB;
      var parts = line.split(" ");
      switch (parts[0]) {
        case "loadint":
          if (prev2Int) {
            result.append(tmp.removeFirst());
          }
          isLoadInt = true;
          tmp.addLast(line);
          break;
          //'add' | 'sub' | 'mul' | 'div' | 'mod' | 'shl' | 'shr' | 'lor' | 'lxor' | 'land'
        //| 'or' | 'xor' | 'and' | 'eq' | 'neq' | 'lt' | 'gt' | 'leq' | 'geq'
        default:
          if (OPT.contains(parts[0])) {
            if (prev2Int) {
              var line1 = tmp.removeFirst().split(" ");
              var line2 = tmp.removeFirst().split(" ");
              var a = new BigInteger(line1[2]);
              var b = new BigInteger(line2[2]);
              if (parts[1].equals(line2[1])) {
                var c = a;
                a = b;
                b = c;
              }
             BigInteger operationResult = switch (parts[0]) {
                case "add" -> a.add(b);
                case "sub" -> a.subtract(b);
                case "mul" -> a.multiply(b);
                case "div" -> a.divide(b);
                case "mod" -> a.mod(b);
                case "shl" -> a.shiftLeft(b.intValueExact());
                case "shr" -> a.shiftRight(b.intValueExact());
                case "lor" -> !a.equals(BigInteger.ZERO) || !b.equals(BigInteger.ZERO) ? BigInteger.ONE : BigInteger.ZERO;
                case "lxor" -> (a.equals(BigInteger.ZERO) && !b.equals(BigInteger.ZERO)) || (!a.equals(BigInteger.ZERO) && b.equals(BigInteger.ZERO)) ? BigInteger.ONE : BigInteger.ZERO;
                case "land" -> !a.equals(BigInteger.ZERO) && !b.equals(BigInteger.ZERO) ? BigInteger.ONE : BigInteger.ZERO;
                case "or" -> a.or(b);
                case "xor" -> a.xor(b);
                case "and" -> a.and(b);
                case "eq" -> a.compareTo(b) == 0 ? BigInteger.ONE : BigInteger.ZERO;
                case "neq" -> a.compareTo(b) != 0 ? BigInteger.ONE : BigInteger.ZERO;
                case "lt" -> a.compareTo(b) < 0 ? BigInteger.ONE : BigInteger.ZERO;
                case "gt" -> a.compareTo(b) > 0 ? BigInteger.ONE : BigInteger.ZERO;
                case "leq" -> a.compareTo(b) <= 0 ? BigInteger.ONE : BigInteger.ZERO;
                case "geq" -> a.compareTo(b) >= 0 ? BigInteger.ONE : BigInteger.ZERO;
                default -> throw new RuntimeException("PANIC while trying to optimize math");
              };
              if (operationResult.compareTo(maxConstantValue) >= 0) {
                buildLine(result, line1);
                buildLine(result, line2);
                buildLine(result, line);
              } else {
                buildLine(result, "loadint", parts[1], operationResult.toString());
              }
              break;
            } else {
              tmp.addLast(line);
            }
          } else {
            tmp.addLast(line);
          }
      }
      if (!isLoadInt) {
        tmp.forEach((ln) -> result.append(ln).append('\n'));
        tmp.clear();
      }
      wasPreviousLoadIntB = wasPreviousLoadIntA;
      wasPreviousLoadIntA = isLoadInt;
    }
    return result.toString();
  }

  private static void buildLine(StringBuilder builder, String... parts) {
    builder.append(parts[0]);
    for (int i = 1; i < parts.length; i++) {
      builder.append(' ').append(parts[i]);
    }
    builder.append('\n');
  }
}
