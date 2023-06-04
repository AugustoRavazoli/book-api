package io.github.augustoravazoli.bookapi.author;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class AuthorService {

  private final AuthorRepository authorRepository;

  @Autowired
  public AuthorService(AuthorRepository authorRepository) {
    this.authorRepository = authorRepository;
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

}
