package org.retrade.achievement.model.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationMessage implements Serializable {
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> templateVariables;
    private String messageId;
    private int retryCount;
}
