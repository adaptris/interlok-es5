package com.adaptris.core.es5.actions;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.core.es5.DocumentAction;
import com.adaptris.core.es5.DocumentWrapper;

/**
 * Extract the action {@code INDEX/UPDATE/DELETE} from the {@link DocumentWrapper} instance or the {@link AdaptrisMessage}.
 * 
 * 
 */
public interface ActionExtractor {

  /**
   * Extract the action {@code INDEX/UPDATE/DELETE} from the {@link DocumentWrapper} instance or the {@link AdaptrisMessage}.
   * <p>
   * Note that eventually {@link DocumentAction#valueOf(String)} is used to create a typesafe action.
   * </p>
   */
  public String extract(AdaptrisMessage msg, DocumentWrapper document) throws ServiceException;
}
