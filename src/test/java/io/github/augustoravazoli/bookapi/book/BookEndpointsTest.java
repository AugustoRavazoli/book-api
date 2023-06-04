package io.github.augustoravazoli.bookapi.book;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;

import io.github.augustoravazoli.bookapi.EndpointsTestTemplate;

@SpringBootTest
class BookEndpointsTest extends EndpointsTestTemplate {

  @Autowired
  private BookRepository bookRepository;

  @BeforeEach
  void setUp() {
    bookRepository.deleteAll();
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
      assertThat(bookRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Don't create book when title is already in use")
    void givenTitleTaken_whenCreateBook_thenReturns409() throws Exception {
      // given
      bookRepository.save(new Book(null, "The Lord of the Rings", "Fantasy", "9780544003415", true));
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
      bookRepository.save(new Book(null, "The Lord of the Rings", "Fantasy", "9780544003415", true));
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
      var book = bookRepository.save(new Book(null, "The Lord of the Rings", "Fantasy", "9780544003415", true));
      var id = book.getId().intValue();
      // when
      client.perform(get("/api/v1/books/{id}", id))
      // then
      .andExpectAll(
        status().isOk(),
        jsonPath("$.id", is(id)),
        jsonPath("$.title", is("The Lord of the Rings")),
        jsonPath("$.description", is("Fantasy")),
        jsonPath("$.isbn", is("9780544003415")),
        jsonPath("$.published", is(true))
      )
      .andDo(document("book/find"));    
    }

    @Test
    @DisplayName("Don't find book when book doesn't exists")
    void givenNonExistentBook_whenFindBook_thenReturns404() throws Exception {
      // given
      var id = 1;
      // when
      client.perform(get("/api/v1/books/{id}", id))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Book with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

  }

  @Nested
  @DisplayName("Book edit scenarios")
  class EditBookEndpointTests {

    private Book book;

    @BeforeEach
    void setUp() {
      var newBook = new Book(null, "The Lord of the Rings", "Fantasy", "9780544003415", true);
      book = bookRepository.save(newBook);
    }

    @Test
    @DisplayName("Edit book with success")
    void whenEditBook_thenReturns200() throws Exception {
      // given
      var newBook = new BookRequest("The Hobbit", "Some detailed description", "9780008376055", false);
      // when
      client.perform(put("/api/v1/books/{id}", book.getId())
        .contentType(APPLICATION_JSON)
        .content(toJson(newBook))
      )
      // then
      .andExpectAll(
        status().isOk(),
        jsonPath("$.id", is(book.getId().intValue())),
        jsonPath("$.title", is("The Hobbit")),
        jsonPath("$.description", is("Some detailed description")),
        jsonPath("$.isbn", is("9780008376055")),
        jsonPath("$.published", is(false))
      )
      .andDo(document("book/edit"));
      // and
      assertThat(bookRepository.count()).isEqualTo(1);
      assertThat(bookRepository.findById(book.getId()).get())
        .usingRecursiveComparison()
        .isNotEqualTo(book);
    }

    @Test
    @DisplayName("Don't edit book when book doesn't exists")
    void givenNonexistentBook_whenEditBook_thenReturns404() throws Exception {
      // given
      bookRepository.delete(book);
      // and
      var newBook = new BookRequest("The Hobbit", "Some detailed description", "9780008376055", false);
      // when
      client.perform(put("/api/v1/books/{id}", 1)
        .contentType(APPLICATION_JSON)
        .content(toJson(newBook))
      )
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Book with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
      assertThat(bookRepository.count()).isZero();
    }

    @Test
    @DisplayName("Don't edit book when title is already in use by other book")
    void givenTitleTaken_whenEditBook_thenReturns409() throws Exception {
      // given
      bookRepository.save(new Book(null, "The Hobbit", "", "", false));
      // and
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
      assertThat(bookRepository.count()).isEqualTo(2);
      assertThat(bookRepository.findById(book.getId()).get())
        .usingRecursiveComparison()
        .isEqualTo(book);
    }

    @Test
    @DisplayName("Don't edit book when ISBN is already in use by other book")
    void givenIsbnTaken_whenEditBook_thenReturns409() throws Exception {
      // given
      bookRepository.save(new Book(null, "", "", "9780008376055", false));
      // and
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
      assertThat(bookRepository.count()).isEqualTo(2);
      assertThat(bookRepository.findById(book.getId()).get())
        .usingRecursiveComparison()
        .isEqualTo(book);
    }

    @Test
    @DisplayName("Don't edit book when new book information are invalid")
    void givenInvalidBook_whenEditBook_thenReturns422() throws Exception {
      // given
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
      assertThat(bookRepository.count()).isEqualTo(1);
      assertThat(bookRepository.findById(book.getId()).get())
        .usingRecursiveComparison()
        .isEqualTo(book);
    }

  }

  @Nested
  @DisplayName("Book delete scenarios")
  class DeleteBookEndpointTests {

    @Test
    @DisplayName("Delete book with success")
    void whenDeleteBook_thenReturns204() throws Exception {
      // given
      var book = bookRepository.save(new Book(null, "The Lord of the Rings", "Fantasy", "9780544003415", true));
      var id = book.getId().intValue();
      // when
      client.perform(delete("/api/v1/books/{id}", id))
      // then
      .andExpectAll(
        status().isNoContent(),
        jsonPath("$").doesNotExist()
      )
      .andDo(document("book/delete"));
      // and
      assertThat(bookRepository.count()).isZero();
    }

    @Test
    @DisplayName("Don't delete book when book doesn't exists")
    void givenNonexistentBook_whenDeleteBook_thenReturns404() throws Exception {
      // given
      var id = 1;
      // when
      client.perform(delete("/api/v1/books/{id}", id))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Book with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

  }

}
