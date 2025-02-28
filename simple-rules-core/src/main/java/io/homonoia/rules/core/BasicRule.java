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

import io.homonoia.rules.api.Facts;
import io.homonoia.rules.api.Rule;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Basic rule implementation class that provides common methods.
 * <p>
 * You can extend this class and override {@link BasicRule#evaluate(io.homonoia.rules.api.Facts)}
 * and {@link BasicRule#execute(io.homonoia.rules.api.Facts)} to provide rule conditions and actions
 * logic.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class BasicRule implements Rule {

  /**
   * Rule name.
   */
  protected String name;

  /**
   * Rule description.
   */
  protected String description;

  /**
   * Rule priority.
   */
  protected int priority;

  /**
   * Rule loop.
   */
  protected boolean loop;

  /**
   * Rule execution state.
   */
  protected final AtomicBoolean fired = new AtomicBoolean(false);

  /**
   * Create a new {@link BasicRule}.
   */
  public BasicRule() {
    this(Rule.DEFAULT_NAME, Rule.DEFAULT_DESCRIPTION, Rule.DEFAULT_PRIORITY);
  }

  /**
   * Create a new {@link BasicRule}.
   *
   * @param name rule name
   */
  public BasicRule(final String name) {
    this(name, Rule.DEFAULT_DESCRIPTION, Rule.DEFAULT_PRIORITY);
  }

  /**
   * Create a new {@link BasicRule}.
   *
   * @param name        rule name
   * @param description rule description
   */
  public BasicRule(final String name, final String description) {
    this(name, description, Rule.DEFAULT_PRIORITY);
  }

  /**
   * Create a new {@link BasicRule}.
   *
   * @param name        rule name
   * @param description rule description
   * @param priority    rule priority
   */
  public BasicRule(final String name, final String description, final int priority) {
    this.name = name;
    this.description = description;
    this.priority = priority;
  }

  /**
   * Create a new {@link BasicRule}.
   *
   * @param name        rule name
   * @param description rule description
   * @param priority    rule priority
   * @param loop        rule loop
   */
  public BasicRule(final String name, final String description, final int priority,
      final boolean loop) {
    this.name = name;
    this.description = description;
    this.priority = priority;
    this.loop = loop;
  }

  /**
   * {@inheritDoc}
   */
  public boolean evaluate(Facts facts) {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public void execute(Facts facts) throws Exception {
    // no op
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  @Override
  public int getPriority() {
    return priority;
  }

  public void setPriority(final int priority) {
    this.priority = priority;
  }

  @Override
  public boolean getLoop() {
    return loop;
  }

  public void setLoop(final boolean loop) {
    this.loop = loop;
  }

  @Override
  public boolean hasFired() {
    return fired.get();
  }

  /*
   * Rules are unique according to their names within a rules engine registry.
   */

  @Override
  public boolean equals(final Object o) {
      if (this == o) {
          return true;
      }
      if (o == null || getClass() != o.getClass()) {
          return false;
      }

    BasicRule basicRule = (BasicRule) o;

      if (priority != basicRule.priority) {
          return false;
      }
      if (!name.equals(basicRule.name)) {
          return false;
      }
      if (loop != basicRule.loop) {
          return false;
      }
    return Objects.equals(description, basicRule.description);

  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + priority;
    result = 31 * result + (loop ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int compareTo(final Rule rule) {
    if (getPriority() < rule.getPriority()) {
      return -1;
    } else if (getPriority() > rule.getPriority()) {
      return 1;
    } else if (getLoop() != rule.getLoop()) {
      return Boolean.compare(getLoop(), rule.getLoop());
    } else {
      return getName().compareTo(rule.getName());
    }
  }

}
