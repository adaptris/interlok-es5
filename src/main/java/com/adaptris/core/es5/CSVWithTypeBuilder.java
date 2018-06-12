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

import javax.validation.constraints.NotNull;

import org.elasticsearch.common.xcontent.XContentBuilder;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.es5.types.ConfiguredTypeBuilder;
import com.adaptris.core.es5.types.TypeBuilder;
import com.adaptris.core.util.Args;

public abstract class CSVWithTypeBuilder extends CSVDocumentBuilderImpl {
  @NotNull
  @AutoPopulated
  private TypeBuilder typeBuilder;
  @AdvancedConfig
  private String addTimestampField;

  public CSVWithTypeBuilder() {
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

  protected void addTimestamp(XContentBuilder b) throws IOException {
    if (!isBlank(addTimestampField)) {
      b.field(addTimestampField, new Date().getTime());
    }
  }
}
