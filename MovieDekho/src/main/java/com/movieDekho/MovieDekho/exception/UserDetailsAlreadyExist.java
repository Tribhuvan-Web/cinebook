package com.movieDekho.MovieDekho.exception;

public class UserDetailsAlreadyExist extends RuntimeException {
    public UserDetailsAlreadyExist(String message) {
        super(message);
    }
}
