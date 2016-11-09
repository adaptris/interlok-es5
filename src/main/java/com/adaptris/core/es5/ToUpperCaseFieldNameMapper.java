package com.adaptris.core.es5;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("es5-uppercase-field-name-mapper")
public class ToUpperCaseFieldNameMapper implements FieldNameMapper {

  @Override
  public String map(String name) {
    return name.toUpperCase();
  }

}
