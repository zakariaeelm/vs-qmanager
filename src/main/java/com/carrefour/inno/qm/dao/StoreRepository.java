package com.carrefour.inno.qm.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.carrefour.inno.qm.model.Store;

@Repository
public interface StoreRepository extends MongoRepository<Store, String> {
    
	Store findByStoreEanContaining(String storeEan);
    Store findByPpsfContaining(String ppsf);
	Store findByStoreEan(String storeEan);
    Store findByPpsf(String ppsf);
}
