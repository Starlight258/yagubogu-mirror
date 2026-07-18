package com.yagubogu.game.exception;

import com.yagubogu.global.exception.YaguBoguException;

public class InvalidGameStateException extends YaguBoguException {

    public InvalidGameStateException(final String message) {
        super(message);
    }
}
