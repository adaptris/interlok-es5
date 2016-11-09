package com.adaptris.core.es5;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("es5-lowercase-field-name-mapper")
public class ToLowerCaseFieldNameMapper implements FieldNameMapper {

  @Override
  public String map(String name) {
    return name.toLowerCase();
  }

}
