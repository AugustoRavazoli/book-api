package io.github.augustoravazoli.bookapi.author;

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
class AuthorControllerTest extends ControllerTestTemplate {

  @Autowired
  private AuthorRepository authorRepository;

  @BeforeEach
  void setUp() {
    authorRepository.deleteAll();
  }

  @Nested
  @DisplayName("Author creation scenarios")
  class CreateAuthorTests {
    
    @Test
    @DisplayName("Create author with success")
    void whenCreateAuthor_thenReturns201() throws Exception {
      // given
      var newAuthor = new AuthorRequest("J.R.R. Tolkien", "tolkien@example.com");
      // when
      client.perform(post("/api/v1/authors")
        .contentType(APPLICATION_JSON)
        .content(toJson(newAuthor))
      )
      // then
      .andExpectAll(
        status().isCreated(),
        redirectedUrlPattern("**/api/v1/authors/*"),
        jsonPath("$.id", notNullValue(Long.class)),
        jsonPath("$.name", is("J.R.R. Tolkien")),
        jsonPath("$.email", is("tolkien@example.com"))
      )
      .andDo(document("author/create", snippet()));
      assertThat(authorRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Don't create author when email is already in use")
    void givenEmailTaken_whenCreateAuthor_thenReturns409() throws Exception {
      // given
      authorRepository.save(new Author(null, "J.R.R. Tolkien", "tolkien@example.com"));
      var newAuthor = new AuthorRequest("C.S. Lewis", "tolkien@example.com");
      // when
      client.perform(post("/api/v1/authors")
        .contentType(APPLICATION_JSON)
        .content(toJson(newAuthor))
      )
      // then
      .andExpectAll(
        status().isConflict(),
        jsonPath("$.message", is("Email \"tolkien@example.com\" already in use")),
        jsonPath("$.details").doesNotExist()
      );
      assertThat(authorRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Don't create author when author information are invalid")
    void givenInvalidAuthor_whenCreateAuthor_thenReturns422() throws Exception {
      // given
      var newAuthor = new AuthorRequest("", "\n"); // using "\n" to trigger both @Email and @NotBlank validations
      // when                                      // avoiding the need to parameterize this test
      client.perform(post("/api/v1/authors")
        .contentType(APPLICATION_JSON)
        .content(toJson(newAuthor))
      )
      // then
      .andExpectAll(
        status().isUnprocessableEntity(),
        jsonPath("$.message", is("Validation errors on your request")),
        jsonPath("$.details", hasSize(3)),
        jsonPath("$.details", containsInAnyOrder(
          violation("name", "must not be blank"),
          violation("email", "must not be blank"),
          violation("email", "must be a well-formed email address")
        ))
      );
      assertThat(authorRepository.count()).isZero();
    }

    private RequestFieldsSnippet snippet() {
      var fields = fields(AuthorRequest.class);
      return requestFields(
        fields.path("name").description("Author's name"),
        fields.path("email").description("Author's email")
      );
    }

  }

}
