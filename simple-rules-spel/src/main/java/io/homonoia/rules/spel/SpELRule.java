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

package io.homonoia.rules.spel;

import io.homonoia.rules.api.Action;
import io.homonoia.rules.api.Condition;
import io.homonoia.rules.api.Facts;
import io.homonoia.rules.api.Rule;
import io.homonoia.rules.core.BasicRule;
import java.util.ArrayList;
import java.util.List;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ParserContext;

/**
 * A {@link Rule} implementation that uses
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#expressions">SpEL</a>
 * to evaluate and execute the rule.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class SpELRule extends BasicRule {

  private Condition condition = Condition.FALSE;
  private final List<Action> actions = new ArrayList<>();
  private final BeanResolver beanResolver;
  private final ParserContext parserContext;

  /**
   * Create a new SpEL rule.
   */
  public SpELRule() {
    this(null, null);
  }

  /**
   * Create a new SpEL rule.
   *
   * @param parserContext used to resolve expressions
   */
  public SpELRule(ParserContext parserContext) {
    this(null, parserContext);
  }

  /**
   * Create a new SpEL rule.
   *
   * @param beanResolver used to resolve bean references in expressions
   */
  public SpELRule(BeanResolver beanResolver) {
    this(beanResolver, null);
  }

  /**
   * Create a new SpEL rule.
   *
   * @param beanResolver  used to resolve bean references in expressions
   * @param parserContext used to resolve expressions
   */
  public SpELRule(BeanResolver beanResolver, ParserContext parserContext) {
    super(Rule.DEFAULT_NAME, Rule.DEFAULT_DESCRIPTION, Rule.DEFAULT_PRIORITY);
    this.beanResolver = beanResolver;
    this.parserContext = parserContext;
  }

  /**
   * Set rule name.
   *
   * @param name of the rule
   * @return this rule
   */
  public SpELRule name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Set rule description.
   *
   * @param description of the rule
   * @return this rule
   */
  public SpELRule description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Set rule priority.
   *
   * @param priority of the rule
   * @return this rule
   */
  public SpELRule priority(int priority) {
    this.priority = priority;
    return this;
  }

  /**
   * Set rule loop.
   *
   * @param loop of the rule
   * @return this rule
   */
  public SpELRule loop(boolean loop) {
    this.loop = loop;
    return this;
  }

  /**
   * Specify the rule's condition as SpEL expression.
   *
   * @param condition of the rule
   * @return this rule
   */
  public SpELRule when(String condition) {
    this.condition = new SpELCondition(condition, beanResolver, parserContext);
    return this;
  }

  /**
   * Add an action specified as an SpEL expression to the rule.
   *
   * @param action to add to the rule
   * @return this rule
   */
  public SpELRule then(String action) {
    this.actions.add(new SpELAction(action, beanResolver, parserContext));
    return this;
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
