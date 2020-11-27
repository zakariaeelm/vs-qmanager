package com.carrefour.inno.qm.conf;


import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

//@Configuration
public class WebClientConfig {

    private static final int READ_TIMEOUT = 10000;

    public static final String DEFAULT_REST_BEAN_NAME = "defaultRestTemplate";

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));


    private final Proxy proxy;

    public WebClientConfig() {
        this.proxy = new Proxy();
        //setters
    }

    //@Primary
    //@Bean(name = DEFAULT_REST_BEAN_NAME)
    public RestTemplate deaultRestTemplateConfig() {
        RestTemplate restTemplate = new RestTemplate(converters());
        restTemplate.setRequestFactory(buildRequestFactory());
        return restTemplate;
    }

    protected HttpComponentsClientHttpRequestFactory buildRequestFactory() {

        HttpClientBuilder clientBuilder = HttpClientBuilder.create();
        clientBuilder.useSystemProperties();

        if (proxy != null && proxy.isEnabled()) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(new AuthScope(proxy.getHost(), proxy.getPort()),
                    new UsernamePasswordCredentials(proxy.getUser(), proxy.getPassword().toString()));
            //clientBuilder.useSystemProperties();
            clientBuilder.setProxy(new HttpHost(proxy.getHost(), proxy.getPort()));
            clientBuilder.setDefaultCredentialsProvider(credsProvider);
            clientBuilder.setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy());
        }

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

        SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());
        CloseableHttpClient httpClient = clientBuilder.setSSLSocketFactory(connectionFactory).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        requestFactory.setReadTimeout(READ_TIMEOUT);

        return requestFactory;
    }

    public List<HttpMessageConverter<?>> converters(){
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        messageConverters.add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        messageConverters.add(new FormHttpMessageConverter());
        messageConverters.add(new ByteArrayHttpMessageConverter());
        messageConverters.add(getMappingJackson2HttpMessageConverter());
        return messageConverters;
    }

    //@Bean
    public MappingJackson2HttpMessageConverter getMappingJackson2HttpMessageConverter() {

        MappingJackson2HttpMessageConverter jsonConverter =
                new MappingJackson2HttpMessageConverter(getJacksonObjectMapper());
        jsonConverter.setSupportedMediaTypes(Arrays.asList(contentType));
        return jsonConverter;
    }

    //@Bean
    //v@Primary
    public ObjectMapper getJacksonObjectMapper() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        builder.serializationInclusion(Include.NON_NULL);
        builder.modules(new JavaTimeModule());
        return builder.build();
    }

    public static class Proxy {
        private boolean enabled;
        private String host;
        private int port;
        private String user;
        private CharSequence password;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public CharSequence getPassword() {
            return password;
        }

        public void setPassword(CharSequence password) {
            this.password = password;
        }
    }
}