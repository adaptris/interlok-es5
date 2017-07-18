package com.adaptris.core.es5;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.common.xcontent.XContentBuilder;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceException;
import com.adaptris.core.es5.types.TypeBuilder;
import com.adaptris.core.util.ExceptionHelper;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Creates a JSON document for elastic search.
 * 
 * <p>
 * The document that is created contains the following characteristics
 * <ul>
 * <li>The message's uniqueID is used as the ID of the document.
 * <li>The message payload is assumed to be a JSON object (not an array) and becomes the document..
 * </ul>
 * </p>
 * 
 * 
 * @config es5-json-document-builder
 *
 */
@XStreamAlias("es5-json-document-builder")
public class JsonDocumentBuilder extends JsonDocumentBuilderImpl {


  public JsonDocumentBuilder() {
    super();
  }

  public JsonDocumentBuilder(TypeBuilder b) {
    this();
    setTypeBuilder(b);
  }

  @Override
  public Iterable<DocumentWrapper> build(AdaptrisMessage msg) throws ProduceException {
    List<DocumentWrapper> result = new ArrayList<>();
    try (Reader buf = msg.getReader()) {
      ObjectMapper mapper = new ObjectMapper();
      JsonParser parser = mapper.getFactory().createParser(buf);
      if (parser.nextToken() != JsonToken.START_OBJECT) {
        throw new ProduceException("Expected the start of a JSON object");
      }
      ObjectNode node = mapper.readTree(parser);
      XContentBuilder jsonContent = jsonBuilder(node);
      result.add(new DocumentWrapper(msg.getUniqueId(), jsonContent, getTypeBuilder().getType(msg)));
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapProduceException(e);
    }
    return result;
  }

}
