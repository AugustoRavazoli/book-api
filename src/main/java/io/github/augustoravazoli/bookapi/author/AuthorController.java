package io.github.augustoravazoli.bookapi.author;

import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

  @GetMapping("/{id}")
  public ResponseEntity<?> findAuthor(@PathVariable long id) {
    var author = Stream.of(id)
      .map(authorService::findAuthor)
      .map(authorMapper::toResponse)
      .findAny()
      .get();
    return ResponseEntity.ok(author);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> editAuthor(@PathVariable long id, @Valid @RequestBody AuthorRequest newAuthor) {
    var editedAuthor = Stream.of(newAuthor)
      .map(authorMapper::toEntity)
      .map(author -> authorService.editAuthor(id, author))
      .map(authorMapper::toResponse)
      .findAny()
      .get();
    return ResponseEntity.ok(editedAuthor);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteAuthor(@PathVariable long id) {
    authorService.deleteAuthor(id); 
    return ResponseEntity.noContent().build();
  }

}
