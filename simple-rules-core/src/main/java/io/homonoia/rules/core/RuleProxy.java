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

import static java.lang.String.format;

import io.homonoia.rules.annotation.Action;
import io.homonoia.rules.annotation.Condition;
import io.homonoia.rules.annotation.Fact;
import io.homonoia.rules.annotation.Loop;
import io.homonoia.rules.annotation.Priority;
import io.homonoia.rules.annotation.Rule;
import io.homonoia.rules.api.Facts;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class to create rule proxies from annotated objects.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 */
public class RuleProxy implements InvocationHandler {

  private final Object target;
  private String name;
  private String description;
  private Integer priority;
  private Boolean loop;
  private Method[] methods;
  private Method conditionMethod;
  private Set<ActionMethodOrderBean> actionMethods;
  private Method compareToMethod;
  private Method toStringMethod;
  private Rule annotation;

  private static final RuleDefinitionValidator ruleDefinitionValidator = new RuleDefinitionValidator();
  private static final Logger LOGGER = LoggerFactory.getLogger(RuleProxy.class);

  /**
   * Makes the rule object implement the {@link io.homonoia.rules.api.Rule} interface.
   *
   * @param rule the annotated rule object.
   * @return a proxy that implements the {@link io.homonoia.rules.api.Rule} interface.
   */
  public static io.homonoia.rules.api.Rule asRule(final Object rule) {
    io.homonoia.rules.api.Rule result;
    if (rule instanceof io.homonoia.rules.api.Rule) {
      result = (io.homonoia.rules.api.Rule) rule;
    } else {
      ruleDefinitionValidator.validateRuleDefinition(rule);
      result = (io.homonoia.rules.api.Rule) Proxy.newProxyInstance(
          io.homonoia.rules.api.Rule.class.getClassLoader(),
          new Class[]{io.homonoia.rules.api.Rule.class, Comparable.class},
          new RuleProxy(rule));
    }
    return result;
  }

  private RuleProxy(final Object target) {
    this.target = target;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] args)
      throws Throwable {
    String methodName = method.getName();
    switch (methodName) {
      case "getName":
        return getRuleName();
      case "getDescription":
        return getRuleDescription();
      case "getPriority":
        return getRulePriority();
      case "getLoop":
        return getRuleLoop();
      case "compareTo":
        return compareToMethod(args);
      case "evaluate":
        return evaluateMethod(args);
      case "execute":
        return executeMethod(args);
      case "equals":
        return equalsMethod(args);
      case "hashCode":
        return hashCodeMethod();
      case "toString":
        return toStringMethod();
      default:
        return null;
    }
  }

  private Object evaluateMethod(final Object[] args)
      throws IllegalAccessException, InvocationTargetException {
    Facts facts = (Facts) args[0];
    Method conditionMethod = getConditionMethod();
    try {
      List<Object> actualParameters = getActualParameters(conditionMethod, facts);
      return conditionMethod.invoke(target, actualParameters.toArray()); // validated upfront
    } catch (NoSuchFactException e) {
      LOGGER.warn(
          "Rule '{}' has been evaluated to false due to a declared but missing fact '{}' in {}",
          getTargetClass().getName(), e.getMissingFact(), facts);
      return false;
    } catch (IllegalArgumentException e) {
      LOGGER
          .warn("Types of injected facts in method '{}' in rule '{}' do not match parameters types",
              conditionMethod.getName(), getTargetClass().getName(), e);
      return false;
    }
  }

  private Object executeMethod(final Object[] args)
      throws IllegalAccessException, InvocationTargetException {
    Facts facts = (Facts) args[0];
    for (ActionMethodOrderBean actionMethodBean : getActionMethodBeans()) {
      Method actionMethod = actionMethodBean.getMethod();
      List<Object> actualParameters = getActualParameters(actionMethod, facts);
      actionMethod.invoke(target, actualParameters.toArray());
    }
    return null;
  }

  private Object compareToMethod(final Object[] args) throws Exception {
    Method compareToMethod = getCompareToMethod();
    Object otherRule = args[0]; // validated upfront
    if (compareToMethod != null && Proxy.isProxyClass(otherRule.getClass())) {
      if (compareToMethod.getParameters().length != 1) {
        throw new IllegalArgumentException("compareTo method must have a single argument");
      }
      RuleProxy ruleProxy = (RuleProxy) Proxy.getInvocationHandler(otherRule);
      return compareToMethod.invoke(target, ruleProxy.getTarget());
    } else {
      return compareTo((io.homonoia.rules.api.Rule) otherRule);
    }
  }

  private List<Object> getActualParameters(Method method, Facts facts) {
    List<Object> actualParameters = new ArrayList<>();
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    for (Annotation[] annotations : parameterAnnotations) {
      if (annotations.length == 1) {
        String factName = ((Fact) (annotations[0])).value(); //validated upfront.
        Object fact = facts.get(factName);
        if (fact == null && !facts.asMap().containsKey(factName)) {
          throw new NoSuchFactException(
              format("No fact named '%s' found in known facts: %n%s", factName, facts), factName);
        }
        actualParameters.add(fact);
      } else {
        actualParameters.add(
            facts); //validated upfront, there may be only one parameter not annotated and which is of type Facts.class
      }
    }
    return actualParameters;
  }

  private boolean equalsMethod(final Object[] args) throws Exception {
    if (!(args[0] instanceof io.homonoia.rules.api.Rule)) {
      return false;
    }
    io.homonoia.rules.api.Rule otherRule = (io.homonoia.rules.api.Rule) args[0];
    int otherPriority = otherRule.getPriority();
    int priority = getRulePriority();
    if (priority != otherPriority) {
      return false;
    }
    boolean otherLoop = otherRule.getLoop();
    boolean loop = getRuleLoop();
    if (loop != otherLoop) {
      return false;
    }
    String otherName = otherRule.getName();
    String name = getRuleName();
    if (!name.equals(otherName)) {
      return false;
    }
    String otherDescription = otherRule.getDescription();
    String description = getRuleDescription();
    return Objects.equals(description, otherDescription);
  }

  private int hashCodeMethod() throws Exception {
    int result = getRuleName().hashCode();
    int priority = getRulePriority();
    boolean loop = getRuleLoop();
    String description = getRuleDescription();
    result = 31 * result + (description != null ? description.hashCode() : 0);
    result = 31 * result + priority;
    result = 31 * result + (loop ? 1 : 0);
    return result;
  }

  private Method getToStringMethod() {
    if (this.toStringMethod == null) {
      Method[] methods = getMethods();
      for (Method method : methods) {
        if ("toString".equals(method.getName())) {
          this.toStringMethod = method;
          return this.toStringMethod;
        }
      }
    }
    return this.toStringMethod;
  }

  private String toStringMethod() throws Exception {
    Method toStringMethod = getToStringMethod();
    if (toStringMethod != null) {
      return (String) toStringMethod.invoke(target);
    } else {
      return getRuleName();
    }
  }

  private int compareTo(final io.homonoia.rules.api.Rule otherRule) throws Exception {
    int otherPriority = otherRule.getPriority();
    int priority = getRulePriority();
    boolean loop = getRuleLoop();
    boolean otherLoop = otherRule.getLoop();
    if (priority < otherPriority) {
      return -1;
    } else if (priority > otherPriority) {
      return 1;
    } else if (loop != otherLoop) {
      return Boolean.compare(loop, otherLoop);
    } else {
      String otherName = otherRule.getName();
      String name = getRuleName();
      return name.compareTo(otherName);
    }
  }

  private int getRulePriority() throws Exception {
    if (this.priority == null) {
      int priority = io.homonoia.rules.api.Rule.DEFAULT_PRIORITY;

      Rule rule = getRuleAnnotation();
      if (rule.priority() != io.homonoia.rules.api.Rule.DEFAULT_PRIORITY) {
        priority = rule.priority();
      }

      Method[] methods = getMethods();
      for (Method method : methods) {
        if (method.isAnnotationPresent(Priority.class)) {
          priority = (int) method.invoke(target);
          break;
        }
      }
      this.priority = priority;
    }
    return this.priority;
  }

  private boolean getRuleLoop() throws Exception {
    if (this.loop == null) {
      boolean loop = io.homonoia.rules.api.Rule.DEFAULT_LOOP;

      Rule rule = getRuleAnnotation();
      if (rule.loop() != io.homonoia.rules.api.Rule.DEFAULT_LOOP) {
        loop = rule.loop();
      }

      Method[] methods = getMethods();
      for (Method method : methods) {
        if (method.isAnnotationPresent(Loop.class)) {
          loop = (boolean) method.invoke(target);
          break;
        }
      }

      this.loop = loop;
    }
    return this.loop;
  }

  private Method getConditionMethod() {
    if (this.conditionMethod == null) {
      Method[] methods = getMethods();
      for (Method method : methods) {
        if (method.isAnnotationPresent(Condition.class)) {
          this.conditionMethod = method;
          return this.conditionMethod;
        }
      }
    }
    return this.conditionMethod;
  }

  private Set<ActionMethodOrderBean> getActionMethodBeans() {
    if (this.actionMethods == null) {
      this.actionMethods = new TreeSet<>();
      Method[] methods = getMethods();
      for (Method method : methods) {
        if (method.isAnnotationPresent(Action.class)) {
          Action actionAnnotation = method.getAnnotation(Action.class);
          int order = actionAnnotation.order();
          this.actionMethods.add(new ActionMethodOrderBean(method, order));
        }
      }
    }
    return this.actionMethods;
  }

  private Method getCompareToMethod() {
    if (this.compareToMethod == null) {
      Method[] methods = getMethods();
      for (Method method : methods) {
        if (method.getName().equals("compareTo")) {
          this.compareToMethod = method;
          return this.compareToMethod;
        }
      }
    }
    return this.compareToMethod;
  }

  private Method[] getMethods() {
    if (this.methods == null) {
      this.methods = getTargetClass().getMethods();
    }
    return this.methods;
  }

  private Rule getRuleAnnotation() {
    if (this.annotation == null) {
      this.annotation = Utils.findAnnotation(Rule.class, getTargetClass());
    }
    return this.annotation;
  }

  private String getRuleName() {
    if (this.name == null) {
      Rule rule = getRuleAnnotation();
      this.name = rule.name().equals(io.homonoia.rules.api.Rule.DEFAULT_NAME) ? getTargetClass()
          .getSimpleName() : rule.name();
    }
    return this.name;
  }

  private String getRuleDescription() {
    if (this.description == null) {
      // Default description = "when " + conditionMethodName + " then " + comma separated actionMethodsNames
      StringBuilder description = new StringBuilder();
      appendConditionMethodName(description);
      appendActionMethodsNames(description);
      Rule rule = getRuleAnnotation();
      this.description =
          rule.description().equals(io.homonoia.rules.api.Rule.DEFAULT_DESCRIPTION) ? description
              .toString() : rule.description();
    }
    return this.description;
  }

  private void appendConditionMethodName(StringBuilder description) {
    Method method = getConditionMethod();
    if (method != null) {
      description.append("when ");
      description.append(method.getName());
      description.append(" then ");
    }
  }

  private void appendActionMethodsNames(StringBuilder description) {
    Iterator<ActionMethodOrderBean> iterator = getActionMethodBeans().iterator();
    while (iterator.hasNext()) {
      description.append(iterator.next().getMethod().getName());
      if (iterator.hasNext()) {
        description.append(",");
      }
    }
  }

  public Object getTarget() {
    return target;
  }

  private Class<?> getTargetClass() {
    return target.getClass();
  }

}
