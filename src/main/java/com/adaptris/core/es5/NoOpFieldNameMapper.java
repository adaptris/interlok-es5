package com.adaptris.core.es5;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("es5-noop-field-name-mapper")
public class NoOpFieldNameMapper implements FieldNameMapper {

  @Override
  public String map(String name) {
    return name;
  }

}
