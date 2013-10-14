package com.bosch.portal.ant.task.upload;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;


/**
 * 
 * @author cem8fe
 */
public class MySecureProtocolSocketFactory implements SecureProtocolSocketFactory {

	private static final SSLSocketFactory dportalSSLSocketFactory;
	private static final SSLSocketFactory defaultSSLSocketFactory;
	
	static {
		// initialisation of SSL Factory
		defaultSSLSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		SSLSocketFactory factory = defaultSSLSocketFactory;
		try {
			char[] password = "changeit".toCharArray();
		
			// load the customKeyStore
			String keystore = "/CustomKeystore";
			InputStream is = MySecureProtocolSocketFactory.class.getResourceAsStream(keystore);
						
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(is, password);

			// init the keymanager factory
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, password);

			// init the trustmanagerfactory
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ks);
			

			// create the ssl socket factory
			SSLContext context = SSLContext.getInstance("SSL");
			context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
			
			factory = context.getSocketFactory();
			
		} catch (Exception e) {
			e.printStackTrace();
			// if any exception, just use the default ssl socket factory
		}
		dportalSSLSocketFactory = factory;
	}
	
	/**
	 * @return the ssl socket factory
	 */
	private static SSLSocketFactory getSSLSocketFactory(String hostName) {
		if ("fe65894.de.bosch.com".equals(hostName))	
			return dportalSSLSocketFactory;
		else if ("rb-wam-d.bosch.com".equals(hostName))	
			return dportalSSLSocketFactory;			
		else
			return defaultSSLSocketFactory;
	}
	
	/**
	 * @see org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory#createSocket(java.net.Socket, java.lang.String, int, boolean)
	 */
	public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
			throws IOException, UnknownHostException {
        return getSSLSocketFactory(host).createSocket(socket, host, port, autoClose);
	}

	/**
	 * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int)
	 */
	public Socket createSocket(String host, int port) 
			throws IOException, UnknownHostException {
        return getSSLSocketFactory(host).createSocket(host, port);
	}

	/**
	 * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int)
	 */
	public Socket createSocket(String host, int port, InetAddress localAddress, int localPort)
			throws IOException, UnknownHostException {
        return getSSLSocketFactory(host).createSocket(host, port, localAddress, localPort);
	}

	/**
	 * @see org.apache.commons.httpclient.protocol.ProtocolSocketFactory#createSocket(java.lang.String, int, java.net.InetAddress, int, org.apache.commons.httpclient.params.HttpConnectionParams)
	 */
	public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) 
			throws IOException, UnknownHostException, ConnectTimeoutException {
        return getSSLSocketFactory(host).createSocket(host, port, localAddress, localPort);
	}

}
