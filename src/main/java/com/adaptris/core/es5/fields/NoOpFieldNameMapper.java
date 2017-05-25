package com.adaptris.core.es5.fields;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Do nothing with the field name.
 * 
 * @config es5-noop-field-name-mapper
 *
 */
@XStreamAlias("es5-noop-field-name-mapper")
public class NoOpFieldNameMapper implements FieldNameMapper {

  @Override
  public String map(String name) {
    return name;
  }

}
