package com.adaptris.core.es5.fields;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Map the field name into lowercase using {@link String#toLowerCase()}
 * 
 * @config es5-lowercase-field-name-mapper
 */
@XStreamAlias("es5-lowercase-field-name-mapper")
public class ToLowerCaseFieldNameMapper implements FieldNameMapper {

  @Override
  public String map(String name) {
    return name.toLowerCase();
  }

}
