package ru.prtprt.party.controller;

import javafx.util.Pair;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;
import ru.prtprt.party.entity.*;
import ru.prtprt.party.entity.embedded.SplitEntityId;
import ru.prtprt.party.mapper.PartyMapper;
import ru.prtprt.party.mapper.PaymentMapper;
import ru.prtprt.party.model.api.PartyApi;
import ru.prtprt.party.model.model.*;
import ru.prtprt.party.repository.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;


@CrossOrigin
@RequiredArgsConstructor
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PartyController implements PartyApi {

    PartyMapper partyMapper;
    PaymentMapper paymentMapper;

    PartyRepository partyRepository;
    UserRepository userRepository;
    EntryRepository entryRepository;
    SplitRepository splitRepository;
    PaymentRepository paymentRepository;
    TelegramPartyRepository telegramPartyRepository;

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
    public ResponseEntity<Party> getOrCreatePartyTelegram(@Valid CreatePartyTelegramRequest body) {

        Optional<TelegramPartyEntity> telegramPartyEntityOpt = telegramPartyRepository.findById(new BigInteger(body.getChatId()));

        if (telegramPartyEntityOpt.isPresent()) {
            System.out.println("TG patry already exist");
            PartyEntity partyEntity = partyRepository.findById(telegramPartyEntityOpt.get().getPartyId()).orElseThrow(RuntimeException::new);
            Party response = partyMapper.mapPartyEntityToParty(partyEntity);
            return ResponseEntity.ok(response);
        }

        PartyEntity partyEntity = new PartyEntity();
        partyEntity.setName(body.getName());
        partyEntity.setCreatorId(new BigInteger(body.getUserId()));

        partyRepository.save(partyEntity);

        TelegramPartyEntity telegramPartyEntity = new TelegramPartyEntity();
        telegramPartyEntity.setPartyId(partyEntity.getPartyId());
        telegramPartyEntity.setTgChat(new BigInteger(body.getChatId()));

        telegramPartyRepository.save(telegramPartyEntity);

        Party response = partyMapper.mapPartyEntityToParty(partyEntity);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Party> getTgParty(String chatId) {
        Optional<TelegramPartyEntity> telegramPartyEntityOpt = telegramPartyRepository.findById(new BigInteger(chatId));

        if (telegramPartyEntityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PartyEntity partyEntity = partyRepository.findById(telegramPartyEntityOpt.get().getPartyId()).orElseThrow(RuntimeException::new);
        Party response = partyMapper.mapPartyEntityToParty(partyEntity);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Party> getParty(String partyId) {
        Optional<PartyEntity> partyEntityOpt = partyRepository.findById(new BigInteger(partyId));

        if (partyEntityOpt.isPresent()) {
            System.out.println("Found party: " + partyEntityOpt.get());
            Party response = partyMapper.mapPartyEntityToParty(partyEntityOpt.get());
            return ResponseEntity.ok(response);
        } else {
            System.out.println("Party not found. Id: " + partyId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
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

        Set<UserEntity> listOfUsers = partyEntityOpt.get().getMemberInParty();
        Comparator<Member> comparatorById = Comparator.comparing(Member::getUserId);
        List<Member> listOfMembers = listOfUsers.stream().map(userEntity -> {
            Member member = new Member();
            member.setLogin(userEntity.getName());
            member.setUserId(userEntity.getUserId().toString());
            return member;
        })
        .sorted(comparatorById)
        .collect(Collectors.toList());

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
                            splitEntity.setCost(new BigDecimal(splitted[1].trim()));
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

        Set<EntryEntity> entryEntityList = partyEntityOpt.get().getEntries();

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
                                        split.setProportion(splitEntity.getCost().toString());
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

    @Override
    public ResponseEntity<ArrayOfPayments> calculateParty(String partyId) {
        Optional<PartyEntity> partyEntityOpt = partyRepository.findById(new BigInteger(partyId));

        if (partyEntityOpt.isEmpty())
            return ResponseEntity.notFound().build();

        Set<EntryEntity> entryEntityList = partyEntityOpt.get().getEntries();

        List<Pair<BigInteger, BigDecimal>> payingList = entryEntityList
                .stream()
                .map(entryEntity -> new Pair<>(entryEntity.getUserIdWhoPaid(), entryEntity.getCost()))
                .collect(Collectors.toList());

        List<Pair<BigInteger, BigDecimal>> debtsList = entryEntityList
                .stream()
                .map(entryEntity ->
                        splitRepository.findAllByIdEntry(entryEntity.getEntryId()))
                .flatMap(Collection::stream)
                .map(splitEntity -> new Pair<>(splitEntity.getId().getUser(), splitEntity.getCost()))
                .collect(Collectors.toList());

        System.out.println("Paying:");
        payingList.forEach(pair -> System.out.println(pair.getKey() + ":" + pair.getValue()));
        System.out.println("Debts:");
        debtsList.forEach(pair -> System.out.println(pair.getKey() + ":" + pair.getValue()));

        Optional<BigDecimal> totalPayingOpt = payingList.stream().map(Pair::getValue).reduce(BigDecimal::add);
        Optional<BigDecimal> totalDebtsOpt = debtsList.stream().map(Pair::getValue).reduce(BigDecimal::add);

        //if total paying not equal total debts return error
        if (totalPayingOpt.isEmpty() ||
                totalDebtsOpt.isEmpty() ||
                !totalPayingOpt.get().subtract(totalDebtsOpt.get()).equals(BigDecimal.ZERO))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        Map<BigInteger, BigDecimal> payingMap = new HashMap<>();
        payingList.forEach(
                pair -> payingMap.merge(pair.getKey(), pair.getValue(), BigDecimal::add)
        );
        Map<BigInteger, BigDecimal> debtsMap = new HashMap<>();
        debtsList.forEach(
                pair -> debtsMap.merge(pair.getKey(), pair.getValue(), BigDecimal::add)
        );

        System.out.println("Paying:");
        payingMap.forEach((key, value) -> System.out.println(key + ":" + value));
        System.out.println("Debts:");
        debtsMap.forEach((key, value) -> System.out.println(key + ":" + value));

        List<BigInteger> totalUsersList = new LinkedList<>();
        totalUsersList.addAll(payingMap.keySet());
        totalUsersList.addAll(debtsMap.keySet());
        totalUsersList = totalUsersList
                .stream()
                .distinct()
                .collect(Collectors.toList());

        System.out.println("Total users:");
        totalUsersList.forEach(System.out::println);

        //calculate total credit for all members (payment-debts)
        //key - user, value - how much the user owes to other users
        Map<BigInteger, BigDecimal> creditMap = new HashMap<>();
        totalUsersList.forEach(user -> creditMap.put(user, BigDecimal.ZERO));

        payingMap.keySet()
                .forEach(key -> creditMap.replace(key, creditMap.get(key).subtract(payingMap.get(key))));
        debtsMap.keySet()
                .forEach(key -> creditMap.replace(key, creditMap.get(key).add(debtsMap.get(key))));

        System.out.println("Total credits:");
        creditMap.forEach((key, value) -> System.out.println(key + ":" + value));

        //who receive money
        Map<BigInteger, BigDecimal> recipientMap = new HashMap<>();
        //who send money
        Map<BigInteger, BigDecimal> senderMap = new HashMap<>();

        creditMap.forEach((key, value) -> {
            if (value.compareTo(BigDecimal.ZERO) > 0)
                senderMap.put(key, value);
            else if (value.compareTo(BigDecimal.ZERO) < 0)
                recipientMap.put(key, value);
        });

        System.out.println("recipientMap:");
        recipientMap.forEach((key, value) -> System.out.println(key + ":" + value));
        System.out.println("senderMap:");
        senderMap.forEach((key, value) -> System.out.println(key + ":" + value));

        //set sender and recipient credits by reverse order
        LinkedList<Pair<BigInteger, BigDecimal>> senderSortedList = new LinkedList<>();
        LinkedList<Pair<BigInteger, BigDecimal>> recipientSortedList = new LinkedList<>();
        senderMap.forEach((key, value) -> senderSortedList.add(new Pair<>(key, value)));
        recipientMap.forEach((key, value) -> recipientSortedList.add(new Pair<>(key, value)));
        Comparator<Pair<BigInteger, BigDecimal>> comparatorByCost = Comparator.comparing(Pair::getValue);
        senderSortedList.sort(comparatorByCost.reversed());
        recipientSortedList.sort(comparatorByCost);

        System.out.println("recipient sorted list:");
        recipientSortedList.forEach(pair -> System.out.println(pair.getKey() + ":" + pair.getValue()));
        System.out.println("sender sorted list:");
        senderSortedList.forEach(pair -> System.out.println(pair.getKey() + ":" + pair.getValue()));

        List<PaymentEntity> paymentEntities = new LinkedList<>();
        //create result
        while (!senderSortedList.isEmpty()) {
            Pair<BigInteger, BigDecimal> senderPair = senderSortedList.get(0);
            Pair<BigInteger, BigDecimal> recipientPair = recipientSortedList.get(0);

            System.out.println("recipientPair sorted list:");
            recipientSortedList.forEach(p -> System.out.println(senderPair.getKey() + ":" + p.getValue()));
            System.out.println("sender sorted list:");
            senderSortedList.forEach(p -> System.out.println(senderPair.getKey() + ":" + p.getValue()));


            BigDecimal difference = senderPair.getValue().add(recipientPair.getValue());
            if (difference.compareTo(BigDecimal.ZERO) < 0) {
                paymentEntities.add(
                        createPaymentEntity(senderPair.getKey(), recipientPair.getKey(), senderPair.getValue(), partyEntityOpt.get().getPartyId()));
                recipientSortedList.remove(0);
                recipientSortedList.add(new Pair<>(recipientPair.getKey(), difference));
                recipientSortedList.sort(comparatorByCost);
                senderSortedList.remove(0);
                System.out.println("a");
            } else if (difference.compareTo(BigDecimal.ZERO) > 0) {
                paymentEntities.add(
                        createPaymentEntity(senderPair.getKey(), recipientPair.getKey(), recipientPair.getValue().abs(), partyEntityOpt.get().getPartyId()));
                recipientSortedList.remove(0);
                senderSortedList.remove(0);
                senderSortedList.addFirst(new Pair<>(senderPair.getKey(), difference));
                System.out.println("b");
            } else {
                paymentEntities.add(
                        createPaymentEntity(senderPair.getKey(), recipientPair.getKey(), senderPair.getValue(), partyEntityOpt.get().getPartyId()));
                recipientSortedList.remove(0);
                senderSortedList.remove(0);
                System.out.println("c");
            }
        }

        System.out.println(paymentEntities);
        paymentRepository.deleteAll(paymentRepository.findAllByPartyId(partyEntityOpt.get().getPartyId()));
        paymentRepository.saveAll(paymentEntities);
        System.out.println(paymentEntities);

        List<Payment> payments = paymentEntities
                .stream()
                .map(paymentMapper::map)
                .collect(Collectors.toList());

        ArrayOfPayments arrayOfPayments = new ArrayOfPayments();
        arrayOfPayments.addAll(payments);
        return ResponseEntity.ok(arrayOfPayments);
    }

    @Override
    public ResponseEntity<ArrayOfPayments> getPartyPayments(String partyId) {
        Optional<PartyEntity> partyEntityOpt = partyRepository.findById(new BigInteger(partyId));

        if (partyEntityOpt.isEmpty())
            return ResponseEntity.notFound().build();

        List<PaymentEntity> allByPartyId = paymentRepository.findAllByPartyId(partyEntityOpt.get().getPartyId());

        if (allByPartyId.isEmpty())
            return ResponseEntity.notFound().build();

        List<Payment> payments = allByPartyId
                .stream()
                .map(paymentMapper::map)
                .collect(Collectors.toList());

        ArrayOfPayments arrayOfPayments = new ArrayOfPayments();
        arrayOfPayments.addAll(payments);

        return ResponseEntity.ok(arrayOfPayments);
    }

    private PaymentEntity createPaymentEntity(BigInteger from, BigInteger to, BigDecimal cost, BigInteger partyId) {
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setUserIdSender(from);
        paymentEntity.setUserIdReceiver(to);
        paymentEntity.setCost(cost);
        paymentEntity.setCurrency("rub");
        paymentEntity.setPartyId(partyId);
        paymentEntity.setIsPaid(Boolean.FALSE);
        paymentEntity.setIsConfirmed(Boolean.FALSE);
        return paymentEntity;
    }
}
