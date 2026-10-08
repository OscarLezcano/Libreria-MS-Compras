package com.bigobooks.purchases_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Carga del contexto completo. Requiere que exista
 * src/main/resources/config/private.properties con una base de datos accesible;
 * se ejecuta solo si se define la propiedad de sistema
 * purchases.test.full=true.
 */
@SpringBootTest(classes = PurchasesServiceApplication.class)
@EnabledIfSystemProperty(named = "purchases.test.full", matches = "true")
class PurchasesServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}