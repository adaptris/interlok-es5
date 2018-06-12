/*
    Copyright Adaptris Ltd.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

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
