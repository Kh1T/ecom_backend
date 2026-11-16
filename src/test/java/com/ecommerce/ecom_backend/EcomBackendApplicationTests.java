package com.ecommerce.ecom_backend;

import com.ecommerce.ecom_backend.api.ApiVersion;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class EcomBackendApplicationTests {

	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;

	@Autowired
	private GroupedOpenApi v1Api;

	@Test
	void contextLoads() {
	}

	@Test
	void apiRoutesAreVersionedAndDocumentedAsV1() {
		boolean versionedAuthRouteFound = false;

		for (var entry : requestMappingHandlerMapping.getHandlerMethods().entrySet()) {
			if (!entry.getValue().getBeanType().getPackageName()
					.startsWith("com.ecommerce.ecom_backend.controller")) {
				continue;
			}

			for (String path : entry.getKey().getPatternValues()) {
				assertTrue(path.startsWith(ApiVersion.V1_PREFIX + "/"), path);
				versionedAuthRouteFound |= path.equals(ApiVersion.V1_AUTH + "/sign-in");
			}
		}

		assertTrue(versionedAuthRouteFound);
		assertEquals("v1", v1Api.getGroup());
	}

}
