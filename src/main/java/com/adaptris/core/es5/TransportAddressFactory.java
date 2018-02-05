package com.adaptris.core.es5;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;

import org.elasticsearch.common.transport.TransportAddress;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;

// Trying to negotiate the differences between the v5 and v6 API.
class TransportAddressFactory {

  private static final String V5_TRANSPORT_CLASS = "org.elasticsearch.common.transport.InetSocketTransportAddress";
  private static final String TRANSPORT_ADDDRESS = "org.elasticsearch.common.transport.TransportAddress";

  static TransportAddress create(InetSocketAddress addr) throws CoreException {
    try {
      if (isInterface(TRANSPORT_ADDDRESS)) {
        return create(Class.forName(V5_TRANSPORT_CLASS), addr);
      }
      // in 6.1.2 it's TransportAddress == a final class.
      return create(Class.forName(TRANSPORT_ADDDRESS), addr);
    }
    catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  private static boolean isInterface(String clazz) {
    try {
      Class c = Class.forName(clazz);
      return c.isInterface();
    }
    catch (Exception e) {
      return false;
    }
  }

  private static TransportAddress create(Class clazz, InetSocketAddress addr) throws Exception {
    Constructor construct = clazz.getConstructor(addr.getClass());
    return (TransportAddress) construct.newInstance(addr);
  }
                                                   
}
