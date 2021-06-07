package net.kamradtfamily.blockingnono;

import io.reactivex.Single;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@RequestMapping("/")
public class Controller {
    RestTemplate restTemplate = new RestTemplate();
    WebClient webClient = WebClient.create();
    @GetMapping("/blocking")
    public Mono<String> getURLBlocking() {
        log.info("querying google");
        return Mono.fromCallable(() -> getHttpBlocking("https://www.google.com"))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(s -> log.info("found content length {}", s.length()));
    }

    @GetMapping("/threadpool")
    public Mono<String> getURLThreadPool() {
        log.info("querying google");
        return Mono.fromCallable(() -> getHttpBlocking("https://www.google.com"))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(s -> log.info("found content length {}", s.length()));
    }

    @GetMapping("/nonblocking")
    public Mono<String> getURLNonBlocking() {
        log.info("querying google");
        return getHttpNonBlocking("https://www.google.com")
                .doOnNext(s -> log.info("found content length {}", s.length()));
    }

    @GetMapping("/rxjavanonblocking")
    public Single<String> getURLRxJavaNonBlocking() {
        log.info("querying google");
        return RxJava2Adapter.monoToSingle(getHttpNonBlocking("https://www.google.com")
                .doOnNext(s -> log.info("found content length {}", s.length())));
    }

    String getHttpBlocking(String url) {
        return restTemplate.getForObject(url, String.class);
    }

    Mono<String> getHttpNonBlocking(String url) {
        return webClient
                .get()
                .uri(url)
                .exchangeToMono(cr -> cr.bodyToMono(String.class));
    }
}
