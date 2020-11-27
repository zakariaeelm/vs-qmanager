package com.carrefour.inno.qm.service;

import com.carrefour.inno.qm.dao.StoreRepository;
import com.carrefour.inno.qm.dao.StoreRepositoryCustom;
import com.carrefour.inno.qm.model.PhenixTrxResponse;
import com.carrefour.inno.qm.model.Store;
import com.carrefour.inno.qm.model.StoreDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class CovidService {

    final Logger logger = LoggerFactory.getLogger(CovidService.class);
    private final String STORE_LOCATION_URL;

    ApiService apiService;
    StoreRepositoryCustom storeRepositoryCustom;
    StoreRepository storeRepository;

    public CovidService(@Value("${api.carrefour.location}") String STORE_LOCATION_URL,
                        ApiService apiService,
                        StoreRepositoryCustom storeRepositoryCustom,
                        StoreRepository storeRepository) {
        this.STORE_LOCATION_URL = STORE_LOCATION_URL;
        this.apiService = apiService;
        this.storeRepositoryCustom = storeRepositoryCustom;
        this.storeRepository = storeRepository;
    }

    public Store computeDiff(String ean, PhenixTrxResponse trx){
        Store store = storeRepository.findByStoreEanContaining(ean);
        if(trx.getData().get(0).getTrxNbr() > 0) {
            Double nbr = Double.valueOf(trx.getData().get(0).getTrxNbr());
            //nbr = (nbr * 1.1);
            Double ratio = Double.valueOf(store.getCustomerByTrx());
            nbr = (nbr * ratio);
            int nbrInt = nbr.intValue();
            int _state = Integer.parseInt(store.getCurrentState());
            _state -= nbr;
            store.setCurrentState("" + _state);
            store.setLastUpdate(generateInstant());
            if(Integer.parseInt(store.getCurrentState()) < 0)
                store.setCurrentState("0");
            storeRepository.save(store);
        }
        return store;
    }
    public Store incrementCount(String ean, int step){

        try {
            //Store store = storeRepositoryCustom.getStoreByStoreEan(ean);
            Store store = storeRepository.findByStoreEanContaining(ean);
            logger.debug("Store " + store.toString());
            logger.info("Store " + store.toString());
            if(store.incrementCountBy(step)){

                //store.setLastUpdate(generateInstant());
                logger.debug("Store " + store.toString());
                logger.info("Store " + store.toString());
                storeRepository.save(store);
                logger.debug("Store "+store.getStoreDesc()+" Updated ", store);

                return store;
            }
        } catch (Exception e) {
            logger.error("ERROR ::: ",e);
        }
        return new Store();

    }
    public List<Store> findAll() {
        return storeRepository.findAll();
    }
    public String generateInstant(){
        DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss.SSS")
                .withLocale( Locale.FRANCE)
                .withZone( ZoneId.systemDefault());
        Instant instant = Instant.now().plus(Duration.ofHours(1));
        String nowString = FOMATTER.format(instant) + "+0200";
        return nowString;        // 20200527T160819.623+0200
    }
    public Boolean isSameDay(String instantString){
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
    public Store findOneByPpsf(String ppsf) {
        return storeRepository.findByPpsfContaining(ppsf);
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
        Store store = new Store();
        if(storeRepository.findByPpsfContaining(ppsf) == null){
            String body = "{\"filters\":[{\"field\":\"additionalPartyIdentifications.additionalPartyIdentificationTypeCode\",\"keys\":[\"PPSF\"]},{\"field\":\"additionalPartyIdentifications.additionalPartyIdentification\",\"keys\":[\"" + ppsf + "\"]}],\"fields\":[\"locationName\",\"format\",\"additionalPartyIdentifications\"]}";
            try{
                String result = apiService.httpsPostCall(STORE_LOCATION_URL, body);
                JSONObject object = new JSONObject(result);
                JSONObject data = ((JSONArray)object.get("data")).getJSONObject(0);

                JSONArray list = (JSONArray)data.get("additionalPartyIdentifications");
                JSONObject storeEan = (JSONObject) list.get(0);

                ObjectMapper mapper = new ObjectMapper();
                StoreDTO storeDTO = mapper.readValue(data.toString(), StoreDTO.class);
                store = new Store( ppsf, storeEan.getString("additionalPartyIdentification"), storeDTO);
                store.setLastUpdate(generateInstant());
                store.setCapacity(capacity);
                store.setCurrentState("0");
                store.setCustomerByTrx(ratio);
                store.setRefreshInterval("1");
                storeRepository.save(store);

            } catch (Exception e) {
                logger.error("failed to get product ::: ", e);
            }
        } else {
            store = storeRepository.findByPpsfContaining(ppsf);
            store.setLastUpdate(generateInstant());
            if(!isSameDay(store.getLastUpdate()))
                store.setCurrentState("0");
            store.setCapacity(capacity);
            store.setCustomerByTrx(ratio);
            storeRepository.save(store);
        }
        return store;
    }
}