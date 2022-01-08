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

import io.homonoia.rules.api.Facts;
import io.homonoia.rules.api.Rule;
import io.homonoia.rules.api.Rules;
import io.homonoia.rules.api.RulesEngine;
import io.homonoia.rules.core.DefaultRulesEngine;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SpELRulesEngineTest {

    @Test
    public void testFactEvaluateAfterInsert() {
        //Given
        RulesEngine rulesEngine = new DefaultRulesEngine();

        Facts facts = new Facts();
        facts.put("rule1", true);

        Rule rule1 = new SpELRule()
                .name("rule1")
                .description("rule1")
                .priority(1)
                .loop(false)
                .when("#rule1 == true")
                .then("put('rule2', true)");

        Rule rule2 = new SpELRule()
                .name("rule2")
                .description("rule2")
                .priority(2)
                .loop(false)
                .when("#rule2 == true")
                .then("put('passed', true)");

        Rules rules = new Rules();
        rules.register(rule1, rule2);

        //When
        rulesEngine.fire(rules, facts);

        //Then
        assertThat(rule1.hasFired()).isTrue();
        assertThat(rule2.hasFired()).isTrue();

        assertThat(facts.getFact("rule1").getValue()).isEqualTo(true);
        assertThat(facts.getFact("rule2").getValue()).isEqualTo(true);
        assertThat(facts.getFact("passed").getValue()).isEqualTo(true);
    }


}
