package org.retrade.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.retrade.common.model.exception.ValidationException;
import org.retrade.main.model.dto.response.OrderResponse;
import org.retrade.main.model.dto.response.SellerBaseResponse;
import org.retrade.main.model.dto.response.SellerRegisterResponse;
import org.retrade.main.model.entity.OrderComboEntity;
import org.retrade.main.model.entity.OrderEntity;
import org.retrade.main.model.entity.OrderStatusEntity;
import org.retrade.main.model.entity.SellerEntity;
import org.retrade.main.repository.SellerRepository;
import org.retrade.main.service.SystemService;
import org.retrade.main.util.AuthUtils;
import org.springframework.stereotype.Service;

import java.util.List;

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
