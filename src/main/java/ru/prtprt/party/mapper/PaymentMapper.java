package ru.prtprt.party.mapper;

import org.springframework.stereotype.Component;
import ru.prtprt.party.entity.PaymentEntity;
import ru.prtprt.party.model.model.Payment;

@Component
public class PaymentMapper {

    public Payment map(PaymentEntity paymentEntity) {
        Payment payment = new Payment();
        payment.setPaymentId(paymentEntity.getPaymentId().toString());
        payment.setUserSenderId(paymentEntity.getUserIdSender().toString());
        payment.setUserReceiverId(paymentEntity.getUserIdReceiver().toString());
        payment.setCost(paymentEntity.getCost().toString());
        payment.currency(paymentEntity.getCurrency());
        payment.setPartyId(paymentEntity.getPartyId().toString());
        payment.setIsConfirmed(paymentEntity.getIsConfirmed());
        payment.setIsPaid(paymentEntity.getIsPaid());
        return payment;
    }
}
