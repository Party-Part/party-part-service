package ru.prtprt.party.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigInteger;

@Entity(name = "telegram_party")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class TelegramPartyEntity {

    @Id
    BigInteger tgChat;
    BigInteger partyId;
}
