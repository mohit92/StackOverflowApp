package com.example.stackoverflowsearch.app;

/**
 * Created by mohit on 6/12/14.
 */

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mohit on 6/8/14.
 */

public class CustomAdapterForAnswers extends ArrayAdapter {

    private final Activity activity;
    private final List list;

    public CustomAdapterForAnswers(Activity activity, List<AnswerData> list){
        super(activity,R.layout.answers_layout,list);
        this.activity = activity;
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder viewHolder;

        if(rowView==null) {
            LayoutInflater layoutInflater = activity.getLayoutInflater();
            rowView = layoutInflater.inflate(R.layout.answers_layout,null);

            viewHolder = new ViewHolder();
            viewHolder.score = (TextView) rowView.findViewById(R.id.answerScoreTextView);
            viewHolder.body = (TextView) rowView.findViewById(R.id.answerBodyTextView);
            viewHolder.author = (TextView) rowView.findViewById(R.id.answerAuthorTextView);
            rowView.setTag(viewHolder);

        }
        else {
            viewHolder = (ViewHolder)rowView.getTag();
        }

        AnswerData item = (AnswerData) list.get(position);
        viewHolder.score.setText((Integer.toString(item.getScore())));
        viewHolder.body.setText(Html.fromHtml(item.getBody().toString()));
        viewHolder.author.setText(item.getAuthor().toString());
        return rowView;
    }

    static class ViewHolder {
        TextView score;
        TextView body;
        TextView author;
    }
}

