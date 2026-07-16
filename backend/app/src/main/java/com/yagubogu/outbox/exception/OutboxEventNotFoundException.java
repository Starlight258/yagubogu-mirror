package com.yagubogu.outbox.exception;

import com.yagubogu.global.exception.YaguBoguException;

public class OutboxEventNotFoundException extends YaguBoguException {

    public OutboxEventNotFoundException(final Long eventId) {
        super("Outbox event not found: id=" + eventId);
    }
}
