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

import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceException;
import com.adaptris.core.es5.fields.FieldNameMapper;
import com.adaptris.core.es5.fields.NoOpFieldNameMapper;
import com.adaptris.core.transform.csv.BasicFormatBuilder;
import com.adaptris.core.transform.csv.FormatBuilder;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.CloseableIterable;
import com.adaptris.core.util.ExceptionHelper;

public abstract class CSVDocumentBuilderImpl implements ElasticDocumentBuilder {
  @NotNull
  @AutoPopulated
  @Valid
  private FormatBuilder format;
  @AdvancedConfig
  @Min(0)
  @InputFieldDefault(value = "0")
  private Integer uniqueIdField;
  @AdvancedConfig
  @NotNull
  @Valid
  private FieldNameMapper fieldNameMapper;

  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  public CSVDocumentBuilderImpl() {
    this(new BasicFormatBuilder());
    setFieldNameMapper(new NoOpFieldNameMapper());
  }

  public CSVDocumentBuilderImpl(FormatBuilder f) {
    setFormat(f);
  }

  public FormatBuilder getFormat() {
    return format;
  }

  public void setFormat(FormatBuilder csvFormat) {
    this.format = Args.notNull(csvFormat, "format");
  }

  public Integer getUniqueIdField() {
    return uniqueIdField;
  }

  /**
   * Specify which field is considered the unique-id
   * 
   * @param i the uniqueIdField to set, defaults to the first field (first field = '0').
   */
  public void setUniqueIdField(Integer i) {
    this.uniqueIdField = i;
  }

  protected int uniqueIdField() {
    return getUniqueIdField() != null ? getUniqueIdField().intValue() : 0;
  }

  protected List<String> buildHeaders(CSVRecord hdrRec) {
    List<String> result = new ArrayList<>();
    for (String hdrValue : hdrRec) {
      result.add(safeName(hdrValue));
    }
    return result;
  }

  private String safeName(String input) {
    return defaultIfBlank(input, "").trim().replaceAll(" ", "_");
  }

  @Override
  public Iterable<DocumentWrapper> build(AdaptrisMessage msg) throws ProduceException {
    CSVDocumentWrapper result = null;
    try {
      CSVFormat format = getFormat().createFormat();
      CSVParser parser = format.parse(msg.getReader());
      result = buildWrapper(parser, msg);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
    return result;
  }

  protected abstract CSVDocumentWrapper buildWrapper(CSVParser parser, AdaptrisMessage msg) throws Exception;
  
  public FieldNameMapper getFieldNameMapper() {
    return fieldNameMapper;
  }

  public void setFieldNameMapper(FieldNameMapper fieldNameMapper) {
    this.fieldNameMapper = Args.notNull(fieldNameMapper, "fieldNameMapper");
  }

  protected abstract class CSVDocumentWrapper implements CloseableIterable<DocumentWrapper>, Iterator {
    protected CSVParser parser;
    protected Iterator<CSVRecord> csvIterator;
    private boolean iteratorInvoked = false;

    public CSVDocumentWrapper(CSVParser p) {
      parser = p;
      csvIterator = p.iterator();
    }

    @Override
    public Iterator<DocumentWrapper> iterator() {
      if (iteratorInvoked) {
        throw new IllegalStateException("iterator already invoked");
      }
      iteratorInvoked = true;
      return this;
    }

    @Override
    public boolean hasNext() {
      return csvIterator.hasNext();
    }

    @Override
    public void close() throws IOException {
      IOUtils.closeQuietly(parser);
    }

  }
}
