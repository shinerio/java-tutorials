package com.shinerio.tutorial.document;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Setter
@Getter
public class Account {

    @Id
    private String id;
    private String owner;
    private Double value;

    public Account(String owner, Double value) {
        this.owner = owner;
        this.value = value;
    }
}
