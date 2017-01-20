package com.adaptris.core.es5.actions;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.es5.DocumentWrapper;
import com.adaptris.util.KeyValuePairList;
import com.thoughtworks.xstream.annotations.XStreamAlias;

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

  public void setMappings(KeyValuePairList mappings) {
    this.mappings = mappings;
  }

}
