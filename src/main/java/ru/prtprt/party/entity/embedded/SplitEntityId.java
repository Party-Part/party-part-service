package ru.prtprt.party.entity.embedded;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.math.BigInteger;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class SplitEntityId implements Serializable {
    @Column(name = "entry_id")
    BigInteger entry;
    @Column(name = "user_id")
    BigInteger user;
}
