package com.example.bulletinboard.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

//409を返す
@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends RuntimeException {

    // 例外のシリアルナンバー
    private static final long serialVersionUID = 2L;

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
