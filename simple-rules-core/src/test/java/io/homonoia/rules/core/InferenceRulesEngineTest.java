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

import io.homonoia.rules.annotation.Action;
import io.homonoia.rules.annotation.Condition;
import io.homonoia.rules.annotation.Fact;
import io.homonoia.rules.annotation.Rule;
import io.homonoia.rules.annotation.*;
import io.homonoia.rules.api.*;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class InferenceRulesEngineTest {

    @Test(expected = NullPointerException.class)
    public void whenFireRules_thenNullRulesShouldNotBeAccepted() {
        InferenceRulesEngine engine = new InferenceRulesEngine();
        engine.fire(null, new Facts());
    }

    @Test(expected = NullPointerException.class)
    public void whenFireRules_thenNullFactsShouldNotBeAccepted() {
        InferenceRulesEngine engine = new InferenceRulesEngine();
        engine.fire(new Rules(), null);
    }

    @Test(expected = NullPointerException.class)
    public void whenCheckRules_thenNullRulesShouldNotBeAccepted() {
        InferenceRulesEngine engine = new InferenceRulesEngine();
        engine.check(null, new Facts());
    }

    @Test(expected = NullPointerException.class)
    public void whenCheckRules_thenNullFactsShouldNotBeAccepted() {
        InferenceRulesEngine engine = new InferenceRulesEngine();
        engine.check(new Rules(), null);
    }

    @Test
    public void testCandidateSelection() {
        // Given
        Facts facts = new Facts();
        facts.put("foo", true);
        DummyRule dummyRule = new DummyRule();
        AnotherDummyRule anotherDummyRule = new AnotherDummyRule();
        Rules rules = new Rules(dummyRule, anotherDummyRule);
        RulesEngine rulesEngine = new InferenceRulesEngine();

        // When
        rulesEngine.fire(rules, facts);

        // Then
        assertThat(dummyRule.isExecuted()).isTrue();
        assertThat(anotherDummyRule.isExecuted()).isFalse();
    }

    @Test
    public void testCandidateLoop() throws Exception {
        // Given
        Facts facts = new Facts();

        BasicRule dummyRule = Mockito.spy(new BasicRule("Dummy Rule", "Dummy Rule", 1, true));
        BasicRule anotherDummyRule = Mockito.spy(new BasicRule("Another Dummy Rule", "Another Dummy Rule", 2, true));

        when(dummyRule.evaluate(any()))
                .thenReturn(true, false);

        when(anotherDummyRule.evaluate(any()))
                .thenReturn(true, true, false);

        Rules rules = new Rules(dummyRule, anotherDummyRule);
        RulesEngine rulesEngine = new InferenceRulesEngine();

        // When
        rulesEngine.fire(rules, facts);

        // Then
        verify(dummyRule, times(1)).execute(any());
        verify(anotherDummyRule, times(2)).execute(any());

        assertThat(rulesEngine.getHistory()).isNotNull();
        assertThat(rulesEngine.getHistory().getExecutionStatus()).isNotNull();
        assertThat(rulesEngine.getHistory().getExecutionStatus().size()).isEqualTo(6);
        assertThat(rulesEngine.getHistory().getExecutionStatus().asMap())
                .hasEntrySatisfying(dummyRule, executionStatuses -> assertThat(executionStatuses).containsExactly(RuleExecutionStatus.EXECUTED, RuleExecutionStatus.SKIPPED, RuleExecutionStatus.SKIPPED));
        assertThat(rulesEngine.getHistory().getExecutionStatus().asMap())
                .hasEntrySatisfying(anotherDummyRule, executionStatuses -> assertThat(executionStatuses).containsExactly(RuleExecutionStatus.EXECUTED, RuleExecutionStatus.EXECUTED, RuleExecutionStatus.SKIPPED));

    }

    @Test
    public void testCandidateOrdering() {
        // Given
        Facts facts = new Facts();
        facts.put("foo", true);
        facts.put("bar", true);
        DummyRule dummyRule = new DummyRule();
        AnotherDummyRule anotherDummyRule = new AnotherDummyRule();
        Rules rules = new Rules(dummyRule, anotherDummyRule);
        RulesEngine rulesEngine = new InferenceRulesEngine();

        // When
        rulesEngine.fire(rules, facts);

        // Then
        assertThat(dummyRule.isExecuted()).isTrue();
        assertThat(anotherDummyRule.isExecuted()).isTrue();
        assertThat(dummyRule.getTimestamp()).isLessThanOrEqualTo(anotherDummyRule.getTimestamp());
    }

    @Test
    public void testRulesEngineListener() {
        // Given
        class StubRulesEngineListener implements RulesEngineListener {

            private boolean executedBeforeEvaluate;
            private boolean executedAfterExecute;

            @Override
            public void beforeEvaluate(Rules rules, Facts facts) {
                executedBeforeEvaluate = true;
            }

            @Override
            public void afterExecute(Rules rules, Facts facts) {
                executedAfterExecute = true;
            }

            private boolean isExecutedBeforeEvaluate() {
                return executedBeforeEvaluate;
            }

            private boolean isExecutedAfterExecute() {
                return executedAfterExecute;
            }
        }

        Facts facts = new Facts();
        facts.put("foo", true);
        DummyRule rule = new DummyRule();
        Rules rules = new Rules(rule);
        StubRulesEngineListener rulesEngineListener = new StubRulesEngineListener();

        // When
        InferenceRulesEngine rulesEngine = new InferenceRulesEngine();
        rulesEngine.registerRulesEngineListener(rulesEngineListener);
        rulesEngine.fire(rules, facts);

        // Then
        // Rules engine listener should be invoked
        assertThat(rulesEngineListener.isExecutedBeforeEvaluate()).isTrue();
        assertThat(rulesEngineListener.isExecutedAfterExecute()).isTrue();
        assertThat(rule.isExecuted()).isTrue();
    }

    @Rule
    static class DummyRule {

        private boolean isExecuted;
        private long timestamp;

        @Condition
        public boolean when(@Fact("foo") boolean foo) {
            return foo;
        }

        @Action
        public void then(Facts facts) {
            isExecuted = true;
            timestamp = System.currentTimeMillis();
            facts.remove("foo");
        }

        @Priority
        public int priority() {
            return 1;
        }

        public boolean isExecuted() {
            return isExecuted;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    @Rule
    static class AnotherDummyRule {

        private boolean isExecuted;
        private long timestamp;

        @Condition
        public boolean when(@Fact("bar") boolean bar) {
            return bar;
        }

        @Action
        public void then(Facts facts) {
            isExecuted = true;
            timestamp = System.currentTimeMillis();
            facts.remove("bar");
        }

        @Priority
        public int priority() {
            return 2;
        }

        public boolean isExecuted() {
            return isExecuted;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

}
