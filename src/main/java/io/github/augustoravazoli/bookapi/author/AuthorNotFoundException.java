package io.github.augustoravazoli.bookapi.author;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import org.springframework.web.server.ResponseStatusException;

public class AuthorNotFoundException extends ResponseStatusException {

  public AuthorNotFoundException(long id) {
    super(NOT_FOUND, String.format("Author with given id \"%s\" doesn't exists", id));
  }

}
