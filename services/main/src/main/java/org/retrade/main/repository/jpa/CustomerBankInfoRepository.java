package org.retrade.main.repository.jpa;

import org.retrade.common.repository.BaseJpaRepository;
import org.retrade.main.model.entity.CustomerBankInfoEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerBankInfoRepository extends BaseJpaRepository<CustomerBankInfoEntity, String> {

}
