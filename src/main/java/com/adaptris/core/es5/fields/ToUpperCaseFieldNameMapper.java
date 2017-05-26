package com.adaptris.core.es5.fields;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Map the field name into uppercase using {@link String#toUpperCase()}
 * 
 * @config es5-uppercase-field-name-mapper
 */
@XStreamAlias("es5-uppercase-field-name-mapper")
public class ToUpperCaseFieldNameMapper implements FieldNameMapper {

  @Override
  public String map(String name) {
    return name.toUpperCase();
  }

}
