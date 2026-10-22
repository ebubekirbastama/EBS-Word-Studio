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
    private final int ink=Color.rgb(236,241,251), muted=Color.rgb(150,163,187), bg=Color.rgb(11,17,31), surface=Color.rgb(24,33,52), accent=Color.rgb(92,130,255);
    private EditText editor;
    private TextView filenameLabel, counter, state;
    private Uri currentUri;
    private String filename="Yeni Belge.docx";
    private boolean changed=false, loading=false;
    private float fontSize=18;
    private LinearLayout root;
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);
        createUi();
        if (getIntent()!=null && Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData()!=null) openUri(getIntent().getData());
    }
    private int dp(float value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private TextView text(String s,int sp,boolean bold,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(color);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private void createUi() {
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(17),dp(12),dp(17),dp(12));root.setBackgroundColor(bg);setContentView(root);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);root.addView(top,new LinearLayout.LayoutParams(-1,dp(60)));
        ImageView appIcon=new ImageView(this);appIcon.setImageResource(R.drawable.ebs_app_icon);top.addView(appIcon,new LinearLayout.LayoutParams(dp(46),dp(46)));
        LinearLayout titleBox=new LinearLayout(this);titleBox.setOrientation(LinearLayout.VERTICAL);titleBox.setPadding(dp(12),0,0,0);top.addView(titleBox,new LinearLayout.LayoutParams(0,-2,1));
        titleBox.addView(text("EBS WORD STUDIO",18,true,ink));
        titleBox.addView(text("Belgeleriniz, her zaman yanınızda",11,false,muted));
        TextView plus=button("+",surface);top.addView(plus,new LinearLayout.LayoutParams(dp(44),dp(43)));plus.setOnClickListener(v->newDocument());
        ImageView brandLogo=new ImageView(this);brandLogo.setImageResource(R.drawable.ebs_logo);brandLogo.setAdjustViewBounds(true);LinearLayout.LayoutParams lpLogo=new LinearLayout.LayoutParams(-1,-2);lpLogo.topMargin=dp(10);root.addView(brandLogo,lpLogo);
        LinearLayout docCard=new LinearLayout(this);docCard.setOrientation(LinearLayout.VERTICAL);docCard.setPadding(dp(17),dp(12),dp(17),dp(13));docCard.setBackground(shape(surface,17));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,dp(15),0,dp(12));root.addView(docCard,cp);
        filenameLabel=text(filename,16,true,ink);filenameLabel.setSingleLine(true);filenameLabel.setEllipsize(TextUtils.TruncateAt.MIDDLE);docCard.addView(filenameLabel);
        state=text("●  Yeni belge  ·  DOCX / TXT / RTF / HTML / ODT",12,false,muted);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.topMargin=dp(5);docCard.addView(state,sp);
        LinearLayout actions=new LinearLayout(this);root.addView(actions,new LinearLayout.LayoutParams(-1,dp(49)));
        addAction(actions,"Aç",accent,()->pickOpen());addAction(actions,"Kaydet",Color.rgb(34,138,108),()->save(false));addAction(actions,"Farklı Kaydet",surface,()->save(true));
        LinearLayout toolbar=new LinearLayout(this);toolbar.setGravity(Gravity.CENTER_VERTICAL);LinearLayout.LayoutParams tb=new LinearLayout.LayoutParams(-1,dp(54));tb.topMargin=dp(12);root.addView(toolbar,tb);
        TextView editLabel=text("DÜZENLEYİCİ",11,true,muted);toolbar.addView(editLabel,new LinearLayout.LayoutParams(0,-2,1));
        smallAction(toolbar,"A−",()->resize(-2));smallAction(toolbar,"A+",()->resize(2));smallAction(toolbar,"Kopyala",()->copy());
        editor=new EditText(this);editor.setTextColor(ink);editor.setHintTextColor(muted);editor.setHint("Yazmaya başlayın veya bir Word dosyası açın…");editor.setTextSize(fontSize);editor.setGravity(Gravity.TOP|Gravity.START);editor.setPadding(dp(18),dp(19),dp(18),dp(19));editor.setBackground(shape(Color.rgb(29,39,58),18));editor.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);editor.setSingleLine(false);editor.setHorizontallyScrolling(false);editor.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,0,1);ep.bottomMargin=dp(12);root.addView(editor,ep);
        editor.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){if(!loading){changed=true;updateHeader();}updateCount();}public void afterTextChanged(Editable e){}});
        LinearLayout bottom=new LinearLayout(this);bottom.setGravity(Gravity.CENTER_VERTICAL);root.addView(bottom,new LinearLayout.LayoutParams(-1,dp(29)));
        counter=text("0 kelime · 0 karakter",12,false,muted);bottom.addView(counter,new LinearLayout.LayoutParams(0,-2,1));
        TextView info=text("ÇEVRİMDIŞI  •  YEREL",10,true,Color.rgb(83,190,151));bottom.addView(info);
        updateCount();
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
