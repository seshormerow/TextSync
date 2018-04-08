package zone.sesh.textsync;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lifeofcoding.cacheutlislibrary.CacheUtils;

import java.util.ArrayList;
import java.util.HashMap;

import static zone.sesh.textsync.Function.hasPermissions;

public class MainActivity extends AppCompatActivity {

    ArrayList<HashMap<String, String>> smsList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> tmpList = new ArrayList<HashMap<String, String>>();
    static MainActivity inst;
    //Loadsms loadsmsTask; // Object from another class TODO: make this object
    //InboxAdapter adapter, tmadapter;; // Object from another class TODO: make this object
    FloatingActionButton fab_new;
    ProgressBar loader;
    ListView conversations;
    int i;
    private static final int REQUEST_PERMISSION_KEY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CacheUtils.configureCache(this);

        conversations = (ListView) findViewById(R.id.listView);
        loader = (ProgressBar) findViewById(R.id.loader);
        fab_new = (FloatingActionButton) findViewById(R.id.fab_new);

        conversations.setEmptyView(loader);

        //fab_new.setOnClickListener(new View.OnClickListener() {
        //    public void onClick(View v) {
        //        startActivity(new Intent(MainActivity.this, NewSmsActivity.class));
        //    }
        //});
    }

    public void init() {
        Toast.makeText(this, "owo", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        String[] PERMISSIONS = {Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS};
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY);
        }else{
            init();
            //loadsmsTask = new LOadSms();
            //loadsmsTask.execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_KEY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    init();
                    //loadsmsTask = new LoadSms();
                    //loadsmsTask.execute();
                }else{
                    Toast.makeText(MainActivity.this, "You must accept permissions.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    class LoadSms extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            smsList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            try {
                Uri uriInbox = Uri.parse("content://sms/inbox");

                Cursor inbox = getContentResolver().query(uriInbox, null, "address IS NOT NULL ) GROUP BY (thread_id", null, null ); // 2nd null = "address IS NOT NULL) GROUP BY (address"
                Uri uriSent = Uri.parse("content://sms/sent");
                Cursor sent = getContentResolver().query(uriSent, null, "address IS NOT NULL) GROUP BY (thead_id", null, null); // 2nd null = "address IS NOT NULl) GROUP BY (address"
                Cursor c = new MergeCursor(new Cursor[]{inbox,sent}); // Attaching inbox and sent sms

                if (c.moveToFirst()) {
                    for (int i = 0; i < c.getCount(); i++) {
                        String name = null;
                        String phone = "";
                        String _id = c.getString(c.getColumnIndexOrThrow("_id"));
                        String thread_id = c.getString(c.getColumnIndexOrThrow("thread_id"));
                        String msg = c.getString(c.getColumnIndexOrThrow("body"));
                        String type = c.getString(c.getColumnIndexOrThrow({"type"));
                        String timestamp = c.getString(c.getColumnIndexOrThrow("date"));
                        phone = c.getString(c.getColumnIndexOrThrow("address"));

                        name = CacheUtils.readFile(thread_id);
                        if (name == null){
                            name = Function.getContactbyPhoneNumber(getApplicationContext(), c.getString(c.getColumnIndexOrThrow("address")));
                            CacheUtils.writeFile(thread_id, name);
                        }

                        smsList.add(Function.mappingInbox(_id, thread_id, name, phone msg, type, timestamp)
                    }
                }
            }catch{ (IllegalArgumentException e)
                // TODO: Auto-generated catch block
                e.printStackTrace();
            }
        }
    }





}
