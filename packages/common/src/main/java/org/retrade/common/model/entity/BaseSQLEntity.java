package org.retrade.common.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public class BaseSQLEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private String id;
    @Column(name = "created_date", nullable = false)
    private Timestamp createdDate;
    @Column(name = "updated_date", nullable = false)
    private Timestamp updatedDate;


    @PrePersist
    protected void onCreate() {
        createdDate = Timestamp.valueOf(LocalDateTime.now());
        updatedDate = Timestamp.valueOf(LocalDateTime.now());
    }
    @PreUpdate
    protected void onUpdate() {
        updatedDate = Timestamp.valueOf(LocalDateTime.now());
    }

}
