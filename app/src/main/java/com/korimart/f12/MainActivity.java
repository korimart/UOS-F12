package com.korimart.f12;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {
    private GradesFragment gf;
    private boolean isAddedFrag;
    private TextView updateMessage;
    private TextView updateLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateMessage = findViewById(R.id.main_updateMessage);
        updateLink = findViewById(R.id.main_updateLink);
        (new Thread(this::checkUpdate)).start();

        getSupportActionBar().hide();
        goToLoginFrag();
    }

    @Override
    public void onBackPressed() {
        if (isAddedFrag){
            isAddedFrag = false;
            goToGradesFrag();
            return;
        }

        super.onBackPressed();
    }

    public void goToGradesFrag() {
        if (gf == null){
            gf = new GradesFragment();
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, gf)
                .commit();
    }

    public void goToErrorFrag(String errorString) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new ErrorFragment(errorString))
                .commit();
    }

    public void goToLoginFrag() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new LoginFragment())
                .commit();
    }

    public void goToOriginalXMLFrag(String originalXML) {
        isAddedFrag = true;
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frag, new OriginalFragment(originalXML))
                .commit();
    }

    private void checkUpdate(){
        UpdateChecker.checkUpdate(this, s -> {
            if (s != null){
                this.runOnUiThread(() -> {
                    updateMessage.setText("최신버전이 아닙니다. 본 버전에 오류가 있을 수 있습니다. 다운로드 :");
                    updateMessage.setTextColor(0xFFFF0000);
                    updateLink.setText(s);
                });
            }
        });
    }
}
