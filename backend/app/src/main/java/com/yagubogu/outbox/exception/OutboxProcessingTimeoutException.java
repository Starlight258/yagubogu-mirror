package com.yagubogu.outbox.exception;

import com.yagubogu.global.exception.YaguBoguException;
import java.time.Duration;

public class OutboxProcessingTimeoutException extends YaguBoguException {

    public OutboxProcessingTimeoutException(final Long eventId, final Duration timeout) {
        super("Outbox processing timed out: id=" + eventId + ", timeout=" + timeout);
    }
}
