package io.github.augustoravazoli.bookapi.book;

import static org.springframework.http.HttpStatus.CONFLICT;
import org.springframework.web.server.ResponseStatusException;

class IsbnAlreadyInUseException extends ResponseStatusException {
  
  public IsbnAlreadyInUseException(String isbn) {
    super(CONFLICT, String.format("Book with given ISBN \"%s\" already exists", isbn));
  }

}
