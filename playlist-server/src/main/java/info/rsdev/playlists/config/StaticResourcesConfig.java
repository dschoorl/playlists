package info.rsdev.playlists.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class StaticResourcesConfig {
    @Bean
    RouterFunction<ServerResponse> spaRouter() {
        ClassPathResource index = new ClassPathResource("/static/index.html");
        List<String> extensions = Arrays.asList("js", "css", "ico", "png", "jpg", "gif", "svg", "woff2");
        RequestPredicate spaPredicate = RequestPredicates.path("/api/**").or(RequestPredicates.path("/error"))
                .or(RequestPredicates.pathExtension(extensions::contains)).negate();
        return RouterFunctions.route().resource(spaPredicate, index).build();
    }
}