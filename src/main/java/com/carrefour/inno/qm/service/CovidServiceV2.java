package com.carrefour.inno.qm.service;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.RequestEntity.post;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.carrefour.inno.qm.dao.StoreRepository;
import com.carrefour.inno.qm.dao.StoreRepositoryCustom;
import com.carrefour.inno.qm.model.PhenixTrxResponse;
import com.carrefour.inno.qm.model.Store;
import com.carrefour.inno.qm.model.StoreDTO;
import com.carrefour.inno.qm.model.Token;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CovidServiceV2 {

	private static final String FR_ZONE_ID = "Europe/Paris";
	private static final String DATE_FORMAT = "yyyyMMdd'T'HHmmss.SSSZ";
	
	private static final int TOKEN_GENERATION_DELAY = 900;
	private static final int MAX_REFRESH_TOKEN_RETRIES = 3;
	private static final int REFRESH_TOKEN_RETRY_DELAY = 10;
	
	private Token phenixToken;
	
    static final Logger logger = LoggerFactory.getLogger(CovidServiceV2.class);
    private final String storeLocalUrl;

    ApiService apiService;
    StoreRepositoryCustom storeRepositoryCustom;
    StoreRepository storeRepository;
    RestTemplate restTemplate;
	DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
    
    private ScheduledExecutorService scheduler;

    public CovidServiceV2(@Value("${api.carrefour.location}") String storeLocalUrl,
                        ApiService apiService,
                        StoreRepositoryCustom storeRepositoryCustom,
                        StoreRepository storeRepository,
                        RestTemplate restTemplate) {
        this.storeLocalUrl = storeLocalUrl;
        this.apiService = apiService;
        this.storeRepositoryCustom = storeRepositoryCustom;
        this.storeRepository = storeRepository;
        this.restTemplate = restTemplate;
        
        logger.info("initialise first phenix token");
        refreshPhenixToken();
        
        scheduler = Executors.newSingleThreadScheduledExecutor();
        logger.info("schedule refresh phenix token every: {} seconds", TOKEN_GENERATION_DELAY);
        scheduler.scheduleWithFixedDelay(this::refreshPhenixToken, 0, TOKEN_GENERATION_DELAY, TimeUnit.SECONDS);
    }
    
    private void refreshPhenixToken() {
    	try {
    		logger.info(">>>start refresh phenix token");
    		phenixToken = generatePhenixToken();
    		logger.info(">>>end refresh phenix token: {}", phenixToken.getToken());
    	} catch (Exception e) {
    		logger.error("error while refresh token", e.getMessage());
    	}
    }
    
    public Store incrementCount(String ean, int step){

        logger.info(">>> start add entrance in store ean: {}, ppsf: {}", ean);
        try {
            Store store = storeRepository.findByStoreEan(ean);
            if (store==null) {
            	throw new RuntimeException("store ean"+ean+" doesn't exist");
            }
            
            store.incrementCurrentState(step);
            store = storeRepository.save(store);
            logger.info(">>> new entrance registred in store ean: {}, ppsf: {}, currentState: {}", store.getStoreEan(), store.getPpsf(), store.getCurrentState());

            return store;
        } catch (Exception e) {
        	logger.error("unhandled error while increment entrance, ean: {}", ean, e);
        	throw new RuntimeException("unhandled error");
        }
    }
    
//    private Store retryIncrementCount(String ean, int step) {
//    	try {
//            logger.info("retry add entrance in store ean: {}, ppsf: {}", ean);
//    		return incrementCount(ean, step);
//    	} catch (Exception e) {
//        	logger.error("unhandled error while retry increment entrance, ean: {}", ean, e);
//        	throw new RuntimeException("unhandled error");
//        }
//    }
    
    public List<Store> findAll() {
        return storeRepository.findAll();
    }
    public static String generateInstant(){
        DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS")
                .withLocale( Locale.FRANCE)
                .withZone( ZoneId.systemDefault());
        Instant instant = Instant.now().plus(Duration.ofHours(1));
        String nowString = FOMATTER.format(instant) + "+0200";
        return nowString;        // 20200527T160819.623+0200
    }

    public static boolean isSameDay(String instantString){
        try {

            Instant instant = Instant.now();

            Date date = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSSZ").parse(instantString);
            Instant reqInstant = date.toInstant();

            return instant.truncatedTo(ChronoUnit.DAYS).equals(reqInstant.truncatedTo(ChronoUnit.DAYS));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }
    
    public Store findOne(String ean) {
        return storeRepository.findByStoreEanContaining(ean);
    }
    public Store initAndGetStore(String ppsf) {
    	
    	logger.info(">>>start get store info ppsf :{}", ppsf);
    	
    	Store store = storeRepository.findByPpsf(ppsf);
        if (store==null) {
        	throw new RuntimeException("store ppsf "+ppsf+" doesn't exist");
        }
        
        if (!isSameDay(store.getLastUpdate())) {
            store.setCurrentState("0");
            store.setLastTotalTrxNbr("0");
        	store.setLastUpdate(getCurrentDateAsString());
            store = storeRepository.save(store);
            logger.info("store state reset ppsf: {}", ppsf);
        }

        
        logger.info(">>>end get store info store :{}", store);
        
        return store;
    }

    public Store updateStoreWithNewState(String ean, String state) {
        Store store = storeRepository.findByStoreEanContaining(ean);
        try {

            store.setCurrentState(state);
            storeRepository.save(store);
            logger.debug("::: UPDATE Store ::: "+store.getStoreDesc()+" Updated ", store);
            return store;

        } catch (Exception e) {
            logger.error("ERROR ::: ",e);
        }

        return new Store();
    }

    public Store storeLocator(String ppsf, String capacity, String ratio) {
        
    	logger.info(">>>start get/ create store ppsf: {}", ppsf);
    	
        Store store = storeRepository.findByPpsf(ppsf);
        
        if (store == null){
        	logger.info("store dosen't exist create it ppsf: {}", ppsf);
        	
            String body = "{\"filters\":[{\"field\":\"additionalPartyIdentifications.additionalPartyIdentificationTypeCode\",\"keys\":[\"PPSF\"]},{\"field\":\"additionalPartyIdentifications.additionalPartyIdentification\",\"keys\":[\"" + ppsf + "\"]}],\"fields\":[\"locationName\",\"format\",\"additionalPartyIdentifications\"]}";
            try{
                String result = apiService.httpsPostCall(storeLocalUrl, body);
                JSONObject object = new JSONObject(result);
                JSONObject data = ((JSONArray)object.get("data")).getJSONObject(0);

                JSONArray list = (JSONArray)data.get("additionalPartyIdentifications");
                JSONObject storeEan = (JSONObject) list.get(0);

                ObjectMapper mapper = new ObjectMapper();
                StoreDTO storeDTO = mapper.readValue(data.toString(), StoreDTO.class);
                String ean = storeEan.getString("additionalPartyIdentification");
                
                store = new Store( ppsf, ean, storeDTO);
                store.setId(ean);
                store.setLastUpdate(getCurrentDateAsString());
                store.setCapacity(capacity);
                store.setCurrentState("0");
                store.setLastTotalTrxNbr("0");
                store.setCustomerByTrx(ratio);
                store.setRefreshInterval("1");
                store = storeRepository.save(store);

                logger.info("new store created : {}", store);
            } catch (Exception e) {
                //logger.error("failed to get product ::: ", e);
            	logger.error("error while request store ean, ppsf: {}", ppsf, e);
            	throw new RuntimeException("unhandled error");
            }
        } else {
            //store = storeRepository.findByPpsfContaining(ppsf);
            if(!isSameDay(store.getLastUpdate())) {
                store.setCurrentState("0");
                store.setLastTotalTrxNbr("0");
                logger.info("store state reset ppsf: {}", ppsf);
            }

            store.setLastUpdate(getCurrentDateAsString());
            store.setCapacity(capacity);
            store.setCustomerByTrx(ratio);
            store = storeRepository.save(store);
            
            logger.info("end get/ reset store store: {}", store);
        }

        return store;
    }
    
    private PhenixTrxResponse getTrxCount(String ean) {
        
//    	if (phenixToken.isExpired()) {
//    		//phenixToken = generatePhenixToken().getToken();
//    	}
        
//        Store store = findOne(ean);
        DatesQuery datesQuery = buildDatesQuery();
        String requestBody = 
        		"{\"filters\":[{\"field\":\"stoEan\",\"keys\":[\"" + ean + "\"]}],\"fromDate\":\"" + datesQuery.getFromDate() + "\",\"toDate\":\"" + datesQuery.getToDate() + "\"}";
        
//        logger.info(":: TOKEN :: " + token + " ::: BODY ::: " + requestBody);
        logger.info("send request to phenix ean: {}, token: {}, body: {}", ean, phenixToken.getToken(), requestBody);
        
        URI uri = UriComponentsBuilder.fromHttpUrl("https://api.mrpf.carrefour.com/production/private/retail/v1/sales/v1/transaction")
                .build(true).toUri();

        RequestEntity<String> request = post(uri)
                .accept(APPLICATION_JSON)
                .header("Content-Type","application/json")
                .header("x-ibm-client-id","f18d3024-61d9-45a5-b784-52ab2ce8cf64")
                .header("x-ibm-client-secret","pV8aL3wE6qU0hR8eL4aM2sK2cV6jR8dC1vH8rS8nI4yC2mL2eJ")
                .header("auth-token", phenixToken.getToken())
                .body(requestBody);

        try {

            ResponseEntity<PhenixTrxResponse> result = restTemplate.exchange(request, PhenixTrxResponse.class);
        	logger.info("get response from phenix ean: {}, result: {}", ean, result);

            if (result.getStatusCode().is2xxSuccessful()) {
            	return result.getBody();
            } else {
            	throw new RuntimeException("bad request");
            }
        } catch (Exception e) {
//            System.out.println("failed to send request : message" + e.getMessage());
        	logger.error("error while request phenix ean: {}", ean, e);
        	throw new RuntimeException("unhandled error");
        }
    }

    public Token generatePhenixToken(){
        
    	URI uri = UriComponentsBuilder.fromHttpUrl("https://api-internal.carrefour.com/production/private/retail/v2/authenticate?env=PROD")
                .build(true).toUri();

        RequestEntity<String> request = post(uri)
                .accept(APPLICATION_JSON)
                .header("x-ibm-client-id","f18d3024-61d9-45a5-b784-52ab2ce8cf64")
                .header("x-ibm-client-secret","pV8aL3wE6qU0hR8eL4aM2sK2cV6jR8dC1vH8rS8nI4yC2mL2eJ")
                .body("{\"ldap_user\":\"CS$_PHENIX_TIC_ET_AU_DELA\",\"ldap_password\":\"Ls$MtTk5\"}");

        try{

//            ResponseEntity<String> result = restTemplate.exchange(request, String.class);
//
//            ObjectMapper mapper = new ObjectMapper();
//            Token token = mapper.readValue(result.getBody(), Token.class);
//
//            //return  result.getBody();
//            return  token;
            
        	ResponseEntity<Token> result = restTemplate.exchange(request, Token.class);
            if (result.getStatusCode().is2xxSuccessful()) {
            	return result.getBody();
            } else {
            	throw new RuntimeException("bad request");
            }
        } catch (Exception e) {
            //System.out.println("failed to send request : message" + e.getMessage());
        	logger.error("error while try generate token for phenix call", e);
        	throw new RuntimeException("unhandled error");
        }
    }  

    public Store incrementAndRefreshStore(String ean, int counter) {
        
    	logger.info(">>>start increment and refresh store ean :{}, counter: {}", ean, counter);
        
    	Store store = this.storeRepository.findByStoreEan(ean);
        if (store == null) {
          throw new RuntimeException("store ean " + ean + " doesn't exist");
        }
        
        logger.info("increment entrances to store ppfs: {}, counter: {}", store.getPpsf(), counter);
        store.incrementCurrentState(counter);
        
        logger.info("request refresh store ean: {}, ppfs: {}", store.getStoreEan(), store.getPpsf());
        
        PhenixTrxResponse trx = getTrxCount(ean);
        if (trx.getData().get(0).getTrxNbr() > 0) {
	    	
        	int totalTrxNbr = trx.getData().get(0).getTrxNbr();
	    	int lastTotalTrxNbr = Integer.parseInt(store.getLastTotalTrxNbr());
	    	int deltaTrxNbr = totalTrxNbr - lastTotalTrxNbr;
	    	if (deltaTrxNbr < 0) {
	    		logger.warn("new total trx nbr is less than the last trx nbr, "
	    				+ "ppsf: {}, totalTrxNbr: {}, lastTotalTrxNbr: {}",
	    				store.getPpsf(), totalTrxNbr, lastTotalTrxNbr);
	    		deltaTrxNbr = 0;
	    	}
	    	
	    	int state = Integer.parseInt(store.getCurrentState()) - deltaTrxNbr;
	    	store.setCurrentState("" + (state < 0 ? 0 : state));
	    	store.setLastTotalTrxNbr("" + totalTrxNbr);
        }
    	store.setLastUpdate(getCurrentDateAsString());
    	store = this.storeRepository.save(store);

    	logger.info(">>>end increment and refresh store :{}", store);
        
        return store;
      }
        
    private static ZonedDateTime getCurrentDate() {
    	return ZonedDateTime.now( ZoneId.of(FR_ZONE_ID) );
    }

    private String getCurrentDateAsString() {
    	return formatDateAsString(getCurrentDate());
    }

    private String formatDateAsString(ZonedDateTime currentDate) {
		return currentDate.format(dateTimeFormatter);
    }

    private DatesQuery buildDatesQuery() {
    	LocalDate currentDate = getCurrentDate().toLocalDate();
    	String fromDate = formatDateAsString(currentDate.atStartOfDay().atZone(ZoneId.of(FR_ZONE_ID)));
    	String toDate = formatDateAsString(LocalDateTime.of(currentDate, LocalTime.MAX).atZone(ZoneId.of(FR_ZONE_ID)));
    	
    	return new DatesQuery(fromDate, toDate);
    }
    
	@PreDestroy
    public void release() {
		logger.info("release ...");
		if (scheduler != null) {
			scheduler.shutdownNow();
		}
	}
    
    public static class DatesQuery {
    	private final String fromDate;
    	private final String toDate;
    	
    	public DatesQuery(String fromDate, String toDate) {
    		this.fromDate = fromDate;
    		this.toDate = toDate;
    	}
    	
    	public String getFromDate() {
			return fromDate;
		}
    	
    	public String getToDate() {
			return toDate;
		}
    	
		@Override
		public String toString() {
			return String.format("DatesQuery [fromDate=%s, toDate=%s]", fromDate, toDate);
		}
    }
    
//    public static void main(String[] args) {
//        Store store = new Store();
//        store.setCurrentState("0");
//        
//    	store.incrementCurrentState(35);
//                	
//    	int totalTrxNbr = 0;
//    	int lastTotalTrxNbr = 0;
//    	int deltaTrxNbr = (lastTotalTrxNbr == 0) ? 0 : (totalTrxNbr - lastTotalTrxNbr);
//
//    	int state = Integer.parseInt(store.getCurrentState()) - deltaTrxNbr;
//    	
//    	store.setCurrentState("" + ((state < 0) ? 0 : state));
//    	store.setLastTotalTrxNbr("" + totalTrxNbr);
//
//    	
//    	//deuxieme passage
//    	store.incrementCurrentState(6);
//    	
//    	deltaTrxNbr = (lastTotalTrxNbr == 0) ? 0 : (totalTrxNbr - lastTotalTrxNbr);
//
//    	state = Integer.parseInt(store.getCurrentState()) - deltaTrxNbr;
//    	
//    	store.setCurrentState("" + ((state < 0) ? 0 : state));
//    	store.setLastTotalTrxNbr("" + totalTrxNbr);
//	
//    	
//    	System.out.println(store);
//    }
    
    public static void main(String[] args) {
		System.out.println(isSameDay("20201216T235959.999+0100"));
	}
}