package com.carrefour.inno.qm.dao;

import com.carrefour.inno.qm.model.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Repository
public class StoreRepositoryCustom {

    @Autowired
    MongoTemplate mongoTemplate;

    public Store getStoreByStoreEan(String ean) {

        Query query = new Query();
        //Pattern pattern = Pattern.compile(code, Pattern.CASE_INSENSITIVE);
        //query.addCriteria(Criteria.where("code").is(code).regex(pattern));
        query.addCriteria(Criteria.where("storeEan").is(ean));
        Store result = mongoTemplate.findOne(query, Store.class);

        return result;
    }
}
