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

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.es5.DocumentAction;
import com.adaptris.core.es5.DocumentWrapper;
import com.adaptris.util.KeyValuePairList;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Map the action provided by the underlying {@link ActionExtractor} into a new value.
 * <p>
 * For instance metadata that indicates an {@link DocumentAction#INDEX} operation comes through as {@code INSERT}. Use a mapped
 * action to map from INSERT to INDEX.
 * </p>
 * 
 * @config es5-mapped-action
 */
@XStreamAlias("es5-mapped-action")
public class MappedAction implements ActionExtractor {

  private ActionExtractor action;
  private KeyValuePairList mappings;
  
  public MappedAction() {
    setMappings(new KeyValuePairList());
  }

  @Override
  public String extract(AdaptrisMessage msg, DocumentWrapper document) throws ServiceException {
    String action = getAction().extract(msg, document);
    String mappedAction = mappings.getValue(action);
    return mappedAction != null ? mappedAction : action;
  }

  public ActionExtractor getAction() {
    return action;
  }

  public void setAction(ActionExtractor action) {
    this.action = action;
  }

  public KeyValuePairList getMappings() {
    return mappings;
  }

  /**
   * The action mappings.
   * <p>
   * The key should be the action returned by the configured {@link #setAction(ActionExtractor)}, the value the actual action
   * intended. If the key does not exist in the map then is returned as-is
   * </p>
   * 
   * @param mappings
   */
  public void setMappings(KeyValuePairList mappings) {
    this.mappings = mappings;
  }

}
