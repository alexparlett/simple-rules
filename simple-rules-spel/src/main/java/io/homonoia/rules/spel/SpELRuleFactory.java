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

import io.homonoia.rules.api.Rule;
import io.homonoia.rules.api.Rules;
import io.homonoia.rules.support.AbstractRuleFactory;
import io.homonoia.rules.support.RuleDefinition;
import io.homonoia.rules.support.reader.RuleDefinitionReader;
import java.io.Reader;
import java.util.List;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ParserContext;

/**
 * Factory to create {@link SpELRule} instances.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class SpELRuleFactory extends AbstractRuleFactory {

  private final RuleDefinitionReader reader;
  private final BeanResolver beanResolver;
  private final ParserContext parserContext;

  /**
   * Create a new {@link SpELRuleFactory} with a given reader.
   *
   * @param reader used to read rule definitions
   * @see io.homonoia.rules.support.reader.YamlRuleDefinitionReader
   * @see io.homonoia.rules.support.reader.JsonRuleDefinitionReader
   */
  public SpELRuleFactory(RuleDefinitionReader reader) {
    this(reader, null, null);
  }

  /**
   * Create a new {@link SpELRuleFactory} with a given reader.
   *
   * @param reader        used to read rule definitions
   * @param parserContext used to parse SpEL expressions
   * @see io.homonoia.rules.support.reader.YamlRuleDefinitionReader
   * @see io.homonoia.rules.support.reader.JsonRuleDefinitionReader
   */
  public SpELRuleFactory(RuleDefinitionReader reader, ParserContext parserContext) {
    this(reader, parserContext, null);
  }

  /**
   * Create a new {@link SpELRuleFactory} with a given reader.
   *
   * @param reader       used to read rule definitions
   * @param beanResolver used to resolve bean references in SpEL expressions
   * @see io.homonoia.rules.support.reader.YamlRuleDefinitionReader
   * @see io.homonoia.rules.support.reader.JsonRuleDefinitionReader
   */
  public SpELRuleFactory(RuleDefinitionReader reader, BeanResolver beanResolver) {
    this(reader, null, beanResolver);
  }

  /**
   * Create a new {@link SpELRuleFactory} with a given reader.
   *
   * @param reader        used to read rule definitions
   * @param parserContext used to parse SpEL expressions
   * @param beanResolver  used to resolve bean references in SpEL expressions
   * @see io.homonoia.rules.support.reader.YamlRuleDefinitionReader
   * @see io.homonoia.rules.support.reader.JsonRuleDefinitionReader
   */
  public SpELRuleFactory(RuleDefinitionReader reader, ParserContext parserContext,
      BeanResolver beanResolver) {
    this.reader = reader;
    this.parserContext = parserContext;
    this.beanResolver = beanResolver;
  }

  /**
   * Create a new {@link SpELRule} from a Reader.
   * <p>
   * The rule descriptor should contain a single rule definition. If no rule definitions are found,
   * a {@link IllegalArgumentException} will be thrown. If more than a rule is defined in the
   * descriptor, the first rule will be returned.
   *
   * @param ruleDescriptor descriptor of rule definition
   * @return a new rule
   * @throws Exception if unable to create the rule from the descriptor
   */
  public Rule createRule(Reader ruleDescriptor) throws Exception {
    List<RuleDefinition> ruleDefinitions = reader.read(ruleDescriptor);
    if (ruleDefinitions.isEmpty()) {
      throw new IllegalArgumentException("rule descriptor is empty");
    }
    return createRule(ruleDefinitions.get(0));
  }

  /**
   * Create a set of {@link SpELRule} from a Reader.
   *
   * @param rulesDescriptor descriptor of rule definitions
   * @return a set of rules
   * @throws Exception if unable to create rules from the descriptor
   */
  public Rules createRules(Reader rulesDescriptor) throws Exception {
    Rules rules = new Rules();
    List<RuleDefinition> ruleDefinitions = reader.read(rulesDescriptor);
    for (RuleDefinition ruleDefinition : ruleDefinitions) {
      rules.register(createRule(ruleDefinition));
    }
    return rules;
  }

  protected Rule createSimpleRule(RuleDefinition ruleDefinition) {
    SpELRule spELRule = new SpELRule(beanResolver, parserContext)
        .name(ruleDefinition.getName())
        .description(ruleDefinition.getDescription())
        .priority(ruleDefinition.getPriority())
        .loop(ruleDefinition.getLoop())
        .when(ruleDefinition.getCondition());
    for (String action : ruleDefinition.getActions()) {
      spELRule.then(action);
    }
    return spELRule;
  }

}
