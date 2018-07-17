package io.github.cepr0.demo;

import lombok.Value;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class UserDto {
	/**
	 * User's name
	 */
	@NotBlank private String name;
	
	/**
	 * User's age
	 */
	@NotNull @Min(1) @Max(150) private Integer age;
	
	public User toUser() {
		return new User(name, age);
	}
}
