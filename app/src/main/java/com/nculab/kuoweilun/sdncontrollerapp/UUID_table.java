package com.nculab.kuoweilun.sdncontrollerapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class UUID_table {
    // 資料功能類別
    // 表格名稱
    public static final String TABLE_NAME = "UUID_table";
    // 其它表格欄位名稱
    public static final String UUID_COLUMN = "UUID";
    public static final String EVENT_COLUMN = "EVENT";
    public static final String SWITCH_ID_COLUMN = "SWITCH_ID";
    public static final String PORT_NO_COLUMN = "PORT_NO";
    public static final String SPEED_COLUMN = "SPEED";
    public static final String DURATION_COLUMN = "DURATION";
    // 使用上面宣告的變數建立表格的SQL指令
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    UUID_COLUMN + " TEXT, " +
                    EVENT_COLUMN + " TEXT, " +
                    SWITCH_ID_COLUMN + " TEXT, " +
                    PORT_NO_COLUMN + " TEXT, " +
                    SPEED_COLUMN + " TEXT, " +
                    DURATION_COLUMN + " TEXT)";
    // 資料庫物件
    private SQLiteDatabase db;

    // 建構子，一般的應用都不需要修改
    public UUID_table(Context context) {
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
        cv.put(UUID_COLUMN, item.getString("UUID"));
        try {
            cv.put(EVENT_COLUMN, item.getString("EVENT"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            cv.put(SWITCH_ID_COLUMN, item.getString("switch_id"));
            cv.put(PORT_NO_COLUMN, item.getString("port_no"));
            cv.put(SPEED_COLUMN, item.getString("speed"));
            cv.put(DURATION_COLUMN, item.getString("duration"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        cv.put(UUID_COLUMN, item.getString("UUID"));
        try {
            cv.put(EVENT_COLUMN, item.getString("EVENT"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            cv.put(SWITCH_ID_COLUMN, item.getString("switch_id"));
            cv.put(PORT_NO_COLUMN, item.getString("port_no"));
            cv.put(SPEED_COLUMN, item.getString("speed"));
            cv.put(DURATION_COLUMN, item.getString("duration"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String where = UUID_COLUMN + "=" + item.getString("UUID");
        // 設定修改資料的條件為編號
        // 執行修改資料並回傳修改的資料數量是否成功
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // 刪除參數指定編號的資料
    public boolean delete(String uuid) throws JSONException {
        // 設定條件為編號，格式為「欄位名稱=資料」
        String where = UUID_COLUMN + "=" + uuid;
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
    public JSONObject get(String uuid) throws JSONException {
        // 準備回傳結果用的物件
        JSONObject item = null;
        // 使用編號為查詢條件
        String where = UUID_COLUMN + "=" + uuid;
        // 執行查詢
        Cursor result = db.query(TABLE_NAME, null, uuid, null, null, null, null, null);
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

    // 取得指定編號的資料物件
    public JSONObject get(String switch_id, String port_no) throws JSONException {
        // 準備回傳結果用的物件
        JSONObject item = null;
        // 使用編號為查詢條件
        String where = SWITCH_ID_COLUMN + "=" + switch_id + " AND " + PORT_NO_COLUMN + "=" + port_no;
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
        result.put("UUID", cursor.getString(cursor.getColumnIndex(UUID_COLUMN)));
        try {
            result.put(EVENT_COLUMN, cursor.getString(cursor.getColumnIndex(EVENT_COLUMN)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            result.put(EVENT_COLUMN, cursor.getString(cursor.getColumnIndex(SWITCH_ID_COLUMN)));
            result.put(EVENT_COLUMN, cursor.getString(cursor.getColumnIndex(PORT_NO_COLUMN)));
            result.put(EVENT_COLUMN, cursor.getString(cursor.getColumnIndex(SPEED_COLUMN)));
            result.put(EVENT_COLUMN, cursor.getString(cursor.getColumnIndex(DURATION_COLUMN)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
