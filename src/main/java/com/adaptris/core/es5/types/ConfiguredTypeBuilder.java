package com.adaptris.core.es5.types;

import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A configured type of document in the index (synomous with mapping).
 * 
 * @config es5-configured-type
 *
 */
@XStreamAlias("es5-configured-type")
public class ConfiguredTypeBuilder implements TypeBuilder {

  private String type;

  public ConfiguredTypeBuilder() {

  }

  public ConfiguredTypeBuilder(String type) {
    this();
    setType(type);
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public String getType(AdaptrisMessage msg) {
    return getType();
  }

}
