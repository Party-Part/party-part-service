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
import ru.prtprt.party.model.model.*;
import ru.prtprt.party.repository.EntryRepository;
import ru.prtprt.party.repository.PartyRepository;
import ru.prtprt.party.repository.SplitRepository;
import ru.prtprt.party.repository.UserRepository;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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
    public ResponseEntity<ArrayOfMembers> getPartyMembers(String partyId) {
        Optional<PartyEntity> partyEntityOpt = partyRepository.findById(new BigInteger(partyId));

        if (partyEntityOpt.isEmpty())
            return ResponseEntity.notFound().build();

        ArrayOfMembers arrayOfMembers = new ArrayOfMembers();

        List<UserEntity> listOfUsers = partyEntityOpt.get().getMemberInParty();

        List<Member> listOfMembers = listOfUsers.stream().map(userEntity -> {
            Member member = new Member();
            member.setLogin(userEntity.getLogin());
            member.setUserId(userEntity.getUserId().toString());
            return member;
        }).collect(Collectors.toList());

        arrayOfMembers.addAll(listOfMembers);

        return ResponseEntity.ok(arrayOfMembers);
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

        partyEntityOpt.get().getEntries().add(entryEntity);
        entryRepository.save(entryEntity);

        //parse split entities and save (update) manually
        HashSet<SplitEntity> splitEntities = new HashSet<>();
        Arrays.stream(body.getSplit().split(";"))
                .map(str -> str.replace("(", "").replace(")", "").trim())
                .forEach(str -> {
                            //pair <userId, percent>
                            String[] splitted = str.split(",");
                            UserEntity splitUser = userRepository.findById(new BigInteger(splitted[0].trim())).orElseThrow();
                            SplitEntityId splitEntityId = new SplitEntityId();
                            splitEntityId.setEntry(entryEntity.getEntryId());
                            splitEntityId.setUser(splitUser.getUserId());
                            SplitEntity splitEntity = new SplitEntity();
                            splitEntity.setId(splitEntityId);
                            splitEntity.setPercent(new BigDecimal(splitted[1].trim()));
                            splitEntities.add(splitEntity);
                        }
                );

        splitRepository.deleteAll(splitRepository.findAllByIdEntry(entryEntity.getEntryId()));
        splitRepository.saveAll(splitEntities);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<ArrayOfEntries> getPartyEntries(String partyId) {
        Optional<PartyEntity> partyEntityOpt = partyRepository.findById(new BigInteger(partyId));

        if (partyEntityOpt.isEmpty())
            return ResponseEntity.notFound().build();

        List<EntryEntity> entryEntityList = partyEntityOpt.get().getEntries();

        List<Entry> entryList = entryEntityList
                .stream()
                .map(
                        entryEntity -> {
                            Entry entry = new Entry();
                            entry.setEntryId(entryEntity.getEntryId().toString());
                            entry.setName(entryEntity.getName());
                            entry.setPartyId(entryEntity.getParty().getPartyId().toString());
                            entry.setCost(entryEntity.getCost().toString());
                            entry.setCurrency(entryEntity.getCurrency());
                            entry.setUserCreatorId(entryEntity.getUserIdCreator().toString());
                            entry.setUserWhoPaidId(entryEntity.getUserIdWhoPaid().toString());

                            List<SplitEntity> splitEntityList = splitRepository.findAllByIdEntry(entryEntity.getEntryId());
                            List<Split> splitList = splitEntityList
                                    .stream()
                                    .map(splitEntity -> {
                                        Split split = new Split();
                                        split.setUserId(splitEntity.getId().getUser().toString());
                                        split.setProportion(splitEntity.getPercent().toString());
                                        return split;
                                    })
                                    .collect(Collectors.toList());
                            entry.setSplit(splitList);
                            return entry;
                        }
                )
                .collect(Collectors.toList());


        ArrayOfEntries arrayOfEntries = new ArrayOfEntries();
        arrayOfEntries.addAll(entryList);

        return ResponseEntity.ok(arrayOfEntries);
    }
}
