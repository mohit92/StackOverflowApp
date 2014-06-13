package com.example.stackoverflowsearch.app;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by mohit on 6/12/14.
 */
public class DisplayAnswers extends OrmLiteBaseActivity<DatabaseHelper> {
    private String questionId;
    private String answerURL;
    private static final String BODY = "body";
    private static final String AUTHOR = "display_name";
    private static final String SCORE = "score";

    private RuntimeExceptionDao<QuestionData,Integer> questionDao;
    private RuntimeExceptionDao<AnswerData, Integer> answerDao;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        questionId = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        setContentView(R.layout.display_results);

        answerURL = getAnswerURL(questionId);
        if(isNetworkAvailable()) {
            try {
                deleteOlderResults(questionId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            new AnswerJsonParserAsyncTask().execute(answerURL);
        } else{
            try {
                showOfflineAnswers(questionId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo!=null && networkInfo.isConnected();
    }

    private void deleteOlderResults(String questionId) throws SQLException {

        answerDao = getHelper().getAnswerRuntimeDao();
        questionDao = getHelper().getQuestionRuntimeDao();

        //find id in QuestionData for the question with questionId
        QueryBuilder<QuestionData,Integer> questionQb = questionDao.queryBuilder();
        questionQb.where().eq(QuestionData.QUESTION_ID_FIELD_NAME,Integer.parseInt(questionId));

        //delete all the answers in "answers" table corresponding to question_id
        DeleteBuilder<AnswerData,Integer> answerDb = answerDao.deleteBuilder();
        answerDb.where().in(AnswerData.QUESTION_ID_FIELD_NAME,questionQb.query());
        answerDb.delete();



    }

    private void showOfflineAnswers(String questionId) throws SQLException {
        questionDao = getHelper().getQuestionRuntimeDao();
        answerDao = getHelper().getAnswerRuntimeDao();

        QueryBuilder<QuestionData,Integer> questionQb = questionDao.queryBuilder();
        questionQb.where().eq(QuestionData.QUESTION_ID_FIELD_NAME, Integer.parseInt(questionId));

        QueryBuilder<AnswerData,Integer> answerQb = answerDao.queryBuilder();
        List<AnswerData> offlineAnswers = answerQb.join(questionQb).query();
        if(offlineAnswers.isEmpty()){
           showToast("No offline results,Please connect to Internet");
            finish();
        }
        else {
            displayAnswers(offlineAnswers);
        }

    }

    class AnswerJsonParserAsyncTask extends AsyncTask<String, String, Void> {

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.answersllProgressBar);
        String jsonString;
        @Override
        protected void onPreExecute() {
            linearLayout.setVisibility(View.VISIBLE);
        }
        protected Void doInBackground(String... params) {

            URL url = null;
            try {

                url = new URL(answerURL);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder stringBuilder = new StringBuilder();

                int read;
                char [] chars = new char[1024];
                while((read = bufferedReader.read(chars)) != -1)
                    stringBuilder.append(chars,0,read);
                jsonString=stringBuilder.toString();
                Log.d("fetched answers",jsonString);

                feedIntoDB(jsonString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }
        protected void  onPostExecute(Void v) {
            linearLayout.setVisibility(View.INVISIBLE);
        }
    }
    private void feedIntoDB (String jsonString) {
        JSONObject results = null;
        try {
            results = new JSONObject(jsonString);

            JSONArray answers = results.getJSONArray("items");
            //List<AnswerData> answerDataList = null;
            AnswerData answerData;
            questionDao = getHelper().getQuestionRuntimeDao();
            answerDao = getHelper().getAnswerRuntimeDao();
            QueryBuilder<QuestionData, Integer> questionQb = questionDao.queryBuilder();
            QuestionData questionData = questionQb.where().eq(QuestionData.QUESTION_ID_FIELD_NAME, Integer.parseInt(questionId)).queryForFirst();
            Log.d("questionId", questionData.toString());
            if(answers.length()>0) {
                for (int i = 0; i < answers.length(); i++) {

                    JSONObject c = answers.getJSONObject(i);
                    JSONObject owner = c.getJSONObject("owner");
                    String body = c.getString(BODY);
                    int score = Integer.parseInt(c.getString(SCORE));
                    String author = owner.getString(AUTHOR);

                    answerData = new AnswerData(questionData, score, author, body);
                    answerDao.create(answerData);
                    //answerDataList.add(answerData);

                }
                QueryBuilder<AnswerData, Integer> statementBuilder = answerDao.queryBuilder();
                statementBuilder.where().eq(AnswerData.QUESTION_ID_FIELD_NAME, questionData);
                List<AnswerData> list = answerDao.query(statementBuilder.prepare());
                displayAnswers(list);
            }
            else{
                showToast("No answers for this question");
                finish();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
    private void displayAnswers(List<AnswerData> list) {
        final ListView listView = (ListView) findViewById(R.id.answerListView);
        final CustomAdapterForAnswers customAdapterForAnswers = new CustomAdapterForAnswers(DisplayAnswers.this,list);

        DisplayAnswers.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                listView.setAdapter(customAdapterForAnswers);


            }
        });
        
    }

    private void showToast(final String toastMessage){
        DisplayAnswers.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_LONG).show();
            }

        });
    }

    private String getAnswerURL(String questionId){
        return "http://api.stackexchange.com/2.2/questions/"+questionId+"/answers?order=desc&sort=activity&site=stackoverflow&filter=!9YdnSK0R1";
    }


}
