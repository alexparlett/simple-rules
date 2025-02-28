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


import io.homonoia.rules.annotation.AnnotatedRuleWithActionMethodHavingMoreThanOneArgumentOfTypeFacts;
import io.homonoia.rules.annotation.AnnotatedRuleWithActionMethodHavingOneArgumentNotOfTypeFacts;
import io.homonoia.rules.annotation.AnnotatedRuleWithActionMethodHavingOneArgumentOfTypeFacts;
import io.homonoia.rules.annotation.AnnotatedRuleWithActionMethodThatReturnsNonVoidType;
import io.homonoia.rules.annotation.AnnotatedRuleWithConditionMethodHavingNonBooleanReturnType;
import io.homonoia.rules.annotation.AnnotatedRuleWithConditionMethodHavingOneArgumentNotOfTypeFacts;
import io.homonoia.rules.annotation.AnnotatedRuleWithLoopMethodHavingArguments;
import io.homonoia.rules.annotation.AnnotatedRuleWithLoopMethodHavingNonBooleanReturnType;
import io.homonoia.rules.annotation.AnnotatedRuleWithMetaRuleAnnotation;
import io.homonoia.rules.annotation.AnnotatedRuleWithMoreThanOneLoopMethod;
import io.homonoia.rules.annotation.AnnotatedRuleWithMoreThanOnePriorityMethod;
import io.homonoia.rules.annotation.AnnotatedRuleWithMultipleAnnotatedParametersAndOneParameterOfSubTypeFacts;
import io.homonoia.rules.annotation.AnnotatedRuleWithMultipleAnnotatedParametersAndOneParameterOfTypeFacts;
import io.homonoia.rules.annotation.AnnotatedRuleWithNonPublicActionMethod;
import io.homonoia.rules.annotation.AnnotatedRuleWithNonPublicConditionMethod;
import io.homonoia.rules.annotation.AnnotatedRuleWithNonPublicLoopMethod;
import io.homonoia.rules.annotation.AnnotatedRuleWithNonPublicPriorityMethod;
import io.homonoia.rules.annotation.AnnotatedRuleWithOneParameterNotAnnotatedWithFactAndNotOfTypeFacts;
import io.homonoia.rules.annotation.AnnotatedRuleWithPriorityMethodHavingArguments;
import io.homonoia.rules.annotation.AnnotatedRuleWithPriorityMethodHavingNonIntegerReturnType;
import io.homonoia.rules.annotation.AnnotatedRuleWithoutActionMethod;
import io.homonoia.rules.annotation.AnnotatedRuleWithoutConditionMethod;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class RuleDefinitionValidatorTest {

  private RuleDefinitionValidator ruleDefinitionValidator;

  @Before
  public void setup() {
    ruleDefinitionValidator = new RuleDefinitionValidator();
  }

  /*
   * Rule annotation test
   */
  @Test(expected = IllegalArgumentException.class)
  public void notAnnotatedRuleMustNotBeAccepted() {
    ruleDefinitionValidator.validateRuleDefinition(new Object());
  }

  @Test
  public void withCustomAnnotationThatIsItselfAnnotatedWithTheRuleAnnotation() {
    ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithMetaRuleAnnotation());
  }

  /*
   * Conditions methods tests
   */
  @Test(expected = IllegalArgumentException.class)
  public void conditionMethodMustBeDefined() {
    ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithoutConditionMethod());
  }

  @Test(expected = IllegalArgumentException.class)
  public void conditionMethodMustBePublic() {
    ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithNonPublicConditionMethod());
  }

  @Test(expected = IllegalArgumentException.class)
  public void whenConditionMethodHasOneNonAnnotatedParameter_thenThisParameterMustBeOfTypeFacts() {
    ruleDefinitionValidator.validateRuleDefinition(
        new AnnotatedRuleWithConditionMethodHavingOneArgumentNotOfTypeFacts());
  }

  @Test(expected = IllegalArgumentException.class)
  public void conditionMethodMustReturnBooleanType() {
    ruleDefinitionValidator
        .validateRuleDefinition(new AnnotatedRuleWithConditionMethodHavingNonBooleanReturnType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void conditionMethodParametersShouldAllBeAnnotatedWithFactUnlessExactlyOneOfThemIsOfTypeFacts() {
    ruleDefinitionValidator.validateRuleDefinition(
        new AnnotatedRuleWithOneParameterNotAnnotatedWithFactAndNotOfTypeFacts());
  }

  /*
   * Action method tests
   */
  @Test(expected = IllegalArgumentException.class)
  public void actionMethodMustBeDefined() {
    ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithoutActionMethod());
  }

  @Test(expected = IllegalArgumentException.class)
  public void actionMethodMustBePublic() {
    ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithNonPublicActionMethod());
  }

  @Test(expected = IllegalArgumentException.class)
  public void actionMethodMustHaveAtMostOneArgumentOfTypeFacts() {
    ruleDefinitionValidator
        .validateRuleDefinition(new AnnotatedRuleWithActionMethodHavingOneArgumentNotOfTypeFacts());
  }

  @Test(expected = IllegalArgumentException.class)
  public void actionMethodMustHaveExactlyOneArgumentOfTypeFactsIfAny() {
    ruleDefinitionValidator.validateRuleDefinition(
        new AnnotatedRuleWithActionMethodHavingMoreThanOneArgumentOfTypeFacts());
  }

  @Test(expected = IllegalArgumentException.class)
  public void actionMethodMustReturnVoid() {
    ruleDefinitionValidator
        .validateRuleDefinition(new AnnotatedRuleWithActionMethodThatReturnsNonVoidType());
  }

  @Test(expected = IllegalArgumentException.class)
  public void actionMethodParametersShouldAllBeAnnotatedWithFactUnlessExactlyOneOfThemIsOfTypeFacts() {
    ruleDefinitionValidator.validateRuleDefinition(
        new AnnotatedRuleWithOneParameterNotAnnotatedWithFactAndNotOfTypeFacts());
  }

  /*
   * Priority method tests
   */

  @Test(expected = IllegalArgumentException.class)
  public void priorityMethodMustBeUnique() {
    ruleDefinitionValidator
        .validateRuleDefinition(new AnnotatedRuleWithMoreThanOnePriorityMethod());
  }

  @Test(expected = IllegalArgumentException.class)
  public void priorityMethodMustBePublic() {
    ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithNonPublicPriorityMethod());
  }

  @Test(expected = IllegalArgumentException.class)
  public void priorityMethodMustHaveNoArguments() {
    ruleDefinitionValidator
        .validateRuleDefinition(new AnnotatedRuleWithPriorityMethodHavingArguments());
  }

  @Test(expected = IllegalArgumentException.class)
  public void priorityMethodReturnTypeMustBeInteger() {
    ruleDefinitionValidator
        .validateRuleDefinition(new AnnotatedRuleWithPriorityMethodHavingNonIntegerReturnType());
  }

  /*
   * Loop method tests
   */

  @Test(expected = IllegalArgumentException.class)
  public void loopMethodMustBeUnique() {
    ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithMoreThanOneLoopMethod());
  }

  @Test(expected = IllegalArgumentException.class)
  public void loopMethodMustBePublic() {
    ruleDefinitionValidator.validateRuleDefinition(new AnnotatedRuleWithNonPublicLoopMethod());
  }

  @Test(expected = IllegalArgumentException.class)
  public void loopMethodMustHaveNoArguments() {
    ruleDefinitionValidator
        .validateRuleDefinition(new AnnotatedRuleWithLoopMethodHavingArguments());
  }

  @Test(expected = IllegalArgumentException.class)
  public void loopMethodReturnTypeMustBeInteger() {
    ruleDefinitionValidator
        .validateRuleDefinition(new AnnotatedRuleWithLoopMethodHavingNonBooleanReturnType());
  }

  /*
   * Valid definition tests
   */
  @Test
  public void validAnnotationsShouldBeAccepted() {
    try {
      ruleDefinitionValidator.validateRuleDefinition(
          new AnnotatedRuleWithMultipleAnnotatedParametersAndOneParameterOfTypeFacts());
      ruleDefinitionValidator.validateRuleDefinition(
          new AnnotatedRuleWithMultipleAnnotatedParametersAndOneParameterOfSubTypeFacts());
      ruleDefinitionValidator
          .validateRuleDefinition(new AnnotatedRuleWithActionMethodHavingOneArgumentOfTypeFacts());
    } catch (Throwable throwable) {
      Assertions.fail("Should not throw exception for valid rule definitions");
    }
  }
}
