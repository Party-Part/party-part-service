package ru.prtprt.party.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.prtprt.party.entity.embedded.SplitEntityId;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity(name = "split")
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString
public class SplitEntity {

    @EmbeddedId
    SplitEntityId id;
    BigDecimal cost;

}
