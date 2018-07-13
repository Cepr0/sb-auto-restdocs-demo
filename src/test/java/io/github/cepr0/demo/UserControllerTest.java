package io.github.cepr0.demo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.headers.HeaderDescriptor;
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

import static org.springframework.hateoas.MediaTypes.HAL_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.halLinks;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "example.com", uriPort = 443)
public class UserControllerTest {

	private static final String DOCS_PATH = "{class-name}/{method-name}";
	
	private static final HeaderDescriptor CONTENT_TYPE_HEADER = headerWithName("Content-Type").description(APPLICATION_JSON_UTF8_VALUE);
	private static final ParameterDescriptor USER_ID_PARAMETER = parameterWithName("id").description("Id of the user being updated");
	private static final FieldDescriptor USER_NAME_FIELD = fieldWithPath("name").description("The user's name");
	private static final FieldDescriptor USER_AGE_FIELD = fieldWithPath("age").description("The user's age");
	private static final FieldDescriptor LINKS_FIELD = PayloadDocumentation.subsectionWithPath("_links").description("The user's related links");
	private static final LinkDescriptor SELF_LINK = HypermediaDocumentation.linkWithRel("self").description("Self link to the created user");
	
	@Autowired private MockMvc mvc;
	@Autowired private UserRepo userRepo;
	
	@Before
	public void setUp() {
		// Populate test data
		userRepo.deleteAll();
		IntStream.range(0, 3).mapToObj(i -> new User("User" + i, i + 10)).forEach(userRepo::create);
	}
	
	@Test
	public void should_return_all_users() throws Exception {
		mvc.perform(get("/users"))
				.andExpect(status().isOk());
	}
	
	@Test
	public void should_return_one_user() throws Exception {
		mvc.perform(get("/users/{id}", userRepo.getAll().get(0).getId()))
				.andExpect(status().isOk());
	}
	
	@Test
	public void should_return_not_found() throws Exception {
		mvc.perform(get("/users/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound());
	}
	
	@Test
	public void should_create_user() throws Exception {
		
		ResultActions result = mvc.perform(post("/users")
				.content("{\"name\": \"user\",\"age\": 18}")
				.contentType(APPLICATION_JSON_UTF8_VALUE))
				.andExpect(content().contentType(HAL_JSON_UTF8_VALUE))
				.andExpect(status().isCreated());
		
		result.andDo(document(DOCS_PATH,
				requestHeaders(CONTENT_TYPE_HEADER),
				requestFields(USER_NAME_FIELD, USER_AGE_FIELD),
				responseFields(USER_NAME_FIELD, USER_AGE_FIELD, LINKS_FIELD),
				links(halLinks(), SELF_LINK),
				responseHeaders(headerWithName("Location").description("Link to the created user"))
		));
	}
	
	@Test
	public void should_update_user() throws Exception {
		ResultActions result = mvc.perform(patch("/users/{id}", userRepo.getAll().get(0).getId())
				.content("{\"name\": \"user_\",\"age\": 20}")
				.contentType(APPLICATION_JSON_UTF8_VALUE))
				.andExpect(content().contentType(HAL_JSON_UTF8_VALUE))
				.andExpect(status().isOk());
		
		result.andDo(document(DOCS_PATH,
				requestHeaders(CONTENT_TYPE_HEADER),
				pathParameters(USER_ID_PARAMETER),
				requestFields(USER_NAME_FIELD, USER_AGE_FIELD),
				responseFields(USER_NAME_FIELD, USER_AGE_FIELD, LINKS_FIELD),
				links(halLinks(), SELF_LINK)
		));
	}
	
	@Test
	public void should_return_not_found_while_updating_user() throws Exception {
		ResultActions result = mvc.perform(patch("/users/{id}", UUID.randomUUID())
				.content("{\"name\": \"user_\",\"age\": 20}")
				.contentType(APPLICATION_JSON_UTF8_VALUE))
				.andExpect(status().isNotFound());
		
		result.andDo(document(DOCS_PATH,
				requestHeaders(CONTENT_TYPE_HEADER),
				pathParameters(USER_ID_PARAMETER),
				requestFields(USER_NAME_FIELD, USER_AGE_FIELD)
		));
	}
	
	@Test
	public void should_delete_user() throws Exception {
		ResultActions result = mvc.perform(delete("/users/{id}", userRepo.getAll().get(0).getId()))
				.andExpect(status().isNoContent());
		
		result.andDo(document(DOCS_PATH, pathParameters(USER_ID_PARAMETER)));
	}
	
	@Test
	public void should_return_not_found_while_deleting_user() throws Exception {
		ResultActions result = mvc.perform(delete("/users/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound());
		
		result.andDo(document(DOCS_PATH, pathParameters(USER_ID_PARAMETER)));
	}
	
	@TestConfiguration
	static class CustomizationConfiguration implements RestDocsMockMvcConfigurationCustomizer {
		
		@Override
		public void customize(MockMvcRestDocumentationConfigurer configurer) {
			configurer.operationPreprocessors()
					.withResponseDefaults(prettyPrint())
					.withRequestDefaults(prettyPrint())
					.and()
					.snippets()
//					.withDefaults(
//							CliDocumentation.curlRequest(),
//							CliDocumentation.httpieRequest(),
//							HttpDocumentation.httpRequest(),
//							HttpDocumentation.httpResponse()
//					)
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