package io.github.augustoravazoli.bookapi.book;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

  List<Book> findAllByAuthorsId(long id);

  boolean existsByTitle(String title);

  boolean existsByIsbn(String isbn);

}
