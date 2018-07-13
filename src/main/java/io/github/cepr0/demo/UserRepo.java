package io.github.cepr0.demo;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepo {
	
	private static final Map<UUID, User> STORAGE = new ConcurrentHashMap<>();
	
	public User create(User user) {
		user.setId(UUID.randomUUID());
		STORAGE.put(user.getId(), user);
		return user;
	}
	
	public Optional<User> update(UUID id, User user) {
		User storedUser = STORAGE.get(id);
		if (storedUser != null) {
			
			String name = user.getName();
			if (name != null) storedUser.setName(name);
			
			Integer age = user.getAge();
			if (age != null) storedUser.setAge(age);
			
			STORAGE.put(id, storedUser);
			return Optional.of(storedUser);
		} else {
			return Optional.empty();
		}
	}
	
	public int delete(UUID id) {
		if (STORAGE.remove(id) != null) return 1;
		else return 0;
	}
	
	public void deleteAll() {
		STORAGE.clear();
	}
	
	public Optional<User> get(UUID id) {
		return Optional.ofNullable(STORAGE.get(id));
	}
	
	public List<User> getAll() {
		return new ArrayList<>(STORAGE.values());
	}
}
