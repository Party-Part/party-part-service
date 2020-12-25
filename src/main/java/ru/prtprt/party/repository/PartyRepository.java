package ru.prtprt.party.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import ru.prtprt.party.entity.PartyEntity;

import java.math.BigInteger;

@Component
public interface PartyRepository extends CrudRepository<PartyEntity, BigInteger> {
}
