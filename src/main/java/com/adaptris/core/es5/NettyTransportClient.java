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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.network.NetworkModule;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.transport.Netty4Plugin;

import io.netty.util.ThreadDeathWatcher;
import io.netty.util.concurrent.GlobalEventExecutor;

// Essentially a copy of PreBuiltTransportClient but only with Netty4 plugins
// Otherwise we get into dependency hell.
public class NettyTransportClient extends TransportClient {

  private static final Collection<Class<? extends Plugin>> PRE_INSTALLED_PLUGINS = Collections.unmodifiableList(
      Arrays.asList(Netty4Plugin.class));

  @SafeVarargs
  public NettyTransportClient(Settings settings, Class<? extends Plugin>... plugins) {
    this(settings, Arrays.asList(plugins));
  }

  public NettyTransportClient(Settings settings, Collection<Class<? extends Plugin>> plugins) {
    super(settings, addPlugins(plugins, PRE_INSTALLED_PLUGINS));
  }

  @Override
  public void close() {
    super.close();
    if (NetworkModule.TRANSPORT_TYPE_SETTING.exists(settings) == false
        || NetworkModule.TRANSPORT_TYPE_SETTING.get(settings).equals(Netty4Plugin.NETTY_TRANSPORT_NAME)) {
      try {
        GlobalEventExecutor.INSTANCE.awaitInactivity(5, TimeUnit.SECONDS);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      try {
        ThreadDeathWatcher.awaitInactivity(5, TimeUnit.SECONDS);
      }
      catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
