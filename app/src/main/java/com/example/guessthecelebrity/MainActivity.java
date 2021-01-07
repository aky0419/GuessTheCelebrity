package com.example.guessthecelebrity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private  String webContent = null;
    private List<String> imageUrls = new ArrayList<>();
    private List<String> names = new ArrayList<>();
    private ImageView imageView;
    int randomTagNumber = 0;
    private Button button1, button2, button3, button4;
    private String wrongName;
    int randomNameNumber;
    private HashSet<String> celebertyNames;
    String[] namesShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        button1 = findViewById(R.id.button1);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);




        DownloadContentFromWeb downloadContentFromWeb = new DownloadContentFromWeb();
        try {
            webContent = downloadContentFromWeb.execute("http://www.posh24.se/kandisar").get();
            stringManipulation(webContent);
            newQuestion();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void guessName(View view) {

        if (String.valueOf(randomTagNumber).equals(view.getTag())) {

            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            newQuestion();

        } else {
            Toast.makeText(this, "Wrong! it was " + namesShown[randomTagNumber], Toast.LENGTH_SHORT).show();
        }





    }

    private class DownloadImageFromUrl extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap bitmap = null;
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream stream = connection.getInputStream();

                bitmap = BitmapFactory.decodeStream(stream);
                return bitmap;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }
    }
    private class DownloadContentFromWeb extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            String results = "";

            try {
                URL url =  new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream stream = connection.getInputStream();
                InputStreamReader reader = new InputStreamReader(stream);
                int data = reader.read();
                while (data != -1) {
                    char c = (char) data;
                    results += c;
                    data = reader.read();
                }
                return results;

            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }



        }
    }

    private void stringManipulation(String webContent) {

        String[] splitWebContent = webContent.split("<div class=\"listedArticles\">");
        String imageContent = splitWebContent[0];
        Pattern pattern = Pattern.compile("<img src=\"(.*?)\"");
        Matcher matcher = pattern.matcher(imageContent);

        while (matcher.find()) {
            imageUrls.add(matcher.group(1));
        }

        pattern = Pattern.compile("alt=\"(.*?)\"");
        matcher = pattern.matcher(imageContent);

        while (matcher.find()) {
            names.add(matcher.group(1));
        }
    }

    private void newQuestion() {
        Random random = new Random();
        int randomImageNumber = random.nextInt(imageUrls.size());

        DownloadImageFromUrl downloadImageFromUrl = new DownloadImageFromUrl();
        try {
            Bitmap bitmap = downloadImageFromUrl.execute(imageUrls.get(randomImageNumber)).get();
            imageView.setImageBitmap(bitmap);

        }catch (Exception e) {
            e.printStackTrace();
        }

        randomTagNumber = random.nextInt(4);

        namesShown = new String[4];
        namesShown[randomTagNumber] = names.get(randomImageNumber);
        // [  ,  , name1 , , ]
        // Set[a,b,c]
        celebertyNames = new HashSet<>();
        while (celebertyNames.size() < 4) {
            randomNameNumber = random.nextInt(names.size());

            while (randomImageNumber == randomNameNumber) {
                randomNameNumber = random.nextInt(names.size());
            }
            celebertyNames.add(names.get(randomNameNumber));
        }
        int i = 0;

        for (String name : celebertyNames) {
            if (i != randomTagNumber) {
                namesShown[i] = name;
            }
            i++;
        }

        button1.setText(namesShown[0]);
        button2.setText(namesShown[1]);
        button3.setText(namesShown[2]);
        button4.setText(namesShown[3]);



    }
}
