package org.retrade.main.model.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;

@Data
public class VietQrBankEntity implements Serializable {
    @Id
    private Long id;
    private String name;
    private String code;
    private String bin;
    private String shortName;
    private String logo;
    private Integer transferSupported;
    private Integer lookupSupported;
}
