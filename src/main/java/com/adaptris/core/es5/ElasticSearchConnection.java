package com.adaptris.core.es5;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.URLName;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.lease.Releasable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.CoreException;
import com.adaptris.core.NoOpConnection;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * 
 * @config es5-connection
 */
@XStreamAlias("es5-connection")
@AdapterComponent
@ComponentProfile(summary = "Connection to an ElasticSearch 5.x instance", tag = "connections,elastic")
@DisplayOrder(order =
{
    "transport-url", "settings"
})
public class ElasticSearchConnection extends NoOpConnection {

  private static final TransportClientFactory DEFAULT_CLIENT_FACTORY = new TransportClientFactory() {
    @Override
    public TransportClient create(Settings s) {
      return new NettyTransportClient(s);
    }
  };

  @XStreamImplicit(itemFieldName = "transport-url")
  @Size(min = 1)
  @Valid
  private List<String> transportUrls;

  @NotNull
  @AutoPopulated
  @Valid
  private KeyValuePairSet settings = null;

  @Valid
  @AdvancedConfig
  private TransportClientFactory transportClientFactory;

  public ElasticSearchConnection() {
    setTransportUrls(new ArrayList<String>());
    setSettings(new KeyValuePairSet());
  }

  public ElasticSearchConnection(KeyValuePairSet settings, String... transportUrls) {
    this();
    setSettings(settings);
    setTransportUrls(new ArrayList<String>(Arrays.asList(transportUrls)));
  }

  protected TransportClient createClient() throws CoreException {
    // Settings s = Settings.settingsBuilder().put(asMap(getSettings())).build();
    // TransportClient transportClient = TransportClient.builder().settings(s).build();
    Settings s = Settings.builder().put(KeyValuePairBag.asMap(getSettings())).build();
    TransportClient transportClient = transportClientFactory().create(s);
    for (String url : getTransportUrls()) {
      transportClient.addTransportAddress(new InetSocketTransportAddress(new InetSocketAddress(getHost(url), getPort(url))));
    }
    return transportClient;
  }

  public KeyValuePairSet getSettings() {
    return settings;
  }

  public void setSettings(KeyValuePairSet kvps) {
    this.settings = Args.notNull(kvps, "Settings");
  }

  public List<String> getTransportUrls() {
    return transportUrls;
  }

  public void setTransportUrls(List<String> transports) {
    this.transportUrls = Args.notNull(transports, "Transport URLS");
  }

  public void addTransportUrl(String url) {
    transportUrls.add(Args.notNull(url, "URL"));
  }

  /**
   * @return the transportClientFactory
   */
  public TransportClientFactory getTransportClientFactory() {
    return transportClientFactory;
  }

  /**
   * @param t the transportClientFactory to set
   */
  public void setTransportClientFactory(TransportClientFactory t) {
    this.transportClientFactory = t;
  }

  TransportClientFactory transportClientFactory() {
    return getTransportClientFactory() != null ? getTransportClientFactory() : DEFAULT_CLIENT_FACTORY;
  }

  protected void closeQuietly(TransportClient c) {
    doClose((Releasable) c);
  }

  private static void doClose(Releasable c) {
    try {
      if (c != null) {
        c.close();
      }
    }
    catch (Exception e) {
      ;
    }
  }

  private static String getHost(String hostUrl) {
    String result = hostUrl;
    if (hostUrl.contains("://")) {
      result = new URLName(hostUrl).getHost();
    }
    else {
      result = hostUrl.substring(0, hostUrl.lastIndexOf(":"));
    }
    return result;
  }

  private static Integer getPort(String hostUrl) {
    Integer result = 0;
    if (hostUrl.contains("://")) {
      result = new URLName(hostUrl).getPort();
    }
    else {
      String s = hostUrl.substring(hostUrl.lastIndexOf(":") + 1);
      s.replaceAll("/", "");
      result = Integer.parseInt(s);
    }
    return result;
  }

}
