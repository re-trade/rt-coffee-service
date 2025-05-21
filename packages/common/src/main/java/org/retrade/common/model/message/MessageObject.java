package org.retrade.common.model.message;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MessageObject<T> implements Serializable {
    private String messageId;
    private T payload;
    private String type;
    private LocalDateTime timestamp;
    private String source;

    private MessageObject(Builder<T> builder) {
        this.messageId = builder.messageId;
        this.payload = builder.payload;
        this.type = builder.type;
        this.timestamp = builder.timestamp;
        this.source = builder.source;
    }

    public static class Builder<T> {
        private String messageId;
        private T payload;
        private String type;
        private LocalDateTime timestamp;
        private String source;

        public Builder() {
            this.messageId = java.util.UUID.randomUUID().toString();
            this.timestamp = LocalDateTime.now();
        }
        public Builder<T> withPayload(T payload) {
            this.payload = payload;
            return this;
        }
        public Builder<T> withType(String type) {
            this.type = type;
            return this;
        }
        public Builder<T> withSource(String source) {
            this.source = source;
            return this;
        }

        public Builder<T> withMessageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder<T> withTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public MessageObject<T> build() {
            if (payload == null) {
                throw new IllegalStateException("Payload cannot be null");
            }
            if (type == null) {
                throw new IllegalStateException("Type cannot be null");
            }
            return new MessageObject<>(this);
        }
    }
}
