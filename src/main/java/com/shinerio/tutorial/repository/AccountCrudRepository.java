package com.shinerio.tutorial.repository;

import com.shinerio.tutorial.document.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountCrudRepository extends ReactiveMongoRepository<Account, String> {
}
