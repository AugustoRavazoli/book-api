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
import io.github.augustoravazoli.bookapi.book.BookMapper;

@RequestMapping("/api/authors")
@Controller
class AuthorController {
  
  private final AuthorService authorService;
  private final AuthorMapper authorMapper;
  private final BookMapper bookMapper;

  @Autowired
  public AuthorController(AuthorService authorService, AuthorMapper authorMapper, BookMapper bookMapper) {
    this.authorService = authorService;
    this.authorMapper = authorMapper;
    this.bookMapper = bookMapper;
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
      .path("/api/authors/{id}")
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

  @GetMapping("/{id}/books")
  public ResponseEntity<?> findAuthorBooks(@PathVariable long id) {
    var books = authorService.findAuthorBooks(id)
      .stream()
      .map(bookMapper::toResponse)
      .toList();
    return ResponseEntity.ok(books);
  }

  @PutMapping("/{author-id}/books/{book-id}")
  public ResponseEntity<?> addBookToAuthor(
    @PathVariable("author-id") long authorId,
    @PathVariable("book-id") long bookId
  ) {
    authorService.addBookToAuthor(authorId, bookId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{author-id}/books/{book-id}")
  public ResponseEntity<?> removeBookFromAuthor(
    @PathVariable("author-id") long authorId,
    @PathVariable("book-id") long bookId
  ) {
    authorService.removeBookFromAuthor(authorId, bookId);
    return ResponseEntity.noContent().build();
  }

}
