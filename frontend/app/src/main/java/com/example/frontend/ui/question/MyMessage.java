package com.example.frontend.ui.question;

public class MyMessage {
    public static final int TYPE_RECEIVED=0;
    public static final int TYPE_SEND=1;
    private String content;
    private String subject;
    private int my_type;

    public MyMessage(String _content, int type, String subject){
        this.content=_content;
        this.my_type=type;
        this.subject=subject;
    }

    public String getContent(){
        return this.content;
    }

    public String getSubject(){
        return this.subject;
    }

    public int getType(){
        return this.my_type;
    }

}
