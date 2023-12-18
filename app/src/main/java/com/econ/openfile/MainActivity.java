package com.econ.openfile;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {
    private static final int OPEN_DIRECTORY_CODE = 1;
    private static final int CREATE_FILE_CODE = 2;
    private static final int OPEN_FILE_CODE = 3;
    private Button open_btn,read_btn,write_btn;
    private Uri wr_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        open_btn = findViewById(R.id.open_dir);

        open_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  openDirectory(getInitialDirectoryUri());
            }
        });

        read_btn = findViewById(R.id.read_file);
        read_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  openFile(wr_uri);
            }
        });

        write_btn = findViewById(R.id.write_file);
        write_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeFile();
            }
        });

    }

    private Uri getInitialDirectoryUri() {
        // 示例：获取外部存储器的根目录作为初始 URI
        File externalStorage = Environment.getExternalStorageDirectory();
        return Uri.fromFile(externalStorage);
    }


    //打开文件的指定目录
    private void openDirectory(Uri uriToload){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,uriToload);

        startActivityForResult(intent,OPEN_DIRECTORY_CODE);
    }

    //创建对应的文件
    private void createFile(Uri pickerInitialUri){
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE,"w_16.json");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
        startActivityForResult(intent,CREATE_FILE_CODE);
    }

    //打开指定的文件
    private void openFile(Uri pickerInitialUri){
         Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
         intent.addCategory(Intent.CATEGORY_OPENABLE);
         intent.setType("*/*");
         //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI,pickerInitialUri);
         startActivityForResult(intent,OPEN_FILE_CODE);
    }


    private void writeFile(){
        try {
            //创建json对象
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("w_ch",16);
            JSONObject ch_list = new JSONObject();
            String[] channel_names = new String[]{"O2","O1","TP10","P6","PZ","T8","F8","P5","TP9","C4","CZ","C3","FZ","T7","F7","FPZ"};
            for (int i = 0; i < channel_names.length; i++) {
                ch_list.put(String.valueOf(i),channel_names[i]);
            }
            jsonObject.put("list",ch_list);
            OutputStream outputStream = getContentResolver().openOutputStream(wr_uri);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write(jsonObject.toString());
            bufferedWriter.close();
            outputStream.close();

            Toast.makeText(MainActivity.this,"文件写入成功",Toast.LENGTH_LONG).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void openElectrodeActivity(View view){
        Intent intent = new Intent(MainActivity.this,ElectrodeActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OPEN_DIRECTORY_CODE
                && resultCode == Activity.RESULT_OK){
            Uri uri = null;
            if (data != null){
                uri = data.getData();
                createFile(uri);
                Log.d("MainActivity", "onActivityResult: " + uri.getPath());
            }
        }

        //创建对应的文档文件
        if (requestCode == CREATE_FILE_CODE
                && resultCode == Activity.RESULT_OK){
            Uri createUri = null;
            if (data != null){
                createUri = data.getData();
                wr_uri = createUri;
                Log.d("MainActivity", "onActivityResult: " + createUri.getPath());
                Toast.makeText(MainActivity.this,"文件创建成功,路径为：" + createUri.getPath(),Toast.LENGTH_LONG).show();
            }
        }

        //从打开的对应的文档文件中读取数据
        if (requestCode == OPEN_FILE_CODE
        && resultCode == Activity.RESULT_OK){
            Uri readUri = null;
            if (data != null){
                readUri = data.getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(readUri);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;
                    StringBuilder builder = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null){
                        Log.d("MainActivity", "onActivityResult: " + line);
                        builder.append(line);
                    }
                    JSONObject jsonObject = new JSONObject(builder.toString());
                    int channels = jsonObject.optInt("w_ch",0);
                    JSONObject ch_list = jsonObject.optJSONObject("list");

                    if (channels != 0){
                        int childrenCount = ch_list.length();
                        if (childrenCount == channels){
                            String[] channel_names = new String[channels];
                            for (int i = 0; i < channels; i++) {
                                channel_names[i] = ch_list.optString(Integer.toString(i),null);
                            }
                            Log.d("MainActivity", "json数据解析完成!!");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}