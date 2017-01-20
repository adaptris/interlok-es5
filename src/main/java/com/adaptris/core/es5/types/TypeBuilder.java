package com.adaptris.core.es5.types;

import com.adaptris.core.AdaptrisMessage;

public interface TypeBuilder {

  String getType(AdaptrisMessage msg) throws Exception;
}
