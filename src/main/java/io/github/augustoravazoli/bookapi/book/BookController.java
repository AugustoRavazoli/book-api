package io.github.augustoravazoli.bookapi.book;

import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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

}
