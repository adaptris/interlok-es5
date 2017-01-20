package com.adaptris.core.es5;

import com.adaptris.core.es5.CSVDocumentBuilder;

public class CsvDocumentBuilderTest extends CsvBuilderCase {

  @Override
  protected CSVDocumentBuilder createBuilder() {
    return new CSVDocumentBuilder();
  }

}
