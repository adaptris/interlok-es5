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

package com.adaptris.core.es5;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.common.xcontent.XContentBuilder;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceException;
import com.adaptris.core.es5.types.TypeBuilder;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Creates a JSON document for elastic search.
 * 
 * <p>
 * The document that is created contains the following characteristics
 * <ul>
 * <li>The message's uniqueID is used as the ID of the document.</li>
 * <li>The message payload is assumed to be a JSON object (not an array) and becomes the document..</li>
 * <li>routing information if configured will be resolved via {@link AdaptrisMessage#resolve(String)}.</li>
 * <li>parent-id information if configured will be resolved via {@link AdaptrisMessage#resolve(String)}.</li>
 * </ul>
 * </p>
 * 
 * 
 * @config es5-json-document-builder
 *
 */
@XStreamAlias("es5-json-document-builder")
public class JsonDocumentBuilder extends JsonDocumentBuilderImpl {

  @AdvancedConfig
  @InputFieldHint(expression = true)
  private String routing;
  @AdvancedConfig
  @InputFieldHint(expression = true)
  private String parent;

  public JsonDocumentBuilder() {
    super();
  }

  public JsonDocumentBuilder(TypeBuilder b) {
    this();
    setTypeBuilder(b);
  }

  @Override
  public Iterable<DocumentWrapper> build(AdaptrisMessage msg) throws ProduceException {
    List<DocumentWrapper> result = new ArrayList<>();
    try (Reader buf = msg.getReader()) {
      ObjectMapper mapper = new ObjectMapper();
      JsonParser parser = mapper.getFactory().createParser(buf);
      if (parser.nextToken() != JsonToken.START_OBJECT) {
        throw new ProduceException("Expected the start of a JSON object");
      }
      ObjectNode node = mapper.readTree(parser);
      XContentBuilder jsonContent = jsonBuilder(node);
      result.add(new DocumentWrapper(msg.getUniqueId(), jsonContent, getTypeBuilder().getType(msg))
          .withParent(msg.resolve(getParent())).withRouting(msg.resolve(getRouting())));
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
    return result;
  }

  public String getRouting() {
    return routing;
  }

  /**
   * Set the routing for this document.
   * 
   * @param routing the routing information (default is null)
   */
  public void setRouting(String routing) {
    this.routing = routing;
  }

  public String getParent() {
    return parent;
  }

  /**
   * Set the parent id for this document
   * 
   * @param parent the parent id (default is null).
   */
  public void setParent(String parent) {
    this.parent = parent;
  }

}
