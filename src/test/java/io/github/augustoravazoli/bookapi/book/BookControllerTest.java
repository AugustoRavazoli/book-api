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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.containsInAnyOrder;

import io.github.augustoravazoli.bookapi.ControllerTestTemplate;

@SpringBootTest
class BookControllerTest extends ControllerTestTemplate {

  @Autowired
  private BookRepository bookRepository;

  @BeforeEach
  void setUp() {
    bookRepository.deleteAll();
  }

  @Nested
  @DisplayName("Book creation scenarios")
  class CreateBookTests {

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

}
