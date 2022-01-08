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
