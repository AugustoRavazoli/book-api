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
import io.github.augustoravazoli.bookapi.book.Book;
import io.github.augustoravazoli.bookapi.book.BookRepository;

@SpringBootTest
class AuthorEndpointsTest extends EndpointsTestTemplate {

  @Autowired
  private AuthorRepository authorRepository;

  @Autowired
  private BookRepository bookRepository;

  @BeforeEach
  void setUp() {
    authorRepository.deleteAll();
    bookRepository.deleteAll();
  }

  @Nested
  @DisplayName("Author creation scenarios")
  class CreateAuthorEndpointTests {
    
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
      // and
      assertThat(authorRepository.findAll()).size().isEqualTo(1)
        .returnToIterable()
        .extracting("name", "email")
        .contains(tuple("J.R.R. Tolkien", "tolkien@example.com"));
    }

    @Test
    @DisplayName("Don't create author when email is already in use")
    void givenEmailTaken_whenCreateAuthor_thenReturns409() throws Exception {
      // given
      authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
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

  @Nested
  @DisplayName("Author find scenarios")
  class FindAuthorEndpointTests {

    @Test
    @DisplayName("Find author with success")
    void whenFindAuthor_thenReturns200() throws Exception {
      // given
      var author = authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
      // when
      client.perform(get("/api/v1/authors/{id}", author.getId()))
      // then
      .andExpectAll(
        status().isOk(),
        jsonPath("$.id", notNullValue(Long.class)),
        jsonPath("$.name", is("J.R.R. Tolkien")),
        jsonPath("$.email", is("tolkien@example.com"))
      )
      .andDo(document("author/find"));    
    }

    @Test
    @DisplayName("Don't find author when author doesn't exists")
    void givenNonexistentAuthor_whenFindAuthor_thenReturns404() throws Exception {
      // when
      client.perform(get("/api/v1/authors/1"))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Author with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

  }

  @Nested
  @DisplayName("Author edit scenarios")
  class EditAuthorEndpointTests {

    @Test
    @DisplayName("Edit author with success")
    void whenEditAuthor_thenReturns200() throws Exception {
      // given
      var author = authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
      var newAuthor = new AuthorRequest("C.S. Lewis", "cslewis@example.com");
      // when
      client.perform(put("/api/v1/authors/{id}", author.getId())
        .contentType(APPLICATION_JSON)
        .content(toJson(newAuthor))
      )
      // then
      .andExpectAll(
        status().isOk(),
        jsonPath("$.id", notNullValue(Long.class)),
        jsonPath("$.name", is("C.S. Lewis")),
        jsonPath("$.email", is("cslewis@example.com"))
      )
      .andDo(document("author/edit"));
      // and
      assertThat(authorRepository.findById(author.getId())).get()
        .extracting("name", "email")
        .contains("C.S. Lewis", "cslewis@example.com");
    }

    @Test
    @DisplayName("Don't edit author when author doesn't exists")
    void givenNonexistentAuthor_whenEditAuthor_thenReturns404() throws Exception {
      // given
      var newAuthor = new AuthorRequest("C.S. Lewis", "cslewis@example.com");
      // when
      client.perform(put("/api/v1/authors/1")
        .contentType(APPLICATION_JSON)
        .content(toJson(newAuthor))
      )
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Author with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

    @Test
    @DisplayName("Don't edit author when email is already in use by other author")
    void givenEmailTaken_whenEditAuthor_thenReturns409() throws Exception {
      // given
      var author = authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
      authorRepository.save(new Author("", "cslewis@example.com"));
      // and
      var newAuthor = new AuthorRequest("C.S. Lewis", "cslewis@example.com");
      // when
      client.perform(put("/api/v1/authors/{id}", author.getId())
        .contentType(APPLICATION_JSON)
        .content(toJson(newAuthor))
      )
      // then
      .andExpectAll(
        status().isConflict(),
        jsonPath("$.message", is("Email \"cslewis@example.com\" already in use")),
        jsonPath("$.details").doesNotExist()
      );
      assertThat(authorRepository.findById(author.getId())).get()
        .extracting("name", "email")
        .contains("J.R.R. Tolkien", "tolkien@example.com");
    }

    @Test
    @DisplayName("Don't edit author when new author information are invalid")
    void givenInvalidAuthor_whenEditAuthor_thenReturns422() throws Exception {
      // given
      var author = authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
      var newAuthor = new AuthorRequest("", "\n"); // using "\n" to trigger both @Email and @NotBlank validations
      // when                                      // avoiding the need to parameterize this test
      client.perform(put("/api/v1/authors/{id}", author.getId())
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
      assertThat(authorRepository.findById(author.getId())).get()
        .extracting("name", "email")
        .contains("J.R.R. Tolkien", "tolkien@example.com");
    }

  }

  @Nested
  @DisplayName("Author delete scenarios")
  class DeleteAuthorEndpointTests {

    @Test
    @DisplayName("Delete author with success")
    void whenDeleteAuthor_thenReturns204() throws Exception {
      // given
      var author = authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
      // when
      client.perform(delete("/api/v1/authors/{id}", author.getId()))
      // then
      .andExpectAll(
        status().isNoContent(),
        jsonPath("$").doesNotExist()
      )
      .andDo(document("author/delete"));
      // and
      assertThat(authorRepository.existsById(author.getId())).isFalse();
    }

    @Test
    @DisplayName("Don't delete author when author doesn't exists")
    void givenNonexistentAuthor_whenDeleteAuthor_thenReturns404() throws Exception {
      // when
      client.perform(delete("/api/v1/authors/1"))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Author with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

  }

  @Nested
  @DisplayName("Add book to author scenarios")
  class AddBookToAuthorEndpointTests {
    
    @Test
    @DisplayName("Add book to author with success")
    void whenAddBookToAuthor_thenReturns204() throws Exception {
      // given
      var author = authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      // when
      client.perform(put("/api/v1/authors/{author-id}/books/{book-id}", author.getId(), book.getId()))
      // then
      .andExpectAll(
        status().isNoContent(),
        jsonPath("$").doesNotExist()
      )
      .andDo(document("author/add-book"));
    }

    @Test
    @DisplayName("Don't add book to author when author doesn't exists")
    void givenNonexistentAuthor_whenAddBookToAuthor_thenReturns404() throws Exception {
      // given
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      // when
      client.perform(put("/api/v1/authors/1/books/{book-id}", book.getId()))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Author with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

    @Test
    @DisplayName("Don't add book to author when book doesn't exists")
    void givenNonexistentBook_whenAddBookToAuthor_thenReturns404() throws Exception {
      // given
      var author = authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
      // when
      client.perform(put("/api/v1/authors/{author-id}/books/1", author.getId()))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Book with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

  }

  @Nested
  @DisplayName("Remove book from author scenarios")
  class RemoveBookFromAuthorEndpointTests {
    
    @Test
    @DisplayName("Remove book from author with success")
    void whenRemoveBookFromAuthor_thenReturns204() throws Exception {
      // given
      var author = authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      // and
      author.addBook(book);
      authorRepository.save(author);
      // when
      client.perform(delete("/api/v1/authors/{author-id}/books/{book-id}", author.getId(), book.getId()))
      // then
      .andExpectAll(
        status().isNoContent(),
        jsonPath("$").doesNotExist()
      )
      .andDo(document("author/remove-book"));
      // and
      assertThat(authorRepository.existsById(author.getId())).isTrue();
      assertThat(bookRepository.existsById(book.getId())).isTrue();
    }

    @Test
    @DisplayName("Don't remove book from author when author doesn't exists")
    void givenNonexistentAuthor_whenRemoveBookFromAuthor_thenReturns404() throws Exception {
      // given
      var book = bookRepository.save(new Book("The Lord of the Rings", "Fantasy", "9780544003415", true));
      // when
      client.perform(delete("/api/v1/authors/1/books/{book-id}", book.getId()))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Author with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

    @Test
    @DisplayName("Don't remove book from author when book doesn't exists")
    void givenNonexistentBook_whenRemoveBookFromAuthor_thenReturns404() throws Exception {
      // given
      var author = authorRepository.save(new Author("J.R.R. Tolkien", "tolkien@example.com"));
      // when
      client.perform(delete("/api/v1/authors/{author-id}/books/1", author.getId()))
      // then
      .andExpectAll(
        status().isNotFound(),
        jsonPath("$.message", is("Book with given id \"1\" doesn't exists")),
        jsonPath("$.details").doesNotExist()
      );
    }

  }

}
