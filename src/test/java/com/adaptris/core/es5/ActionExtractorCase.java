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

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.es5.actions.ConfiguredAction;
import com.adaptris.core.es5.actions.JsonPathAction;
import com.adaptris.core.es5.actions.MappedAction;
import com.adaptris.core.es5.actions.MetadataAction;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairList;

public class ActionExtractorCase {

  @Test
  public void testConstantAction() {
    for(DocumentAction val: DocumentAction.values()) {
      ConfiguredAction action = new ConfiguredAction();
      action.setAction(val.name());
      assertEquals(val.name(), action.extract(AdaptrisMessageFactory.getDefaultInstance().newMessage(), null));
    }
    AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    msg.addMetadata("key", "index");
    ConfiguredAction action = new ConfiguredAction("%message{key}");
    assertEquals("index", action.extract(msg, null));
  }
  
  @Test
  public void testMetadataAction() {
    final String KEY = "my_action";
    MetadataAction action = new MetadataAction();
    action.setMetadataKey(KEY);
    for(DocumentAction val: DocumentAction.values()) {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      msg.addMetadata(KEY, val.name());
      assertEquals(val.name(), action.extract(msg, null));
    }
  }
  
  @Test
  public void testJsonPathAction() throws IOException, ServiceException {
    final String FIELD_NAME = "myaction";
    final String JSON_PATH = "$." + FIELD_NAME;
    JsonPathAction action = new JsonPathAction();
    action.setJsonPath(JSON_PATH);
    for(DocumentAction val: DocumentAction.values()) {
      XContentBuilder builder = XContentFactory.jsonBuilder();
      builder.startObject();
      builder.field(FIELD_NAME, val.name());
      builder.endObject();
      assertEquals(val.name(), action.extract(null, new DocumentWrapper("uid", builder, "")));
    }
  }
  
  @Test
  public void testMappedAction() throws ServiceException {
    final String KEY = "myaction";
    MetadataAction ma = new MetadataAction();
    ma.setMetadataKey(KEY);
    MappedAction action = new MappedAction();
    action.setAction(ma);
    KeyValuePairList mappings = new KeyValuePairList();
    for(DocumentAction val: DocumentAction.values()) {
      mappings.add(new KeyValuePair(val.name().substring(0, 3), val.name()));
    }
    action.setMappings(mappings);
    
    for(DocumentAction val: DocumentAction.values()) {
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage();
      msg.addMetadata(KEY, val.name().substring(0, 3));
      
      assertEquals(val, DocumentAction.valueOf(action.extract(msg, null)));
    }
  }

}
