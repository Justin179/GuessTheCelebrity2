package tw.com.nec.justin_chen.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    List<String> celebURLs = new ArrayList<String>();
    List<String> celebNames = new ArrayList<String>();

    int chosenCeleb = 0;
    ImageView imageView;
    Button button;
    Button button2;
    Button button3;
    Button button4;

    // 正確的位置 0 1 2 3
    int locationOfCorrectAnswer;
    // 4 names
    String[] answers = new String[4];


    public void celebChosen(View view){
        // tag 0 1 2 3
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            Toast.makeText(getApplicationContext(),"Correct!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(),"His/Her name is "+answers[locationOfCorrectAnswer],Toast.LENGTH_LONG).show();
        }

        // 再產生下一題
        try {
            createNewQuestion();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    // 用另一個執行緒，取得網頁文字
    public class DownloadTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... urls) {

            StringBuilder result = new StringBuilder();
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection)url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while(data!=-1){

                    char current = (char)data;
                    result.append(current);

                    data = reader.read();
                }
                return result.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // download the whole inputstream in one go
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                // convert data downloaded into an image
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);
                return myBitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DownloadTask task = new DownloadTask();
        try {
            String result = task.execute("http://www.posh24.com/celebrities").get();
            System.out.println("取得網頁文字: "+result);

            // <div class="sidebarContainer"> 在這個之後的 img src不要取
            String[] splitedResult = result.split("<div class=\"sidebarContainer\">");

            // 大頭照
            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(splitedResult[0]);
            // 這全做完
            while(m.find()){
//                System.out.println(m.group(1));
                celebURLs.add(m.group(1));
            }

            // 名人名
            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitedResult[0]);
            // 才會做這些
            while(m.find()){
//                System.out.println(m.group(1));
                celebNames.add(m.group(1));
            }

            createNewQuestion();



        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    public void createNewQuestion() throws ExecutionException, InterruptedException {
        // pick a random number
        // download the image to display on the ui
        //System.out.println(celebURLs.size()); // 40 (0-39)
        Random rand = new Random();
        chosenCeleb = rand.nextInt(celebURLs.size()); // 0-40 (41)

        // 圖像
        String url = celebURLs.get(chosenCeleb);

        ImageDownloader imageTask = new ImageDownloader();
        Bitmap image = imageTask.execute(url).get();
        imageView = (ImageView) findViewById(R.id.imageView);
        // 圖像放到UI
        imageView.setImageBitmap(image);

        locationOfCorrectAnswer = rand.nextInt(4);

        int incorrectAnswerLocation;

        for(int i = 0; i<4; i++){ // 0 1 2 3

            if(i==locationOfCorrectAnswer){ // 正解
                answers[i] = celebNames.get(chosenCeleb);
            }else {
                incorrectAnswerLocation = rand.nextInt(celebNames.size());

                while(incorrectAnswerLocation==chosenCeleb){
                    incorrectAnswerLocation = rand.nextInt(celebNames.size());
                }

                answers[i] = celebNames.get(incorrectAnswerLocation);
            }
        }

        button = (Button)findViewById(R.id.button);
        button2 = (Button)findViewById(R.id.button2);
        button3 = (Button)findViewById(R.id.button3);
        button4 = (Button)findViewById(R.id.button4);

        button.setText(answers[0]);
        button2.setText(answers[1]);
        button3.setText(answers[2]);
        button4.setText(answers[3]);
    }

}
