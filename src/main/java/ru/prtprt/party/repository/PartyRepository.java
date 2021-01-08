package ru.prtprt.party.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import ru.prtprt.party.entity.PartyEntity;
import ru.prtprt.party.entity.UserEntity;

import java.math.BigInteger;
import java.util.List;

@Component
public interface PartyRepository extends CrudRepository<PartyEntity, BigInteger> {
    List<PartyEntity> getAllByCreatorId(BigInteger creatorId);
    List<PartyEntity> getAllByMemberInParty(UserEntity member);
}
