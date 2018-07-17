package io.github.cepr0.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_UTF8_VALUE;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@ExposesResourceFor(value = UserResource.class)
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
	
	private final UserRepo userRepo;
	
	/**
	 * Returns list of all users.
	 *
	 * @title Get all users
	 */
	@GetMapping(produces = HAL_JSON_UTF8_VALUE)
	public ResponseEntity getAll() {
		return ResponseEntity.ok(userRepo.getAll().stream().map(UserResource::new).collect(toList()));
	}
	
	/**
	 * Returns the specified user's data.
	 *
	 * @param id User's ID
	 * @title Get one user
	 */
	@GetMapping("/{id}")
	public ResponseEntity get(@PathVariable("id") UUID id) {
		return userRepo.get(id)
				.map(UserResource::new)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	/**
	 * Creates a new user.
	 *
	 * @title Create user
	 * @param userDto a new user's data
	 */
	@PostMapping
	public ResponseEntity create(@RequestBody UserDto userDto) {
		User user = userRepo.create(userDto.toUser());
		return ResponseEntity.created(URI.create(linkTo(methodOn(UserController.class).get(user.getId())).withSelfRel().getHref()))
				.body(new UserResource(user));
	}
	
	/**
	 * Update existed user by its ID.
	 *
	 * @param id User's ID
	 * @param userDto User's new data
	 * @title Update user
	 */
	@PatchMapping("/{id}")
	public ResponseEntity update(@PathVariable("id") UUID id, @RequestBody UserDto userDto) {
		return userRepo.update(id, userDto.toUser())
				.map(UserResource::new)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	/**
	 * Delete existed user by its ID.
	 *
	 * @param id User's ID
	 * @title Delete user
	 */
	@DeleteMapping("/{id}")
	public ResponseEntity delete(@PathVariable("id") UUID id) {
		if(userRepo.delete(id) == 1) return ResponseEntity.noContent().build();
		else return ResponseEntity.notFound().build();
	}
}
