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
    private String id;
    private String password;
    private String errorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        goToLoginFrag();
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void goToGradesFrag() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new GradesFragment())
                .commit();
    }

    public void goToErrorFrag() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new ErrorFragment())
                .commit();
    }

    public void setErrorText(String text){
        errorText = text;
    }

    public String getErrorText() {
        return errorText;
    }

    public void goToLoginFrag() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frag, new LoginFragment())
                .commit();
    }
}
