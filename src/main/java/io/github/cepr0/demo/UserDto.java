package io.github.cepr0.demo;

import lombok.NonNull;
import lombok.Value;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Value
public class UserDto {
	/**
	 * User's name
	 */
	@NotBlank private String name;
	
	/**
	 * User's age
	 */
	@NonNull @Min(1) @Max(150) private Integer age;
	
	public User toUser() {
		return new User(name, age);
	}
}
