package ru.prtprt.party.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;

@Entity(name = "entry")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class EntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    BigInteger entryId;
    //    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
//    @JoinTable(name = "\"user\"",
//            joinColumns = @JoinColumn(name = "user_id"))
    BigInteger userIdCreator;
    //    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
//    @JoinTable(name = "\"user\"",
//            joinColumns = @JoinColumn(name = "user_id"))
    BigInteger userIdWhoPaid;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    PartyEntity party;
    String name;
    BigDecimal cost;
    String currency;

}
