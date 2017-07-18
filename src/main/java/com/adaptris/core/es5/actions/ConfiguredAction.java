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
