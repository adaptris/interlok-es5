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

import org.elasticsearch.common.xcontent.XContentBuilder;

public class DocumentWrapper {

  private final String uniqueId;
  private String routing;
  private String parent;
  private DocumentAction action;
  private final String type;
  private final XContentBuilder content;
  
  public DocumentWrapper(String uid, XContentBuilder content, String type) {
    this.uniqueId = uid;
    this.content = content;
    this.type = type;
  }
  
  public String type() {
    return type;
  }

  public XContentBuilder content() {
    return content;
  }

  public String uniqueId() {
    return uniqueId;
  }

  public String routing() {
    return routing;
  }

  public void setRouting(String routing) {
    this.routing = routing;
  }

  public String parent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public DocumentWrapper withRouting(String r) {
    setRouting(r);
    return this;
  }

  public DocumentWrapper withParent(String p) {
    setParent(p);
    return this;
  }

  public DocumentWrapper withAction(String s) {
    setAction(DocumentAction.valueOf(s));
    return this;
  }

  public DocumentWrapper withAction(DocumentAction a) {
    setAction(a);
    return this;
  }

  /**
   * The action to be taken with this document
   * 
   * @return the action associated with the Document.
   */
  public DocumentAction action() {
    return action;
  }

  public void setAction(DocumentAction action) {
    this.action = action;
  }
  

}
