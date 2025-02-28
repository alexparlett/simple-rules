/*
 * The MIT License
 *
 * Copyright (c) 2022, Alex Parlett (alex.parlett@homonoia-studios.co.uk)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.homonoia.rules.core;

import io.homonoia.rules.api.Action;
import io.homonoia.rules.api.Condition;
import io.homonoia.rules.api.Facts;
import java.util.List;

class DefaultRule extends BasicRule {

  private final Condition condition;
  private final List<Action> actions;

  DefaultRule(String name, String description, int priority, boolean loop, Condition condition,
      List<Action> actions) {
    super(name, description, priority, loop);
    this.condition = condition;
    this.actions = actions;
  }

  @Override
  public boolean evaluate(Facts facts) {
    if (!getLoop() && fired.get()) {
      return false;
    }
    return condition.evaluate(facts);
  }

  @Override
  public void execute(Facts facts) throws Exception {
    fired.getAndSet(true);
    for (Action action : actions) {
      action.execute(facts);
    }
  }

}
