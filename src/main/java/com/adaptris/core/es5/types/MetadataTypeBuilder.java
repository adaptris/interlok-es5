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

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Derive the type of document in the index (synomous with mapping) from metadata.
 * 
 * @config es5-metadata-type
 *
 */
@XStreamAlias("es5-metadata-type")
public class MetadataTypeBuilder implements TypeBuilder {

  @NotBlank
  private String key;

  public MetadataTypeBuilder() {

  }

  public MetadataTypeBuilder(String key) {
    this();
    setKey(key);
  }

  public String getKey() {
    return key;
  }

  public void setKey(String type) {
    this.key = type;
  }

  @Override
  public String getType(AdaptrisMessage msg) {
    return msg.getMetadataValue(getKey());
  }

}
