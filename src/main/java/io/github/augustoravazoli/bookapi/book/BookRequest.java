package io.github.augustoravazoli.bookapi.book;

import org.hibernate.validator.constraints.ISBN;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record BookRequest(

  @NotBlank
  String title,

  @NotBlank
  String description,

  @ISBN
  @NotBlank
  String isbn,

  @NotNull
  Boolean published

) {}
