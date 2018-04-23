package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Token_table {
    // 資料功能類別
    // 表格名稱
    public static final String TABLE_NAME = "Token_table";
    // 其它表格欄位名稱
    public static final String TOKEN_COLUMN = "token";
    public static final String URL_COLUMN = "URL";
    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    TOKEN_COLUMN + " TEXT, " +
                    URL_COLUMN + " TEXT)";
    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public Token_table(Context context) {
        db = MyDbHelper.getDatabase(context);
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close() {
        db.close();
    }

    // 新增參數指定的物件
    public JSONObject insert(JSONObject item) throws JSONException {
        // 建立準備新增資料的ContentValues物件
        ContentValues cv = new ContentValues();
        // 加入ContentValues物件包裝的新增資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(TOKEN_COLUMN, item.getString("token"));
        cv.put(URL_COLUMN, item.getString("URL"));
        // 新增一筆資料並取得編號
        // 第一個參數是表格名稱
        // 第二個參數是沒有指定欄位值的預設值
        // 第三個參數是包裝新增資料的ContentValues物件
        long id = db.insert(TABLE_NAME, null, cv);
        // 設定編號
//        item.setId(id);
        // 回傳結果
        return item;
    }

    // 修改參數指定的物件
    public boolean update(JSONObject item) throws JSONException {
        // 建立準備修改資料的ContentValues物件
        ContentValues cv = new ContentValues();
        // 加入ContentValues物件包裝的修改資料
        // 第一個參數是欄位名稱， 第二個參數是欄位的資料
        cv.put(TOKEN_COLUMN, item.getString("token"));
        cv.put(URL_COLUMN, item.getString("URL"));
        String where = URL_COLUMN + "=" + item.getString("URL");
        // 設定修改資料的條件為編號
        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean delete(String URL) throws JSONException {
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = URL_COLUMN + "=" + URL;
        // 刪除指定編號資料並回傳刪除是否成功
        return db.delete(TABLE_NAME, where, null) > 0;
    }

    // 讀取所有記事資料
    public ArrayList<JSONObject> getAll() throws JSONException {
        ArrayList<JSONObject> result = new ArrayList<JSONObject>();
        //游標指向該資料表
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null, null);
        //將所有資料轉成Item並添加進List
        while (cursor.moveToNext()) {
            result.add(getRecord(cursor));
        }
        //關閉游標
        cursor.close();
        return result;
    }

    // 取得指定編號的資料物件
    public JSONObject get(String URL) throws JSONException {
        // 準備回傳結果用的物件
        JSONObject item = null;
        // 使用編號為查詢條件
        String where = URL_COLUMN + "=" + URL;
        // 執行查詢
        Cursor result = db.query(TABLE_NAME, null, where, null, null, null, null, null);
        // 如果有查詢結果
        if (result.moveToFirst()) {
            // 讀取包裝一筆資料的物件
            item = getRecord(result);
        }
        // 關閉Cursor物件
        result.close();
        // 回傳結果
        return item;
    }

    // 把游標Cursor取得的資料轉換成目前的資料包裝為物件
    public JSONObject getRecord(Cursor cursor) throws JSONException {
        // 準備回傳結果用的物件
        JSONObject result = new JSONObject();
        result.put("token", cursor.getString(cursor.getColumnIndex(TOKEN_COLUMN)));
        result.put("URL", cursor.getString(cursor.getColumnIndex(URL_COLUMN)));
        return result;
    }

    // 取得資料數量
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }
        return result;
    }

}
