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

import io.homonoia.rules.api.Fact;
import io.homonoia.rules.api.Facts;
import io.homonoia.rules.api.Rule;
import io.homonoia.rules.api.Rules;
import io.homonoia.rules.api.RulesEngine;
import io.homonoia.rules.api.RulesEngineParameters;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inference {@link RulesEngine} implementation.
 * <p>
 * Rules are selected based on given facts and fired according to their natural order which is
 * priority by default. This implementation continuously selects and fires rules until no more rules
 * are applicable.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public final class InferenceRulesEngine extends AbstractRulesEngine {

  private static final Logger LOGGER = LoggerFactory.getLogger(InferenceRulesEngine.class);

  /**
   * Create a new inference rules engine with default parameters.
   */
  public InferenceRulesEngine() {
    this(new RulesEngineParameters());
  }

  /**
   * Create a new inference rules engine.
   *
   * @param parameters of the engine
   */
  public InferenceRulesEngine(RulesEngineParameters parameters) {
    super(parameters);
  }

  @Override
  public void fire(Rules rules, Facts facts) {
    Objects.requireNonNull(rules, "Rules must not be null");
    Objects.requireNonNull(facts, "Facts must not be null");
    Set<Rule> selectedRules;
    triggerListenersBeforeRules(rules, facts);
    do {
      LOGGER.debug("Selecting candidate rules based on the following facts: {}", facts);
      selectedRules = selectCandidates(rules, facts);
      if (!selectedRules.isEmpty()) {
        doFire(new Rules(selectedRules), facts);
      } else {
        LOGGER.debug("No candidate rules found for facts: {}", facts);
      }
    } while (!selectedRules.isEmpty());
    triggerListenersAfterRules(rules, facts);
  }

  private Set<Rule> selectCandidates(Rules rules, Facts facts) {
    Set<Rule> candidates = new TreeSet<>();
    for (Rule rule : rules) {
      final String name = rule.getName();
      if (!shouldBeEvaluated(rule, facts)) {
        LOGGER.debug("Rule '{}' has been skipped before being evaluated", name);
        continue;
      }
      boolean evaluationResult = false;
      try {
        evaluationResult = rule.evaluate(facts);
      } catch (RuntimeException exception) {
        LOGGER.error("Rule '" + name + "' evaluated with error", exception);
        triggerListenersOnEvaluationError(rule, facts, exception);
        // give the option to either skip next rules on evaluation error or continue by considering the evaluation error as false
        if (parameters.isSkipOnFirstNonTriggeredRule()) {
          LOGGER.debug(
              "Next rules will be skipped since parameter skipOnFirstNonTriggeredRule is set");
          break;
        }
      }
      if (evaluationResult) {
        triggerListenersAfterEvaluate(rule, facts, true);
        candidates.add(rule);
      } else {
        LOGGER.debug("Rule '{}' has been evaluated to false, it has not been executed", name);
        triggerListenersAfterEvaluate(rule, facts, false);
        if (parameters.isSkipOnFirstNonTriggeredRule()) {
          LOGGER.debug(
              "Next rules will be skipped since parameter skipOnFirstNonTriggeredRule is set");
          break;
        }
      }
    }
    return candidates;
  }

  void doFire(Rules rules, Facts facts) {
    if (rules.isEmpty()) {
      LOGGER.warn("No rules registered! Nothing to apply");
      return;
    }
    logEngineParameters();
    log(rules);
    log(facts);
    LOGGER.debug("Rules evaluation started");
    for (Rule rule : rules) {
      final String name = rule.getName();
      final int priority = rule.getPriority();
      if (priority > parameters.getPriorityThreshold()) {
        LOGGER.debug(
            "Rule priority threshold ({}) exceeded at rule '{}' with priority={}, next rules will be skipped",
            parameters.getPriorityThreshold(), name, priority);
        break;
      }
      LOGGER.debug("Rule '{}' triggered", name);
      try {
        triggerListenersBeforeExecute(rule, facts);
        rule.execute(facts);
        LOGGER.debug("Rule '{}' performed successfully", name);
        triggerListenersOnSuccess(rule, facts);
        if (parameters.isSkipOnFirstAppliedRule()) {
          LOGGER.debug("Next rules will be skipped since parameter skipOnFirstAppliedRule is set");
          break;
        }
      } catch (Exception exception) {
        LOGGER.error("Rule '" + name + "' performed with error", exception);
        triggerListenersOnFailure(rule, exception, facts);
        if (parameters.isSkipOnFirstFailedRule()) {
          LOGGER.debug("Next rules will be skipped since parameter skipOnFirstFailedRule is set");
          break;
        }
      }
    }
  }

  private void logEngineParameters() {
    LOGGER.debug("{}", parameters);
  }

  private void log(Rules rules) {
    LOGGER.debug("Registered rules:");
    for (Rule rule : rules) {
      LOGGER.debug("Rule { name = '{}', description = '{}', priority = '{}'}",
          rule.getName(), rule.getDescription(), rule.getPriority());
    }
  }

  private void log(Facts facts) {
    LOGGER.debug("Known facts:");
    for (Fact<?> fact : facts) {
      LOGGER.debug("{}", fact);
    }
  }

  @Override
  public Map<Rule, Boolean> check(Rules rules, Facts facts) {
    Objects.requireNonNull(rules, "Rules must not be null");
    Objects.requireNonNull(facts, "Facts must not be null");
    triggerListenersBeforeRules(rules, facts);
    Map<Rule, Boolean> result = doCheck(rules, facts);
    triggerListenersAfterRules(rules, facts);
    return result;
  }

  private Map<Rule, Boolean> doCheck(Rules rules, Facts facts) {
    LOGGER.debug("Checking rules");
    Map<Rule, Boolean> result = new HashMap<>();
    for (Rule rule : rules) {
      if (shouldBeEvaluated(rule, facts)) {
        result.put(rule, rule.evaluate(facts));
      }
    }
    return result;
  }

  private void triggerListenersOnFailure(final Rule rule, final Exception exception, Facts facts) {
    ruleListeners.forEach(ruleListener -> ruleListener.onFailure(rule, facts, exception));
  }

  private void triggerListenersOnSuccess(final Rule rule, Facts facts) {
    ruleListeners.forEach(ruleListener -> ruleListener.onSuccess(rule, facts));
  }

  private void triggerListenersBeforeExecute(final Rule rule, Facts facts) {
    ruleListeners.forEach(ruleListener -> ruleListener.beforeExecute(rule, facts));
  }

  private boolean triggerListenersBeforeEvaluate(Rule rule, Facts facts) {
    return ruleListeners.stream()
        .allMatch(ruleListener -> ruleListener.beforeEvaluate(rule, facts));
  }

  private void triggerListenersAfterEvaluate(Rule rule, Facts facts, boolean evaluationResult) {
    ruleListeners
        .forEach(ruleListener -> ruleListener.afterEvaluate(rule, facts, evaluationResult));
  }

  private void triggerListenersOnEvaluationError(Rule rule, Facts facts, Exception exception) {
    ruleListeners.forEach(ruleListener -> ruleListener.onEvaluationError(rule, facts, exception));
  }

  private void triggerListenersBeforeRules(Rules rule, Facts facts) {
    rulesEngineListeners
        .forEach(rulesEngineListener -> rulesEngineListener.beforeEvaluate(rule, facts));
  }

  private void triggerListenersAfterRules(Rules rule, Facts facts) {
    rulesEngineListeners
        .forEach(rulesEngineListener -> rulesEngineListener.afterExecute(rule, facts));
  }

  private boolean shouldBeEvaluated(Rule rule, Facts facts) {
    return triggerListenersBeforeEvaluate(rule, facts);
  }
}
