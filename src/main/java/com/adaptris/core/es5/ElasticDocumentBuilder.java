package com.adaptris.core.es5;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceException;

public interface ElasticDocumentBuilder {

  Iterable<DocumentWrapper> build(AdaptrisMessage msg) throws ProduceException;
  
}
