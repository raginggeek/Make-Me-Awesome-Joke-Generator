package net.raginggeek.makemeawesome;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class MainActivity extends Activity {
    private static final String URL = "http://api.icndb.com/jokes/random?" +
        "limitTo=[nerdy]&firstName={first}&lastName={last}";
    private TextView jokeView;
    private RestTemplate template = new RestTemplate();
    private AsyncTask<String, Void, String> task;
    private ShareActionProvider shareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jokeView = findViewById(R.id.jokeView);
        Button jokeButton = findViewById(R.id.getJokeButton);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        jokeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                task = new JokeTask().execute(
                        prefs.getString("first", "Geoff"),
                        prefs.getString("last", "Finch")
                );
            }
        });

        template.getMessageConverters().add(new GsonHttpMessageConverter());
        if (savedInstanceState != null) {
            jokeView.setText(savedInstanceState.getString("display"));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("display", jokeView.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.actionShare);
        shareActionProvider = (ShareActionProvider) item.getActionProvider();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionJoke:
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                task = new JokeTask().execute(
                        prefs.getString("first", "Geoff"),
                        prefs.getString("last", "Finch")
                );
                return true;
            case R.id.preferences:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (task != null) task.cancel(true);
    }

    private void setIntent(String jokeText) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, jokeText);
        shareActionProvider.setShareIntent(intent);
    }

    private class JokeTask extends AsyncTask<String, Void, String> {
        @Override
        protected  String doInBackground(String... params) {
            IcndbJoke joke = template.getForObject(URL, IcndbJoke.class,
                    params[0], params[1]);
            return joke.getJoke();
        }

        @Override
        protected void onPostExecute(String result) {
            jokeView.setText(result);
            setIntent(result);
        }
    }
}
