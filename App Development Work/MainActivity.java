package com.example.SunshineWeather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sunshireweather.R;

import org.json.JSONException;

import java.io.IOException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements ForecastAdapter.ForecastAdapterOnClickHandler {
    private static final String TAG = MainActivity.class.getSimpleName();

//    TextView mWeatherDisplayTV;

    TextView mErrorMessageTextView;

    ProgressBar mLoadingIndicator;

    RecyclerView mRecyclerView;

    ForecastAdapter mForecastAdapter;

    Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_forcast);

        mErrorMessageTextView = (TextView) findViewById(R.id.tv_error_message_display);

//        mWeatherDisplayTV = (TextView) findViewById(R.id.tv_weather_data);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(this);

        mRecyclerView.setAdapter(mForecastAdapter);

        mLoadingIndicator = (ProgressBar) findViewById(R.id.loading_weather_progress);

        loadWeatherData();

         // Don't need this since we're getting real data from the internet
//        String[] pseudoWeather = {"12","34", "88", "41", "97", "85" } ;

//
//        for(String weatherData : pseudoWeather ){
//
//            mWeatherDisplayTV.setText(weatherData + "\n \n \n ");
//        }


    }

    private void loadWeatherData() {

        showWeatherDataView();

        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
    }

    private void showWeatherDataView(){
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
//        mWeatherDisplayTV.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(){
//        mWeatherDisplayTV.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onForecastClick(String weatherForDay) {
        if(mToast != null){
            mToast.cancel();
        }
        Context context = this;

        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context,destinationClass);

        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, weatherForDay);
        startActivity(intentToStartDetailActivity);

        mToast =  Toast.makeText(this, "Displaying Details",Toast.LENGTH_LONG);

        mToast.show();
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String... params) {

            if(params.length == 0){
                return null;
            }

            String location = params[0];

            URL weatherRequestURL = NetworkUtils.buildUrl(location);
            try{
                String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestURL);

                String[] simpleJsonWeatherData = OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(MainActivity.this,jsonWeatherResponse);

                return simpleJsonWeatherData;

            }
            catch (IOException | JSONException e){
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(String[] weatherData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if(weatherData != null){

                showWeatherDataView();

                mForecastAdapter.setmWeatherData(weatherData);
            }

            else{
                showErrorMessage();
            }
        }
    }


    private void openLocationInMap() {
        String addressString = "Scottsdale, AZ";
        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't call " + geoLocation.toString()
                    + ", no receiving apps installed!");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.forecast, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int menuItemThatWasSelected = item.getItemId();

        if(menuItemThatWasSelected == R.id.action_refresh){
            Context context = MainActivity.this;
            String message = "Refresh Clicked";
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();

            mForecastAdapter.setmWeatherData(null);
            loadWeatherData();
            return true;
        }

        if (menuItemThatWasSelected == R.id.action_map){
            openLocationInMap();
            return true;
        }

        if (menuItemThatWasSelected == R.id.action_settings){
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}