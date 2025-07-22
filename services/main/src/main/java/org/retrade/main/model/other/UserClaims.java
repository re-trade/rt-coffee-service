package org.retrade.main.model.other;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.retrade.main.model.constant.JwtTokenType;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserClaims {
    private String username;
    private List<String> roles;
    private JwtTokenType tokenType;
    private String sessionId;
}
