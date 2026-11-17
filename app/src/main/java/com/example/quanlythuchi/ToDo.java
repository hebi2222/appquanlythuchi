package com.example.quanlythuchi;

public class ToDo {

    public final long id;
    public final String title;
    public final String content;
    public final String date;
    public final String type;
    public final boolean done;

    public ToDo(long id, String title, String content,
                String date, String type, boolean done) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.type = type;
        this.done = done;
    }
}
