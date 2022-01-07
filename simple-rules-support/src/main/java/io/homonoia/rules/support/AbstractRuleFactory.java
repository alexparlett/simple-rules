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

package io.homonoia.rules.support;

import io.homonoia.rules.api.Rule;
import io.homonoia.rules.support.composite.ActivationRuleGroup;
import io.homonoia.rules.support.composite.CompositeRule;
import io.homonoia.rules.support.composite.ConditionalRuleGroup;
import io.homonoia.rules.support.composite.UnitRuleGroup;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for rule factories.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public abstract class AbstractRuleFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRuleFactory.class);

  private static final List<String> ALLOWED_COMPOSITE_RULE_TYPES = Arrays.asList(
      UnitRuleGroup.class.getSimpleName(),
      ConditionalRuleGroup.class.getSimpleName(),
      ActivationRuleGroup.class.getSimpleName()
  );

  protected Rule createRule(RuleDefinition ruleDefinition) {
    if (ruleDefinition.isCompositeRule()) {
      return createCompositeRule(ruleDefinition);
    } else {
      return createSimpleRule(ruleDefinition);
    }
  }

  protected abstract Rule createSimpleRule(RuleDefinition ruleDefinition);

  protected Rule createCompositeRule(RuleDefinition ruleDefinition) {
    if (ruleDefinition.getCondition() != null) {
      LOGGER.warn(
          "Condition '{}' in composite rule '{}' of type {} will be ignored.",
          ruleDefinition.getCondition(),
          ruleDefinition.getName(),
          ruleDefinition.getCompositeRuleType());
    }
    if (ruleDefinition.getActions() != null && !ruleDefinition.getActions().isEmpty()) {
      LOGGER.warn(
          "Actions '{}' in composite rule '{}' of type {} will be ignored.",
          ruleDefinition.getActions(),
          ruleDefinition.getName(),
          ruleDefinition.getCompositeRuleType());
    }
    CompositeRule compositeRule;
    String name = ruleDefinition.getName();
    switch (ruleDefinition.getCompositeRuleType()) {
      case "UnitRuleGroup":
        compositeRule = new UnitRuleGroup(name);
        break;
      case "ActivationRuleGroup":
        compositeRule = new ActivationRuleGroup(name);
        break;
      case "ConditionalRuleGroup":
        compositeRule = new ConditionalRuleGroup(name);
        break;
      default:
        throw new IllegalArgumentException(
            "Invalid composite rule type, must be one of " + ALLOWED_COMPOSITE_RULE_TYPES);
    }
    compositeRule.setDescription(ruleDefinition.getDescription());
    compositeRule.setPriority(ruleDefinition.getPriority());
    compositeRule.setLoop(ruleDefinition.getLoop());

    for (RuleDefinition composingRuleDefinition : ruleDefinition.getComposingRules()) {
      compositeRule.addRule(createRule(composingRuleDefinition));
    }

    return compositeRule;
  }

}
