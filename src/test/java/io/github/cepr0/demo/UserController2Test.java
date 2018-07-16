package io.github.cepr0.demo;

import capital.scalable.restdocs.AutoDocumentation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.stream.IntStream;

import static capital.scalable.restdocs.jackson.JacksonResultHandlers.prepareJackson;
import static capital.scalable.restdocs.response.ResponseModifyingPreprocessors.limitJsonArrayLength;
import static capital.scalable.restdocs.response.ResponseModifyingPreprocessors.replaceBinaryContent;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserController2Test {
	
	@Autowired private WebApplicationContext context;
	@Autowired private ObjectMapper objectMapper;
	@Autowired private UserRepo userRepo;
	
	@Rule public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();
	
	private MockMvc mockMvc;
	
	@Before
	public void setUp() {
		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(context)
				.alwaysDo(prepareJackson(objectMapper))
				.alwaysDo(commonDocumentation())
				.apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation)
						.uris()
						.withScheme("http")
						.withHost("localhost")
						.withPort(8080)
						.and().snippets()
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
						))
				.build();
		
		userRepo.deleteAll();
		IntStream.range(0, 3).mapToObj(i -> new User("User" + i, i + 10)).forEach(userRepo::create);
	}
	
	private RestDocumentationResultHandler commonDocumentation() {
		return MockMvcRestDocumentation.document("{class-name}/{method-name}", Preprocessors.preprocessRequest(), commonResponsePreprocessor());
	}
	
	private OperationResponsePreprocessor commonResponsePreprocessor() {
		return Preprocessors.preprocessResponse(replaceBinaryContent(), limitJsonArrayLength(objectMapper), Preprocessors.prettyPrint());
	}
	
	@Test
	public void getUser() throws Exception {
		mockMvc.perform(get("/users/{id}", userRepo.getAll().get(0).getId()))
				.andExpect(status().isOk())
				.andDo(commonDocumentation().document(AutoDocumentation.responseFields().responseBodyAsType(UserResource.class)));
	}

	@TestConfiguration
	static class CustomizationConfiguration {

		@Bean
		public UserRepo userRepo() {
			return new UserRepo();
		}
	}
}
