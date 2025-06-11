package com.example.bulletinboard.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

//404 NOT_FOUNDを返す
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    
    //例外のシリアルナンバー
    private static final long serialVersionUID = 1L;
    
    public ResourceNotFoundException(String message) {
        super(message);
    }

}
