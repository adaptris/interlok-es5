package com.adaptris.core.es5.actions;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.es5.DocumentWrapper;

public interface ActionExtractor {
  public String extract(AdaptrisMessage msg, DocumentWrapper document) throws ServiceException;
}
