package com.p99training.BookStoreSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching  // activates Spring's cache abstraction — @Cacheable / @CacheEvict annotations now work
public class BookStoreSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookStoreSystemApplication.class, args);
	}

}
