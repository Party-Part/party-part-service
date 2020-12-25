package ru.prtprt.party.entity.embedded;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.apache.tomcat.jni.BIOCallback;
import ru.prtprt.party.entity.EntryEntity;
import ru.prtprt.party.entity.PartyEntity;
import ru.prtprt.party.entity.UserEntity;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.math.BigInteger;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SplitEntityId implements Serializable {
    @Column(name = "entry_id")
    BigInteger entry;
    @Column(name = "user_id")
    BigInteger user;
}
