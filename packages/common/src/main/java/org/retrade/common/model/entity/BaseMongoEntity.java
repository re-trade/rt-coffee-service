package org.retrade.common.model.entity;

import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
public class BaseMongoEntity implements Serializable {
    @Id
    @Field(value = "id", targetType = FieldType.OBJECT_ID)
    private String id;
    @Field(value = "created_date", targetType = FieldType.TIMESTAMP)
    private Instant createdDate;
    @Field(value = "updated_date", targetType = FieldType.TIMESTAMP)
    private Instant updatedDate;

    public BaseMongoEntity() {
        createdDate = Instant.now();
        updatedDate = Instant.now();
    }

    public void updateUpdatedDate() {
        updatedDate = Instant.now();
    }
}
