package io.github.cepr0.demo;

import capital.scalable.restdocs.AutoDocumentation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.hypermedia.HypermediaDocumentation;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.templates.TemplateFormats;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;
import java.util.stream.IntStream;

import static capital.scalable.restdocs.response.ResponseModifyingPreprocessors.limitJsonArrayLength;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "example.com", uriPort = 443)
public class UserControllerAutoDocsTest {

	private static final String DOCS_PATH = "{class-name}/{method-name}";
	
	private static final HeaderDescriptor CONTENT_TYPE_HEADER = headerWithName("Content-Type").description(APPLICATION_JSON_UTF8_VALUE);
	private static final HeaderDescriptor RESPONSE_CONTENT_TYPE_HEADER = headerWithName("Content-Type").description(HAL_JSON_UTF8_VALUE);
	private static final ParameterDescriptor USER_ID_PARAMETER = parameterWithName("id").description("The user's ID");
	private static final FieldDescriptor USER_NAME_FIELD = PayloadDocumentation.fieldWithPath("name").description("The user's name");
	private static final FieldDescriptor USER_AGE_FIELD = PayloadDocumentation.fieldWithPath("age").description("The user's age");
	private static final FieldDescriptor LINKS_FIELD = PayloadDocumentation.subsectionWithPath("_links").description("The user's related links");
	private static final LinkDescriptor SELF_LINK = HypermediaDocumentation.linkWithRel("self").description("Self link to the created user");
	
	private RestDocumentationResultHandler userResourceResponse;
	
	@Autowired private MockMvc mvc;
	@Autowired private UserRepo userRepo;
	@Autowired private RestDocumentationResultHandler docHandler;
	
	@Before
	public void setUp() {
		// Populate test data
		userRepo.deleteAll();
		IntStream.range(0, 3).mapToObj(i -> new User("User" + i, i + 10)).forEach(userRepo::create);
		
		userResourceResponse = docHandler.document(AutoDocumentation.responseFields().responseBodyAsType(UserResource.class));
	}
	
	@Test
	public void shouldReturnAllUsers() throws Exception {
		ResultActions result = mvc.perform(get("/users"))
				.andExpect(content().contentType(HAL_JSON_UTF8_VALUE))
				.andExpect(status().isOk());

		result.andDo(docHandler.document(
				PayloadDocumentation.responseFields(
						PayloadDocumentation.subsectionWithPath("[]")
							.description("A list of all <<user-controller-test-should-return-one-user-ok-http-response, users>>")
				),
				responseHeaders(RESPONSE_CONTENT_TYPE_HEADER)
		));
	}
	
	@Test
	public void shouldReturnOneUser() throws Exception {
		ResultActions result = mvc.perform(get("/users/{id}", userRepo.getAll().get(0).getId()))
				.andExpect(content().contentType(HAL_JSON_UTF8_VALUE))
				.andExpect(status().isOk());
		
		result.andDo(userResourceResponse);
	}
	
	@Test
	public void shouldReturnNotFound() throws Exception {
		ResultActions result = mvc.perform(get("/users/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void shouldCreateUser() throws Exception {
		
		ResultActions result = mvc.perform(post("/users")
				.content("{\"name\": \"user\",\"age\": 18}")
				.contentType(APPLICATION_JSON_UTF8_VALUE))
				.andExpect(content().contentType(HAL_JSON_UTF8_VALUE))
				.andExpect(status().isCreated());
		
		result.andDo(userResourceResponse);
	}
	
	@Test
	public void shouldUpdateUser() throws Exception {
		ResultActions result = mvc.perform(patch("/users/{id}", userRepo.getAll().get(0).getId())
				.content("{\"name\": \"user\",\"age\": 20}")
				.contentType(APPLICATION_JSON_UTF8_VALUE))
				.andExpect(content().contentType(HAL_JSON_UTF8_VALUE))
				.andExpect(status().isOk());
		
		result.andDo(userResourceResponse);
	}
	
	@Test
	public void shouldReturnNotFoundWhileUpdatingUser() throws Exception {
		ResultActions result = mvc.perform(patch("/users/{id}", UUID.randomUUID())
				.content("{\"name\": \"user\",\"age\": 20}")
				.contentType(APPLICATION_JSON_UTF8_VALUE))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void shouldDeleteUser() throws Exception {
		ResultActions result = mvc.perform(delete("/users/{id}", userRepo.getAll().get(0).getId()))
				.andExpect(status().isNoContent());
	}
	
	@Test
	public void shouldReturnNotFoundWhileDeletingUser() throws Exception {
		ResultActions result = mvc.perform(delete("/users/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound());
	}
	
	@TestConfiguration
	static class CustomizationConfiguration implements RestDocsMockMvcConfigurationCustomizer {
		
		@Autowired private ObjectMapper objectMapper;
		
		@Override
		public void customize(MockMvcRestDocumentationConfigurer configurer) {
			configurer.operationPreprocessors()
					.withRequestDefaults(prettyPrint())
					.withResponseDefaults(prettyPrint(), limitJsonArrayLength(objectMapper))
					.and()
					.snippets()
					.withDefaults(
							CliDocumentation.curlRequest(),
							HttpDocumentation.httpRequest(),
							HttpDocumentation.httpResponse(),
							AutoDocumentation.requestFields(),
							AutoDocumentation.responseFields(),
							AutoDocumentation.pathParameters(),
							AutoDocumentation.requestParameters(),
							AutoDocumentation.description(),
							AutoDocumentation.methodAndPath(),
							AutoDocumentation.section()
					)
					.withTemplateFormat(TemplateFormats.asciidoctor());
		}
		
		@Bean
		public RestDocumentationResultHandler restDocumentation() {
			return MockMvcRestDocumentation.document(DOCS_PATH);
		}
		
		@Bean
		public UserRepo userRepo() {
			return new UserRepo();
		}
	}
}