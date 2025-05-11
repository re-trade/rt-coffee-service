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
    @Field(name = "id", targetType = FieldType.OBJECT_ID)
    private String id;
    @Field(name = "created_date", targetType = FieldType.TIMESTAMP)
    private Instant createdDate;
    @Field(name = "updated_date", targetType = FieldType.TIMESTAMP)
    private Instant updatedDate;

    public BaseMongoEntity() {
        createdDate = Instant.now();
        updatedDate = Instant.now();
    }

    public void updateUpdatedDate() {
        updatedDate = Instant.now();
    }
}
