package com.adaptris.core.es5;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.es5.types.ConfiguredTypeBuilder;
import com.adaptris.core.es5.types.TypeBuilder;
import com.adaptris.core.transform.csv.BasicFormatBuilder;
import com.adaptris.core.transform.csv.FormatBuilder;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Builds a simple document for elastic search.
 * 
 * <p>
 * The document that is created contains the following characteristics
 * <ul>
 * <li>The first record of the CSV is assumed to be a header row, and is used as the fieldName for each entry</li>
 * <li>The "unique-id" for the document is derived from the specified column, duplicates may have unexpected results depending on
 * your configuration.</li>
 * </ul>
 * </p>
 * 
 * @author lchan
 * @config es5-csv-document-builder
 *
 */
@XStreamAlias("es5-csv-document-builder")
public class CSVDocumentBuilder extends CSVWithTypeBuilder {

  @AdvancedConfig
  @InputFieldDefault(value = "true")
  private Boolean useHeaderRecord;

  public CSVDocumentBuilder() {
    this(new BasicFormatBuilder(), new ConfiguredTypeBuilder());
  }

  public CSVDocumentBuilder(TypeBuilder typeBuilder) {
    this(new BasicFormatBuilder(), typeBuilder);
  }

  public CSVDocumentBuilder(FormatBuilder f) {
    this(f, new ConfiguredTypeBuilder());
  }

  public CSVDocumentBuilder(FormatBuilder f, TypeBuilder typeBuilder) {
    super();
    setFormat(f);
    setTypeBuilder(typeBuilder);
  }

  public Boolean getUseHeaderRecord() {
    return useHeaderRecord;
  }

  /**
   * Whether or not the document contains a header row.
   * 
   * @param b the useHeaderRecord to set, defaults to true.
   */
  public void setUseHeaderRecord(Boolean b) {
    this.useHeaderRecord = b;
  }

  private boolean useHeaderRecord() {
    return getUseHeaderRecord() != null ? getUseHeaderRecord().booleanValue() : true;
  }

  @Override
  protected CSVDocumentWrapper buildWrapper(CSVParser parser, AdaptrisMessage msg) throws Exception {
    return new MyWrapper(parser, getTypeBuilder().getType(msg));
  }

  private class MyWrapper extends CSVDocumentWrapper {
    private List<String> headers = new ArrayList<>();
    private String type;

    public MyWrapper(CSVParser p, String mytype) {
      super(p);
      if (useHeaderRecord()) {
        headers = buildHeaders(csvIterator.next());
      }
      type = mytype;
    }

    @Override
    public DocumentWrapper next() {
      DocumentWrapper result = null;
      try {
        CSVRecord record = csvIterator.next();
        int idField = 0;
        if (uniqueIdField() <= record.size()) {
          idField = uniqueIdField();
        }
        else {
          throw new IllegalArgumentException("unique-id field > number of fields in record");
        }
        String uniqueId = record.get(idField);
        XContentBuilder builder = jsonBuilder();
        builder.startObject();
        addTimestamp(builder);
        for (int i = 0; i < record.size(); i++) {
          String fieldName = getFieldNameMapper().map(headers.size() > 0 ? headers.get(i) : "field_" + i);
          String data = record.get(i);
          builder.field(fieldName, new Text(data));
        }
        builder.endObject();

        result = new DocumentWrapper(uniqueId, builder, type);
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
      return result;
    }

  }
}
