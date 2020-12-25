package ru.prtprt.party.repository;

import org.apache.tomcat.jni.BIOCallback;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import ru.prtprt.party.entity.EntryEntity;
import ru.prtprt.party.entity.SplitEntity;
import ru.prtprt.party.entity.embedded.SplitEntityId;

import java.math.BigInteger;
import java.util.List;

@Component
public interface SplitRepository extends CrudRepository<SplitEntity, SplitEntityId> {
    List<SplitEntity> findAllByIdEntry(BigInteger entryEntity);
}
