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

package com.adaptris.core.es5.actions;

import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.es5.DocumentWrapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Derive the action from metadata.
 * 
 * @config es5-metadata-action
 *
 */
@XStreamAlias("es5-metadata-action")
public class MetadataAction implements ActionExtractor {

  @InputFieldDefault(value = "action")
  private String metadataKey;

  @Override
  public String extract(AdaptrisMessage msg, DocumentWrapper document) {
    return msg.getMetadataValue(metadataKey());
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  /**
   * Set the metadata key from which to derive the action.
   * 
   * @param metadataKey the key, default if not specified is {@code action}
   */
  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }
  
  private String metadataKey() {
    return getMetadataKey() != null ? getMetadataKey() : "action";
  }

}
