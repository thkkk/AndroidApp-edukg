package com.example.frontend.utils;

/**
 * 在数据库中的存储结构
 */
public class RecordToStore {
    public String type;
    public String uri;
    public String content;


    public RecordToStore() {
        super();
    }
    public RecordToStore(String type, String uri, String content) {
        // e.g. key = uri
        super();
        this.type = type;
        this.uri = uri;
        this.content = content;
    }

    @Override
    public String toString() {
        return "Record [type=" + type + ", uri=" + uri + ", content=" + content + "]";
    }
}
