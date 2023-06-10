package io.github.augustoravazoli.bookapi.book;

import org.springframework.stereotype.Component;

@Component
public class BookMapper {

  public Book toEntity(BookRequest bookRequest) {
    return new Book(
      bookRequest.title(),
      bookRequest.description(),
      bookRequest.isbn(),
      bookRequest.published()
    );
  }

  public BookResponse toResponse(Book book) {
    return new BookResponse(
      book.getId(),
      book.getTitle(),
      book.getDescription(),
      book.getIsbn(),
      book.isPublished()
    );
  }

}
