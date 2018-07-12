package io.github.cepr0.demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentationConfigurer;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.templates.TemplateFormats;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "example.com", uriPort = 443)
@Import(UserRepo.class)
public class UserControllerTest {
	
	@Autowired private MockMvc mvc;
	@Autowired private UserRepo userRepo;
	
	@Test
	public void should_return_all_users() throws Exception {
		mvc.perform(get("/users"))
				.andDo(print())
				.andExpect(status().isOk());
	}
	
	@Test
	public void should_return_one_user() throws Exception {
		mvc.perform(get("/users/{id}", userRepo.getAll().get(0).getId()))
				.andDo(print())
				.andExpect(status().isOk());
	}
	
	@Test
	public void should_return_not_found() throws Exception {
		mvc.perform(get("/users/{id}", UUID.randomUUID()))
				.andDo(print())
				.andExpect(status().isNotFound());
	}
	
	@TestConfiguration
	static class CustomizationConfiguration implements RestDocsMockMvcConfigurationCustomizer {
		
		@Override
		public void customize(MockMvcRestDocumentationConfigurer configurer) {
			configurer.operationPreprocessors()
					.withResponseDefaults(prettyPrint())
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
			return MockMvcRestDocumentation.document("{class-name}/{method-name}");
		}
	}
}