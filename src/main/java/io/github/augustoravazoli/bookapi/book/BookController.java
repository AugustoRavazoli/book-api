package io.github.augustoravazoli.bookapi.book;

import java.util.stream.Stream;
import org.springframework.data.domain.Pageable;
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
import io.github.augustoravazoli.bookapi.author.AuthorMapper;

@RequestMapping("/api/books")
@Controller
class BookController {

  private final BookService bookService;
  private final BookMapper bookMapper;
  private final AuthorMapper authorMapper;

  public BookController(BookService bookService, BookMapper bookMapper, AuthorMapper authorMapper) {
    this.bookService = bookService;
    this.bookMapper = bookMapper;
    this.authorMapper = authorMapper;
  }

  @PostMapping
  public ResponseEntity<?> createBook(@Valid @RequestBody BookRequest book, UriComponentsBuilder builder) {
    var savedBook = Stream.of(book)
      .map(bookMapper::toEntity)
      .map(bookService::createBook)
      .map(bookMapper::toResponse)
      .findAny()
      .get();
    var location = builder
      .path("/api/books/{id}")
      .buildAndExpand(savedBook.id())
      .toUri();
    return ResponseEntity.created(location).body(savedBook);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> findBook(@PathVariable long id) {
    var book = Stream.of(id)
      .map(bookService::findBook)
      .map(bookMapper::toResponse)
      .findAny()
      .get();
    return ResponseEntity.ok(book);
  }

  @GetMapping
  public ResponseEntity<?> findAllBooks(Pageable page) {
    var books = bookService.findAllBooks(page)
      .map(bookMapper::toResponse);
    return ResponseEntity.ok()
      .header("X-Total-Count", String.valueOf(books.getTotalElements()))
      .body(books.getContent());
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> editBook(@PathVariable long id, @Valid @RequestBody BookRequest newBook) {
    var editedBook = Stream.of(newBook)
      .map(bookMapper::toEntity)
      .map(book -> bookService.editBook(id, book))
      .map(bookMapper::toResponse)
      .findAny()
      .get();
    return ResponseEntity.ok(editedBook);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteBook(@PathVariable long id) {
    bookService.deleteBook(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/authors")
  public ResponseEntity<?> findBookAuthors(@PathVariable long id) {
    var authors = bookService.findBookAuthors(id)
      .stream()
      .map(authorMapper::toResponse)
      .toList();
    return ResponseEntity.ok(authors);
  }

  @PutMapping("/{book-id}/authors/{author-id}")
  public ResponseEntity<?> addAuthorToBook(
    @PathVariable("book-id") long bookId,
    @PathVariable("author-id") long authorId
  ) {
    bookService.addAuthorToBook(bookId, authorId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{book-id}/authors/{author-id}")
  public ResponseEntity<?> removeAuthorFromBook(
    @PathVariable("book-id") long bookId,
    @PathVariable("author-id") long authorId
  ) {
    bookService.removeAuthorFromBook(bookId, authorId);
    return ResponseEntity.noContent().build();
  }

}
