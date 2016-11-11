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

  public DocumentAction action() {
    return action;
  }

  public void setAction(DocumentAction action) {
    this.action = action;
  }
  

}
