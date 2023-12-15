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
        intent.setType("application/txt");
        intent.putExtra(Intent.EXTRA_TITLE,"invoice.txt");
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
            OutputStream outputStream = getContentResolver().openOutputStream(wr_uri);
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            bufferedWriter.write("田想的自行车丢了");
            bufferedWriter.close();
            outputStream.close();

            Toast.makeText(MainActivity.this,"文件写入成功",Toast.LENGTH_LONG).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String getFilePathFromUri(Uri uri) {
        String filePath = null;
        if (uri != null) {
            if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
                // 如果是 file:// 类型的 Uri，则直接获取文件路径
                filePath = uri.getPath();
            } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
                // 如果是 content:// 类型的 Uri，则使用 ContentResolver 查询文件路径
                Cursor cursor = null;
                try {
                    String[] projection = { MediaStore.MediaColumns.DATA };
                    cursor = getContentResolver().query(uri, projection, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                        filePath = cursor.getString(columnIndex);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
        return filePath;
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
                    while ((line = bufferedReader.readLine()) != null){
                        Log.d("MainActivity", "onActivityResult: " + line);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


}