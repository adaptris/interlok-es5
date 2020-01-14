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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.ProducerCase;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.es5.types.ConfiguredTypeBuilder;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class BulkOperationTest extends ProducerCase {

  private static final String EXAMPLE_COMMENT_HEADER = "\n<!--" + "\n-->\n";

  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }

  @Test
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
