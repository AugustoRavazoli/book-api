package io.github.augustoravazoli.bookapi.book;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import static jakarta.persistence.GenerationType.IDENTITY;
import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.REMOVE;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import io.github.augustoravazoli.bookapi.author.Author;

@Table(name = "books")
@Entity
public class Book {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String title;

  @ManyToMany(cascade = { MERGE, REMOVE })
  private Set<Author> authors = new HashSet<>();

  @Column(nullable = false)
  private String description;

  @Column(nullable = false, unique = true)
  private String isbn;

  private boolean published;

  public Book() {}

  public Book(String title, String description, String isbn, boolean published) {
    this.title = title;
    this.description = description;
    this.isbn = isbn;
    this.published = published;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  protected void setTitle(String title) {
    this.title = title;
  }

  public Set<Author> getAuthors() {
    return authors;
  }

  public String getDescription() {
    return description;
  }

  protected void setDescription(String description) {
    this.description = description;
  }

  public String getIsbn() {
    return isbn;
  }

  protected void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public boolean isPublished() {
    return published;
  }

  protected void setPublished(boolean published) {
    this.published = published;
  }

  public void addAuthor(Author author) {
    authors.add(author);
    author.getBooks().add(this);
  }

  public void removeAuthor(Author author) {
    authors.remove(author);
    author.getBooks().remove(this);
  }

}
