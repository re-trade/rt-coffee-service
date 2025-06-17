package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.repository.SellerRepository;
import org.retrade.main.service.SystemService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {
    private final SellerRepository sellerRepository;
    private final AuthUtils authUtils;

    @Override
    public void approveSeller(String sellerId) {
        var accountEntity = authUtils.getUserAccountFromAuthentication();
        var sellerEntity = sellerRepository.findById(sellerId).orElseThrow(()->new ValidationException("sellerId not found"));
        if (sellerEntity.getVerified() == true)
            throw new ValidationException("sellerId is already verified");
        sellerEntity.setVerified(true);
        sellerRepository.save(sellerEntity);
    }
}
