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

package io.homonoia.rules.tutorials.shop;

import io.homonoia.rules.mvel.MVELRule;
import io.homonoia.rules.mvel.MVELRuleFactory;
import io.homonoia.rules.support.reader.YamlRuleDefinitionReader;
import io.homonoia.rules.api.Facts;
import io.homonoia.rules.api.Rule;
import io.homonoia.rules.api.Rules;
import io.homonoia.rules.api.RulesEngine;
import io.homonoia.rules.core.DefaultRulesEngine;

import java.io.FileReader;

public class Launcher {

    public static void main(String[] args) throws Exception {
        //create a person instance (fact)
        Person tom = new Person("Tom", 14);
        Facts facts = new Facts();
        facts.put("person", tom);

        // create rules
        MVELRule ageRule = new MVELRule()
                .name("age rule")
                .description("Check if person's age is > 18 and mark the person as adult")
                .priority(1)
                .when("person.age > 18")
                .then("person.setAdult(true);");
        MVELRuleFactory ruleFactory = new MVELRuleFactory(new YamlRuleDefinitionReader());
        String fileName = args.length != 0 ? args[0] : "simple-rules-tutorials/src/main/java/org/homonoia/rules/tutorials/shop/alcohol-rule.yml";
        Rule alcoholRule = ruleFactory.createRule(new FileReader(fileName));

        // create a rule set
        Rules rules = new Rules();
        rules.register(ageRule);
        rules.register(alcoholRule);

        //create a default rules engine and fire rules on known facts
        RulesEngine rulesEngine = new DefaultRulesEngine();

        System.out.println("Tom: Hi! can I have some Vodka please?");
        rulesEngine.fire(rules, facts);
    }

}
