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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.homonoia.rules.api.RulesEngineParameters;
import org.junit.Before;
import org.junit.Test;

public class SkipOnFirstFailedRuleTest extends AbstractTest {

  @Before
  public void setup() throws Exception {
    super.setup();
    RulesEngineParameters parameters = new RulesEngineParameters().skipOnFirstFailedRule(true);
    rulesEngine = new DefaultRulesEngine(parameters);
  }

  @Test
  public void testSkipOnFirstFailedRule() throws Exception {
    // Given
    when(rule1.evaluate(facts)).thenReturn(true);
    when(rule2.compareTo(rule1)).thenReturn(1);
    final Exception exception = new Exception("fatal error!");
    doThrow(exception).when(rule1).execute(facts);
    rules.register(rule1);
    rules.register(rule2);

    // When
    rulesEngine.fire(rules, facts);

    // Then
    //Rule 1 should be executed
    verify(rule1).execute(facts);
    //Rule 2 should be skipped since Rule 1 has failed
    verify(rule2, never()).execute(facts);
  }

}
