package ru.prtprt.party.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.apache.tomcat.jni.BIOCallback;
import org.postgresql.ds.common.BaseDataSource;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.math.BigInteger;

@Entity(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    BigInteger paymentId;
    BigInteger userIdSender;
    BigInteger userIdReceiver;
    BigInteger partyId;
    BigDecimal cost;
    String currency;
    Boolean isPaid;
    Boolean isConfirmed;
}
