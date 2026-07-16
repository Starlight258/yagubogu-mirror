package com.yagubogu.outbox.exception;

import com.yagubogu.global.exception.YaguBoguException;

public class UnsupportedOutboxEventTypeException extends YaguBoguException {

    public UnsupportedOutboxEventTypeException(final String eventType) {
        super("Unsupported outbox event type: " + eventType);
    }
}
