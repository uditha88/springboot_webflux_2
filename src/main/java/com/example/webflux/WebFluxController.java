package com.example.webflux;

import com.example.webflux.domain.User;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class WebFluxController implements ErrorController {

    private static final String PATH = "/error";

    @GetMapping("/helloFlux")
    public String helloWorld() throws InterruptedException {
        System.out.println("####### Operation starts " + LocalDateTime.now());
        getUsersFlux().subscribe(
                e -> System.out.println(e.getName())
        );
        // no delay between 2 sysouts
//        getUsersClassic().forEach(
//                t -> System.out.println(t.getName())
//        );
        // there's a 12 sex delay between 2 sysouts
        // Important: So this confirms that flux make it reactive async programming
        System.out.println("####### Operation ends " + LocalDateTime.now());
        return "Hello World new 2";
    }

    @GetMapping("/getUsersFlux")
    public Flux<User> getUsersFlux() throws InterruptedException {

            Flux f = Mono.delay(Duration.ofSeconds(5))
                            .thenMany(Flux.just(
                                            new User("uditha", "uditha@uditha.com"),
                                            new User("madumal", "madumal@madumal.com"),
                                            new User("perera", "perera@perera.com")));
            return f;
    }

    @GetMapping("/getUsersClassic")
    public List<User> getUsersClassic() throws InterruptedException {


        List l = Arrays.asList(
                new User("uditha", "uditha@uditha.com"),
                new User("madumal", "madumal@madumal.com"),
                new User("perera", "perera@perera.com")
        );

        TimeUnit.SECONDS.sleep(12);
        // the above blocks the main thead

        return l;
    }
}
