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

import javax.validation.constraints.Min;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.es5.actions.ActionExtractor;
import com.adaptris.core.services.splitter.CloseableIterable;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Index/Delete/Update a document(s) to ElasticSearch.
 * 
 * <p>
 * {@link ProduceDestination#getDestination(AdaptrisMessage)} should return the index that the documents will be inserted against
 * ElasticSearch; the {@code type} is taken from the DocumentBuilder
 * </p>
 * <p>
 * The action for each document is driven by the configured {@link ActionExtractor} instance. In the event of an
 * {@link DocumentAction#UPSERT} action then the same {@link XContentBuilder} from the {@link DocumentWrapper} is used as both the
 * update and upsert document via {@code setDoc(XContentBuilder}} and {@code setUpsert(XContentBuilder)}. This makes the assumption
 * that the document generated contains all the data required, not just a subset. If in doubt; stick to a normal
 * {@link DocumentAction#UPDATE} which will throw a {@code DocumentMissingException} failing the messages.
 * </p>
 * 
 * @author lchan
 * @config es5-bulk-operation
 *
 */
@XStreamAlias("es5-bulk-operation")
@AdapterComponent
@ComponentProfile(summary = "Use the bulk API to interact with an ElasticSearch 5.x instance", tag = "producer,elastic,bulk,batch")
@DisplayOrder(order =
{
    "batchWindow", "documentBuilder", "action", "refreshPolicy"
})
public class BulkOperation extends SingleOperation {

  private static final int DEFAULT_BATCH_WINDOW = 10000;

  @Min(0)
  @InputFieldDefault(value = "10000")
  private Integer batchWindow;

  public BulkOperation() {
    super();
  }

  public BulkOperation(ProduceDestination dest, ElasticDocumentBuilder b) {
    super(dest, b);
  }

  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination destination, long timeout) throws ProduceException {
    try {
      final String index = destination.getDestination(msg);
      BulkRequestBuilder bulkRequest = transportClient.prepareBulk().setRefreshPolicy(getRefreshPolicy());
      long total = 0;
      try (CloseableIterable<DocumentWrapper> docs = ensureCloseable(getDocumentBuilder().build(msg))) {
        int count = 0;
        for (DocumentWrapper doc : docs) {
          count++;
          total++;
          DocumentAction action = doc.action() != null ? doc.action() : DocumentAction.valueOf(actionExtractor().extract(msg, doc));
          switch(action) {
          case INDEX:
            bulkRequest.add(transportClient.prepareIndex(index, doc.type(), doc.uniqueId()).setRouting(doc.routing())
                .setParent(doc.parent()).setSource(doc.content()));
            break;
          case UPDATE:
            bulkRequest.add(transportClient.prepareUpdate(index, doc.type(), doc.uniqueId()).setRouting(doc.routing())
                .setParent(doc.parent()).setDoc(doc.content()));
            break;
          case DELETE:
            bulkRequest.add(
                transportClient.prepareDelete(index, doc.type(), doc.uniqueId()).setRouting(doc.routing()).setParent(doc.parent()));
            break;
          case UPSERT:
            bulkRequest.add(transportClient.prepareUpdate(index, doc.type(), doc.uniqueId()).setRouting(doc.routing())
                .setParent(doc.parent()).setDoc(doc.content()).setUpsert(doc.content()));
            break;
          default:
            throw new ProduceException("Unrecognized action: " + action);
          }
          if (count >= batchWindow()) {
            doSend(bulkRequest);
            count = 0;
            bulkRequest = transportClient.prepareBulk().setRefreshPolicy(getRefreshPolicy());
          }
        }
      }
      if (bulkRequest.numberOfActions() > 0) {
        doSend(bulkRequest);
      }
      log.trace("Produced a total of {} documents", total);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
    return msg;
  }

  private void doSend(BulkRequestBuilder bulkRequest) throws Exception {
    int count = bulkRequest.numberOfActions();
    BulkResponse response = bulkRequest.get();
    if (response.hasFailures()) {
      throw new ProduceException(response.buildFailureMessage());
    }
    log.trace("Producing batch of {} requests took {}", count, response.getTook().toString());
    return;
  }

  /**
   * @return the batchCount
   */
  public Integer getBatchWindow() {
    return batchWindow;
  }

  /**
   * @param b the batchCount to set
   */
  public void setBatchWindow(Integer b) {
    this.batchWindow = b;
  }

  int batchWindow() {
    return getBatchWindow() != null ? getBatchWindow().intValue() : DEFAULT_BATCH_WINDOW;
  }

}
