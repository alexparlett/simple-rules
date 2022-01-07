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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import io.homonoia.rules.annotation.Action;
import io.homonoia.rules.annotation.AnnotatedRuleWithMetaRuleAnnotation;
import io.homonoia.rules.annotation.Condition;
import io.homonoia.rules.annotation.Loop;
import io.homonoia.rules.annotation.Priority;
import io.homonoia.rules.annotation.Rule;
import io.homonoia.rules.api.Rules;
import org.junit.Test;

public class RuleProxyTest {

  @Test
  public void proxyingHappensEvenWhenRuleIsAnnotatedWithMetaRuleAnnotation() {
    // Given
    AnnotatedRuleWithMetaRuleAnnotation rule = new AnnotatedRuleWithMetaRuleAnnotation();

    // When
    io.homonoia.rules.api.Rule proxy = RuleProxy.asRule(rule);

    // Then
    assertNotNull(proxy.getDescription());
    assertNotNull(proxy.getName());
  }

  @Test
  public void asRuleForObjectThatImplementsRule() {
    Object rule = new BasicRule();
    io.homonoia.rules.api.Rule proxy = RuleProxy.asRule(rule);

    assertNotNull(proxy.getDescription());
    assertNotNull(proxy.getName());
  }

  @Test
  public void asRuleForObjectThatHasProxied() {
    Object rule = new DummyRule();
    io.homonoia.rules.api.Rule proxy1 = RuleProxy.asRule(rule);
    io.homonoia.rules.api.Rule proxy2 = RuleProxy.asRule(proxy1);

    assertEquals(proxy1.getDescription(), proxy2.getDescription());
    assertEquals(proxy1.getName(), proxy2.getName());
  }

  @Test(expected = IllegalArgumentException.class)
  public void asRuleForPojo() {
    Object rule = new Object();
    io.homonoia.rules.api.Rule proxy = RuleProxy.asRule(rule);
  }

  @Test
  public void invokeEquals() {

    Object rule = new DummyRule();
    io.homonoia.rules.api.Rule proxy1 = RuleProxy.asRule(rule);
    io.homonoia.rules.api.Rule proxy2 = RuleProxy.asRule(proxy1);
    io.homonoia.rules.api.Rule proxy3 = RuleProxy.asRule(proxy2);
    // @see Object#equals(Object) reflexive
    assertEquals(rule, rule);
    assertEquals(proxy1, proxy1);
    assertEquals(proxy2, proxy2);
    assertEquals(proxy3, proxy3);
    // @see Object#equals(Object) symmetric
    assertNotEquals(rule, proxy1);
    assertNotEquals(proxy1, rule);
    assertEquals(proxy1, proxy2);
    assertEquals(proxy2, proxy1);
    // @see Object#equals(Object) transitive consistent
    assertEquals(proxy1, proxy2);
    assertEquals(proxy2, proxy3);
    assertEquals(proxy3, proxy1);
    // @see Object#equals(Object) non-null
    assertNotEquals(rule, null);
    assertNotEquals(proxy1, null);
    assertNotEquals(proxy2, null);
    assertNotEquals(proxy3, null);
  }


  @Test
  public void invokeHashCode() {

    Object rule = new DummyRule();
    io.homonoia.rules.api.Rule proxy1 = RuleProxy.asRule(rule);
    io.homonoia.rules.api.Rule proxy2 = RuleProxy.asRule(proxy1);
    // @see Object#hashCode rule1
    assertEquals(proxy1.hashCode(), proxy1.hashCode());
    // @see Object#hashCode rule2
    assertEquals(proxy1, proxy2);
    assertEquals(proxy1.hashCode(), proxy2.hashCode());
    // @see Object#hashCode rule3
    assertNotEquals(rule, proxy1);
    assertNotEquals(rule.hashCode(), proxy1.hashCode());
  }

  @Test
  public void invokeToString() {

    Object rule = new DummyRule();
    io.homonoia.rules.api.Rule proxy1 = RuleProxy.asRule(rule);
    io.homonoia.rules.api.Rule proxy2 = RuleProxy.asRule(proxy1);

    assertEquals(proxy1.toString(), proxy1.toString());

    assertEquals(proxy1.toString(), proxy2.toString());

    assertEquals(rule.toString(), proxy1.toString());
  }

  @Test
  public void testCompareTo() {

    @Rule
    class MyComparableRule implements Comparable<MyComparableRule> {

      int comparisonCriteria;

      MyComparableRule(int comparisonCriteria) {
        this.comparisonCriteria = comparisonCriteria;
      }

      @Condition
      public boolean when() {
        return true;
      }

      @Action
      public void then() {
      }

      @Override
      public int compareTo(MyComparableRule otherRule) {
        return Integer.compare(comparisonCriteria, otherRule.comparisonCriteria);
      }
    }

    Object rule1 = new MyComparableRule(1);
    Object rule2 = new MyComparableRule(2);
    Object rule3 = new MyComparableRule(2);
    io.homonoia.rules.api.Rule proxy1 = RuleProxy.asRule(rule1);
    io.homonoia.rules.api.Rule proxy2 = RuleProxy.asRule(rule2);
    io.homonoia.rules.api.Rule proxy3 = RuleProxy.asRule(rule3);
    assertEquals(proxy1.compareTo(proxy2), -1);
    assertEquals(proxy2.compareTo(proxy1), 1);
    assertEquals(proxy2.compareTo(proxy3), 0);

    try {
      Rules rules = new Rules();
      rules.register(rule1, rule2);

      Rules mixedRules = new Rules(rule3);
      mixedRules.register(proxy1, proxy2);

      Rules yetAnotherRulesSet = new Rules(proxy1, proxy2);
      yetAnotherRulesSet.register(rule3);
    } catch (Exception exception) {
      fail("Should not fail with " + exception.getMessage());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCompareToWithIncorrectSignature() {

    @Rule
    class InvalidComparableRule {

      @Condition
      public boolean when() {
        return true;
      }

      @Action
      public void then() {
      }

      public int compareTo() {
        return 0;
      }
    }

    Object rule = new InvalidComparableRule();
    Rules rules = new Rules();
    rules.register(rule);
  }

  @Test
  public void testPriorityFromAnnotation() {

    @Rule(priority = 1)
    class MyRule {

      @Condition
      public boolean when() {
        return true;
      }

      @Action
      public void then() {
      }
    }

    Object rule = new MyRule();
    io.homonoia.rules.api.Rule proxy = RuleProxy.asRule(rule);
    assertEquals(1, proxy.getPriority());
  }

  @Test
  public void testPriorityFromMethod() {

    @Rule
    class MyRule {

      @Condition
      public boolean when() {
        return true;
      }

      @Action
      public void then() {
      }

      @Priority
      public int getPriority() {
        return 2;
      }
    }

    Object rule = new MyRule();
    io.homonoia.rules.api.Rule proxy = RuleProxy.asRule(rule);
    assertEquals(2, proxy.getPriority());
  }

  @Test
  public void testPriorityPrecedence() {

    @Rule(priority = 1)
    class MyRule {

      @Condition
      public boolean when() {
        return true;
      }

      @Action
      public void then() {
      }

      @Priority
      public int getPriority() {
        return 2;
      }
    }

    Object rule = new MyRule();
    io.homonoia.rules.api.Rule proxy = RuleProxy.asRule(rule);
    assertEquals(2, proxy.getPriority());
  }

  @Test
  public void testDefaultPriority() {

    @Rule
    class MyRule {

      @Condition
      public boolean when() {
        return true;
      }

      @Action
      public void then() {
      }
    }

    Object rule = new MyRule();
    io.homonoia.rules.api.Rule proxy = RuleProxy.asRule(rule);
    assertEquals(io.homonoia.rules.api.Rule.DEFAULT_PRIORITY, proxy.getPriority());
  }

  @Test
  public void testLoopFromAnnotation() {

    @Rule(loop = false)
    class MyRule {

      @Condition
      public boolean when() {
        return true;
      }

      @Action
      public void then() {
      }
    }

    Object rule = new MyRule();
    io.homonoia.rules.api.Rule proxy = RuleProxy.asRule(rule);
    assertEquals(false, proxy.getLoop());
  }

  @Test
  public void testLoopFromMethod() {

    @Rule
    class MyRule {

      @Condition
      public boolean when() {
        return true;
      }

      @Action
      public void then() {
      }

      @Loop
      public boolean getLoop() {
        return false;
      }
    }

    Object rule = new MyRule();
    io.homonoia.rules.api.Rule proxy = RuleProxy.asRule(rule);
    assertEquals(false, proxy.getLoop());
  }

  @Test
  public void testLoopPrecedence() {

    @Rule(loop = false)
    class MyRule {

      @Condition
      public boolean when() {
        return true;
      }

      @Action
      public void then() {
      }

      @Loop
      public boolean getLoop() {
        return true;
      }
    }

    Object rule = new MyRule();
    io.homonoia.rules.api.Rule proxy = RuleProxy.asRule(rule);
    assertEquals(true, proxy.getLoop());
  }

  @Test
  public void testDefaultLoop() {

    @Rule
    class MyRule {

      @Condition
      public boolean when() {
        return true;
      }

      @Action
      public void then() {
      }
    }

    Object rule = new MyRule();
    io.homonoia.rules.api.Rule proxy = RuleProxy.asRule(rule);
    assertEquals(io.homonoia.rules.api.Rule.DEFAULT_LOOP, proxy.getLoop());
  }

  @Rule
  static class DummyRule {

    @Condition
    public boolean when() {
      return true;
    }

    @Action
    public void then() {
    }

    @Override
    public String toString() {
      return "I am a Dummy rule";
    }

  }

}
