package ru.prtprt.party.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

@Entity(name = "entry")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
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
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "party",
            joinColumns = @JoinColumn(name = "party_id"))
    PartyEntity party;
    String name;
    BigDecimal cost;
    String currency;

}
