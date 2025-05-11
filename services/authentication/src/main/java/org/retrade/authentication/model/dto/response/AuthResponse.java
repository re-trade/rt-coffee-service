package org.retrade.authentication.model.dto.response;

import lombok.Builder;
import lombok.Data;
import org.retrade.authentication.model.constant.JwtTokenType;

import java.util.EnumMap;
import java.util.List;

@Data
@Builder
public class AuthResponse {
    private EnumMap<JwtTokenType, String> tokens;
    private List<String> roles;
    private boolean twoFA;
}
