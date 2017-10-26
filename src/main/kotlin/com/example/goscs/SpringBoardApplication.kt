package com.example.goscs

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.io.Resource
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.io.File
import java.time.Duration

@SpringBootApplication
class SpringBoardApplication {

    @Bean
    fun run(@Value("file:///\${SCS_ROOT}") dir: Resource)
            = ApplicationRunner {

        dir
                .file
                .run {
                    Flux.just("config-service", "eureka-service", "zipkin-service").map { File(this, it) }
                }
                .subscribeOn(Schedulers.elastic())
                .map {
                    ProcessBuilder("/bin/bash")
                            .directory(it)
                            .command("mvn", "spring-boot:run")
                }
                .delayElements(Duration.ofSeconds(10)) // launch each one 10s after the first one
                .subscribe {
                    println("about to start ${it.command()} in ${it.directory()}")
                    it.start()
                }
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(SpringBoardApplication::class.java, *args)
}
