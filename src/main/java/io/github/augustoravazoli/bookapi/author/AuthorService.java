package io.github.augustoravazoli.bookapi.author;

import java.util.List;
import org.springframework.stereotype.Service;
import io.github.augustoravazoli.bookapi.book.Book;
import io.github.augustoravazoli.bookapi.book.BookNotFoundException;
import io.github.augustoravazoli.bookapi.book.BookRepository;

@Service
class AuthorService {

  private final AuthorRepository authorRepository;
  private final BookRepository bookRepository;

  public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
    this.authorRepository = authorRepository;
    this.bookRepository = bookRepository;
  }

  public Author createAuthor(Author author) {
    if (authorRepository.existsByEmail(author.getEmail())) {
      throw new EmailAlreadyInUseException(author.getEmail());
    }
    return authorRepository.save(author);
  }

  public Author findAuthor(long id) {
    return authorRepository
      .findById(id)
      .orElseThrow(() -> new AuthorNotFoundException(id));
  }

  public Author editAuthor(long id, Author newAuthor) {
    return authorRepository
      .findById(id)
      .map(author -> {
        if (!newAuthor.getEmail().equals(author.getEmail()) 
          && authorRepository.existsByEmail(newAuthor.getEmail())
        ) {
          throw new EmailAlreadyInUseException(newAuthor.getEmail());
        }
        author.setName(newAuthor.getName());
        author.setEmail(newAuthor.getEmail());
        return authorRepository.save(author);
      })
      .orElseThrow(() -> new AuthorNotFoundException(id));
  }

  public void deleteAuthor(long id) {
    if (!authorRepository.existsById(id)) {
      throw new AuthorNotFoundException(id);
    }
    authorRepository.deleteById(id);
  }

  public List<Book> findAuthorBooks(long id) {
    if (!authorRepository.existsById(id)) {
      throw new AuthorNotFoundException(id);
    }
    return bookRepository.findAllByAuthorsId(id);
  }

  public void addBookToAuthor(long authorId, long bookId) {
    var author = authorRepository.findById(authorId)
      .orElseThrow(() -> new AuthorNotFoundException(authorId));
    var book = bookRepository.findById(bookId)
      .orElseThrow(() -> new BookNotFoundException(bookId));
    author.addBook(book);
    authorRepository.save(author);
  }

  public void removeBookFromAuthor(long authorId, long bookId) {
    var author = authorRepository.findById(authorId)
      .orElseThrow(() -> new AuthorNotFoundException(authorId));
    var book = bookRepository.findById(bookId)
      .orElseThrow(() -> new BookNotFoundException(bookId));
    author.removeBook(book);
    authorRepository.save(author);
  }

}
