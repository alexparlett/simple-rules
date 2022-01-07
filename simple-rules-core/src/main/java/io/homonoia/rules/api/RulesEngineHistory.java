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

package io.homonoia.rules.api;

import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

public class RulesEngineHistory implements RuleListener, RulesEngineListener {

    private final MultiValuedMap<Rule, RuleExecutionStatus> executionStatus;

    public RulesEngineHistory() {
        this.executionStatus = new ArrayListValuedHashMap<>();
    }

    public RulesEngineHistory(final MultiValuedMap<Rule, RuleExecutionStatus> other) {
        this.executionStatus = new ArrayListValuedHashMap<>(other);
    }

    public MultiValuedMap<Rule, RuleExecutionStatus> getExecutionStatus() {
        return MultiMapUtils.unmodifiableMultiValuedMap(executionStatus);
    }

    @Override
    public void afterEvaluate(final Rule rule, final Facts facts, final boolean evaluationResult) {
        if (!evaluationResult) {
            executionStatus.put(rule, RuleExecutionStatus.SKIPPED);
        }
    }

    @Override
    public void onEvaluationError(final Rule rule, final Facts facts, final Exception exception) {
        executionStatus.put(rule, RuleExecutionStatus.EVALUATION_FAILURE);
    }

    @Override
    public void onSuccess(final Rule rule, final Facts facts) {
        executionStatus.put(rule, RuleExecutionStatus.EXECUTED);
    }

    @Override
    public void onFailure(final Rule rule, final Facts facts, final Exception exception) {
        executionStatus.put(rule, RuleExecutionStatus.EXECUTION_FAILURE);
    }

    @Override
    public void beforeEvaluate(final Rules rules, final Facts facts) {
        executionStatus.clear();
        rules.forEach(rule -> executionStatus.put(rule, RuleExecutionStatus.NOT_EVALUATED));
    }
}
