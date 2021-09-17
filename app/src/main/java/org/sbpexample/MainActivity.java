package org.sbpexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.net.URI;
import java.net.URISyntaxException;

import payments.sbp.SbpUtils;

public class MainActivity extends AppCompatActivity {
    //this link should be obtained after  QR scanning from
    private String sbpLink = "https://qr.nspk.ru/AD10006K1GQ7788G9ACAAM970SGCOLNM?type=02&&sum=1100&cur=RUB&crc=CD70";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View v = findViewById(R.id.tvBtn);
        String redirect = String.format("redirect=%s", BuildConfig.scheme);
        String link = sbpLink;
        try {
            link = appendParameter(sbpLink, redirect);
        } catch (URISyntaxException e) {
            //TODO add exception handling code
        }
        String finalLink = link;
        v.setOnClickListener(view -> SbpUtils.getInstance().showSbpListDialog(MainActivity.this, finalLink));
    }

    @Override
    protected void onDestroy() {
        SbpUtils.getInstance().cleanup();
        super.onDestroy();
    }

    String appendParameter(String link, String parameter) throws URISyntaxException {
        URI oldUri = new URI(link);

        String newQuery = oldUri.getQuery();
        if (newQuery == null) {
            newQuery = parameter;
        } else {
            newQuery += "&" + parameter;
        }

        return new URI(oldUri.getScheme(), oldUri.getAuthority(),
                oldUri.getPath(), newQuery, oldUri.getFragment()).toString();
    }
}