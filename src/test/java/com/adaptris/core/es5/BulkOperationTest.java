package com.adaptris.core.es5;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ProducerCase;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.es5.types.ConfiguredTypeBuilder;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class BulkOperationTest extends ProducerCase {

  private static final String EXAMPLE_COMMENT_HEADER = "\n<!--" + "\n-->\n";

  public BulkOperationTest(String name) {
    super(name);
  }

  public void testRefreshPolicy() throws Exception {
    BulkOperation producer = new BulkOperation();
    assertNull(producer.getRefreshPolicy());
    producer.setRefreshPolicy("wait_until");
    assertEquals("wait_until", producer.getRefreshPolicy());
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    KeyValuePairSet settings = new KeyValuePairSet();
    settings.add(new KeyValuePair("cluster.name", "my-cluster"));
    settings.add(new KeyValuePair("client.transport.sniff", "true"));
    ElasticSearchConnection esc = new ElasticSearchConnection();
    esc.setSettings(settings);
    esc.addTransportUrl("localhost:9300");
    esc.addTransportUrl("localhost:9301");
    esc.addTransportUrl("localhost:9302");

    BulkOperation producer = new BulkOperation();
    producer.setBatchWindow(1000);
    producer.setDestination(new ConfiguredProduceDestination("myIndex"));
    producer.setDocumentBuilder(new SimpleDocumentBuilder(new ConfiguredTypeBuilder("myType")));
    return new StandaloneProducer(esc, producer);
  }

  @Override
  protected String getExampleCommentHeader(Object o) {
    return super.getExampleCommentHeader(o) + EXAMPLE_COMMENT_HEADER;
  }

}
