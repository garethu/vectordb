package com.yootiful.vectordbs;


import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class YootifulVectordbsApplication {
    private final boolean ingest = true;

    public static void main(String[] args) {
        SpringApplication.run(YootifulVectordbsApplication.class, args);
    }
    @Bean
    TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    @Bean
    ApplicationRunner demo(TokenTextSplitter tokenTextSplitter, VectorStore vectorStore, JdbcClient db) {
        return args -> {

            // load the data into the vector database, if the flag is on
            if (ingest) {
                System.out.println("**************************");
                System.out.println("Ingesting starting....");
                System.out.println("**************************");
                Instant start = Instant.now();
                // get all the data to ingest
                var products = db.sql("select * from product")
                        .query(new DataClassRowMapper<>(Product.class))
                        .list();

                int total = products.size();
                AtomicInteger counter = new AtomicInteger(0);
                AtomicBoolean spinning = new AtomicBoolean(true);

                Thread spinnerThread = new Thread(() -> {
                    String[] frames = { "|", "/", "-", "\\" };
                    int i = 0;
                    while (spinning.get()) {
                        System.out.print(String.format("\r  %s  Ingesting... [%d / %d]  ",
                                frames[i++ % frames.length], counter.get(), total));
                        System.out.flush();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                    System.out.printf("\r  Done! Ingested %d vectors.%s%n", total, " ".repeat(20));
                });
                spinnerThread.setDaemon(true);
                spinnerThread.start();

                for (var p : products) {
                    var document = new Document(p.description(),
                            Map.of("id", p.id(),
                                    "uuid", p.uuid(),
                                    "description", p.description()
                            ));

                    var split = tokenTextSplitter.apply(List.of(document));
                    vectorStore.add(split);
                    counter.incrementAndGet();
                }

                spinning.set(false);
                try {
                    spinnerThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                Instant end = Instant.now();
                calculateProcess(start, end);
            }

            System.out.println("##############################################################################");
            System.out.println("Finding similarities for::: green trousers for winter");
            System.out.println("##############################################################################");
            var similar = vectorStore
                    .similaritySearch(SearchRequest.query("green trousers for winter").withTopK(10));


            System.out.println("Count: " + similar.size());
            for (var s : similar) {
                System.out.println("==============");
                var id = s.getMetadata().get("id");
                System.out.println("id: " + id);
                s.getMetadata().forEach((k, v) -> System.out.println(k + "=" + v));
            }

        };
    }

    record Product(Integer id, String uuid, String description) {
    }

    private void calculateProcess(Instant start, Instant end) {
        Duration timeElapsed = Duration.between(start, end);
        long minutes = timeElapsed.toMinutes();
        long seconds = timeElapsed.minusMinutes(minutes).toSeconds();

        System.out.println(String.format(">>> Vectorisation took: %d minutes and %d seconds", minutes, seconds));

    }
}
