package com.example.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Comprueba que React servido desde http://localhost:3000 puede llamar a la API
 * con el encabezado Authorization (seccion 6 de la guia).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CorsSecurityTests {

    private static final String ORIGIN = "http://localhost:3000";

    @Autowired
    private MockMvc mockMvc;

    /** Preflight de la ruta autenticada: Spring Security debe permitirlo. */
    @Test
    void preflightDeRutaAutenticadaPermiteOriginYAuthorization() throws Exception {
        mockMvc.perform(options("/api/usuarios/me")
                        .header("Origin", ORIGIN)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Headers",
                        org.hamcrest.Matchers.containsStringIgnoringCase("authorization")));
    }

    /** Peticion simple con Authorization debe incluir el encabezado de CORS. */
    @Test
    void peticionConAuthorizationDevuelveEncabezadosCors() throws Exception {
        mockMvc.perform(get("/api/usuarios/me")
                        .header("Origin", ORIGIN)
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }

    /** El login (POST) tambien debe funcionar desde React. */
    @Test
    void preflightDeLoginPermiteOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", ORIGIN)
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", ORIGIN));
    }
}
