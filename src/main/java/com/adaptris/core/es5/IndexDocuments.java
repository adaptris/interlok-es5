package com.adaptris.core.es5;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.services.splitter.CloseableIterable;
import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Add a document(s) to ElasticSearch.
 * 
 * <p>
 * {@link ProduceDestination#getDestination(AdaptrisMessage)} should return the type of document that we are submitting to into
 * ElasticSearch; the {@code index} is taken from the underlying {@link ElasticSearchConnection}.
 * </p>
 * 
 * @author lchan
 * @config es5-index-document
 *
 */
@XStreamAlias("es5-index-document")
@ComponentProfile(summary = "Use the standard API to produce to an ElasticSearch 5.x instance", tag = "producer,elastic")
@DisplayOrder(order =
{
    "action", "documentBuilder"
})
public class IndexDocuments extends ElasticSearchProducer {

  protected transient TransportClient transportClient = null;

  @Valid
  @NotNull
  @AutoPopulated
  private ElasticDocumentBuilder documentBuilder;

  public IndexDocuments() {
    setDocumentBuilder(new SimpleDocumentBuilder());
  }

  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
    request(msg, destination, defaultTimeout());
  }


  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination destination, long timeout) throws ProduceException {
    try {
      final String type = destination.getDestination(msg);
      final String index = retrieveConnection(ElasticSearchConnection.class).getIndex();
      try (CloseableIterable<DocumentWrapper> docs = ensureCloseable(documentBuilder.build(msg))) {
        docs.forEach(e -> {
          IndexResponse response = transportClient.prepareIndex(index, type, e.uniqueId()).setSource(e.content()).get();
          log.trace("Added document {} version {} to {}", response.getId(), response.getVersion(), index);
        });
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



}
