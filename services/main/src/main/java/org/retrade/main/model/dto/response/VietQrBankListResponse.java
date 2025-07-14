package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.entity.VietQrBankEntity;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VietQrBankListResponse {
    private String code;
    private String desc;
    private List<VietQrBankEntity> data;
}
