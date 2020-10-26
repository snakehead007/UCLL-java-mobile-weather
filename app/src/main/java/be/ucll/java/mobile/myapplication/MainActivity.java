package be.ucll.java.mobile.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WeatherObservation;
import org.geonames.WebService;
import org.json.JSONStringer;

public class MainActivity extends AppCompatActivity {
    private static final String GEONAME_USERNAME = "dtvzkvqstjomgirpet";
    private TextView txtCity, txtCountry, txtTemperature, txtPressure, txtHumidity;
    private EditText txtSearch;
    private ImageButton btnSearch;
    private Handler myHandler = null;
    String placeName,countryName;
    double hum,temp;
    int press;
    private final static int MESSAGE_UPDATE_TEXT_CHILD_THREAD =1;

    private void updateText()
    {
        txtCity.setText(placeName);
        txtCountry.setText(countryName);
        txtTemperature.setText(String.valueOf(temp)+" Graden Celsuis");
        txtHumidity.setText(String.valueOf(hum)+" grams/cubic meter");
        txtPressure.setText(String.valueOf(press)+" Pascal");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WebService.setUserName(GEONAME_USERNAME);
        txtCity = findViewById(R.id.txtCity);
        txtCountry = findViewById(R.id.txtCountry);
        txtTemperature = findViewById(R.id.txtTemperature);
        txtPressure = findViewById(R.id.txtPressure);
        txtHumidity = findViewById(R.id.txtHumidity);
        txtSearch = findViewById(R.id.txtSearch);
        btnSearch = findViewById(R.id.btnSearch);
        createUpdateUiHandler();

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
                            ToponymSearchResult searchResult = null;
                            try {
                                searchCriteria.setQ(String.valueOf(txtSearch.getText().toString()));
                                searchResult = WebService.search(searchCriteria);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Toponym result = searchResult.getToponyms().get(0);
                            countryName = result.getCountryName();
                            try {
                                WeatherObservation weather = WebService.findNearByWeather(result.getLatitude(), result.getLongitude());
                                placeName = WebService.findNearbyPlaceName(result.getLatitude(), result.getLongitude()).get(0).getName();
                                temp = weather.getTemperature();
                                hum =  weather.getHumidity();
                                press = weather.getElevation();
                                Message message = new Message();
                                // Set message type.
                                message.what = MESSAGE_UPDATE_TEXT_CHILD_THREAD;
                                myHandler.sendMessage(message);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    t.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void createUpdateUiHandler()
    {
        if(myHandler == null)
        {
            myHandler = new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    updateText();
                }
            };
        }
    }
}
//CitySearch respo = new Gson().fromJson(jsono.toString(), CitySearch.class);