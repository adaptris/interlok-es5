package com.adaptris.core.es5.actions;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.es5.DocumentWrapper;
import com.adaptris.util.KeyValuePairList;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Map the action provided by the underlying {@link ActionExtractor} into a new value.
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
