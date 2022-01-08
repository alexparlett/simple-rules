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
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SpELRuleTest {

  @Test
  public void whenTheRuleIsTriggered_thenConditionShouldBeEvaluated() {
    // given
    Person person = new Person("foo", 20);
    Facts facts = new Facts();
    facts.put("person", person);

    SpELRule spelRule = new SpELRule().name("spel rule").description("rule using SpEL").priority(1)
        .when("#person.age > 18")
        .then("#person.setAdult(true)");

    // when
    boolean evaluationResult = spelRule.evaluate(facts);

    // then
    assertThat(evaluationResult).isTrue();
  }

  @Test
  public void whenTheConditionIsTrue_thenActionsShouldBeExecuted() throws Exception {
    // given
    Person foo = new Person("foo", 20);
    Facts facts = new Facts();
    facts.put("person", foo);

    SpELRule spelRule = new SpELRule().name("spel rule").description("rule using SpEL").priority(1)
        .when("#person.age > 18")
        .then("#person.setAdult(true)");

    // when
    spelRule.execute(facts);

    // then
    assertThat(foo.isAdult()).isTrue();
  }

  @Test
  public void whenTheConditionIsTrue_thenActionsShouldBeExecutedAndFactsUpdatedViaPut() throws Exception {
    // given
    Person foo = new Person("foo", 20);
    Facts facts = new Facts();
    facts.put("person", foo);

    SpELRule spelRule = new SpELRule().name("spel rule").description("rule using SpEL").priority(1)
        .when("#person.age > 18")
        .then("put(\"person2\", new io.homonoia.rules.spel.Person(\"bar\", 17))");

    // when
    spelRule.execute(facts);

    // then
    assertThat(facts.getFact("person2")).isNotNull();

    final Person person2 = facts.getFact("person2").getValue(Person.class);
    assertThat(person2.getName()).isEqualTo("bar");
    assertThat(person2.getAge()).isEqualTo(17);
  }

}
