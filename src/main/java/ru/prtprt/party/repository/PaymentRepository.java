package ru.prtprt.party.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import ru.prtprt.party.entity.PaymentEntity;

import java.math.BigInteger;
import java.util.List;

@Component
public interface PaymentRepository extends CrudRepository<PaymentEntity, BigInteger> {
    List<PaymentEntity> findAllByPartyId(BigInteger partyId);
}