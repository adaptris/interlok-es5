package com.adaptris.core.es5;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;

public interface TransportClientFactory {
  TransportClient create(Settings s);
}
