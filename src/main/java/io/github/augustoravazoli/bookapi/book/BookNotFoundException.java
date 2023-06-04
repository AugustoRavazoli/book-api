package io.github.augustoravazoli.bookapi.book;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.web.server.ResponseStatusException;

class BookNotFoundException extends ResponseStatusException {

  public BookNotFoundException(long id) {
    super(NOT_FOUND, String.format("Book with given id \"%s\" doesn't exists", id));
  }

}
