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

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.es5.DocumentAction;
import com.adaptris.core.es5.DocumentWrapper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * A Static configured action.
 * 
 * @config es5-configured-action
 *
 */
@XStreamAlias("es5-configured-action")
public class ConfiguredAction implements ActionExtractor {

  @NotBlank
  @AutoPopulated
  @InputFieldHint(expression = true)
  private String action = DocumentAction.INDEX.name();

  public ConfiguredAction() {

  }

  public ConfiguredAction(String a) {
    this();
    setAction(a);
  }

  public ConfiguredAction(DocumentAction a) {
    this(a.name());
  }

  @Override
  public String extract(AdaptrisMessage msg, DocumentWrapper document) {
    return msg.resolve(getAction());
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

}
