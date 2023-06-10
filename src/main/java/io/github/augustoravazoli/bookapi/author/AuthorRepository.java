package io.github.augustoravazoli.bookapi.author;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

  List<Author> findAllByBooksId(long id);

  boolean existsByEmail(String email);

}
