package io.github.augustoravazoli.bookapi.author;

public record AuthorResponse(
  long id,
  String name,
  String email
) {}
