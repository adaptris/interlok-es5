package com.adaptris.core.es5;

import javax.validation.constraints.NotNull;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("es5-configured-action")
public class ConfiguredAction implements ActionExtractor {

  @NotNull
  private DocumentAction action = DocumentAction.INDEX;

  @Override
  public String extract(AdaptrisMessage msg, DocumentWrapper document) {
    return getAction().name();
  }

  public DocumentAction getAction() {
    return action;
  }

  public void setAction(DocumentAction action) {
    this.action = action;
  }

}
