package io.github.augustoravazoli.bookapi.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class BookService {

  private final BookRepository bookRepository;

  @Autowired
  public BookService(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  public Book createBook(Book book) {
    if (bookRepository.existsByTitle(book.getTitle())) {
      throw new TitleAlreadyInUseException(book.getTitle());
    }
    if (bookRepository.existsByIsbn(book.getIsbn())) {
      throw new IsbnAlreadyInUseException(book.getIsbn());
    }
    return bookRepository.save(book);
  }

  public Book findBook(long id) {
    return bookRepository
      .findById(id)
      .orElseThrow(() -> new BookNotFoundException(id));
  }

}
