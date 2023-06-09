package io.github.augustoravazoli.bookapi.author;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import static jakarta.persistence.GenerationType.IDENTITY;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import io.github.augustoravazoli.bookapi.book.Book;

@Table(name = "authors")
@Entity
public class Author {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @ManyToMany(mappedBy = "authors")
  private Set<Book> books = new HashSet<>();

  public Author() {}

  public Author(String name, String email) {
    this.name = name;
    this.email = email;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  protected void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  protected void setEmail(String email) {
    this.email = email;
  }

  public Set<Book> getBooks() {
    return books;
  }

}
