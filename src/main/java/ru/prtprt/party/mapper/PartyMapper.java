package ru.prtprt.party.mapper;

import org.springframework.stereotype.Component;
import ru.prtprt.party.entity.PartyEntity;
import ru.prtprt.party.model.model.Party;

import java.math.BigInteger;

@Component
public class PartyMapper {
    public Party mapPartyEntityToParty(PartyEntity entity) {

        Party party = new Party();
        party.setName(entity.getName());
        party.setPartyId(entity.getPartyId().toString());
        party.setUserCreatorId(entity.getCreatorId().toString());

        return party;
    }

    public PartyEntity mapPartyEntityToParty(Party party) {
        PartyEntity partyEntity = new PartyEntity();
        partyEntity.setCreatorId(new BigInteger(party.getUserCreatorId()));
        partyEntity.setName(party.getName());
        partyEntity.setPartyId(new BigInteger(party.getPartyId()));
        return partyEntity;
    }
}
