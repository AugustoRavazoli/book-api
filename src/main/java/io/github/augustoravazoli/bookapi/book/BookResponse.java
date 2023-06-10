package io.github.augustoravazoli.bookapi.book;

public record BookResponse(
  long id,
  String title,
  String description,
  String isbn,
  boolean published
) {}
