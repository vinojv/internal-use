package com.example.deepakds.videoland;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.VideoView;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ChatBubbleActivity extends Activity {
    private static final String TAG = "ChatActivity";

    ChatArrayAdapter chatArrayAdapter;
    ListView listView;
    EditText chatText,urltxt;
//    private Button buttonSend;
    ImageButton playImageButton, pauseImageButton;
    String videoUrl;
    VideoView videoView;

    Intent intent;
    private boolean side = false;

    public  int stopPosition;
    public int count = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        setContentView(R.layout.activity_chat);

        videoView = (VideoView)findViewById(R.id.youtubevideoview);
        chatText = (EditText) findViewById(R.id.msgbox);
        urltxt = (EditText)findViewById(R.id.videourl);
        playImageButton = (ImageButton)findViewById(R.id.playbutton);
        pauseImageButton = (ImageButton)findViewById(R.id.pausebutton);
        listView = (ListView) findViewById(R.id.listView1);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.activity_chat_singlemessage);
        listView.setAdapter(chatArrayAdapter);

        urltxt.setText("https://www.youtube.com/watch?v=pEDQ1z1-PvU");

        chatText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });

        urltxt.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub

                if (keyCode == event.KEYCODE_ENTER) {
                    new YourAsyncTask().execute();
                }

                if(keyCode == event.KEYCODE_BACK){
                    finish();
                }
                return true;
            }
        });

        playImageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                videoView.setVideoURI(video);
//                videoView.requestFocus();

                if(count == 0){
                    new YourAsyncTask().execute();
                    count++;
                }

//                pDialog.show();
                if(videoView.isPlaying() == false){
//                    videoView.resume();
                    System.out.println(videoView);
                    videoView.seekTo(stopPosition);
                    videoView.start();
                }


//                videoView.start();

//                if(videoView.isPlaying())
//                    pDialog.dismiss();
            }
        });

        pauseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopPosition = videoView.getCurrentPosition();
                videoView.pause();

//                if(videoView.isPlaying()){
//                    videoView.pause();
//                }
//                else{
//                    Toast.makeText(getApplicationContext(), "Video is not playing!", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
    }

    private boolean sendChatMessage() {
        chatArrayAdapter.add(new ChatMessage(side, chatText.getText().toString()));
        chatText.setText("");
        side = !side;
        return true;
    }

    private class YourAsyncTask extends AsyncTask<Void, Void, Void>
    {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(ChatBubbleActivity.this, "", "Loading Video wait...", true);
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            try
            {
                String url = urltxt.getText().toString();
                videoUrl = getUrlVideoRTSP(url);
                Log.e("Video url for playing=========>>>>>", videoUrl);
            }
            catch (Exception e)
            {
                Log.e("Login Soap Calling in Exception", e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            progressDialog.dismiss();

            videoView.setVideoURI(Uri.parse(videoUrl));
//            MediaController mc = new MediaController(MainActivity.this);
//            videoView.setMediaController(mc);
            videoView.requestFocus();
            videoView.start();
//            mc.show();
        }

    }

    public static String getUrlVideoRTSP(String urlYoutube)
    {
        try
        {
            String gdy = "http://gdata.youtube.com/feeds/api/videos/";
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            String id = extractYoutubeId(urlYoutube);
            URL url = new URL(gdy + id);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            Document doc = documentBuilder.parse(connection.getInputStream());
            Element el = doc.getDocumentElement();
            NodeList list = el.getElementsByTagName("media:content");///media:content
            String cursor = urlYoutube;
            for (int i = 0; i < list.getLength(); i++)
            {
                Node node = list.item(i);
                if (node != null)
                {
                    NamedNodeMap nodeMap = node.getAttributes();
                    HashMap<String, String> maps = new HashMap<String, String>();
                    for (int j = 0; j < nodeMap.getLength(); j++)
                    {
                        Attr att = (Attr) nodeMap.item(j);
                        maps.put(att.getName(), att.getValue());
                    }
                    if (maps.containsKey("yt:format"))
                    {
                        String f = maps.get("yt:format");
                        if (maps.containsKey("url"))
                        {
                            cursor = maps.get("url");
                        }
                        if (f.equals("1"))
                            return cursor;
                    }
                }
            }
            return cursor;
        }
        catch (Exception ex)
        {
            Log.e("Get Url Video RTSP Exception======>>", ex.toString());
        }
        return urlYoutube;

    }

    protected static String extractYoutubeId(String url) throws MalformedURLException
    {
        String id = null;
        try
        {
            String query = new URL(url).getQuery();
            if (query != null)
            {
                String[] param = query.split("&");
                for (String row : param)
                {
                    String[] param1 = row.split("=");
                    if (param1[0].equals("v"))
                    {
                        id = param1[1];
                    }
                }
            }
            else
            {
                if (url.contains("embed"))
                {
                    id = url.substring(url.lastIndexOf("/") + 1);
                }
            }
        }
        catch (Exception ex)
        {
            Log.e("Exception", ex.toString());
        }
        return id;
    }
    public void onPause ()
    {
        super.onPause();
        videoView.stopPlayback();
    }
}