package io.github.cepr0.demo;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.core.Relation;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@Value
@EqualsAndHashCode(callSuper = false)
@Relation(value = "user", collectionRelation = "users")
public class UserResource extends ResourceSupport {
	private String name;
	private Integer age;
	
	public UserResource(User user) {
		this.name = user.getName();
		this.age = user.getAge();
		add(linkTo(methodOn(UserController.class).get(user.getId())).withSelfRel());
	}
}
