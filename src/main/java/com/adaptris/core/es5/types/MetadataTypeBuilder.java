package com.adaptris.core.es5.types;

import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Derive the type of document in the index (synomous with mapping) from metadata.
 * 
 * @config es5-metadata-type
 *
 */
@XStreamAlias("es5-metadata-type")
public class MetadataTypeBuilder implements TypeBuilder {

  @NotBlank
  private String key;

  public MetadataTypeBuilder() {

  }

  public MetadataTypeBuilder(String key) {
    this();
    setKey(key);
  }

  public String getKey() {
    return key;
  }

  public void setKey(String type) {
    this.key = type;
  }

  @Override
  public String getType(AdaptrisMessage msg) {
    return msg.getMetadataValue(getKey());
  }

}
