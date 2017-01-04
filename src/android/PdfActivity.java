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

  private List<Button> ListBtnView = new ArrayList<Button>();

  private TextView headerTitle;

  private String title;

  private PdfUtils pdfUtils;

  private int btnId;

  private boolean btnPressed;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getLayoutResourceByName("activity_pdf"));

    pdfUtils = PdfUtils.getInstance(this, PdfUtils.Mode.EXTERNAL);

    String pdf = getIntent().getStringExtra("file");
    title = getIntent().getStringExtra("title");

    btn1 = (Button) findViewById(getIdResourceByName("btn1"));
    btn2 = (Button) findViewById(getIdResourceByName("btn2"));
    btn3 = (Button) findViewById(getIdResourceByName("btn3"));

    ListBtnView.add(btn1);
    ListBtnView.add(btn2);
    ListBtnView.add(btn3);

    btnPressed = false;

    for(int i = 0; i < btnsList.size();i++){
      ListBtnView.get(i).setVisibility(View.VISIBLE);
      ListBtnView.get(i).setText(btnsList.get(i).getName());
      if(!btnsList.get(i).isDefaulf().equals("true")) {
        ListBtnView.get(i).setBackgroundResource(getDrawableResourceByName("btn_unchecked"));
        ListBtnView.get(i).setTextColor(Color.parseColor("#000053"));
      }
      final int finalI = i;
      ListBtnView.get(i).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          btnId = finalI;
          btnPressed = true;
          finish();
        }
      });
    }

    final File file = pdfUtils.saveFile(this, title, pdf);

    if(file == null)
      callbackContext.error("Error opening Pdf");

    PDFView pdfView = (PDFView) findViewById(getIdResourceByName("pdfView"));
    headerTitle = (TextView) findViewById(getIdResourceByName("WebViewHeaderTitle"));
    LinearLayout header =
            (LinearLayout) findViewById(getIdResourceByName("pdf_layout_header"));

    Button headerBackButton = (Button) findViewById(getIdResourceByName("faBackButton"));
    Button shareBtn = (Button) findViewById(getIdResourceByName("btn_share"));

    setTypefaces();

    headerBackButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        finish();
      }
    });

    shareBtn.setVisibility(View.VISIBLE);
    shareBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.putExtra(Intent.EXTRA_EMAIL, "");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title);
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
    callbackContext.success();
  }

  @Override public void onPageChanged(int page, int pageCount) {

  }

  @Override
  protected void onStop() {
    if(btnPressed)
      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, btnsList.get(btnId).getId()));
    else
      callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "PdfActivity Finished"));

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
