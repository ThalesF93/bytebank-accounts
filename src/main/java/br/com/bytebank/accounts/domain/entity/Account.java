package br.com.bytebank.accounts.domain.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Table(name = "accounts")
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Account {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final int ACCOUNT_NUMBER_MIN   = 10_000_000;
    private static final int ACCOUNT_NUMBER_RANGE = 90_000_000;
    private static final int AGENCY_MIN           = 100_000;
    private static final int AGENCY_RANGE         = 900_000;
    private static final int AGENCY_DIGITS        = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column
    private UUID customerId;

    @Column
    private String agency;

    @Column
    private String accountNumber;

    @Column(nullable = true)
    private Boolean isActive;

    @Column
    private BigDecimal balance = BigDecimal.ZERO;

    @Column
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column
    private String createByUser;

    @Column
    private String editByUser;

    @Column
    @UpdateTimestamp
    private LocalDateTime editedAt;

    public Account() {
        this.accountNumber = generateAccountNumber();
        this.balance = new BigDecimal("0");
        this.agency = generateAgencyNumber();
    }

    private String generateAccountNumber(){
        int number = SECURE_RANDOM.nextInt(ACCOUNT_NUMBER_RANGE) + ACCOUNT_NUMBER_MIN;
        return String.valueOf(number);
    }

    private String generateAgencyNumber(){
        return String.format("%0" + AGENCY_DIGITS + "d",
                SECURE_RANDOM.nextInt(AGENCY_RANGE) + AGENCY_MIN);
    }


}
