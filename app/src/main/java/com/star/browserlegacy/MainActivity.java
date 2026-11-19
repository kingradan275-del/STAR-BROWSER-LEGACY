package com.star.browserlegacy;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private static final String PREFS = "star_browser_data";
    private static final String GOOGLE = "https://www.google.com/search?q=";
    private LinearLayout root;
    private FrameLayout webContainer;
    private EditText addressBar;
    private Button backButton, forwardButton;
    private SharedPreferences prefs;
    private final ArrayList<BrowserTab> tabs = new ArrayList<BrowserTab>();
    private int activeTab = 0;
    private boolean nightMode = false;

    private class BrowserTab {
        WebView web;
        String title = "New tab";
        String url = "about:blank";
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        nightMode = prefs.getBoolean("night", false);
        root = (LinearLayout)findViewById(R.id.root);
        webContainer = (FrameLayout)findViewById(R.id.webContainer);
        addressBar = (EditText)findViewById(R.id.addressBar);
        backButton = (Button)findViewById(R.id.backButton);
        forwardButton = (Button)findViewById(R.id.forwardButton);

        backButton.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ if(current().web.canGoBack()) current().web.goBack(); }});
        forwardButton.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ if(current().web.canGoForward()) current().web.goForward(); }});
        findViewById(R.id.goButton).setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ navigateFromBar(); }});
        findViewById(R.id.menuButton).setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ showMainMenu(v); }});
        findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ showHome(); }});
        findViewById(R.id.bookmarksButton).setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ showBookmarks(); }});
        findViewById(R.id.tabsButton).setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ showTabs(); }});
        findViewById(R.id.historyButton).setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ showHistory(); }});
        addressBar.setOnEditorActionListener(new TextView.OnEditorActionListener(){ public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event){ if(actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)){ navigateFromBar(); return true; } return false; }});
        createTab(true);
        applyTheme();
    }

    private BrowserTab current(){ return tabs.get(activeTab); }

    private void createTab(boolean showStart) {
        final BrowserTab tab = new BrowserTab();
        tab.web = new WebView(this);
        tab.web.getSettings().setJavaScriptEnabled(true);
        tab.web.getSettings().setDomStorageEnabled(true);
        tab.web.getSettings().setLoadsImagesAutomatically(true);
        tab.web.setWebChromeClient(new WebChromeClient(){
            @Override public void onReceivedTitle(WebView view, String title){ tab.title = title == null ? "STAR Browser" : title; if(tab == current()) setAddress(view.getUrl()); }
        });
        tab.web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view, String url){
                if(url == null) return false;
                if(url.startsWith("http://") || url.startsWith("https://")) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch(Exception e) { Toast.makeText(MainActivity.this, "Cannot open this link", Toast.LENGTH_SHORT).show(); }
                return true;
            }
            @Override public void onPageFinished(WebView view, String url){
                tab.url = url == null ? "about:blank" : url;
                if(tab == current()){ setAddress(url); updateNavButtons(); }
                if(url != null && (url.startsWith("http://") || url.startsWith("https://"))) addHistory(tab.title, url);
            }
        });
        tab.web.setDownloadListener(new DownloadListener(){ public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength){ startDownload(url, contentDisposition, mimetype); }});
        tabs.add(tab);
        activeTab = tabs.size()-1;
        webContainer.removeAllViews();
        webContainer.addView(tab.web, new FrameLayout.LayoutParams(-1,-1));
        if(showStart) loadStartPage(tab);
        updateNavButtons();
    }

    private void loadStartPage(BrowserTab tab){
        String html = "<!doctype html><html><head><meta name='viewport' content='width=device-width, initial-scale=1'><style>body{margin:0;background:#f3f6fb;color:#17233b;font-family:Arial,sans-serif;text-align:center}.top{background:#101c35;color:white;padding:42px 15px 30px}.logo{font-size:42px;font-weight:bold;letter-spacing:5px;color:#25c4df}.sub{font-size:12px;letter-spacing:3px;color:#cbd7e8}.box{margin:25px 18px;padding:18px;background:white;border-radius:14px;box-shadow:0 3px 12px #ccd3df}.hint{color:#71809a;font-size:13px}.grid{display:flex;flex-wrap:wrap;justify-content:center}.tile{display:inline-block;margin:8px;padding:14px 8px;width:105px;background:white;border:1px solid #dce3ee;border-radius:10px;color:#17233b;text-decoration:none;font-size:14px}.dot{font-size:25px;color:#14aeca}.foot{margin-top:35px;color:#8b96a9;font-size:11px}</style></head><body><div class='top'><div class='logo'>STAR</div><div class='sub'>BROWSER LEGACY</div></div><div class='box'><div style='font-size:20px;font-weight:bold'>Welcome to STAR</div><p class='hint'>Search the web or open a website</p><p class='hint'>Use the address bar above to get started.</p></div><div class='grid'><a class='tile' href='https://www.google.com'><div class='dot'>G</div>Google</a><a class='tile' href='https://www.youtube.com'><div class='dot'>▶</div>YouTube</a><a class='tile' href='https://wikipedia.org'><div class='dot'>W</div>Wikipedia</a><a class='tile' href='https://github.com'><div class='dot'>⌘</div>GitHub</a></div><div class='foot'>POWERED BY STAR · VERSION 1.0</div></body></html>";
        tab.url = "about:blank";
        tab.title = "STAR BROWSER LEGACY";
        tab.web.loadDataWithBaseURL("https://star.local/", html, "text/html", "UTF-8", null);
        if(tab == current()) addressBar.setText("");
    }

    private void navigateFromBar(){
        String text = addressBar.getText().toString().trim();
        if(text.length() == 0) return;
        String url;
        if(text.matches("(?i)^[a-z][a-z0-9+.-]*://.*") || text.startsWith("about:")) url = text;
        else if(text.contains(".") && !text.contains(" ")) url = "https://" + text;
        else { try { url = GOOGLE + URLEncoder.encode(text, "UTF-8"); } catch(Exception e){ url = GOOGLE + text; } }
        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(addressBar.getWindowToken(),0);
        current().web.loadUrl(url);
    }

    private void setAddress(String url){ if(url != null && !url.equals("about:blank") && !url.startsWith("https://star.local")) addressBar.setText(url); }
    private void updateNavButtons(){ if(tabs.size()>0){ backButton.setEnabled(current().web.canGoBack()); forwardButton.setEnabled(current().web.canGoForward()); } }

    private void showMainMenu(View anchor){
        PopupMenu menu = new PopupMenu(this, anchor);
        String[] labels = {"New tab", "Bookmarks", "History", "Downloads", "Share page", "Night mode", "Settings", "About STAR"};
        for(int i=0;i<labels.length;i++) menu.getMenu().add(Menu.NONE,i,i,labels[i]);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){ public boolean onMenuItemClick(MenuItem item){
            switch(item.getItemId()){
                case 0: createTab(true); return true;
                case 1: showBookmarks(); return true;
                case 2: showHistory(); return true;
                case 3: showDownloads(); return true;
                case 4: sharePage(); return true;
                case 5: nightMode = !nightMode; prefs.edit().putBoolean("night",nightMode).apply(); applyTheme(); return true;
                case 6: showSettings(); return true;
                case 7: showAbout(); return true;
            } return false;
        }});
        menu.show();
    }

    private void applyTheme(){
        if(root == null) return;
        root.setBackgroundColor(nightMode ? 0xFF111827 : 0xFFF4F6FA);
        webContainer.setBackgroundColor(nightMode ? 0xFF111827 : 0xFFFFFFFF);
    }

    private JSONArray getArray(String key){ try { return new JSONArray(prefs.getString(key,"[]")); } catch(Exception e){ return new JSONArray(); } }
    private void saveArray(String key, JSONArray array){ prefs.edit().putString(key,array.toString()).apply(); }
    private void addHistory(String title,String url){
        if(url == null || url.startsWith("about:") || url.startsWith("https://star.local")) return;
        JSONArray a = getArray("history");
        JSONObject o = new JSONObject();
        try { o.put("title", title == null ? url : title); o.put("url",url); o.put("time",System.currentTimeMillis()); a.put(o); } catch(JSONException ignored){}
        if(a.length()>300){ JSONArray b=new JSONArray(); for(int i=Math.max(0,a.length()-250);i<a.length();i++) b.put(a.opt(i)); a=b; }
        saveArray("history",a);
    }

    private void showBookmarks(){ showSavedList("bookmarks", "Bookmarks", true); }
    private void showHistory(){ showSavedList("history", "History", false); }
    private void showSavedList(final String key,String title,final boolean editable){
        final JSONArray a = getArray(key);
        final ArrayList<String> names = new ArrayList<String>();
        final ArrayList<String> urls = new ArrayList<String>();
        for(int i=a.length()-1;i>=0;i--){ JSONObject o=a.optJSONObject(i); if(o!=null){ names.add(o.optString("title",o.optString("url"))); urls.add(o.optString("url")); } }
        if(names.size()==0){ new AlertDialog.Builder(this).setTitle(title).setMessage("Nothing here yet.").setPositiveButton("OK",null).show(); return; }
        final String[] rows = new String[names.size()];
        for(int i=0;i<rows.length;i++) rows[i]=names.get(i)+"\n"+urls.get(i);
        AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle(title).setItems(rows,new DialogInterface.OnClickListener(){ public void onClick(DialogInterface d,int which){ current().web.loadUrl(urls.get(which)); }}).setNegativeButton("Close",null);
        if(editable){ b.setNeutralButton("Add current page",new DialogInterface.OnClickListener(){ public void onClick(DialogInterface d,int w){ addBookmark(current().title,current().web.getUrl()); }}); }
        else { b.setNeutralButton("Clear history",new DialogInterface.OnClickListener(){ public void onClick(DialogInterface d,int w){ saveArray("history",new JSONArray()); Toast.makeText(MainActivity.this,"History cleared",Toast.LENGTH_SHORT).show(); }}); }
        b.show();
    }

    private void addBookmark(String title,String url){
        if(url==null || url.equals("about:blank") || url.startsWith("https://star.local")){ Toast.makeText(this,"Open a website first",Toast.LENGTH_SHORT).show(); return; }
        JSONArray a=getArray("bookmarks"); JSONObject o=new JSONObject();
        try{o.put("title",title==null?url:title);o.put("url",url);a.put(o);}catch(JSONException ignored){}
        saveArray("bookmarks",a); Toast.makeText(this,"Bookmark saved",Toast.LENGTH_SHORT).show();
    }

    private void showTabs(){
        if(tabs.size()==0){createTab(true);return;}
        final String[] names=new String[tabs.size()+1];
        for(int i=0;i<tabs.size();i++) names[i]=(i==activeTab?"● ":"")+tabs.get(i).title+"\n"+tabs.get(i).url;
        names[tabs.size()]="＋  New tab";
        new AlertDialog.Builder(this).setTitle("Open tabs · "+tabs.size()).setItems(names,new DialogInterface.OnClickListener(){ public void onClick(DialogInterface d,int which){ if(which==tabs.size()) createTab(true); else switchTab(which); }}).setNegativeButton("Close",null).setNeutralButton("Close current tab",new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int w){closeCurrentTab();}}).show();
    }
    private void switchTab(int index){ if(index<0||index>=tabs.size())return; activeTab=index; webContainer.removeAllViews(); webContainer.addView(current().web,new FrameLayout.LayoutParams(-1,-1)); setAddress(current().web.getUrl()); updateNavButtons(); }
    private void closeCurrentTab(){ if(tabs.size()<=1){ loadStartPage(current()); return; } BrowserTab old=current(); webContainer.removeView(old.web); old.web.destroy(); tabs.remove(activeTab); activeTab=Math.max(0,activeTab-1); switchTab(activeTab); }

    private void showDownloads(){
        DownloadManager dm=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        Cursor c=null; ArrayList<String> lines=new ArrayList<String>();
        try{ c=dm.query(new DownloadManager.Query()); if(c!=null) while(c.moveToNext()){ String name=c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE)); int status=c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)); String state=status==DownloadManager.STATUS_SUCCESSFUL?"Complete":status==DownloadManager.STATUS_FAILED?"Failed":"In progress"; lines.add((name==null?"Download":name)+" — "+state); } }catch(Exception ignored){} finally{if(c!=null)c.close();}
        String msg=lines.size()==0?"No downloads yet.":join(lines,"\n");
        new AlertDialog.Builder(this).setTitle("Downloads").setMessage(msg).setPositiveButton("OK",null).show();
    }
    private String join(List<String> values,String sep){StringBuilder b=new StringBuilder();for(int i=0;i<values.size();i++){if(i>0)b.append(sep);b.append(values.get(i));}return b.toString();}

    private void startDownload(String url,String disposition,String mime){
        if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){ requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},44); Toast.makeText(this,"Allow storage permission, then tap the download again",Toast.LENGTH_LONG).show(); return; }
        try{ DownloadManager.Request request=new DownloadManager.Request(Uri.parse(url)); request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); request.setTitle(disposition==null?"STAR download":disposition); if(mime!=null)request.setMimeType(mime); request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"STAR_"+System.currentTimeMillis()); ((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(request); Toast.makeText(this,"Download started",Toast.LENGTH_SHORT).show(); }catch(Exception e){Toast.makeText(this,"Download could not start",Toast.LENGTH_SHORT).show();}
    }
    private void sharePage(){ try{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,current().web.getUrl());startActivity(Intent.createChooser(i,"Share page"));}catch(Exception ignored){} }

    private void showSettings(){
        final String[] items={"Search engine: Google","Home page: STAR start page","Night mode: "+(nightMode?"On":"Off"),"Clear browsing history"};
        new AlertDialog.Builder(this).setTitle("STAR Settings").setItems(items,new DialogInterface.OnClickListener(){public void onClick(DialogInterface d,int w){
            if(w==0) new AlertDialog.Builder(MainActivity.this).setTitle("Default search engine").setMessage("Google is the default search engine in version 1.0.").setPositiveButton("OK",null).show();
            else if(w==1) showHome();
            else if(w==2){nightMode=!nightMode;prefs.edit().putBoolean("night",nightMode).apply();applyTheme();}
            else if(w==3) new AlertDialog.Builder(MainActivity.this).setTitle("Clear history?").setMessage("This removes saved browsing history.").setNegativeButton("Cancel",null).setPositiveButton("Clear",new DialogInterface.OnClickListener(){public void onClick(DialogInterface x,int y){saveArray("history",new JSONArray());}}).show();
        }}).setNegativeButton("Close",null).show();
    }
    private void showAbout(){
        new AlertDialog.Builder(this).setTitle("STAR BROWSER LEGACY").setMessage("Version 1.0\n\nPOWERED BY STAR\nCreated by Radan\n\nA lightweight browser concept for legacy Android devices.\nMinimum Android: 4.2.2\nRendering: Android WebView\nSearch: Google").setPositiveButton("OK",null).show();
    }
    private void showHome(){ loadStartPage(current()); addressBar.setText(""); }

    @Override public void onBackPressed(){ if(current().web.canGoBack()) current().web.goBack(); else showHome(); }
    @Override protected void onDestroy(){ for(BrowserTab t:tabs){ if(t.web!=null)t.web.destroy(); } super.onDestroy(); }
}
