package io.github.cepr0.demo;

import lombok.Data;

import java.util.UUID;

@Data
public class User {
	private UUID id;
	private String name;
	private Integer age;
	
	public User(String name, Integer age) {
		this.name = name;
		this.age = age;
	}
}
