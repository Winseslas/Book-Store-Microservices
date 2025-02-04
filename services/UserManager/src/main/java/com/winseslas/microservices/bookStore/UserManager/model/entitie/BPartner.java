package com.winseslas.microservices.bookStore.UserManager.model.entitie;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.winseslas.microservices.bookStore.UserManager.model.enums.Gender;
import com.winseslas.microservices.bookStore.UserManager.validation.OneMandatoryField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;


@AllArgsConstructor
@Builder
@Data
@Getter
@Setter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@OneMandatoryField
@Table(name="bpartner")
public class BPartner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String value;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column
    private Boolean isCustomer;

    @Column
    private Boolean isAuthor;

    @Column
    private Boolean isEmployee;

    @Column(nullable = false)
    private String profileUrl = "https://www.svgrepo.com/show/452030/avatar-default.svg";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @OneToOne(mappedBy = "bpartner")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bpartnergroup_id", nullable = false)
    private BPartnerGroup bpartnerGroup;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonIgnore
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonIgnore
    private Instant updatedAt;

    @PrePersist
    private void initializeCreateAt() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    private void initializeUpdateAt() {
        this.updatedAt = Instant.now();
    }
}
