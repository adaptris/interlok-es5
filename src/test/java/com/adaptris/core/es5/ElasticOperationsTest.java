package com.adaptris.core.es5;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.BaseCase;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.es5.actions.ConfiguredAction;
import com.adaptris.core.es5.types.ConfiguredTypeBuilder;

public class ElasticOperationsTest {

  private static final String SINGLE_OP_MAPPING = "messages";
  private static final String SINGLE_OP_INDEX = "single";

  private static final String BULK_OP_MAPPING = "messages";
  private static final String BULK_OP_INDEX = "bulk";

  private static final String BULK_OP_GEO_MAPPING = "messages";
  private static final String BULK_OP_GEO_INDEX = "bulk_geo";

  private static EmbeddedElasticServer ES_SERVER;
  @Rule
  public TestName testName = new TestName();

  // 2018-01-30 - Been getting certificate path errors trying to
  // download the installers; so the ES_SERVER doesn't work.
  private static final boolean TESTS_ENABLED = false;

  @BeforeClass
  public static void bootstrapElastic() throws Exception {
    // es.addIndex(SIMPLE_INDEX, SIMPLE_MAPPING_TYPE, MAPPING_SPEC);
    // Dynamic indexes to save the day...
    if (TESTS_ENABLED) {
      ES_SERVER = new EmbeddedElasticServer();
      ES_SERVER.start();
    }
  }

  @AfterClass
  public static void shutdownElastic() throws Exception {
    if (TESTS_ENABLED) {
      ES_SERVER.stop();
    }
  }

  @Test
  public void testSingleOperation() throws Exception {
    if (TESTS_ENABLED) {

      ElasticSearchConnection conn = ES_SERVER.createConnection();
      SingleOperation producer = new SingleOperation(new ConfiguredProduceDestination(SINGLE_OP_INDEX),
          new SimpleDocumentBuilder(new ConfiguredTypeBuilder(SINGLE_OP_MAPPING)));
      StandaloneProducer esProducer = new StandaloneProducer(conn, producer);
      try {
        TransportClient client = conn.createClient();

        AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(testName.getMethodName());
        BaseCase.start(esProducer);
        esProducer.doService(msg);
        // Success; we should check that the thing exists.
        GetResponse getter = client.prepareGet(SINGLE_OP_INDEX, SINGLE_OP_MAPPING, msg.getUniqueId()).get();
        assertNotNull(getter.getSourceAsString());
        assertEquals(testName.getMethodName(), getter.getSourceAsMap().get("content"));

        msg.addMessageHeader(testName.getMethodName(), testName.getMethodName());
        producer.setAction(new ConfiguredAction(DocumentAction.UPDATE));
        esProducer.doService(msg);

        getter = client.prepareGet(SINGLE_OP_INDEX, SINGLE_OP_MAPPING, msg.getUniqueId()).get();
        assertNotNull(getter.getSourceAsString());
        assertEquals(testName.getMethodName(), ((Map) getter.getSourceAsMap().get("metadata")).get(testName.getMethodName()));

        // Now delete that document.
        producer.setAction(new ConfiguredAction(DocumentAction.DELETE));
        esProducer.doService(msg);
        getter = client.prepareGet(SINGLE_OP_INDEX, SINGLE_OP_MAPPING, msg.getUniqueId()).get();
        assertNull(getter.getSourceAsString());

      }
      finally {
        BaseCase.stop(esProducer);
      }
    }
    else {
      System.err.println("Tests Disabled");
    }
  }

  @Test
  public void testBulkOperation_CSV() throws Exception {
    if (TESTS_ENABLED) {
      ElasticSearchConnection conn = ES_SERVER.createConnection();
      BulkOperation producer = new BulkOperation(new ConfiguredProduceDestination(BULK_OP_INDEX),
          new CSVDocumentBuilder(new ConfiguredTypeBuilder(BULK_OP_MAPPING)));
      producer.setAction(new ConfiguredAction(DocumentAction.INDEX));
      producer.setBatchWindow(2);
      StandaloneProducer esProducer = new StandaloneProducer(conn, producer);

      try {
        TransportClient client = conn.createClient();

        AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CsvBuilderCase.CSV_INPUT);
        BaseCase.start(esProducer);
        esProducer.doService(msg);

        // Success; we should check that the thing exists.
        GetResponse getter = client.prepareGet(BULK_OP_INDEX, BULK_OP_MAPPING, "UID-1").get();
        assertNotNull(getter.getSourceAsString());
        assertEquals("UID-1", getter.getSourceAsMap().get("productuniqueid"));

        // Now update it.
        producer.setAction(new ConfiguredAction(DocumentAction.UPDATE));
        esProducer.doService(msg);
        getter = client.prepareGet(BULK_OP_INDEX, BULK_OP_MAPPING, "UID-1").get();
        assertNotNull(getter.getSourceAsString());

        // Now delete it
        producer.setAction(new ConfiguredAction(DocumentAction.DELETE));
        esProducer.doService(msg);
        getter = client.prepareGet(BULK_OP_INDEX, BULK_OP_MAPPING, "UID-1").get();
        assertNull(getter.getSourceAsString());

      }
      finally {
        BaseCase.stop(esProducer);
      }
    }
    else {
      System.err.println("Tests Disabled");
    }

  }

  @Test
  public void testBulkOperation_CSV_WithGeoPoint() throws Exception {
    if (TESTS_ENABLED) {
      ElasticSearchConnection conn = ES_SERVER.createConnection();
      BulkOperation producer = new BulkOperation(new ConfiguredProduceDestination(BULK_OP_GEO_INDEX),
          new CSVWithGeoPointBuilder(new ConfiguredTypeBuilder(BULK_OP_GEO_MAPPING)));
      producer.setAction(new ConfiguredAction(DocumentAction.INDEX));
      StandaloneProducer esProducer = new StandaloneProducer(conn, producer);
      try {
        TransportClient client = conn.createClient();
        AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage(CsvGeopointBuilderTest.CSV_WITH_LATLONG);
        BaseCase.start(esProducer);
        esProducer.doService(msg);

        // Success; we should check that the thing exists.
        GetResponse getter = client.prepareGet(BULK_OP_GEO_INDEX, BULK_OP_GEO_MAPPING, "UID-1").get();
        assertNotNull(getter.getSourceAsString());
        assertEquals("UID-1", getter.getSourceAsMap().get("productuniqueid"));

        // Now update it.
        producer.setAction(new ConfiguredAction(DocumentAction.UPDATE));
        esProducer.doService(msg);
        getter = client.prepareGet(BULK_OP_GEO_INDEX, BULK_OP_GEO_MAPPING, "UID-1").get();
        assertNotNull(getter.getSourceAsString());

        // Now delete it
        producer.setAction(new ConfiguredAction(DocumentAction.DELETE));
        esProducer.doService(msg);
        getter = client.prepareGet(BULK_OP_GEO_INDEX, BULK_OP_GEO_MAPPING, "UID-1").get();
        assertNull(getter.getSourceAsString());

      }
      finally {
        BaseCase.stop(esProducer);
      }
    }
    else {
      System.err.println("Tests Disabled");
    }
  }
}
