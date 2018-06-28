/*
    Copyright Adaptris Ltd.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.adaptris.core.es5;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.adaptris.core.es5.fields.FieldNameMapper;
import com.adaptris.core.es5.fields.NoOpFieldNameMapper;
import com.adaptris.core.es5.fields.ToLowerCaseFieldNameMapper;
import com.adaptris.core.es5.fields.ToUpperCaseFieldNameMapper;

public class FieldNameMapperTest {
  final String INPUT = "abcABC";

  @Test
  public void testNoOpMapper() {
    FieldNameMapper mapper = new NoOpFieldNameMapper();
    assertEquals(INPUT, mapper.map(INPUT));
  }
  
  @Test
  public void testToUpperCaseMapper() {
    FieldNameMapper mapper = new ToUpperCaseFieldNameMapper();
    assertEquals(INPUT.toUpperCase(), mapper.map(INPUT));
  }

  @Test
  public void testToLowerCaseMapper() {
    FieldNameMapper mapper = new ToLowerCaseFieldNameMapper();
    assertEquals(INPUT.toLowerCase(), mapper.map(INPUT));
  }

}
