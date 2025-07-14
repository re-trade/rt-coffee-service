package org.retrade.main.repository;

import org.retrade.common.model.dto.request.QueryWrapper;
import org.retrade.main.model.entity.VietQrBankEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface VietQrBankRepository {
    List<VietQrBankEntity> getAll();

    Page<VietQrBankEntity> getAll(Pageable pageable);

    Page<VietQrBankEntity> search(QueryWrapper queryWrapper);

    Optional<VietQrBankEntity> getBankByBin(String bin);
}
