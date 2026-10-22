package com.ebs.wordstudio;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.zip.*;

/** Offline text editor codec. DOCX import is plain text, not fidelity-preserving. */
public final class DocumentCodec {
    private DocumentCodec() {}
    private static final int MAX_PART_BYTES = 24 * 1024 * 1024;
    public static String read(Context context, Uri uri, String filename) throws Exception {
        String ext=extension(filename);
        try(InputStream raw=context.getContentResolver().openInputStream(uri)) {
            if(raw==null)throw new IOException("Dosya erişimi sağlanamadı. Dosyayı telefona indirip tekrar açın.");
            BufferedInputStream input=new BufferedInputStream(raw);
            input.mark(8);
            byte[] signature=new byte[4];int n=input.read(signature);input.reset();
            boolean zip=n==4 && signature[0]=='P' && signature[1]=='K';
            if(ext.equals("docx")||ext.equals("odt")||zip) {
                if(!zip) throw new IOException("Bu dosya gerçek bir DOCX/ODT arşivi değil. Eski DOC dosyası olabilir.");
                // Use Android's native XmlPullParser. DOM parser features may be unsupported on devices.
                return readOfficeZip(input,ext.equals("odt")?"content.xml":"word/document.xml",ext.equals("odt"));
            }
            if(ext.equals("doc"))throw new IOException("Eski .doc formatı desteklenmiyor. DOCX olarak dönüştürün.");
            byte[] data=readAll(input,MAX_PART_BYTES);
            String s=new String(data,StandardCharsets.UTF_8);
            if(ext.equals("html")||ext.equals("htm"))return Html.fromHtml(s,Html.FROM_HTML_MODE_LEGACY).toString().trim();
            if(ext.equals("rtf"))return parseRtf(s);
            return s;
        }
    }
    private static String readOfficeZip(InputStream input,String part,boolean odt) throws Exception {
        try(ZipInputStream zip=new ZipInputStream(input)) {
            ZipEntry e;
            while((e=zip.getNextEntry())!=null) {
                if(e.getName().equals(part))return parseOfficeXml(readAll(zip,MAX_PART_BYTES),odt);
                zip.closeEntry();
            }
        } catch(ZipException ex) { throw new IOException("Word dosyasının arşiv yapısı okunamıyor: "+ex.getMessage(),ex); }
        throw new IOException("Word belgesinin metin bölümü bulunamadı ("+part+").");
    }
    private static String parseOfficeXml(byte[] data,boolean odt) throws Exception {
        XmlPullParser p=Xml.newPullParser();
        p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,true);
        p.setInput(new ByteArrayInputStream(data),"UTF-8");
        StringBuilder out=new StringBuilder();
        boolean inParagraph=false, inText=false, wroteParagraph=false;
        int pDepth=-1, textDepth=-1;
        for(int event=p.getEventType();event!=XmlPullParser.END_DOCUMENT;event=p.next()) {
            if(event==XmlPullParser.START_TAG) {
                String name=p.getName();
                if((name.equals("p")||odt&&name.equals("h"))&&!inParagraph) {
                    if(wroteParagraph)out.append('\n');
                    inParagraph=true; wroteParagraph=true; pDepth=p.getDepth();
                } else if(inParagraph) {
                    if(name.equals(odt?"line-break":"br")||name.equals("cr"))out.append('\n');
                    else if(name.equals("tab"))out.append('\t');
                    else if(odt&&name.equals("s")) {
                        String count=p.getAttributeValue(null,"c"); int spaces=1;
                        try {if(count!=null) spaces=Math.min(100,Integer.parseInt(count));}catch(NumberFormatException ignored){}
                        for(int i=0;i<spaces;i++)out.append(' ');
                    }
                    if(!odt&&name.equals("t")) {inText=true;textDepth=p.getDepth();}
                }
            } else if(event==XmlPullParser.TEXT) {
                if(inParagraph&&(odt||inText))out.append(p.getText());
            } else if(event==XmlPullParser.END_TAG) {
                if(!odt&&inText&&p.getDepth()==textDepth)inText=false;
                if(inParagraph&&p.getDepth()==pDepth)inParagraph=false;
            }
        }
        if(!wroteParagraph)throw new IOException("Bu Word belgesinde okunabilir paragraf metni bulunamadı.");
        return out.toString();
    }
    private static String parseRtf(String s) {
        return s.replaceAll("\\\\par\\b ?","\n").replaceAll("\\\\line\\b ?","\n")
                .replaceAll("\\\\tab\\b ?","\t").replaceAll("\\\\'[0-9a-fA-F]{2}","")
                .replaceAll("\\\\[a-zA-Z]+-?\\d* ?","").replaceAll("[{}]","").trim();
    }
    public static String extension(String filename) {
        if(filename==null)return "";int dot=filename.lastIndexOf('.');return dot<0?"":filename.substring(dot+1).toLowerCase(Locale.ROOT);
    }
    public static void write(Context context,Uri uri,String name,String content) throws Exception {
        String ext=extension(name);
        try(OutputStream stream=context.getContentResolver().openOutputStream(uri,"wt")) {
            if(stream==null)throw new IOException("Dosyaya yazma izni alınamadı.");
            if(ext.equals("docx"))writeDocx(stream,content);
            else if(ext.equals("txt")||ext.equals("md"))stream.write(content.getBytes(StandardCharsets.UTF_8));
            else if(ext.equals("html")||ext.equals("htm"))stream.write(("<!doctype html><html lang=\"tr\"><meta charset=\"utf-8\"><body><div style=\"white-space:pre-wrap\">"+esc(content)+"</div></body></html>").getBytes(StandardCharsets.UTF_8));
            else if(ext.equals("rtf"))stream.write(("{\\rtf1\\ansi\\uc1 " + content.replace("\\","\\\\").replace("{","\\{").replace("}","\\}").replace("\n","\\par ") + "}").getBytes(StandardCharsets.UTF_8));
            else throw new IOException("Bu formata kaydetme desteklenmiyor: "+ext);
        }
    }
    public static void writeDocx(OutputStream target,String content) throws IOException {
        try(ZipOutputStream zip=new ZipOutputStream(target)) {
            add(zip,"[Content_Types].xml","<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/></Types>");
            add(zip,"_rels/.rels","<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>");
            StringBuilder doc=new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>");
            for(String paragraph:content.split("\n",-1))doc.append("<w:p><w:r><w:t xml:space=\"preserve\">").append(esc(paragraph)).append("</w:t></w:r></w:p>");
            doc.append("<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\"/></w:sectPr></w:body></w:document>");
            add(zip,"word/document.xml",doc.toString());zip.finish();
        }
    }
    private static String esc(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");}
    private static void add(ZipOutputStream zip,String name,String content)throws IOException{zip.putNextEntry(new ZipEntry(name));zip.write(content.getBytes(StandardCharsets.UTF_8));zip.closeEntry();}
    private static byte[] readAll(InputStream in,int max)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>max)throw new IOException("Belge metni çok büyük (24 MB sınırı).");out.write(b,0,n);}return out.toByteArray();}
}
