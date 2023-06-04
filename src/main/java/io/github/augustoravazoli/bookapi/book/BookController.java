package io.github.augustoravazoli.bookapi.book;

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

@RequestMapping("/api/v1/books")
@Controller
class BookController {

  private final BookService bookService;
  private final BookMapper bookMapper;

  @Autowired
  public BookController(BookService bookService, BookMapper bookMapper) {
    this.bookService = bookService;
    this.bookMapper = bookMapper;
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
      .path("/api/v1/books/{id}")
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

}
