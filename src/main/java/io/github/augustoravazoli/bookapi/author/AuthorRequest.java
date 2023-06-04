package io.github.augustoravazoli.bookapi.author;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

record AuthorRequest(

  @NotBlank
  String name,

  @Email
  @NotBlank
  String email

) {}
