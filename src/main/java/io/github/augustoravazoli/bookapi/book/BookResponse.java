package io.github.augustoravazoli.bookapi.book;

record BookResponse(
  long id,
  String title,
  String description,
  String isbn,
  boolean published
) {}
