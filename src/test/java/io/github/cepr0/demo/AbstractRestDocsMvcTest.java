package io.github.cepr0.demo;

import capital.scalable.restdocs.AutoDocumentation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.cli.CliDocumentation;
import org.springframework.restdocs.http.HttpDocumentation;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static capital.scalable.restdocs.jackson.JacksonResultHandlers.prepareJackson;
import static capital.scalable.restdocs.response.ResponseModifyingPreprocessors.limitJsonArrayLength;
import static capital.scalable.restdocs.response.ResponseModifyingPreprocessors.replaceBinaryContent;

@RunWith(SpringRunner.class)
@WebMvcTest
public abstract class AbstractRestDocsMvcTest {
	
	protected static final String DOC_PATH = "{class-name}/{method-name}";
	
	@Autowired private WebApplicationContext context;
	@Autowired private ObjectMapper objectMapper;
	
	@Rule public final JUnitRestDocumentation restDoc = new JUnitRestDocumentation();
	
	protected MockMvc mvc;
	
	@Before
	public void setUpMockMvc() {
		mvc = MockMvcBuilders
				.webAppContextSetup(context)
				.alwaysDo(prepareJackson(objectMapper))
				.alwaysDo(commonDoc())
				.apply(MockMvcRestDocumentation.documentationConfiguration(restDoc)
						.uris()
						.withScheme("https")
						.withHost("example.com")
						.withPort(443)
						.and().snippets()
						.withDefaults(
								PayloadDocumentation.requestBody(),
								PayloadDocumentation.responseBody(),
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
	}
	
	protected RestDocumentationResultHandler commonDoc() {
		return MockMvcRestDocumentation.document(DOC_PATH, commonRequestPreprocessor(), commonResponsePreprocessor()
		);
	}
	
	protected OperationRequestPreprocessor commonRequestPreprocessor() {
		return Preprocessors.preprocessRequest(Preprocessors.prettyPrint());
	}

	protected OperationResponsePreprocessor commonResponsePreprocessor() {
		return Preprocessors.preprocessResponse(Preprocessors.prettyPrint(), limitJsonArrayLength(objectMapper), replaceBinaryContent());
	}
}
