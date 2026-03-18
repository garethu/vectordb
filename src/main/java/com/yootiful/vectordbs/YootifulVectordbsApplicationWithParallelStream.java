package com.yootiful.vectordbs;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Map;

//@SpringBootApplication
public class YootifulVectordbsApplicationWithParallelStream {

    public static void main(String[] args) {
        SpringApplication.run(YootifulVectordbsApplicationWithParallelStream.class, args);
    }

    //@Bean
    TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter();
    }

    private final boolean ingest = true;

    //@Bean
    ApplicationRunner demo(TokenTextSplitter tokenTextSplitter, JdbcClient db, VectorStore vectorStore) {
        return args -> {

            var products = db.sql("select * from products")
                    .query(new DataClassRowMapper<>(Product.class))
                    .list();

            if (ingest) {
                products
                        .parallelStream()
                        .forEach(product -> {
                            var document = new Document(product.id() + " " + product.description(),
                                    Map.of("id", product.id(),
                                            "description", product.description()
                                    ));

                            var split = tokenTextSplitter.apply(List.of(document));
                            vectorStore.add(split);
                        });

                for (var p : products) {
                    var document = new Document(p.id() + " " + p.description(),
                            Map.of("id", p.description(),
                                    "description", p.description()
                            ));

                    var split = tokenTextSplitter.apply(List.of(document));
                    vectorStore.add(split);
                }
            }

            var single = products
                    .parallelStream()
                    .toList()
                    .get(0);

            System.out.println("###################################################");
            System.out.println("Finding similarities for ID: " + single.id());
            System.out.println("###################################################");

            var similar = vectorStore
                    .similaritySearch(single.id() + " " + single.description());

            System.out.println("Count: " + similar.size());
            for (var s : similar) {
                System.out.println("==============");
                var id = s.getMetadata().get("id");
                System.out.println("id: " + id);
                s.getMetadata().forEach((k, v) -> System.out.println(k + "=" + v));
            }
        };
    }
    record Product(String id, String description) {
    }

}
