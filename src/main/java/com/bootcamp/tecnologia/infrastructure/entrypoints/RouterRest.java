package com.bootcamp.tecnologia.infrastructure.entrypoints;

import com.bootcamp.tecnologia.infrastructure.entrypoints.handler.UserHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction() {
        return route(POST("/user"), userHandler::createUser);
    }
}
