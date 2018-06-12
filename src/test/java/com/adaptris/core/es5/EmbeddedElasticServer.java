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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.adaptris.core.PortManager;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic.Builder;
import pl.allegro.tech.embeddedelasticsearch.IndexSettings;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

public class EmbeddedElasticServer {

  private static final String DEFAULT_ELASTIC_VERSION = "5.2.2";
  private static final int ES_BASE_PORT = 9300;
  private EmbeddedElastic instance = null;
  private Builder builder = EmbeddedElastic.builder();
  private Integer tcpPort;
  private Integer httpPort;
  private String clusterName;

  public EmbeddedElasticServer() {
    this("elasticsearch", DEFAULT_ELASTIC_VERSION);
  }

  public EmbeddedElasticServer(String clusterName) {
    this(clusterName, DEFAULT_ELASTIC_VERSION);
  }

  public EmbeddedElasticServer(String name, String version) {
    tcpPort = PortManager.nextUnusedPort(ES_BASE_PORT);
    httpPort = PortManager.nextUnusedPort(ES_BASE_PORT);
    this.clusterName = name;
    builder = EmbeddedElastic.builder().withElasticVersion(version).withSetting(PopularProperties.CLUSTER_NAME,
        name).withSetting(PopularProperties.TRANSPORT_TCP_PORT, tcpPort).withSetting(PopularProperties.HTTP_PORT, httpPort);
  }

  public ElasticSearchConnection createConnection() {
    ElasticSearchConnection c = new ElasticSearchConnection(new KeyValuePairSet(Arrays.asList(new KeyValuePair[]
    {
      new KeyValuePair("cluster.name", clusterName)
    })), "localhost:" + tcpPort); 
    return c;
  }

  public void addIndex(String name, String type, String mappingSpec) {
    builder.withIndex(name, IndexSettings.builder().withType(type, mappingSpec).build());
  }

  public void addIndex(String name, String type, InputStream mappingSpec) throws IOException {
    builder.withIndex(name, IndexSettings.builder().withType(type, mappingSpec).build());
  }

  public void addIndex(String name, String type, File mappingSpec) throws IOException {
    try (InputStream in = new FileInputStream(mappingSpec)) {
      addIndex(name, type, in);
    }
  }

  public void start() throws IOException, InterruptedException {
    try {
      instance = builder.withStartTimeout(120, TimeUnit.SECONDS).build();
      instance.start();
    }
    catch (IOException | InterruptedException e) {
      stop();
      throw e;
    }
  }

  public void stop() {
    if (instance != null) {
      try {
        instance.stop();
        PortManager.release(instance.getTransportTcpPort());
        PortManager.release(instance.getHttpPort());
        PortManager.release(tcpPort);
        PortManager.release(httpPort);
        instance = null;
      }
      catch (Exception e) {

      }
    }
  }

}
