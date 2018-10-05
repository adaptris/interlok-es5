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

import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceException;
import com.adaptris.core.es5.types.TypeBuilder;
import com.adaptris.core.util.CloseableIterable;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.ReadContext;
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Parse a json array and create documents from it for elasticsearch
 * 
 * <p>
 * The unique-id for each document created is derived from the {@link JsonArrayDocumentBuilder#getUniqueIdJsonPath()} which defaults
 * to {@code $.uniqueid}
 * </p>
 * 
 * @config es5-json-array-document-builder
 *
 */
@XStreamAlias("es5-json-array-document-builder")
public class JsonArrayDocumentBuilder extends JsonDocumentBuilderImpl {

  private static final int DEFAULT_BUFFER_SIZE = 8192;
  private static final String UID_PATH = "$.uniqueid";

  private transient Logger log = LoggerFactory.getLogger(this.getClass());

  @AdvancedConfig
  private String addTimestampField;
  @AdvancedConfig
  @InputFieldDefault(value = "8192")
  private Integer bufferSize;
  @AdvancedConfig
  @InputFieldDefault(value = UID_PATH)
  private String uniqueIdJsonPath;
  @AdvancedConfig
  private String routingJsonPath;
  @AdvancedConfig
  private String parentJsonPath;

  public JsonArrayDocumentBuilder() {

  }

  public JsonArrayDocumentBuilder(TypeBuilder b) {
    this();
    setTypeBuilder(b);
  }

  @Override
  public Iterable<DocumentWrapper> build(AdaptrisMessage msg) throws ProduceException {
    try {
      BufferedReader buf = new BufferedReader(msg.getReader(), bufferSize());
      String type = getTypeBuilder().getType(msg);
      ObjectMapper mapper = new ObjectMapper();
      JsonParser parser = mapper.getFactory().createParser(buf);
      if (parser.nextToken() != JsonToken.START_ARRAY) {
        throw new ProduceException("Expected an array");
      }
      return new JsonDocumentWrapper(mapper, parser, type);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
  }

  public String getAddTimestampField() {
    return addTimestampField;
  }

  /**
   * Set the timestamp field to be added, if any.
   * 
   * @param s the timestamp.
   */
  public void setAddTimestampField(String s) {
    this.addTimestampField = s;
  }

  protected ObjectNode addTimestamp(ObjectNode b) {
    if (!isBlank(getAddTimestampField())) {
      b.put(getAddTimestampField(), new Date().getTime());
    }
    return b;
  }

  public Integer getBufferSize() {
    return bufferSize;
  }

  /**
   * Set the internal buffer size.
   * <p>
   * This is used when; the default buffer size matches the default buffer size in {@link BufferedReader} and {@link BufferedWriter}
   * , changes to the buffersize will impact performance and memory usage depending on the underlying operating system/disk.
   * </p>
   * 
   * @param b the buffer size (default is 8192).
   */
  public void setBufferSize(Integer b) {
    this.bufferSize = b;
  }

  int bufferSize() {
    return getBufferSize() != null ? getBufferSize().intValue() : DEFAULT_BUFFER_SIZE;
  }

  public String getUniqueIdJsonPath() {
    return uniqueIdJsonPath;
  }

  /**
   * Specify the json path to the unique id.
   * 
   * @param s the json path to the unique-id; if not specified {@code $.uniqueid}
   */
  public void setUniqueIdJsonPath(String s) {
    this.uniqueIdJsonPath = s;
  }

  public String getRoutingJsonPath() {
    return routingJsonPath;
  }

  /**
   * Set the JSON path to extract the routing information.
   * 
   * @param path the path to routing information, defaults to null if not specified.
   */
  public void setRoutingJsonPath(String path) {
    this.routingJsonPath = path;
  }

  public String getParentJsonPath() {
    return parentJsonPath;
  }

  /**
   * Set the JSON path to the parent ID.
   * 
   * @param path defaults to null if not specified.
   */
  public void setParentJsonPath(String path) {
    this.parentJsonPath = path;
  }

  String uidPath() {
    return !isBlank(getUniqueIdJsonPath()) ? getUniqueIdJsonPath() : UID_PATH;
  }


  private class JsonDocumentWrapper implements CloseableIterable<DocumentWrapper>, Iterator<DocumentWrapper> {
    private String type;
    private final JsonParser parser;
    private final ObjectMapper mapper;

    private DocumentWrapper nextMessage;
    private transient Configuration jsonConfig = new Configuration.ConfigurationBuilder().jsonProvider(new JsonSmartJsonProvider())
        .mappingProvider(new JacksonMappingProvider()).options(EnumSet.noneOf(Option.class)).build();

    public JsonDocumentWrapper(ObjectMapper mapper, JsonParser parser, String type) {
      this.mapper = mapper;
      this.parser = parser;
      this.type = type;
    }

    @Override
    public DocumentWrapper next() {
      DocumentWrapper result = nextMessage;
      nextMessage = null;
      return result;
    }

    private DocumentWrapper buildNext() throws IOException {
      DocumentWrapper result = null;
      if (parser.nextToken() == JsonToken.START_OBJECT) {
        ObjectNode node = addTimestamp(mapper.readTree(parser));
        // Add the timestamp before we start futzing with jsonBuilder...
        addTimestamp(node);
        String jsonString = node.toString();
        XContentBuilder jsonContent = jsonBuilder(jsonString);
        ReadContext ctx = JsonPath.parse(jsonString, jsonConfig);
        result = new DocumentWrapper(get(ctx, uidPath()), jsonContent, type).withParent(getQuietly(ctx, getParentJsonPath()))
            .withRouting(getQuietly(ctx, getRoutingJsonPath()));
      }
      return result;
    }

    private String get(ReadContext ctx, String path) {
      return ctx.read(path);
    }

    private String getQuietly(ReadContext ctx, String path) {
      String result = null;
      if (isBlank(path)) {
        return null;
      }
      try {
        result = get(ctx, path);
      } catch (PathNotFoundException e) {
        result = null;
      }
      return result;
    }

    @Override
    public Iterator<DocumentWrapper> iterator() {
      return this;
    }

    @Override
    public boolean hasNext() {
      if (nextMessage == null) {
        try {
          nextMessage = buildNext();
        }
        catch (IOException e) {
          log.warn("Could not construct next DocumentWrapper; badly formed JSON?", e);
          throw new RuntimeException("Could not construct next DocumentWrapper", e);
        }
      }
      return nextMessage != null;
    }

    @Override
    public void close() throws IOException {
      IOUtils.closeQuietly(parser);
    }
  }


}
