package com.adaptris.core.es5;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.es5.actions.ActionExtractor;
import com.adaptris.core.es5.actions.ConfiguredAction;
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
 * 
 * @author lchan
 * @config es5-bulk-operation
 *
 */
@XStreamAlias("es5-bulk-operation")
@AdapterComponent
@ComponentProfile(summary = "Use the bulk API to produce to an ElasticSearch 5.x instance", tag = "producer,elastic,bulk")
@DisplayOrder(order =
{
    "batchWindow", "documentBuilder", "action",
})
public class BulkIndexDocuments extends IndexDocuments {

  private static final ActionExtractor DEFAULT_ACTION = new ConfiguredAction(DocumentAction.INDEX);
  private static final int DEFAULT_BATCH_WINDOW = 10000;

  @Min(0)
  private Integer batchWindow;
  
  @AdvancedConfig
  @Valid
  private ActionExtractor action;

  public BulkIndexDocuments() {
    super();
  }

  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination destination, long timeout) throws ProduceException {
    try {
      final String index = destination.getDestination(msg);
      BulkRequestBuilder bulkRequest = transportClient.prepareBulk();
      try (CloseableIterable<DocumentWrapper> docs = ensureCloseable(getDocumentBuilder().build(msg))) {
        int count = 0;
        for (DocumentWrapper doc : docs) {
          count++;
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
            bulkRequest
                .add(transportClient.prepareDelete(index, doc.type(), doc.uniqueId()).setRouting(doc.routing())
                    .setParent(doc.parent()));
            break;
          default:
            throw new ProduceException("Unrecognized action: " + action);
          }
          if (count >= batchWindow()) {
            doSend(bulkRequest);
            count = 0;
            bulkRequest = transportClient.prepareBulk();
          }
        }
      }
      if (bulkRequest.numberOfActions() > 0) {
        doSend(bulkRequest);
      }
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
    log.trace("Producing batch of {} actions took {}", count, response.getTook().toString());
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


  public ActionExtractor getAction() {
    return action;
  }


  /**
   * Set the action to be performed in the event the {@link DocumentWrapper} does not specify it.
   * 
   * @param action the action, the default will be to INDEX
   */
  public void setAction(ActionExtractor action) {
    this.action = action;
  }

  protected ActionExtractor actionExtractor() {
    return getAction() != null ? getAction() : DEFAULT_ACTION;
  }
}
