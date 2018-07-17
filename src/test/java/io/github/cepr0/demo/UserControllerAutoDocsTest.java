package io.github.cepr0.demo;

import capital.scalable.restdocs.AutoDocumentation;
import capital.scalable.restdocs.payload.JacksonResponseFieldSnippet;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.hypermedia.HypermediaDocumentation;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;
import java.util.stream.IntStream;

import static org.springframework.hateoas.MediaTypes.HAL_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserControllerAutoDocsTest extends AbstractRestDocsMvcTest {
	
	private static final ConstrainedFields USER_CONSTRAINED_FIELDS = new ConstrainedFields(UserDto.class);
	
	private static final HeaderDescriptor CONTENT_TYPE_HEADER = headerWithName("Content-Type").description(APPLICATION_JSON_UTF8_VALUE);
	private static final HeaderDescriptor RESPONSE_CONTENT_TYPE_HEADER = headerWithName("Content-Type").description(HAL_JSON_UTF8_VALUE);

	private static final ParameterDescriptor USER_ID_PARAMETER = parameterWithName("id").attributes(key("type").value("UUID")).description("The user's ID");
	private static final FieldDescriptor USER_NAME_FIELD = USER_CONSTRAINED_FIELDS.name("name").description("The user's name");
	private static final FieldDescriptor USER_AGE_FIELD = USER_CONSTRAINED_FIELDS.name("age").description("The user's age");
	private static final FieldDescriptor LINKS_FIELD = PayloadDocumentation.subsectionWithPath("_links").description("The user's related links");

	private static final LinkDescriptor SELF_LINK = HypermediaDocumentation.linkWithRel("self").description("Self link to the created user");
	
	@Autowired private UserRepo userRepo;
	
	private static final JacksonResponseFieldSnippet USER_RESOURCE_RESPONSE = AutoDocumentation.responseFields().responseBodyAsType(UserResource.class);
	
	@Before
	public void setUp() {
		// Populate test data
		userRepo.deleteAll();
		IntStream.range(0, 3).mapToObj(i -> new User("User" + i, i + 10)).forEach(userRepo::create);
	}
	
	@Test
	public void shouldReturnAllUsers() throws Exception {
		ResultActions result = mvc.perform(get("/users"))
				.andExpect(content().contentType(HAL_JSON_UTF8_VALUE))
				.andExpect(status().isOk());
		
		result.andDo(commonDoc().document(
				USER_RESOURCE_RESPONSE,
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
		
		result.andDo(commonDoc().document(
				pathParameters(USER_ID_PARAMETER),
				USER_RESOURCE_RESPONSE,
				responseHeaders(RESPONSE_CONTENT_TYPE_HEADER)
		));
	}
	
	@Test
	public void shouldReturnNotFound() throws Exception {
		ResultActions result = mvc.perform(get("/users/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound());
		
		result.andDo(commonDoc().document(pathParameters(USER_ID_PARAMETER)));
	}
	
	@Test
	public void shouldCreateUser() throws Exception {
		
		ResultActions result = mvc.perform(post("/users")
				.content("{\"name\": \"user\",\"age\": 18}")
				.contentType(APPLICATION_JSON_UTF8_VALUE))
				.andExpect(content().contentType(HAL_JSON_UTF8_VALUE))
				.andExpect(status().isCreated());
		
		result.andDo(commonDoc().document(
				requestHeaders(CONTENT_TYPE_HEADER),
				USER_RESOURCE_RESPONSE,
				responseHeaders(
						headerWithName("Location").description("Link to the created user"),
						RESPONSE_CONTENT_TYPE_HEADER
				)
		));
	}
	
	@Test
	public void shouldUpdateUser() throws Exception {
		ResultActions result = mvc.perform(patch("/users/{id}", userRepo.getAll().get(0).getId())
				.content("{\"name\": \"user\",\"age\": 20}")
				.contentType(APPLICATION_JSON_UTF8_VALUE))
				.andExpect(content().contentType(HAL_JSON_UTF8_VALUE))
				.andExpect(status().isOk());
		
		result.andDo(commonDoc().document(
				requestHeaders(CONTENT_TYPE_HEADER),
				pathParameters(USER_ID_PARAMETER),
				USER_RESOURCE_RESPONSE,
				responseHeaders(RESPONSE_CONTENT_TYPE_HEADER)
		));
	}
	
	@Test
	public void shouldReturnNotFoundWhileUpdatingUser() throws Exception {
		ResultActions result = mvc.perform(patch("/users/{id}", UUID.randomUUID())
				.content("{\"name\": \"user\",\"age\": 20}")
				.contentType(APPLICATION_JSON_UTF8_VALUE))
				.andExpect(status().isNotFound());
		
		result.andDo(commonDoc().document(pathParameters(USER_ID_PARAMETER)));
	}
	
	@Test
	public void shouldDeleteUser() throws Exception {
		ResultActions result = mvc.perform(delete("/users/{id}", userRepo.getAll().get(0).getId()))
				.andExpect(status().isNoContent());
		
		result.andDo(commonDoc().document(pathParameters(USER_ID_PARAMETER)));
	}
	
	@Test
	public void shouldReturnNotFoundWhileDeletingUser() throws Exception {
		ResultActions result = mvc.perform(delete("/users/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound());
		
		result.andDo(commonDoc().document(pathParameters(USER_ID_PARAMETER)));
	}
	
	@TestConfiguration
	static class CustomizationConfiguration {
		
		@Bean
		public UserRepo userRepo() {
			return new UserRepo();
		}
	}
}