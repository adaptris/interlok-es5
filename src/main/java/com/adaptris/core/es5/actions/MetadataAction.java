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
