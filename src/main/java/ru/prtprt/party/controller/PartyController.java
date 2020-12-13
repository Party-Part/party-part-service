package ru.prtprt.party.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.prtprt.party.model.api.PartyApi;
import ru.prtprt.party.model.model.ArrayOfEntries;
import ru.prtprt.party.model.model.ArrayOfMembers;
import ru.prtprt.party.model.model.ArrayOfPayments;
import ru.prtprt.party.model.model.Party;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
public class PartyController implements PartyApi {
    @Override
    public Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    @Override
    public Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getAcceptHeader() {
        return Optional.empty();
    }

    @Override
    public ResponseEntity<String> addPartyEntry(String partyId) {
        return null;
    }

    @Override
    public ResponseEntity<Void> addPartyMember(String partyId) {
        return null;
    }

    @Override
    public ResponseEntity<ArrayOfPayments> calculateParty(String partyId) {
        return null;
    }

    @Override
    public ResponseEntity<Party> createParty() {
        return null;
    }

    @Override
    public ResponseEntity<Party> getParty(String partyId) {
        return new ResponseEntity<Party>(
                new Party().name("test ok"), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ArrayOfEntries> getPartyEntries(String partyId) {
        return null;
    }

    @Override
    public ResponseEntity<ArrayOfMembers> getPartyMembers(String partyId) {
        return null;
    }
}
