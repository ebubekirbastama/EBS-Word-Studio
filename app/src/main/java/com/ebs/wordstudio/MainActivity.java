package com.ebs.wordstudio;

// EBS Builder app/build.gradle.kts dosyasini yeniden olustururken bu namespace kullanilir.
import com.ebs.forensic.detector.R;

import android.app.*;
import android.os.*;
import android.content.*;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int OPEN=11, SAVE_AS=12;
    private final int ink=Color.rgb(35,47,63), muted=Color.rgb(103,118,135), bg=Color.rgb(246,248,251), surface=Color.rgb(231,236,245), accent=Color.rgb(48,94,202);
    private EditText editor;
    private TextView filenameLabel, counter, state;
    private Uri currentUri;
    private String filename="Yeni Belge.docx";
    private boolean changed=false, loading=false;
    private float fontSize=18;
    private LinearLayout root;
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);getWindow().setNavigationBarColor(bg);
        createUi();
        if (getIntent()!=null && Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData()!=null) openUri(getIntent().getData());
    }
    private int dp(float value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView text(String s,int sp,boolean bold,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private void createUi() {
        // Responsive, image-free document workspace. The editor gets every remaining pixel.
        root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);
        root.setPadding(dp(9),dp(6),dp(9),dp(5));
        setContentView(root);

        LinearLayout header=new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(header,new LinearLayout.LayoutParams(-1,dp(42)));
        TextView appName=text("EBS WORD STUDIO",15,true,ink);
        header.addView(appName,new LinearLayout.LayoutParams(0,-1,1));
        TextView newButton=button("+ Yeni",surface);
        newButton.setPadding(dp(14),0,dp(14),0);
        header.addView(newButton,new LinearLayout.LayoutParams(-2,dp(36)));
        newButton.setOnClickListener(v->newDocument());

        LinearLayout fileRow=new LinearLayout(this);
        fileRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams fr=new LinearLayout.LayoutParams(-1,dp(35));
        root.addView(fileRow,fr);
        filenameLabel=text(filename,14,true,ink);
        filenameLabel.setSingleLine(true);
        filenameLabel.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        fileRow.addView(filenameLabel,new LinearLayout.LayoutParams(0,-1,1));
        state=text("Yeni belge",11,false,muted);
        state.setSingleLine(true);
        fileRow.addView(state,new LinearLayout.LayoutParams(-2,-1));

        // Scrollable actions remain usable on narrow phones and in landscape.
        HorizontalScrollView scroll=new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,dp(49)));
        LinearLayout actions=new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        scroll.addView(actions,new HorizontalScrollView.LayoutParams(-2,-1));
        toolbarAction(actions,"Aç",accent,()->pickOpen());
        toolbarAction(actions,"Kaydet",Color.rgb(33,126,92),()->save(false));
        toolbarAction(actions,"Farklı Kaydet",surface,()->save(true));
        toolbarAction(actions,"A−",surface,()->resize(-2));
        toolbarAction(actions,"A+",surface,()->resize(2));
        toolbarAction(actions,"Kopyala",surface,()->copy());

        editor=new EditText(this);
        editor.setTextColor(ink);
        editor.setHintTextColor(muted);
        editor.setHint("Word / DOCX belgesini açın veya metninizi buraya yazın…");
        editor.setTextSize(fontSize);
        editor.setGravity(Gravity.TOP|Gravity.START);
        editor.setPadding(dp(15),dp(14),dp(15),dp(14));
        editor.setBackground(shape(Color.WHITE,9));
        editor.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        editor.setSingleLine(false);
        editor.setHorizontallyScrolling(false);
        editor.setVerticalScrollBarEnabled(true);
        editor.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,0,1);
        ep.topMargin=dp(4);
        root.addView(editor,ep);
        editor.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count){if(!loading){changed=true;updateHeader();}updateCount();}
            public void afterTextChanged(Editable e){}
        });

        LinearLayout footer=new LinearLayout(this);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(footer,new LinearLayout.LayoutParams(-1,dp(27)));
        counter=text("0 kelime · 0 karakter",11,false,muted);
        footer.addView(counter,new LinearLayout.LayoutParams(0,-1,1));
        TextView info=text("YEREL / ÇEVRİMDIŞI",10,true,Color.rgb(33,126,92));
        footer.addView(info);
        updateCount();
    }
    private void toolbarAction(LinearLayout row,String title,int color,Runnable action){
        TextView t=button(title,color);
        t.setTextColor((color==accent||color==Color.rgb(33,126,92))?Color.WHITE:ink);
        t.setTextSize(12);
        t.setPadding(dp(13),0,dp(13),0);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(36));
        p.rightMargin=dp(6);
        row.addView(t,p);
        t.setOnClickListener(v->action.run());
    }
    private TextView button(String label,int color){TextView t=text(label,13,true,ink);t.setGravity(Gravity.CENTER);t.setBackground(shape(color,12));return t;}
    private void addAction(LinearLayout row,String name,int color,Runnable action){TextView b=button(name,color);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-1,name.equals("Farklı Kaydet")?1.5f:1f);p.rightMargin=dp(7);row.addView(b,p);b.setOnClickListener(v->action.run());}
    private void smallAction(LinearLayout row,String title,Runnable action){TextView t=button(title,surface);t.setTextSize(12);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,dp(38));p.leftMargin=dp(8);t.setPadding(dp(12),0,dp(12),0);row.addView(t,p);t.setOnClickListener(v->action.run());}
    private void resize(int diff){fontSize=Math.max(12,Math.min(32,fontSize+diff));editor.setTextSize(fontSize);}
    private void copy(){((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Belge",editor.getText().toString()));Toast.makeText(this,"Metin kopyalandı",Toast.LENGTH_SHORT).show();}
    private void updateHeader(){filenameLabel.setText(filename+(changed?"  •":""));state.setText(changed?"● Kaydedilmemiş değişiklikler":"● Kaydedildi / hazır");}
    private void updateCount(){String s=editor==null?"":editor.getText().toString();String trimmed=s.trim();int words=trimmed.isEmpty()?0:trimmed.split("\\s+").length;counter.setText(String.format(Locale.getDefault(),"%d kelime  ·  %d karakter",words,s.length()));}
    private void newDocument(){confirmDiscard(()->{currentUri=null;filename="Yeni Belge.docx";loading=true;editor.setText("");loading=false;changed=false;updateHeader();});}
    private void confirmDiscard(Runnable action){if(!changed){action.run();return;}new AlertDialog.Builder(this).setTitle("Kaydedilmemiş değişiklikler").setMessage("Değişiklikleri kaydetmeden devam etmek istiyor musunuz?").setNegativeButton("Vazgeç",null).setPositiveButton("Devam et",(d,w)->action.run()).show();}
    private void pickOpen(){confirmDiscard(()->{Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);intent.setType("*/*");intent.addCategory(Intent.CATEGORY_OPENABLE);intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(intent,OPEN);});}
    private String nameFor(Uri uri){String n=null;try(Cursor c=getContentResolver().query(uri,null,null,null,null)){if(c!=null&&c.moveToFirst()){int idx=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(idx>=0)n=c.getString(idx);}}catch(Exception ignored){}return n==null?"Belge.docx":n;}
    private void openUri(Uri uri){String name=nameFor(uri);try{String content=DocumentCodec.read(this,uri,name);currentUri=uri;filename=name;loading=true;editor.setText(content);editor.setSelection(0);loading=false;changed=false;updateHeader();Toast.makeText(this,"Belge açıldı",Toast.LENGTH_SHORT).show();}catch(Exception ex){loading=false;showError("Belge okunamadı",ex);}}
    private String saveName(){String ext=DocumentCodec.extension(filename);if(ext.equals("docx")||ext.equals("txt")||ext.equals("md")||ext.equals("html")||ext.equals("htm")||ext.equals("rtf"))return filename;int dot=filename.lastIndexOf('.');return (dot>0?filename.substring(0,dot):filename)+".docx";}
    private void save(boolean forcePicker){String name=saveName();if(!forcePicker&&currentUri!=null&&name.equals(filename)&&!name.endsWith(".docx")&&!name.endsWith(".odt")){write(currentUri,name);return;}Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType(name.endsWith(".docx")?"application/vnd.openxmlformats-officedocument.wordprocessingml.document":name.endsWith(".rtf")?"application/rtf":name.endsWith(".html")?"text/html":"text/plain");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,name);startActivityForResult(i,SAVE_AS);}
    private void write(Uri uri,String name){try{DocumentCodec.write(this,uri,name,editor.getText().toString());currentUri=uri;filename=name;changed=false;updateHeader();Toast.makeText(this,"Belge kaydedildi",Toast.LENGTH_LONG).show();}catch(Exception ex){showError("Kaydetme başarısız",ex);}}
    private void showError(String title,Exception ex){new AlertDialog.Builder(this).setTitle(title).setMessage(ex.getClass().getSimpleName()+": "+String.valueOf(ex.getMessage())).setPositiveButton("Tamam",null).show();}
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();if(req==OPEN){try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);}catch(Exception ignored){}openUri(uri);}else if(req==SAVE_AS)write(uri,saveName());}
    @Override public void onBackPressed(){confirmDiscard(()->MainActivity.super.onBackPressed());}
}
