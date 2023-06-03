package io.github.augustoravazoli.bookapi.book;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import static jakarta.persistence.GenerationType.IDENTITY;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table(name = "books")
@Entity
class Book {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String title;

  @Column(nullable = false)
  private String description;

  @Column(nullable = false, unique = true)
  private String isbn;

  private boolean published;

  public Book() {}

  public Book(Long id, String title, String description, String isbn, boolean published) {
    this.id = id;
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

  public String getDescription() {
    return description;
  }

  public String getIsbn() {
    return isbn;
  }

  public boolean isPublished() {
    return published;
  }

}