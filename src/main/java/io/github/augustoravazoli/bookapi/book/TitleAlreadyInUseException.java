package io.github.augustoravazoli.bookapi.book;

import static org.springframework.http.HttpStatus.CONFLICT;
import org.springframework.web.server.ResponseStatusException;

class TitleAlreadyInUseException extends ResponseStatusException {
  
  public TitleAlreadyInUseException(String title) {
    super(CONFLICT, String.format("Book with given title \"%s\" already exists", title));
  }

}
