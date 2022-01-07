***

<div align="center">
    <b><em>Simple Rules</em></b><br>
    The simple, stupid rules engine for Java&trade;
</div>

<div align="center">

[![MIT license](http://img.shields.io/badge/license-MIT-brightgreen.svg?style=flat)](http://opensource.org/licenses/MIT)
[![Build Status](https://github.com/alexparlett/simple-rules/workflows/Java%20CI/badge.svg)](https://github.com/alexparlett/simple-rules/actions)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.homonoia/simple-rules-core/badge.svg?style=flat)](http://search.maven.org/#artifactdetails|org.homonoia|simple-rules-core|4.1.0|)
[![Javadoc](https://www.javadoc.io/badge/io.homonoia/simple-rules-core.svg)](http://www.javadoc.io/doc/io.homonoia/simple-rules-core)
[![Project status](https://img.shields.io/badge/Project%20status-Active-brightgreen)](https://img.shields.io/badge/Project%20status-Active-brightgreen)

</div>

***

## Latest news

* 06/12/2020: Version 4.1 is out with a new module to support [Apache JEXL](https://commons.apache.org/proper/commons-jexl/) as an additional supported expression language! You can find all details about other changes in the [release notes](https://github.com/alexparlett/simple-rules/releases).

## What is Simple Rules?

Simple Rules is a Java rules engine inspired by an article called *"[Should I use a Rules Engine?](http://martinfowler.com/bliki/RulesEngine.html)"* of [Martin Fowler](http://martinfowler.com/) in which Martin says:

> You can build a simple rules engine yourself. All you need is to create a bunch of objects with conditions and actions, store them in a collection, and run through them to evaluate the conditions and execute the actions.

This is exactly what Simple Rules does, it provides the `Rule` abstraction to create rules with conditions and actions, and the `RulesEngine` API that runs through a set of rules to evaluate conditions and execute actions.

## Core features

 * Lightweight library and easy to learn API
 * POJO based development with an annotation programming model
 * Useful abstractions to define business rules and apply them easily with Java
 * The ability to create composite rules from primitive ones
 * The ability to define rules using an Expression Language (Like MVEL, SpEL and JEXL)

## Example

### 1. First, define your rule..

#### Either in a declarative way using annotations:

```java
@Rule(name = "weather rule", description = "if it rains then take an umbrella")
public class WeatherRule {

    @Condition
    public boolean itRains(@Fact("rain") boolean rain) {
        return rain;
    }
    
    @Action
    public void takeAnUmbrella() {
        System.out.println("It rains, take an umbrella!");
    }
}
```

#### Or in a programmatic way with a fluent API:

```java
Rule weatherRule = new RuleBuilder()
        .name("weather rule")
        .description("if it rains then take an umbrella")
        .when(facts -> facts.get("rain").equals(true))
        .then(facts -> System.out.println("It rains, take an umbrella!"))
        .build();
```

#### Or using an Expression Language:

```java
Rule weatherRule = new MVELRule()
        .name("weather rule")
        .description("if it rains then take an umbrella")
        .when("rain == true")
        .then("System.out.println(\"It rains, take an umbrella!\");");
```

#### Or using a rule descriptor:

Like in the following `weather-rule.yml` example file:

```yaml
name: "weather rule"
description: "if it rains then take an umbrella"
condition: "rain == true"
actions:
  - "System.out.println(\"It rains, take an umbrella!\");"
```

```java
MVELRuleFactory ruleFactory = new MVELRuleFactory(new YamlRuleDefinitionReader());
Rule weatherRule = ruleFactory.createRule(new FileReader("weather-rule.yml"));
```

### 2. Then, fire it!

```java
public class Test {
    public static void main(String[] args) {
        // define facts
        Facts facts = new Facts();
        facts.put("rain", true);

        // define rules
        Rule weatherRule = ...
        Rules rules = new Rules();
        rules.register(weatherRule);

        // fire rules on known facts
        RulesEngine rulesEngine = new DefaultRulesEngine();
        rulesEngine.fire(rules, facts);
    }
}
```

This is the hello world of Simple Rules. You can find other examples like the [Shop](https://github.com/alexparlett/simple-rules/wiki/shop), [Airco](https://github.com/alexparlett/simple-rules/wiki/air-conditioning) or [WebApp](https://github.com/alexparlett/simple-rules/wiki/web-app) tutorials in the wiki.

This is built off the fantastic work of [Easy Rules](https://github.com/j-easy/easy-rules) by Mahmoud Ben Hassine and forked to define differences in the api and bring out of maintenance mode.
 

## Contribution

You are welcome to contribute to the project with pull requests on GitHub.

If you believe you found a bug or have any question, please use the [issue tracker](https://github.com/alexparlett/simple-rules/issues).

## Credits

![YourKit Java Profiler](https://www.yourkit.com/images/yklogo.png)

Many thanks to [YourKit, LLC](https://www.yourkit.com/) for providing a free license of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/index.jsp) to support the development of Easy Rules.

## License

Simple Rules has been released under the terms of the MIT license:

```
The MIT License (MIT)

Copyright (c) 2021 Alex Parlett (alex.parlett@homonoia-studios.co.uk)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
