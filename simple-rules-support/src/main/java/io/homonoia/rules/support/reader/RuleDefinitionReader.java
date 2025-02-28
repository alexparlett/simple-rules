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

package io.homonoia.rules.support.reader;

import io.homonoia.rules.support.RuleDefinition;
import java.io.Reader;
import java.util.List;

/**
 * Strategy interface for {@link io.homonoia.rules.support.RuleDefinition} readers.
 *
 * @author Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 * @see JsonRuleDefinitionReader
 * @see YamlRuleDefinitionReader
 */
@FunctionalInterface
public interface RuleDefinitionReader {

  /**
   * Read a list of rule definitions from a rule descriptor.
   *
   * <strong> The descriptor is expected to contain a collection of rule definitions
   * even for a single rule.</strong>
   *
   * @param reader of the rules descriptor
   * @return a list of rule definitions
   * @throws Exception if a problem occurs during rule definition reading
   */
  List<RuleDefinition> read(Reader reader) throws Exception;

}
