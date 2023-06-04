package io.github.augustoravazoli.bookapi;

import java.util.Map;
import static java.util.stream.Collectors.joining;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.modifyUris;
import org.springframework.restdocs.constraints.ConstraintDescriptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(RestDocumentationExtension.class)
public abstract class EndpointsTestTemplate {

  protected MockMvc client;

  @Autowired
  private ObjectMapper mapper;  

  @BeforeEach
  void setUp(WebApplicationContext context, RestDocumentationContextProvider provider) {
    client = MockMvcBuilders.webAppContextSetup(context)
      .apply(documentationConfiguration(provider)
        .operationPreprocessors()
        .withRequestDefaults(prettyPrint(), modifyUris().host("example.com").removePort())
        .withResponseDefaults(prettyPrint())
      )
      .build();
  }

  protected <T> String toJson(T object) throws JsonProcessingException {
    return mapper.writeValueAsString(object);
  } 

  protected Map<String, String> violation(String field, String message) {
    return Map.of("field", field, "message", message);
  }

  protected ConstrainedFields fields(Class<?> clazz) {
    return new ConstrainedFields(clazz);
  }

  protected static class ConstrainedFields {

    private final ConstraintDescriptions constraints;

    ConstrainedFields(Class<?> clazz) {
      constraints = new ConstraintDescriptions(clazz);
    }

    public FieldDescriptor path(String path) {
      var description = constraints.descriptionsForProperty(path)
        .stream()
        .filter(s -> !s.isEmpty())
        .collect(joining(". "));
      return fieldWithPath(path).attributes(key("constraints").value(description));
    }

  }

}
