package org.retrade.main.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerStatusResponse {
    private boolean canLogin;
    private boolean registered;
    private boolean banned;
    private boolean registerFailed;
}
