package io.github.augustoravazoli.bookapi.author;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AuthorRepository extends JpaRepository<Author, Long> {

  boolean existsByEmail(String email);

}
