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

package io.homonoia.rules.support.composite;

import static org.assertj.core.api.Assertions.assertThat;

import io.homonoia.rules.annotation.Action;
import io.homonoia.rules.annotation.Condition;
import io.homonoia.rules.annotation.Priority;
import io.homonoia.rules.annotation.Rule;
import io.homonoia.rules.api.Facts;
import io.homonoia.rules.api.Rules;
import io.homonoia.rules.core.BasicRule;
import io.homonoia.rules.core.DefaultRulesEngine;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConditionalRuleGroupTest {

  private static List<String> actions = new ArrayList<>();

  private TestRule rule1, rule2, conditionalRule;
  private ConditionalRuleGroup conditionalRuleGroup;

  private Facts facts = new Facts();
  private Rules rules = new Rules();

  private DefaultRulesEngine rulesEngine = new DefaultRulesEngine();

  @Before
  public void setUp() {
    conditionalRule = new TestRule("conditionalRule", "description0", 0, true);
    rule1 = new TestRule("rule1", "description1", 1, true);
    rule2 = new TestRule("rule2", "description2", 2, true);
    conditionalRuleGroup = new ConditionalRuleGroup();
    conditionalRuleGroup.addRule(rule1);
    conditionalRuleGroup.addRule(rule2);
    conditionalRuleGroup.addRule(conditionalRule);
    rules.register(conditionalRuleGroup);
  }

  @After
  public void tearDown() {
    rules.clear();
    actions.clear();
  }

  @Test
  public void rulesMustNotBeExecutedIfConditionalRuleEvaluatesToFalse() {
    // Given
    conditionalRule.setEvaluationResult(false);

    // When
    rulesEngine.fire(rules, facts);

    // Then
    /*
     * The composing rules should not be executed
     * since the conditional rule evaluate to FALSE
     */

    // primaryRule should not be executed
    Assertions.assertThat(conditionalRule.hasFired()).isFalse();
    //Rule 1 should not be executed
    Assertions.assertThat(rule1.hasFired()).isFalse();
    //Rule 2 should not be executed
    Assertions.assertThat(rule2.hasFired()).isFalse();
  }

  @Test
  public void selectedRulesMustBeExecutedIfConditionalRuleEvaluatesToTrue() {
    // Given
    rule1.setEvaluationResult(false);

    // When
    rulesEngine.fire(rules, facts);

    // Then
    /*
     * Selected composing rules should be executed
     * since the conditional rule evaluates to TRUE
     */

    // primaryRule should be executed
    Assertions.assertThat(conditionalRule.hasFired()).isTrue();
    //Rule 1 should not be executed
    Assertions.assertThat(rule1.hasFired()).isFalse();
    //Rule 2 should be executed
    Assertions.assertThat(rule2.hasFired()).isTrue();
  }

  @Test
  public void whenARuleIsRemoved_thenItShouldNotBeEvaluated() {
    // Given
    conditionalRuleGroup.removeRule(rule2);

    // When
    rulesEngine.fire(rules, facts);

    // Then
    // primaryRule should be executed
    Assertions.assertThat(conditionalRule.hasFired()).isTrue();
    //Rule 1 should be executed
    Assertions.assertThat(rule1.hasFired()).isTrue();
    // Rule 2 should not be executed
    Assertions.assertThat(rule2.hasFired()).isFalse();
  }

  @Test
  public void testCompositeRuleWithAnnotatedComposingRules() {
    // Given
    MyRule rule = new MyRule();
    conditionalRuleGroup.addRule(rule);

    // When
    rulesEngine.fire(rules, facts);

    // Then
    Assertions.assertThat(conditionalRule.hasFired()).isTrue();
    assertThat(rule.isExecuted()).isTrue();
  }

  @Test
  public void whenAnnotatedRuleIsRemoved_thenItsProxyShouldBeRetrieved() {
    // Given
    MyRule rule = new MyRule();
    MyAnnotatedRule annotatedRule = new MyAnnotatedRule();
    conditionalRuleGroup.addRule(rule);
    conditionalRuleGroup.addRule(annotatedRule);
    conditionalRuleGroup.removeRule(annotatedRule);

    // When
    rulesEngine.fire(rules, facts);

    // Then
    Assertions.assertThat(conditionalRule.hasFired()).isTrue();
    assertThat(rule.isExecuted()).isTrue();
    assertThat(annotatedRule.isExecuted()).isFalse();
  }

  @Test(expected = IllegalArgumentException.class)
  public void twoRulesWithSameHighestPriorityIsNotAllowed() {
    conditionalRuleGroup.addRule(new MyOtherRule(0));// same priority as conditionalRule
    conditionalRuleGroup.addRule(new MyOtherRule(1));
    conditionalRuleGroup.addRule(new MyRule());
    conditionalRuleGroup.evaluate(facts);
  }

  @Test
  public void twoRulesWithSamePriorityIsAllowedIfAnotherRuleHasHigherPriority() {
    MyOtherRule rule1 = new MyOtherRule(3);
    conditionalRuleGroup.addRule(rule1);
    conditionalRuleGroup.addRule(new MyOtherRule(2));
    conditionalRuleGroup.addRule(new MyRule());
    rules.register(conditionalRuleGroup);
    rulesEngine.fire(rules, facts);
    assertThat(rule1.isExecuted()).isTrue();
  }

  @Test
  public void aRuleWithoutPriorityHasLowestPriority() {
    // given
    UnprioritizedRule rule = new UnprioritizedRule();
    conditionalRuleGroup.addRule(rule);

    // when
    rulesEngine.fire(rules, facts);

    // then
    assertThat(actions).containsExactly(
        "conditionalRule",
        "rule1",
        "rule2",
        "UnprioritizedRule");
  }

  @Test
  public void testComposingRulesExecutionOrder() {
    // When
    rulesEngine.fire(rules, facts);

    // Then
    // rule 1 has higher priority than rule 2 (lower values for highers priorities),
    // it should be executed first
    assertThat(actions).containsExactly(
        "conditionalRule",
        "rule1",
        "rule2");
  }

  @Rule
  public static class MyRule {

    boolean executed;

    @Condition
    public boolean when() {
      return true;
    }

    @Action
    public void then() {
      executed = true;
    }

    @Priority
    public int priority() {
      return 2;
    }

    public boolean isExecuted() {
      return executed;
    }

  }

  @Rule
  public static class MyAnnotatedRule {

    private boolean executed;

    @Condition
    public boolean evaluate() {
      return true;
    }

    @Action
    public void execute() {
      executed = true;
    }

    @Priority
    public int priority() {
      return 3;
    }

    public boolean isExecuted() {
      return executed;
    }
  }

  @Rule
  public static class MyOtherRule {

    boolean executed;
    private int priority;

    public MyOtherRule(int priority) {
      this.priority = priority;
    }

    @Condition
    public boolean when() {
      return true;
    }

    @Action
    public void then() {
      executed = true;
    }

    @Priority
    public int priority() {
      return priority;
    }

    public boolean isExecuted() {
      return executed;
    }

  }

  @Rule
  public static class UnprioritizedRule {

    boolean executed;

    @Condition
    public boolean when() {
      return true;
    }

    @Action
    public void then() {
      executed = true;
      actions.add("UnprioritizedRule");
    }

    public boolean isExecuted() {
      return executed;
    }

  }

  public static class TestRule extends BasicRule {

    boolean evaluationResult;

    TestRule(String name, String description, int priority, boolean evaluationResult) {
      super(name, description, priority);
      this.evaluationResult = evaluationResult;
    }

    @Override
    public boolean evaluate(Facts facts) {
      return evaluationResult;
    }

    @Override
    public void execute(Facts facts) {
      this.fired.set(true);
      actions.add(name);
    }

    void setEvaluationResult(boolean evaluationResult) {
      this.evaluationResult = evaluationResult;
    }
  }
}
