package ru.prtprt.party.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import ru.prtprt.party.entity.EntryEntity;

import java.math.BigInteger;

@Component
public interface EntryRepository extends CrudRepository<EntryEntity, BigInteger> {
}