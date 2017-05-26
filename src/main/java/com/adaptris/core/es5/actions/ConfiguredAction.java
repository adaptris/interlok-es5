package com.adaptris.core.es5.actions;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
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

  @NotNull
  @AutoPopulated
  private DocumentAction action = DocumentAction.INDEX;

  public ConfiguredAction() {

  }

  public ConfiguredAction(DocumentAction a) {
    this();
    setAction(a);
  }

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
