package io.github.augustoravazoli.bookapi.author;

import org.springframework.stereotype.Component;

@Component
public class AuthorMapper {

  public Author toEntity(AuthorRequest authorRequest) {
    return new Author(
      authorRequest.name(),
      authorRequest.email()
    );
  }

  public AuthorResponse toResponse(Author author) {
    return new AuthorResponse(
      author.getId(),
      author.getName(),
      author.getEmail()
    );
  }

}
