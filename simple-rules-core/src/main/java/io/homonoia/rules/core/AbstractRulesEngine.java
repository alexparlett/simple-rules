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

import io.homonoia.rules.api.RuleListener;
import io.homonoia.rules.api.RulesEngine;
import io.homonoia.rules.api.RulesEngineHistory;
import io.homonoia.rules.api.RulesEngineListener;
import io.homonoia.rules.api.RulesEngineParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for {@link RulesEngine} implementations.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public abstract class AbstractRulesEngine implements RulesEngine {

  RulesEngineParameters parameters;
  RulesEngineHistory rulesEngineHistory;
  List<RuleListener> ruleListeners;
  List<RulesEngineListener> rulesEngineListeners;

  AbstractRulesEngine() {
    this(new RulesEngineParameters());
  }

  AbstractRulesEngine(final RulesEngineParameters parameters) {
    this.parameters = parameters;
    this.ruleListeners = new ArrayList<>();
    this.rulesEngineListeners = new ArrayList<>();
    this.rulesEngineHistory = new RulesEngineHistory();

    registerRuleListener(rulesEngineHistory);
  }

  /**
   * Return a copy of the rules engine parameters.
   *
   * @return copy of the rules engine parameters
   */
  @Override
  public RulesEngineParameters getParameters() {
    return new RulesEngineParameters(
        parameters.isSkipOnFirstAppliedRule(),
        parameters.isSkipOnFirstFailedRule(),
        parameters.isSkipOnFirstNonTriggeredRule(),
        parameters.getPriorityThreshold()
    );
  }

  @Override
  public RulesEngineHistory getHistory() {
    return new RulesEngineHistory(rulesEngineHistory.getExecutionStatus());
  }

  /**
   * Return an unmodifiable list of the registered rule listeners.
   *
   * @return an unmodifiable list of the registered rule listeners
   */
  @Override
  public List<RuleListener> getRuleListeners() {
    return Collections.unmodifiableList(ruleListeners);
  }

  /**
   * Return an unmodifiable list of the registered rules engine listeners
   *
   * @return an unmodifiable list of the registered rules engine listeners
   */
  @Override
  public List<RulesEngineListener> getRulesEngineListeners() {
    return Collections.unmodifiableList(rulesEngineListeners);
  }

  public void registerRuleListener(RuleListener ruleListener) {
    ruleListeners.add(ruleListener);
  }

  public void registerRuleListeners(List<RuleListener> ruleListeners) {
    this.ruleListeners.addAll(ruleListeners);
  }

  public void registerRulesEngineListener(RulesEngineListener rulesEngineListener) {
    rulesEngineListeners.add(rulesEngineListener);
  }

  public void registerRulesEngineListeners(List<RulesEngineListener> rulesEngineListeners) {
    this.rulesEngineListeners.addAll(rulesEngineListeners);
  }
}
