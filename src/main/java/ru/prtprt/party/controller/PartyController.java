package ru.prtprt.party.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.prtprt.party.entity.PartyEntity;
import ru.prtprt.party.entity.UserEntity;
import ru.prtprt.party.mapper.PartyMapper;
import ru.prtprt.party.model.api.PartyApi;
import ru.prtprt.party.model.model.CreatePartyRequest;
import ru.prtprt.party.model.model.Party;
import ru.prtprt.party.repository.PartyRepository;
import ru.prtprt.party.repository.UserRepository;

import javax.validation.Valid;
import java.math.BigInteger;
import java.util.Optional;


@RequiredArgsConstructor
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PartyController implements PartyApi {

    PartyMapper partyMapper;

    PartyRepository partyRepository;
    UserRepository userRepository;

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
}
