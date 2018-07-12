package io.github.cepr0.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@ExposesResourceFor(value = UserResource.class)
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
	
	private final UserRepo userRepo;
	
	@GetMapping
	public ResponseEntity getAll() {
		return ResponseEntity.ok(userRepo.getAll().stream().map(UserResource::new).collect(toList()));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity get(@PathVariable("id") UUID id) {
		return userRepo.get(id)
				.map(UserResource::new)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	@PostMapping
	public ResponseEntity create(@RequestBody UserDto userDto) {
		User user = userRepo.create(userDto.toUser());
		return ResponseEntity.created(URI.create(linkTo(methodOn(UserController.class).get(user.getId())).withSelfRel().getHref()))
				.body(new UserResource(user));
	}
	
	@PatchMapping("/{id}")
	public ResponseEntity update(@PathVariable("id") UUID id, @RequestBody UserDto userDto) {
		return userRepo.update(id, userDto.toUser())
				.map(UserResource::new)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity delete(@PathVariable("id") UUID id) {
		if(userRepo.delete(id) == 1) return ResponseEntity.noContent().build();
		else return ResponseEntity.notFound().build();
	}
}
