package ru.prtprt.party.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import ru.prtprt.party.entity.UserEntity;

import java.math.BigInteger;

@Component
public interface UserRepository extends CrudRepository<UserEntity, BigInteger> {
}
