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

package io.homonoia.rules.tutorials.fizzbuzz;

import io.homonoia.rules.annotation.Action;
import io.homonoia.rules.annotation.Condition;
import io.homonoia.rules.annotation.Fact;
import io.homonoia.rules.annotation.Priority;
import io.homonoia.rules.annotation.Rule;

@Rule
public class NonFizzBuzzRule {

  @Condition
  public boolean isNotFizzNorBuzz(@Fact("number") Integer number) {
    // can return true, because this is the latest rule to trigger according to assigned priorities
    // and in which case, the number is not fizz nor buzz
    return number % 5 != 0 || number % 7 != 0;
  }

  @Action
  public void printInput(@Fact("number") Integer number) {
    System.out.print(number);
  }

  @Priority
  public int getPriority() {
    return 3;
  }
}
