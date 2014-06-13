package com.example.stackoverflowsearch.app;

/**
 * Created by mohit on 6/12/14.
 */

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "answers")
class AnswerData {
    public static final String QUESTION_ID_FIELD_NAME = "question_id";

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign=true,columnName = QUESTION_ID_FIELD_NAME)
    QuestionData question;


    @DatabaseField
    private int score;

    @DatabaseField
    private String author;

    @DatabaseField
    private String body;

    AnswerData() {

    }

    public AnswerData(QuestionData question, int score, String author, String body) {

        this.question = question;
        this.score = score;
        this.author = author;
        this.body = body;
    }

    public int getId() {return id;}

    public void setQuestionId(QuestionData questionData) {
        this.question =questionData;
    }
    public QuestionData getQuestionId() {
        return question;
    }

    public void setScore(int score) {
        this.score = score;
    }
    public int getScore() {
        return score;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
    public String getAuthor() {
        return author;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

}

