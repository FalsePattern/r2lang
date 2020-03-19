package falsepattern.r2lang;

import java.util.ArrayList;
import java.util.Stack;

public class RegisterManager {
  private ArrayList<Integer> activeRegisters = new ArrayList<>();

  public int allocateNew() {
    int i = 0;
    for (; i < activeRegisters.size(); i++) {
      if (!activeRegisters.contains(i)) {
        break;
      }
    }
    activeRegisters.add(i);
    return i;
  }

  public int getTopRegister() {
    return activeRegisters.get(activeRegisters.size() - 1);
  }

  public int getSecondRegister() {
    return activeRegisters.get(activeRegisters.size() - 2);
  }

  public void freeRegister(int register) {
    activeRegisters.remove((Integer)register);
  }

  public int freeTopRegister() {
    int result = getTopRegister();
    activeRegisters.remove(activeRegisters.size() - 1);
    return result;
  }

  public void clear() {
    activeRegisters.clear();
  }
}
