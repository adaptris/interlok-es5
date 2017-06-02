package com.adaptris.core.es5;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.es5.actions.ActionExtractor;
import com.adaptris.core.es5.actions.ConfiguredAction;
import com.adaptris.core.services.splitter.CloseableIterable;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * INDEX/UPDATE/DELETE a document(s) to ElasticSearch.
 * 
 * <p>
 * {@link ProduceDestination#getDestination(AdaptrisMessage)} should return the index of document that we are submitting to into
 * ElasticSearch; the {@code type} will be derived from the DocumentWrapper itself.
 * </p>
 * <p>
 * Of course, you can configure a {@link ElasticDocumentBuilder} implementation that creates multiple documents, but this will mean
 * that all operations are made individually using the standard single document API rather than the BULK API. For performance
 * reasons you should consider using {@link BulkOperation} where appropriate.
 * </p>
 * <p>
 * The action for each document is driven by the configured {@link ActionExtractor} instance. In the event of an
 * {@link DocumentAction#UPSERT} action then the same {@link XContentBuilder} from the {@link DocumentWrapper} is used as both the
 * update and upsert document via {@code setDoc(XContentBuilder}} and {@code setUpsert(XContentBuilder)}. This makes the assumption
 * that the document generated contains all the data required, not just a subset. If in doubt; stick to a normal
 * {@link DocumentAction#UPDATE} which will correctly throw a {@code DocumentMissingException}.
 * </p>
 * 
 * 
 * @author lchan
 * @config es5-single-operation
 *
 */
@XStreamAlias("es5-single-operation")
@ComponentProfile(summary = "Use the standard API to interact with an ElasticSearch 5.x instance", tag = "producer,elastic")
@DisplayOrder(order =
{
    "action", "documentBuilder", "refreshPolicy"
})
public class SingleOperation extends ElasticSearchProducer {
  private static final ActionExtractor DEFAULT_ACTION = new ConfiguredAction(DocumentAction.INDEX);

  protected transient TransportClient transportClient = null;

  @Valid
  @NotNull
  @AutoPopulated
  private ElasticDocumentBuilder documentBuilder;

  @AdvancedConfig
  @Valid
  private ActionExtractor action;

  @AdvancedConfig
  private String refreshPolicy;

  public SingleOperation() {
    setDocumentBuilder(new SimpleDocumentBuilder());
  }

  public SingleOperation(ProduceDestination dest, ElasticDocumentBuilder b) {
    this();
    setDestination(dest);
    setDocumentBuilder(b);
  }

  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
    request(msg, destination, defaultTimeout());
  }

  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination destination, long timeout) throws ProduceException {
    try {
      final String index = destination.getDestination(msg);
      try (CloseableIterable<DocumentWrapper> docs = ensureCloseable(documentBuilder.build(msg))) {
        for (DocumentWrapper doc : docs) {
          DocumentAction action = doc.action() != null ? doc.action() : DocumentAction.valueOf(actionExtractor().extract(msg, doc));
          switch (action) {
          case INDEX: {
            IndexResponse response = transportClient.prepareIndex(index, doc.type(), doc.uniqueId()).setRouting(doc.routing())
                .setParent(doc.parent()).setSource(doc.content()).setRefreshPolicy(getRefreshPolicy()).get();
            log.trace("INDEX:: document {} version {} in {}", response.getId(), response.getVersion(), index);
            break;
          }
          case UPDATE: {
            UpdateResponse response = transportClient.prepareUpdate(index, doc.type(), doc.uniqueId()).setRouting(doc.routing())
                .setParent(doc.parent()).setDoc(doc.content()).setRefreshPolicy(getRefreshPolicy()).get();
            log.trace("UPDATE:: document {} version {} in {}", response.getId(), response.getVersion(), index);
            break;
          }
          case DELETE: {
            DeleteResponse response = transportClient.prepareDelete(index, doc.type(), doc.uniqueId()).setRouting(doc.routing())
                .setParent(doc.parent()).setRefreshPolicy(getRefreshPolicy()).get();
            log.trace("DELETE:: document {} version {} in {}", response.getId(), response.getVersion(), index);
            break;
          }
          case UPSERT: {
            UpdateResponse response = transportClient.prepareUpdate(index, doc.type(), doc.uniqueId()).setRouting(doc.routing())
                .setParent(doc.parent()).setDoc(doc.content()).setUpsert(doc.content()).get();
            log.trace("UPSERT:: document {} version {} in {}", response.getId(), response.getVersion(), index);
          }
          default:
            throw new ProduceException("Unrecognized action: " + action);
          }
        }
      }
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
    return msg;
  }

  @Override
  public void close() {
    super.close();
    retrieveConnection(ElasticSearchConnection.class).closeQuietly(transportClient);
  }

  @Override
  public void init() throws CoreException {
    super.init();
    transportClient = retrieveConnection(ElasticSearchConnection.class).createClient();
  }

  /**
   * @return the documentBuilder
   */
  public ElasticDocumentBuilder getDocumentBuilder() {
    return documentBuilder;
  }

  /**
   * @param b the documentBuilder to set
   */
  public void setDocumentBuilder(ElasticDocumentBuilder b) {
    this.documentBuilder = b;
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

  /**
   * @return the refreshPolicy
   */
  public String getRefreshPolicy() {
    return refreshPolicy;
  }

  /**
   * Set the refresh policy.
   * 
   * @param s the refreshPolicy to set, generally "true", "false" or "wait_until", default is null.
   */
  public void setRefreshPolicy(String s) {
    this.refreshPolicy = s;
  }
}
