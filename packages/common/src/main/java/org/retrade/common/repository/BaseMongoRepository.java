package org.retrade.common.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.io.Serializable;

public interface BaseMongoRepository <T, ID extends Serializable> extends MongoRepository<T, ID> {

}
