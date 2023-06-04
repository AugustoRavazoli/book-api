package io.github.augustoravazoli.bookapi.author;

import static org.springframework.http.HttpStatus.CONFLICT;
import org.springframework.web.server.ResponseStatusException;

class EmailAlreadyInUseException extends ResponseStatusException {

  public EmailAlreadyInUseException(String email) {
    super(CONFLICT, String.format("Email \"%s\" already in use", email));
  }

}
