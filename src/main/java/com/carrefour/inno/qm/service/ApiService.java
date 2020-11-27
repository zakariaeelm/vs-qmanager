package com.carrefour.inno.qm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.carrefour.inno.qm.model.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.RequestEntity.post;
import static org.springframework.http.RequestEntity.get;

@Service
public class ApiService {

    final Logger logger = LoggerFactory.getLogger(ApiService.class);

    private RestTemplate restTemplate;

    private final String AUTH_PROD_URL;
    private final String INTI_CLIENT_ID;
    private final String INTI_CLIENT_SECRET;
    private final String LDAP_USER;
    private final String LDAP_PWD;
    private final String HEADER_CLIENT_ID;
    private final String HEADER_CLIENT_SECRET;
    private final String HEADER_TOKEN;

    public ApiService(RestTemplate restTemplate,
                      @Value("${api.carrefour.prod.inti.authentication}") String AUTH_PROD_URL,
                      @Value("${api.carrefour.prod.inti.clientid}") String INTI_CLIENT_ID,
                      @Value("${api.carrefour.prod.inti.clientsecret}") String INTI_CLIENT_SECRET,
                      @Value("${api.carrefour.prod.inti.ldap.username}") String LDAP_USER,
                      @Value("${api.carrefour.prod.inti.ldap.password}") String LDAP_PWD,
                      @Value("${api.carrefour.header.clientid}") String HEADER_CLIENT_ID,
                      @Value("${api.carrefour.header.clientsecret}") String HEADER_CLIENT_SECRET,
                      @Value("${api.carrefour.header.token}") String HEADER_TOKEN) {

        this.restTemplate = restTemplate;
        this.AUTH_PROD_URL = AUTH_PROD_URL;
        this.INTI_CLIENT_ID = INTI_CLIENT_ID;
        this.INTI_CLIENT_SECRET = INTI_CLIENT_SECRET;
        this.LDAP_USER = LDAP_USER;
        this.LDAP_PWD = LDAP_PWD;
        this.HEADER_CLIENT_ID = HEADER_CLIENT_ID;
        this.HEADER_CLIENT_SECRET = HEADER_CLIENT_SECRET;
        this.HEADER_TOKEN = HEADER_TOKEN;
    }

    public Token generateToken(){
        URI uri = UriComponentsBuilder.fromHttpUrl(AUTH_PROD_URL)
                .build(true)
                .toUri();

        RequestEntity<String> request = post(uri)
                .accept(APPLICATION_JSON)
                .header( HEADER_CLIENT_ID,INTI_CLIENT_ID)
                .header(HEADER_CLIENT_SECRET,INTI_CLIENT_SECRET)
                .body("{\"ldap_user\":\"" + LDAP_USER + "\",\"ldap_password\":\"" + LDAP_PWD + "\"}");

        try{

            ResponseEntity<String> result = restTemplate.exchange(request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            Token token = mapper.readValue(result.getBody(), Token.class);

            //return  result.getBody();
            return  token;

        } catch (Exception e) {
            logger.error("failed to send request : message ", e);
        }
        return null;
    }
    public String httpsPostCall(String baseUrl, String body){

        String token = generateToken().getToken();
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .build(true).toUri();

        RequestEntity<String> request = post(uri)
                .accept(APPLICATION_JSON)
                .contentType(APPLICATION_JSON)
                .header( HEADER_CLIENT_ID,INTI_CLIENT_ID)
                .header(HEADER_CLIENT_SECRET,INTI_CLIENT_SECRET)
                .header(HEADER_TOKEN, token)
                .body(body);

        try{

            ResponseEntity<String> result = restTemplate.exchange(request, String.class);
            return  result.getBody();

        } catch (Exception e) {
            System.out.println("failed to send POST request : message" + e.getMessage());
        }
        return null;
    }
    public String httpsGetCall(String baseUrl, String params){

        String token = generateToken().getToken();
        URI uri = UriComponentsBuilder.fromHttpUrl(baseUrl + params)
                .build(true).toUri();

        RequestEntity<Void> request = get(uri)
                .accept(APPLICATION_JSON)
                .header(HEADER_CLIENT_ID,INTI_CLIENT_ID)
                .header(HEADER_CLIENT_SECRET,INTI_CLIENT_SECRET)
                .header(HEADER_TOKEN, token).build();

        try{

            ResponseEntity<String> result = restTemplate.exchange(request, String.class);

            return  result.getBody();

        } catch (Exception e) {
            System.out.println("failed to send request : message" + e.getMessage());
        }
        return null;
    }
}