package org.apache.cordova.pdfpluginmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

import org.apache.cordova.PluginResult;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.cordova.pdfpluginmanager.PDFViewer.btnsList;
import static org.apache.cordova.pdfpluginmanager.PDFViewer.callbackContext;

public class PdfActivity extends AppCompatActivity
        implements OnPageChangeListener, OnLoadCompleteListener {

  int pageNumber = 0;

  private Button btn1;

  private Button btn2;

  private Button btn3;

  private TextView warningText;

  private List<Button> ListBtnView = new ArrayList<Button>();

  private TextView headerTitle;

  private String title;
  private String subject;
  private String disabledDescription;

  private PdfUtils pdfUtils;

  private LinearLayout footerBar;

  private int btnId;

  private boolean btnPressed;
  private boolean btnBackPressed;
  private boolean hasReachedEndOfFile = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getLayoutResourceByName("activity_pdf"));

    pdfUtils = PdfUtils.getInstance(this, PdfUtils.Mode.EXTERNAL);

    String pdf = getIntent().getStringExtra("file");
    title = getIntent().getStringExtra("title");
    subject = getIntent().getStringExtra("subject");
    disabledDescription = getIntent().getStringExtra("disabledDescription");


    warningText = (TextView) findViewById(getIdResourceByName("warning_disabled_btns"));

    btn1 = (Button) findViewById(getIdResourceByName("btn1"));
    btn2 = (Button) findViewById(getIdResourceByName("btn2"));
    btn3 = (Button) findViewById(getIdResourceByName("btn3"));

    footerBar = (LinearLayout) findViewById(getIdResourceByName("footerBar"));

    ListBtnView.add(btn1);
    ListBtnView.add(btn2);
    ListBtnView.add(btn3);

    btnPressed = false;
    btnBackPressed = false;
      
    if(btnsList.size() == 0){
      footerBar.setVisibility(View.GONE);
    }else {
      if(disabledDescription != null){
        warningText.setText(disabledDescription);
        warningText.setVisibility(View.VISIBLE);
      }

      for (int i = 0; i < btnsList.size(); i++) {
        ListBtnView.get(i).setVisibility(View.VISIBLE);
        ListBtnView.get(i).setText(btnsList.get(i).getName());
        if (!btnsList.get(i).isDefaulf().equals("true")) {
          ListBtnView.get(i).setBackgroundResource(getDrawableResourceByName("btn_unchecked"));
        }else{
          ListBtnView.get(i).setTextColor(Color.WHITE);
        }

        if (btnsList.get(i).isDisabledUntilEOF() != null && btnsList.get(i).isDisabledUntilEOF().equalsIgnoreCase("true")) {
          ListBtnView.get(i).setEnabled(false);
        }

        final int finalI = i;
        ListBtnView.get(i).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            btnId = finalI;
            btnBackPressed = false;
            btnPressed = true;
            finish();
          }
        });
      }
    }

    final File file = pdfUtils.saveFile(this, title+".pdf", pdf);

    if(file == null)
      callbackContext.error("Error opening Pdf");

    PDFView pdfView = (PDFView) findViewById(getIdResourceByName("pdfView"));
    headerTitle = (TextView) findViewById(getIdResourceByName("WebViewHeaderTitle"));
    LinearLayout header =
            (LinearLayout) findViewById(getIdResourceByName("pdf_layout_header"));

    RelativeLayout headerBackButton = (RelativeLayout) findViewById(getIdResourceByName("faBackButton"));
    Button shareBtn = (Button) findViewById(getIdResourceByName("btn_share"));

    setTypefaces();

    headerBackButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        btnBackPressed = true;
        finish();
      }
    });

    shareBtn.setVisibility(View.VISIBLE);
    shareBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.putExtra(Intent.EXTRA_EMAIL, "");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        sharingIntent.setType("application/pdf");
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
      }
    });

    header.setVisibility(View.VISIBLE);

    headerTitle.setText(title);

    pdfView.fromFile(file)
            .defaultPage(pageNumber)
            .onPageChange(this)
            .enableAnnotationRendering(true)
            .onLoad(this)
            .scrollHandle(new DefaultScrollHandle(this))
            .load();
  }

  @Override public void loadComplete(int nbPages) {
    //callbackContext.success();
  }

  @Override public void onPageChanged(int page, int pageCount) {
    if(hasReachedEndOfFile) return;

    if(page == pageCount-1){
      hasReachedEndOfFile = true;
      for (int i = 0; i < ListBtnView.size(); i++) {
        ListBtnView.get(i).setEnabled(true);
      }
    }
  }
            
  @Override
  public void onBackPressed() {
    btnBackPressed = true;
    super.onBackPressed();
  }

  @Override
  protected void onStop() {
      if(btnPressed){ // normal button events
       callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, String.valueOf(btnsList.get(btnId).getId())));
    }else if(btnBackPressed){ //exiting activity with backbutton
       callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "-1"));
    }/*else {  //activity went into background (unknown reasons for now)
        PluginResult result = new PluginResult(PluginResult.Status.OK, "-2");
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }*/
    super.onStop();
  }

  public void setTypefaces(){
    Typeface font1 = Typeface.createFromAsset(
            this.getAssets(),
            "fonts/gothic_trade_bold.ttf");
    headerTitle.setTypeface(font1);

    Typeface font2 = Typeface.createFromAsset(
            this.getAssets(),
            "fonts/lucinda_grande_regular.ttf");
    btn1.setTypeface(font2);
    btn2.setTypeface(font2);
    btn3.setTypeface(font2);
  }

  public int getIdResourceByName(String resName) {
    String packageName = getPackageName();
    int resId = getResources().getIdentifier(resName, "id", packageName);
    return resId;
  }

  public int getLayoutResourceByName(String resName) {
    String packageName = getPackageName();
    int resId = getResources().getIdentifier(resName, "layout", packageName);
    return resId;
  }

  public int getDrawableResourceByName(String resName) {
    String packageName = getPackageName();
    int resId = getResources().getIdentifier(resName, "drawable", packageName);
    return resId;
  }

}
