package com.carrefour.inno.qm.conf;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


@Configuration
public class RestTemplateConfig {

	final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

	@Value("${READ_TIMEOUT:3000}")
	private int readTimeout;

	@Value("${CONNECTION_TIMEOUT:3000}")
	private int connectionTimeout;

	@Value("${MAX_CONNECTIONS_PER_ROUTE:180}")
	private int maxConnectionsPerRoute;

	@Value("${TOTAL_CONNECTIONS:200}")
	private int totalConnections;

	@Bean
	public RestTemplate restTemplate()
			throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

		logger.info(">>>HTTP client config, total connections: {}, max connection/route: {}, "
						+ "connection timeout: {}, read timeout: {}",
				totalConnections, maxConnectionsPerRoute, connectionTimeout, readTimeout);

//    	TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
//
//        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
//                .loadTrustMaterial(null, acceptingTrustStrategy)
//                .build();
//
//        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
//
//        CloseableHttpClient httpClient = HttpClients.custom()
//                .setConnectionManager(buildConnectionManager())
//        		.setSSLSocketFactory(csf)
//                .build();


		TrustStrategy acceptingTrustStrategy = new TrustStrategy() {
			public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
				return true;
			}
		};

		SSLContext sslContext = null;
		try {
			sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			//throw new ServiceException(GlobalErrorMessage.INTERNAL_SERVER_ERROR);
			throw new RuntimeException(e);
		}

		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

		CloseableHttpClient httpClient = HttpClients.custom()
				.setSSLSocketFactory(sslConnectionFactory)
				.setConnectionManager(buildConnectionManager(sslConnectionFactory))
				.build();


		HttpComponentsClientHttpRequestFactory requestFactory =
				new HttpComponentsClientHttpRequestFactory();

		requestFactory.setHttpClient(httpClient);

		requestFactory.setReadTimeout(readTimeout);
		requestFactory.setConnectTimeout(connectionTimeout);

		RestTemplate restTemplate = new RestTemplate(requestFactory);
		return restTemplate;
	}

	private PoolingHttpClientConnectionManager buildConnectionManager(SSLConnectionSocketFactory sslConnectionFactory) {

		Registry<ConnectionSocketFactory> socketFactoryRegistry =
				RegistryBuilder.<ConnectionSocketFactory> create().register("https", sslConnectionFactory).build();

		PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		poolingHttpClientConnectionManager.setMaxTotal(totalConnections);
		poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);

		return poolingHttpClientConnectionManager;
	}
}