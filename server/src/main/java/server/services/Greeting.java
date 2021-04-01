package server.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Greeting {

    private final long id;
    private final String content;

    public List<String> getList() {
        return list;
    }

    private List<String> list = new ArrayList<>();

    public Greeting(long id, String content) {
        this.id = id;
        this.content = content;
        this.list = Stream.of("item 1","item 2","item 2").collect(Collectors.toList());
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}