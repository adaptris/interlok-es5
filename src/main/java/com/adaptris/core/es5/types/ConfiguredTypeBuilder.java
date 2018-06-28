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

package com.adaptris.core.es5.types;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A configured type of document in the index (synomous with mapping).
 * 
 * @config es5-configured-type
 *
 */
@XStreamAlias("es5-configured-type")
public class ConfiguredTypeBuilder implements TypeBuilder {

  @InputFieldHint(expression = true)
  @NotBlank
  private String type;

  public ConfiguredTypeBuilder() {

  }

  public ConfiguredTypeBuilder(String type) {
    this();
    setType(type);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getType(AdaptrisMessage msg) {
    return msg.resolve(getType());
  }

}
