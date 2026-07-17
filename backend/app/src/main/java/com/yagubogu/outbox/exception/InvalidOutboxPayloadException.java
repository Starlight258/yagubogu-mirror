package com.yagubogu.outbox.exception;

import com.yagubogu.global.exception.YaguBoguException;

public class InvalidOutboxPayloadException extends YaguBoguException {

    public InvalidOutboxPayloadException(final String message) {
        super(message);
    }
}
