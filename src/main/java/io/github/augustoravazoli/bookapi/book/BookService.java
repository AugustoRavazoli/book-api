package io.github.augustoravazoli.bookapi.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.github.augustoravazoli.bookapi.author.AuthorNotFoundException;
import io.github.augustoravazoli.bookapi.author.AuthorRepository;

@Service
class BookService {

  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;

  @Autowired
  public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
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

  public Book editBook(long id, Book newBook) {
    return bookRepository
      .findById(id)
      .map(book -> {
        if (!newBook.getTitle().equals(book.getTitle()) && bookRepository.existsByTitle(newBook.getTitle())) {
          throw new TitleAlreadyInUseException(newBook.getTitle());
        }
        if (!newBook.getIsbn().equals(book.getIsbn()) && bookRepository.existsByIsbn(newBook.getIsbn())) {
          throw new IsbnAlreadyInUseException(newBook.getIsbn());
        }
        book.setTitle(newBook.getTitle());
        book.setDescription(newBook.getDescription());
        book.setIsbn(newBook.getIsbn());
        book.setPublished(newBook.isPublished());
        return bookRepository.save(book);
      })
      .orElseThrow(() -> new BookNotFoundException(id));
  }

  public void deleteBook(long id) {
    if (!bookRepository.existsById(id)) {
      throw new BookNotFoundException(id);
    }
    bookRepository.deleteById(id);
  }

  public void addAuthorToBook(long bookId, long authorId) {
    var book = bookRepository.findById(bookId)
      .orElseThrow(() -> new BookNotFoundException(bookId));
    var author = authorRepository.findById(authorId)
      .orElseThrow(() -> new AuthorNotFoundException(authorId));
    book.addAuthor(author);
    bookRepository.save(book);
  }

  public void removeAuthorFromBook(long bookId, long authorId) {
    var book = bookRepository.findById(bookId)
      .orElseThrow(() -> new BookNotFoundException(bookId));
    var author = authorRepository.findById(authorId)
      .orElseThrow(() -> new AuthorNotFoundException(authorId));
    book.removeAuthor(author);
    bookRepository.save(book);
  }

}
