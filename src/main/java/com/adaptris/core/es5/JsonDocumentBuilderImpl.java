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

import java.io.IOException;
import java.util.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.es5.types.ConfiguredTypeBuilder;
import com.adaptris.core.es5.types.TypeBuilder;
import com.adaptris.core.util.Args;
import com.fasterxml.jackson.databind.node.ObjectNode;


public abstract class JsonDocumentBuilderImpl implements ElasticDocumentBuilder {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());
  @NotNull
  @AutoPopulated
  @Valid
  private TypeBuilder typeBuilder;
  @AdvancedConfig
  private String addTimestampField;

  public JsonDocumentBuilderImpl() {
    setTypeBuilder(new ConfiguredTypeBuilder());
  }

  /**
   * @return the typeBuilder
   */
  public TypeBuilder getTypeBuilder() {
    return typeBuilder;
  }

  /**
   * @param typeBuilder the typeBuilder to set
   */
  public void setTypeBuilder(TypeBuilder typeBuilder) {
    this.typeBuilder = Args.notNull(typeBuilder, "TypeBuilder");
  }

  public String getAddTimestampField() {
    return addTimestampField;
  }

  /**
   * Specify a value here to emit the current ms since epoch as the fields value.
   * 
   * @param s the fieldname (default null)
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

  protected XContentBuilder jsonBuilder(ObjectNode node) throws IOException {
    // Add the TS first.
    ObjectNode withTs = addTimestamp(node);
    return jsonBuilder(withTs.toString());
  }

  protected XContentBuilder jsonBuilder(String jsonString) throws IOException {
    XContentBuilder jsonContent = XContentFactory.jsonBuilder();
    // According to the ES docs; this is the rare situation where using NamedXContentRegistry.EMPTY is fine
    // as we aren't ever calling XContentParser#namedObject(Class, String, Object)...
    try (XContentParser p = XContentFactory.xContent(XContentType.JSON).createParser(NamedXContentRegistry.EMPTY, jsonString)) {
      jsonContent.copyCurrentStructure(p);
    }
    return jsonContent;
  }
}
