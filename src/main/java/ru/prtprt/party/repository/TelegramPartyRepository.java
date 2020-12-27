package ru.prtprt.party.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import ru.prtprt.party.entity.TelegramPartyEntity;

import java.math.BigInteger;

@Component
public interface TelegramPartyRepository extends CrudRepository<TelegramPartyEntity, BigInteger> {
}
