package io.github.augustoravazoli.bookapi.author;

import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.validation.Valid;

@RequestMapping("/api/v1/authors")
@Controller
class AuthorController {
  
  private final AuthorService authorService;
  private final AuthorMapper authorMapper;

  @Autowired
  public AuthorController(AuthorService authorService, AuthorMapper authorMapper) {
    this.authorService = authorService;
    this.authorMapper = authorMapper;
  }

  @PostMapping
  public ResponseEntity<?> createAuthor(
    @Valid @RequestBody AuthorRequest authorRequest, 
    UriComponentsBuilder builder
  ) {
    var savedAuthor = Stream.of(authorRequest)
      .map(authorMapper::toEntity)
      .map(authorService::createAuthor)
      .map(authorMapper::toResponse)
      .findFirst()
      .get();
    var location = builder
      .path("/api/v1/authors/{id}")
      .buildAndExpand(savedAuthor.id())
      .toUri();
    return ResponseEntity.created(location).body(savedAuthor);
  }

}
