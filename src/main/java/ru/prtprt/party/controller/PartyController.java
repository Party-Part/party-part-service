package ru.prtprt.party.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.prtprt.party.entity.EntryEntity;
import ru.prtprt.party.entity.PartyEntity;
import ru.prtprt.party.entity.SplitEntity;
import ru.prtprt.party.entity.UserEntity;
import ru.prtprt.party.entity.embedded.SplitEntityId;
import ru.prtprt.party.mapper.PartyMapper;
import ru.prtprt.party.model.api.PartyApi;
import ru.prtprt.party.model.model.AddPartyEntryRequest;
import ru.prtprt.party.model.model.CreatePartyRequest;
import ru.prtprt.party.model.model.Party;
import ru.prtprt.party.repository.EntryRepository;
import ru.prtprt.party.repository.PartyRepository;
import ru.prtprt.party.repository.SplitRepository;
import ru.prtprt.party.repository.UserRepository;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;


@RequiredArgsConstructor
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PartyController implements PartyApi {

    PartyMapper partyMapper;

    PartyRepository partyRepository;
    UserRepository userRepository;
    EntryRepository entryRepository;
    SplitRepository splitRepository;

    @Override
    public ResponseEntity<Party> createParty(@Valid CreatePartyRequest body) {

        PartyEntity partyEntity = new PartyEntity();
        partyEntity.setName(body.getName());
        partyEntity.setCreatorId(new BigInteger(body.getUserId()));

        partyRepository.save(partyEntity);

        Party response = partyMapper.mapPartyEntityToParty(partyEntity);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Party> getParty(String partyId) {
        Optional<PartyEntity> partyEntityOpt = partyRepository.findById(new BigInteger(partyId));

        if (partyEntityOpt.isPresent()) {
            Party response = partyMapper.mapPartyEntityToParty(partyEntityOpt.get());
            return ResponseEntity.ok(response);
        } else
            return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<Void> addPartyMember(String partyId, @Valid String userId) {

        Optional<UserEntity> userEntityOpt = userRepository.findById(new BigInteger(userId));
        Optional<PartyEntity> partyEntityOpt = partyRepository.findById(new BigInteger(partyId));

        if (userEntityOpt.isEmpty() || partyEntityOpt.isEmpty())
            return ResponseEntity.notFound().build();

        partyEntityOpt.get().getMemberInParty().add(userEntityOpt.get());
        partyRepository.save(partyEntityOpt.get());

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<String> addPartyEntry(String partyId, @Valid AddPartyEntryRequest body) {


        Optional<PartyEntity> partyEntityOpt = partyRepository.findById(new BigInteger(partyId));
        Optional<UserEntity> userCreatorEntityOpt = userRepository.findById(new BigInteger(body.getUserCreatorId()));
        Optional<UserEntity> userWhoPaidEntityOpt = userRepository.findById(new BigInteger(body.getUserWhoPaidId()));

        if (userCreatorEntityOpt.isEmpty() || userWhoPaidEntityOpt.isEmpty() || partyEntityOpt.isEmpty())
            return ResponseEntity.notFound().build();

        EntryEntity entryEntity = new EntryEntity();

        entryEntity.setName(body.getName());
        entryEntity.setCost(new BigDecimal(body.getCost()));
        entryEntity.setCurrency(body.getCurrency());
        entryEntity.setUserIdCreator(new BigInteger(body.getUserCreatorId()));
        entryEntity.setUserIdWhoPaid(new BigInteger(body.getUserWhoPaidId()));
        entryEntity.setParty(partyEntityOpt.get());

        System.out.println(Arrays.toString(body.getSplit().split(";")));

        HashSet<SplitEntity> splitEntities = new HashSet<>();

        Arrays.stream(body.getSplit().split(";"))
                .map(str -> str.replace("(", "").replace(")", "").trim())
                .forEach(str -> {
                            //pair <userId, percent>
                            String[] splitted = str.split(",");
                            UserEntity splitUser = userRepository.findById(new BigInteger(splitted[0])).orElseThrow();
                            SplitEntityId splitEntityId = new SplitEntityId();
                            splitEntityId.setEntry(entryEntity.getEntryId());
                            splitEntityId.setUser(splitUser.getUserId());
                            SplitEntity splitEntity = new SplitEntity();
                            splitEntity.setId(splitEntityId);
                            splitEntity.setPercent(new BigDecimal(splitted[1]));
                            splitEntities.add(splitEntity);
                        }
                );

        splitRepository.deleteAll(splitRepository.findAllByIdEntry(entryEntity.getEntryId()));
        splitRepository.saveAll(splitEntities);

        entryRepository.save(entryEntity);
        System.out.println(entryEntity.getEntryId());

        return ResponseEntity.ok().build();
    }
}
