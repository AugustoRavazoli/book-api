package io.github.augustoravazoli.bookapi.book;

import static java.util.Arrays.asList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;

import io.github.augustoravazoli.bookapi.EndpointsTestTemplate;
import io.github.augustoravazoli.bookapi.author.Author;
import io.github.augustoravazoli.bookapi.author.AuthorRepository;

@SpringBootTest
class BookEndpointsTest extends EndpointsTestTemplate {

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private AuthorRepository authorRepository;

  @BeforeEach
  void setUp() {
    bookRepository.deleteAll();
    authorRepository.deleteAll();
  }

  @Nested
  @DisplayName("Book creation scenarios")
  class CreateBookEndpointTests {

    @Test
    @DisplayName("Create book with success")
    void whenCreateBook_thenReturns201() throws Exception {
      // given
      var newBook = new BookRequest("The Lord of the Rings", "Fantasy", "9780544003415", true);
      // when
      client.perform(post("/api/v1/books")
        .contentType(APPLICATION_JSON)
        .content(toJson(newBook))
      )
      // then
      .andExpectAll(
        status().isCreated(),
        redirectedUrlPattern("**/api/v1/books/*"),
        jsonPath("$.id", notNullValue(Long.class)),
        jsonPath("$.title", is("The Lord of the Rings")),
        jsonPath("$.description", is("Fantasy")),
        jsonPath("$.isbn", is("9780544003415")),
        jsonPath("$.published", is(true))
      )
      .andDo(document("book/create", snippet()));
      // and
      assertThat(bookRepository.findAll()).size().isEqualTo(1)
        .returnToIterable()
        .extracting("title", "description", "isbn", "published")
        .contains(tuple("The Lord of the Rings", "Fantasy", "9780544003415", true));
    }

    @Test
    @DisplayName("Don't create book when title is already in use")
    void givenTitleTaken_whenCreateBook_thenReturns409() throws Exception {
      // given
      bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      var newBook = new BookRequest("The Lord of the Rings", "Fantasy", "9780008376055", false);
      // when
      client.perform(post("/api/v1/books")
        .contentType(APPLICATION_JSON)
        .content(toJson(newBook))
      )
      // then
      .andExpectAll(
        status().isConflict(),
        jsonPath("$.message", is("Book with given title \"The Lord of the Rings\" already exists")),
        jsonPath("$.details").doesNotExist()
      );
      assertThat(bookRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Don't create book when ISBN is already in use")
    void givenIsbnTaken_whenCreateBook_thenReturns409() throws Exception {
      // given
      bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      var newBook = new BookRequest("The Hobbit", "Fantasy", "9780544003415", false);
      // when
      client.perform(post("/api/v1/books")
        .contentType(APPLICATION_JSON)
        .content(toJson(newBook))
      )
      // then
      .andExpectAll(
        status().isConflict(),
        jsonPath("$.message", is("Book with given ISBN \"9780544003415\" already exists")),
        jsonPath("$.details").doesNotExist()
      );
      assertThat(bookRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Don't create book when book information are invalid")
    void givenInvalidBook_whenCreateBook_thenReturns422() throws Exception {
      // given
      var newBook = new BookRequest("", "", "", null);
      // when
      client.perform(post("/api/v1/books")
        .contentType(APPLICATION_JSON)
        .content(toJson(newBook))
      )
      // then
      .andExpectAll(
        status().isUnprocessableEntity(),
        jsonPath("$.message", is("Validation errors on your request")),
        jsonPath("$.details", hasSize(5)),
        jsonPath("$.details", containsInAnyOrder(
          violation("title", "must not be blank"),
          violation("description", "must not be blank"),
          violation("isbn", "must not be blank"),
          violation("isbn", "invalid ISBN"),
          violation("published", "must not be null")
        ))
      );
      assertThat(bookRepository.count()).isZero();
    }

    private RequestFieldsSnippet snippet() {
      var fields = fields(BookRequest.class);
      return requestFields(
        fields.path("title").description("Book's title"),
        fields.path("description").description("Book's description"),
        fields.path("isbn").description("Book's ISBN"),
        fields.path("published").description("Book's publication state")
      );
    }

  }

  @Nested
  @DisplayName("Book find scenarios")
  class FindBookEndpointTests {

    @Test
    @DisplayName("Find book with success")
    void whenFindBook_thenReturns200() throws Exception {
      // given
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      // when
      client.perform(get("/api/v1/books/{id}", book.getId()))
      // then
      .andExpectAll(
        status().isOk(),
        jsonPath("$.id", notNullValue(Long.class)),
        jsonPath("$.title", is("The Lord of the Rings")),
        jsonPath("$.description", is("Fantasy")),
        jsonPath("$.isbn", is("9780544003415")),
        jsonPath("$.published", is(true))
      )
      .andDo(document("book/find"));    
    }

    @Test
    @DisplayName("Don't find book when book doesn't exists")
    void givenNonexistentBook_whenFindBook_thenReturns404() throws Exception {
      // when
      client.perform(get("/api/v1/books/1"))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Book with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

  }

  @Nested
  @DisplayName("Book find all scenarios")
  class FindAllBooksEndpointTests {
    
    @Test
    @DisplayName("Find all books with success")
    void whenFindAllBooks_thenReturns200() throws Exception {
      // given
      bookRepository.saveAll(asList(
        new Book("The Lord of the Rings", "Fantasy", "9780544003415", true),
        new Book("The Hobbit", "Some detailed description", "9780008376055", false),
        new Book("The Silmarillion", "description", "9780618391110", true),
        new Book("The Chronicles of Narnia", "description again", "9780060847133", false)
      ));
      // when
      client.perform(get("/api/v1/books"))
      // then
      .andExpectAll(
        status().isOk(),
        header().string("X-Total-Count", "4"),
        jsonPath("$", hasSize(4))
      )
      .andDo(document("book/find-all"));
    }

    @Test
    @DisplayName("Find all books paginated with success")
    void givenPage_whenFindAllBooks_thenReturns200() throws Exception {
      // given
      bookRepository.saveAll(asList(
        new Book("The Lord of the Rings", "Fantasy", "9780544003415", true),
        new Book("The Hobbit", "Some detailed description", "9780008376055", false),
        new Book("The Silmarillion", "description", "9780618391110", true),
        new Book("The Chronicles of Narnia", "description again", "9780060847133", false)
      ));
      // when
      client.perform(get("/api/v1/books")
        .param("page", "0")
        .param("size", "2")
      )
      // then
      .andExpectAll(
        status().isOk(),
        header().string("X-Total-Count", "4"),
        jsonPath("$", hasSize(2))
      );
    }

  }

  @Nested
  @DisplayName("Book edit scenarios")
  class EditBookEndpointTests {

    @Test
    @DisplayName("Edit book with success")
    void whenEditBook_thenReturns200() throws Exception {
      // given
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      var newBook = new BookRequest("The Hobbit", "Some detailed description", "9780008376055", false);
      // when
      client.perform(put("/api/v1/books/{id}", book.getId())
        .contentType(APPLICATION_JSON)
        .content(toJson(newBook))
      )
      // then
      .andExpectAll(
        status().isOk(),
        jsonPath("$.id", notNullValue(Long.class)),
        jsonPath("$.title", is("The Hobbit")),
        jsonPath("$.description", is("Some detailed description")),
        jsonPath("$.isbn", is("9780008376055")),
        jsonPath("$.published", is(false))
      )
      .andDo(document("book/edit"));
      // and
      assertThat(bookRepository.findById(book.getId())).get()
        .extracting("title", "description", "isbn", "published")
        .contains("The Hobbit", "Some detailed description", "9780008376055", false);
    }

    @Test
    @DisplayName("Don't edit book when book doesn't exists")
    void givenNonexistentBook_whenEditBook_thenReturns404() throws Exception {
      // given
      var newBook = new BookRequest("The Hobbit", "Some detailed description", "9780008376055", false);
      // when
      client.perform(put("/api/v1/books/1")
        .contentType(APPLICATION_JSON)
        .content(toJson(newBook))
      )
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Book with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

    @Test
    @DisplayName("Don't edit book when title is already in use by other book")
    void givenTitleTaken_whenEditBook_thenReturns409() throws Exception {
      // given
      bookRepository.save(new Book("The Hobbit", "", "", false));
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      var newBook = new BookRequest("The Hobbit", "Some detailed description", "9780008376055", false);
      // when
      client.perform(put("/api/v1/books/{id}", book.getId())
        .contentType(APPLICATION_JSON)
        .content(toJson(newBook))
      )
      // then
      .andExpectAll(
        status().isConflict(),
        jsonPath("$.message", is("Book with given title \"The Hobbit\" already exists")),
        jsonPath("$.details").doesNotExist()
      );
      assertThat(bookRepository.findById(book.getId())).get()
        .extracting("title", "description", "isbn", "published")
        .contains("The Lord of the Rings", "Fantasy", "9780544003415", true);
    }

    @Test
    @DisplayName("Don't edit book when ISBN is already in use by other book")
    void givenIsbnTaken_whenEditBook_thenReturns409() throws Exception {
      // given
      bookRepository.save(new Book("", "", "9780008376055", false));
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      var newBook = new BookRequest("The Hobbit", "Some detailed description", "9780008376055", false);
      // when
      client.perform(put("/api/v1/books/{id}", book.getId())
        .contentType(APPLICATION_JSON)
        .content(toJson(newBook))
      )
      // then
      .andExpectAll(
        status().isConflict(),
        jsonPath("$.message", is("Book with given ISBN \"9780008376055\" already exists")),
        jsonPath("$.details").doesNotExist()
      );
      assertThat(bookRepository.findById(book.getId())).get()
        .extracting("title", "description", "isbn", "published")
        .contains("The Lord of the Rings", "Fantasy", "9780544003415", true);
    }

    @Test
    @DisplayName("Don't edit book when new book information are invalid")
    void givenInvalidBook_whenEditBook_thenReturns422() throws Exception {
      // given
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      var newBook = new BookRequest("", "", "", null);
      // when
      client.perform(put("/api/v1/books/{id}", book.getId())
        .contentType(APPLICATION_JSON)
        .content(toJson(newBook))
      )
      // then
      .andExpectAll(
        status().isUnprocessableEntity(),
        jsonPath("$.message", is("Validation errors on your request")),
        jsonPath("$.details", hasSize(5)),
        jsonPath("$.details", containsInAnyOrder(
          violation("title", "must not be blank"),
          violation("description", "must not be blank"),
          violation("isbn", "must not be blank"),
          violation("isbn", "invalid ISBN"),
          violation("published", "must not be null")
        ))
      );
      assertThat(bookRepository.findById(book.getId())).get()
        .extracting("title", "description", "isbn", "published")
        .contains("The Lord of the Rings", "Fantasy", "9780544003415", true);
    }

  }

  @Nested
  @DisplayName("Book delete scenarios")
  class DeleteBookEndpointTests {

    @Test
    @DisplayName("Delete book with success")
    void whenDeleteBook_thenReturns204() throws Exception {
      // given
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      // when
      client.perform(delete("/api/v1/books/{id}", book.getId()))
      // then
      .andExpectAll(
        status().isNoContent(),
        jsonPath("$").doesNotExist()
      )
      .andDo(document("book/delete"));
      // and
      assertThat(bookRepository.existsById(book.getId())).isFalse();
    }

    @Test
    @DisplayName("Don't delete book when book doesn't exists")
    void givenNonexistentBook_whenDeleteBook_thenReturns404() throws Exception {
      // when
      client.perform(delete("/api/v1/books/1"))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Book with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

  }
  
  @Nested
  @DisplayName("Find book authors scenarios")
  class FindBookAuthorsEndpointTests {

    @Test
    @DisplayName("Find book authors with success")
    void whenFindBookAuthors_thenReturns200() throws Exception {
      // given
      var book = bookRepository.save(new Book("Design Patterns", "Some description", "9780201633610", true));
      var authors = authorRepository.saveAll(asList(
        new Author("Erich Gamma", "erich@example.com"),
        new Author("Richard Helm", "richard@example.com"),
        new Author("Ralph Johnson", "ralph@example.com"),
        new Author("John Vlissides", "john@example.com")
      ));
      authors.forEach(author -> book.addAuthor(author));
      bookRepository.save(book);
      // when
      client.perform(get("/api/v1/books/{id}/authors", book.getId()))
      // then
      .andExpectAll(
        status().isOk(),
        jsonPath("$", hasSize(4))
      )
      .andDo(document("book/find-authors"));
    }

    @Test
    @DisplayName("Don't find book authors when book doesn't exists")
    void givenNonexistentBook_whenFindBookAuthors_thenReturns404() throws Exception {
      // when
      client.perform(get("/api/v1/books/1/authors"))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Book with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

  }

  @Nested
  @DisplayName("Add author to book scenarios")
  class AddAuthorToBookEndpointTests {
    
    @Test
    @DisplayName("Add author to book with success")
    void whenAddAuthorToBook_thenReturns204() throws Exception {
      // given
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      var author = authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
      // when
      client.perform(put("/api/v1/books/{book-id}/authors/{author-id}", book.getId(), author.getId()))
      // then
      .andExpectAll(
        status().isNoContent(),
        jsonPath("$").doesNotExist()
      )
      .andDo(document("book/add-author"));
    }

    @Test
    @DisplayName("Don't add author to book when book doesn't exists")
    void givenNonexistentBook_whenAddAuthorToBook_thenReturns404() throws Exception {
      // given
      var author = authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
      // when
      client.perform(put("/api/v1/books/1/authors/{author-id}", author.getId()))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Book with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

    @Test
    @DisplayName("Don't add author to book when author doesn't exists")
    void givenNonexistentAuthor_whenAddAuthorToBook_thenReturns404() throws Exception {
      // given
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      // when
      client.perform(put("/api/v1/books/{book-id}/authors/1", book.getId()))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Author with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

  }

  @Nested
  @DisplayName("Remove author from book scenarios")
  class RemoveAuthorFromBookEndpointTests {
    
    @Test
    @DisplayName("Remove author from book with success")
    void whenRemoveAuthorFromBook_thenReturns204() throws Exception {
      // given
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      var author = authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
      // and
      book.addAuthor(author);
      bookRepository.save(book);
      // when
      client.perform(delete("/api/v1/books/{book-id}/authors/{author-id}", book.getId(), author.getId()))
      // then
      .andExpectAll(
        status().isNoContent(),
        jsonPath("$").doesNotExist()
      )
      .andDo(document("book/remove-author"));
      // and
      assertThat(bookRepository.existsById(book.getId())).isTrue();
      assertThat(authorRepository.existsById(author.getId())).isTrue();
    }

    @Test
    @DisplayName("Don't remove author from book when book doesn't exists")
    void givenNonexistentBook_whenRemoveAuthorFromBook_thenReturns404() throws Exception {
      // given
      var author = authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
      // when
      client.perform(delete("/api/v1/books/1/authors/{author-id}", author.getId()))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Book with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

    @Test
    @DisplayName("Don't remove author from book when author doesn't exists")
    void givenNonexistentAuthor_whenRemoveAuthorFromBook_thenReturns404() throws Exception {
      // given
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      // when
      client.perform(delete("/api/v1/books/{book-id}/authors/1", book.getId()))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Author with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

  }

}
