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

package io.homonoia.rules.api;

/**
 * This interface represents a rule's condition.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
@FunctionalInterface
public interface Condition {

  /**
   * Evaluate the condition according to the known facts.
   *
   * @param facts known when evaluating the rule.
   * @return true if the rule should be triggered, false otherwise
   */
  boolean evaluate(Facts facts);

  /**
   * A NoOp {@link Condition} that always returns false.
   */
  Condition FALSE = facts -> false;

  /**
   * A NoOp {@link Condition} that always returns true.
   */
  Condition TRUE = facts -> true;
}
