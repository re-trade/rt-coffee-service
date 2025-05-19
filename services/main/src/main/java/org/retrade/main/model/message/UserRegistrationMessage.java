package org.retrade.main.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegistrationMessage implements Serializable {
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime registrationDate;
    private String messageId;
    private int retryCount;
}
