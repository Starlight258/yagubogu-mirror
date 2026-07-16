package com.yagubogu.outbox.exception;

import com.yagubogu.global.exception.YaguBoguException;

public class OutboxPayloadSerializationException extends YaguBoguException {

    public OutboxPayloadSerializationException(final String gameCode, final Throwable cause) {
        super("Failed to serialize outbox payload: gameCode=" + gameCode, cause);
    }
}
