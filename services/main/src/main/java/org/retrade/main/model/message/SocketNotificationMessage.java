package org.retrade.main.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocketNotificationMessage implements Serializable {
    private String messageId;
    private String accountId;
    private String title;
    private String type;
    private String content;
}
