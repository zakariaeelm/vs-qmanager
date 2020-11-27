package com.carrefour.inno.qm.dao;

import com.carrefour.inno.qm.model.Store;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends MongoRepository<Store, String> {
    Store findByStoreEanContaining(String storeEan);
    Store findByPpsfContaining(String ppsf);
}
