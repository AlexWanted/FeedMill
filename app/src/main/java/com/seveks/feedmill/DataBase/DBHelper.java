package com.seveks.feedmill.DataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.seveks.feedmill.Fragments.MainSettingsFragment;
import com.seveks.feedmill.Outputs;
import com.seveks.feedmill.SettingsActivity;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = DBHelper.class.getSimpleName();

    public static final String DB_NAME = "settings",
            TBL_PRESETS = "presets",
            TBL_CONTROLLERS = "controllers",
            TBL_INS = "ins",
            TBL_OUTS = "outs",

            COL_PRESET_ID = "preset_id",
            COL_PRESET_NAME = "preset_name",

            COL_CONTROLLER_ID = "controller_id",
            COL_CONTROLLER_IP = "controller_ip",
            COL_CONTROLLER_NAME = "controller_name",

            COL_IN_ID = "in_id",
            COL_IN_NUMBER = "in_number",
            COL_IN_DESCRIPTION = "in_description",

            COL_OUT_ID = "out_id",
            COL_OUT_NUMBER = "out_number",
            COL_OUT_DESCRIPTION = "out_description";
    Context context;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String presetsTableCreationSQL = "CREATE TABLE "+TBL_PRESETS+" ("+
                COL_PRESET_ID+" integer primary key autoincrement, "+
                COL_PRESET_NAME+" text);";


        String controllersTableCreationSQL = "CREATE TABLE "+TBL_CONTROLLERS+" ("+
                COL_CONTROLLER_ID+" integer primary key autoincrement, "+
                COL_PRESET_ID+" integer, "+
                COL_CONTROLLER_IP+" text, "+
                COL_CONTROLLER_NAME+" text, "+
                "FOREIGN KEY("+COL_PRESET_ID+") REFERENCES "+TBL_PRESETS+"("+COL_PRESET_ID+"));";

        String insTableCreationSQL = "CREATE TABLE "+ TBL_INS +" ("+
                COL_IN_ID+" integer primary key autoincrement, "+
                COL_CONTROLLER_ID+" integer, "+
                COL_IN_NUMBER+" integer, "+
                COL_IN_DESCRIPTION+" text,"+
                "FOREIGN KEY("+COL_CONTROLLER_ID+") REFERENCES "+TBL_CONTROLLERS+"("+COL_CONTROLLER_ID+"));";

        String outsTableCreationSQL = "CREATE TABLE "+ TBL_OUTS +" ("+
                COL_OUT_ID+" integer primary key autoincrement, "+
                COL_CONTROLLER_ID+" integer, "+
                COL_OUT_NUMBER+" integer, "+
                COL_OUT_DESCRIPTION+" text,"+
                "FOREIGN KEY("+COL_CONTROLLER_ID+") REFERENCES "+TBL_CONTROLLERS+"("+COL_CONTROLLER_ID+"));";

        sqLiteDatabase.execSQL(presetsTableCreationSQL);
        sqLiteDatabase.execSQL(controllersTableCreationSQL);
        sqLiteDatabase.execSQL(insTableCreationSQL);
        sqLiteDatabase.execSQL(outsTableCreationSQL);
    }

    public int insertPreset(String presetName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PRESET_NAME, presetName);
        long presetId = db.insert(TBL_PRESETS, null, cv);
        cv.clear();
        db.close();
        Log.d(TAG, "insertController query: id = "+String.valueOf(presetId));
        return (int)presetId;
    }

    public int insertController(int presetId, String ip, String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PRESET_ID, presetId);
        cv.put(COL_CONTROLLER_NAME, name);
        cv.put(COL_CONTROLLER_IP, ip);
        long controllerId = db.insert(TBL_CONTROLLERS, null, cv);
        for (int i=1; i<=16; i++){
            insertIn((int)controllerId, i, String.valueOf(i));
            insertOut((int)controllerId, i, "Описание");
        }
        cv.clear();
        db.close();
        Log.d(TAG, "insertController query: id = "+String.valueOf(controllerId));
        return (int)controllerId;
    }

    public void insertIn(int controllerId, int number, String description){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CONTROLLER_ID, controllerId);
        cv.put(COL_IN_NUMBER, number);
        cv.put(COL_IN_DESCRIPTION, description);
        db.insert(TBL_INS, null, cv);
        cv.clear();
        db.close();
    }

    public void insertOut(int controllerId, int number, String description){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CONTROLLER_ID, controllerId);
        cv.put(COL_OUT_NUMBER, number);
        cv.put(COL_OUT_DESCRIPTION, description);
        db.insert(TBL_OUTS, null, cv);
        cv.clear();
        db.close();
    }

    public void removePreset(int presetId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT "+COL_CONTROLLER_ID+" "+
                                    "FROM "+TBL_CONTROLLERS+" "+
                                    "WHERE "+COL_PRESET_ID+" = ? ",new String[]{String.valueOf(presetId)});
        db = this.getWritableDatabase();
        if (c.moveToFirst()){
            do {
                int controllerId = c.getInt(c.getColumnIndex(COL_CONTROLLER_ID));
                db.delete(TBL_INS, COL_CONTROLLER_ID+" = ?", new String[]{String.valueOf(controllerId)});
            } while (c.moveToNext());
            db.delete(TBL_CONTROLLERS, COL_PRESET_ID+" = ?", new String[]{String.valueOf(presetId)});
        }
        c.close();
        int deletedRows = db.delete(TBL_PRESETS, COL_PRESET_ID+" = ?", new String[]{String.valueOf(presetId)});
        db.close();
        Log.d(TAG, "removeController query: rows = "+deletedRows);
    }
    public void removeController(int controllerId){
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TBL_INS, COL_CONTROLLER_ID+" = ?", new String[]{String.valueOf(controllerId)});
        int deletedRows = db.delete(TBL_CONTROLLERS, COL_CONTROLLER_ID+" = ?", new String[]{String.valueOf(controllerId)});
        db.close();
        Log.d(TAG, "removeController query: rows = "+deletedRows);
    }

    public void editPreset(int presetId, String presetName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_PRESET_NAME, presetName);
        int updatedRows = db.update(TBL_PRESETS, cv, COL_PRESET_ID+" = ? ", new String[]{String.valueOf(presetId)});
        cv.clear();
        db.close();
        Log.d(TAG, "editPreset query: rows = "+updatedRows);
    }
    public void editController(int controllerId, String ip, String name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CONTROLLER_IP, ip);
        cv.put(COL_CONTROLLER_NAME, name);
        int updatedRows = db.update(TBL_CONTROLLERS, cv, COL_CONTROLLER_ID+" = ? ", new String[]{String.valueOf(controllerId)});
        cv.clear();
        db.close();
        Log.d(TAG, "editController query: rows = "+updatedRows);
    }
    public void editIn(int inId, String inDescription){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_IN_DESCRIPTION, inDescription);
        int updatedRows = db.update(TBL_INS, cv, COL_IN_ID+" = ?", new String[]{String.valueOf(inId)});
        cv.clear();
        db.close();
        Log.d(TAG, "editOut query: rows = "+updatedRows);
    }
    public void editOut(int outId, String outDescription){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_OUT_DESCRIPTION, outDescription);
        int updatedRows = db.update(TBL_OUTS, cv, COL_OUT_ID+" = ?", new String[]{String.valueOf(outId)});
        cv.clear();
        db.close();
        Log.d(TAG, "editOut query: rows = "+updatedRows);
    }

    public ArrayList<Preset> getPresets(){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Preset> result = new ArrayList<>();
        Cursor presetsQuery = db.rawQuery("SELECT * FROM "+TBL_PRESETS, new String[]{});
        if (presetsQuery.moveToFirst()){
            do {
                int presetId = presetsQuery.getInt(presetsQuery.getColumnIndex(COL_PRESET_ID));
                String presetName = presetsQuery.getString(presetsQuery.getColumnIndex(COL_PRESET_NAME));
                result.add(new Preset(presetId, presetName));
            } while (presetsQuery.moveToNext());
        }
        presetsQuery.close();
        db.close();
        Log.d(TAG, "getPresets query: rows = "+result.size());
        return result;
    }

    public ArrayList<Controller> getControllers(int presetId){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Controller> result = new ArrayList<>();
        Cursor controllersQuery = db.rawQuery("SELECT * FROM "+TBL_CONTROLLERS+" " +
                                                   "WHERE "+COL_PRESET_ID+" = ? " +
                                                   "ORDER BY "+COL_CONTROLLER_IP,
                                                   new String[]{String.valueOf(presetId)});
        if (controllersQuery.moveToFirst()){
            do {
                int controllerId = controllersQuery.getInt(controllersQuery.getColumnIndex(COL_CONTROLLER_ID));
                String ip = controllersQuery.getString(controllersQuery.getColumnIndex(COL_CONTROLLER_IP));
                String name = controllersQuery.getString(controllersQuery.getColumnIndex(COL_CONTROLLER_NAME));
                result.add(new Controller(controllerId, ip, name));
            } while (controllersQuery.moveToNext());
        }
        controllersQuery.close();
        db.close();
        Log.d(TAG, "getControllers query: rows = "+result.size());
        return result;
    }

    public ArrayList<Outputs> getInsOuts(int controllerId){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Outputs> result = new ArrayList<>();
        Cursor insQuery = db.rawQuery("SELECT * FROM "+ TBL_INS +" " +
                        "WHERE "+COL_CONTROLLER_ID+" = ? " +
                        "ORDER BY "+COL_IN_NUMBER,
                new String[]{String.valueOf(controllerId)});
        Cursor outsQuery = db.rawQuery("SELECT * FROM "+ TBL_OUTS +" " +
                        "WHERE "+COL_CONTROLLER_ID+" = ? " +
                        "ORDER BY "+COL_OUT_NUMBER,
                new String[]{String.valueOf(controllerId)});
        if (insQuery.moveToFirst() && outsQuery.moveToFirst()){
            do {
                String inDescription = insQuery.getString(insQuery.getColumnIndex(COL_IN_DESCRIPTION));
                int number = insQuery.getInt(insQuery.getColumnIndex(COL_IN_NUMBER));
                int inID = insQuery.getInt(insQuery.getColumnIndex(COL_IN_ID));
                int outID = outsQuery.getInt(outsQuery.getColumnIndex(COL_OUT_ID));
                String outDescription = outsQuery.getString(outsQuery.getColumnIndex(COL_OUT_DESCRIPTION));
                result.add(new Outputs(inID, outID, outDescription, inDescription, number, false, false));
            } while (insQuery.moveToNext() && outsQuery.moveToNext());

        }
        insQuery.close();
        outsQuery.close();
        db.close();
        Log.d(TAG, "getInsOuts query: rows = "+result.size());
        return result;

    }

    /**
     * @return возвращает ноль если контроллера нет
     */
    public Controller getControllerById(int contrllerId){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ TBL_CONTROLLERS +" " +
                        "WHERE "+COL_CONTROLLER_ID+" = ? ",
                new String[]{String.valueOf(contrllerId)});
        Controller controller = null;
        if (cursor.moveToFirst()) {
            String controllerIp = cursor.getString(cursor.getColumnIndex(COL_CONTROLLER_IP));
            String controllerName = cursor.getString(cursor.getColumnIndex(COL_CONTROLLER_NAME));
            controller = new Controller(contrllerId, controllerIp, controllerName);
        }
        cursor.close();
        db.close();
        Log.d(TAG, "getControllerById query");
        return controller;
    }

    /**
     * @return возвращает ноль если контроллера нет
     */
    public Controller getControllerByIp(int presetId, String controllerIp) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ TBL_CONTROLLERS +" " +
                        "WHERE "+COL_CONTROLLER_IP+" = ? " +
                        "AND "+COL_PRESET_ID+" = ?",
                new String[]{controllerIp, String.valueOf(presetId)});
        Controller controller = null;
        if (cursor.moveToFirst()) {
            int controllerId = cursor.getInt(cursor.getColumnIndex(COL_CONTROLLER_ID));
            String controllerName = cursor.getString(cursor.getColumnIndex(COL_CONTROLLER_NAME));
            controller = new Controller(controllerId, controllerIp, controllerName);
        }
        cursor.close();
        db.close();
        Log.d(TAG, "getControllerByIp query");
        return controller;
    }
    /*
    public String getControllerIPByID(int controllerID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ TBL_CONTROLLERS +" " +
                        "WHERE "+COL_CONTROLLER_ID+" = ? ",
                new String[]{String.valueOf(controllerID)});
        String ip;
        if(cursor.moveToFirst()) {
            ip = cursor.getString(cursor.getColumnIndex(COL_CONTROLLER_IP));
        } else {
            ip = "";
        }
        cursor.close();
        return ip;
    }

    public String getControllerNameByID(int presetID, String ip) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ TBL_CONTROLLERS +" " +
                        "WHERE "+COL_CONTROLLER_IP+" = ? " +
                        "AND "+COL_PRESET_ID+" = ?",
                new String[]{ip, String.valueOf(presetID)});
        String name;
        if(cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(COL_CONTROLLER_NAME));
        } else {
            name = "";
        }
        cursor.close();
        Log.d(TAG, "getControllerNameByID = "+name);
        return name;
    }*/

    /*public ArrayList<In> getIns(int controllerId){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<In> result = new ArrayList<>();
        Cursor insQuery = db.rawQuery("SELECT * FROM "+ TBL_INS +" " +
                                               "WHERE "+COL_CONTROLLER_ID+" = ? " +
                                               "ORDER BY "+COL_IN_NUMBER,
                                               new String[]{String.valueOf(controllerId)});
        if (insQuery.moveToFirst()){
            do {
                int inId = insQuery.getInt(insQuery.getColumnIndex(COL_IN_ID));
                String inDescription = insQuery.getString(insQuery.getColumnIndex(COL_IN_DESCRIPTION));
                result.add(new In(inId, controllerId, inDescription));
            } while (insQuery.moveToNext());
        }
        return result;
    }

    public ArrayList<Out> getOuts(int controllerId){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<Out> result = new ArrayList<>();
        Cursor outsQuery = db.rawQuery("SELECT * FROM "+ TBL_OUTS +" " +
                        "WHERE "+COL_CONTROLLER_ID+" = ? " +
                        "ORDER BY "+COL_OUT_NUMBER,
                new String[]{String.valueOf(controllerId)});
        if (outsQuery.moveToFirst()){
            do {
                int outId = outsQuery.getInt(outsQuery.getColumnIndex(COL_OUT_ID));
                String outDescription = outsQuery.getString(outsQuery.getColumnIndex(COL_OUT_DESCRIPTION));
                result.add(new Out(outId, controllerId, outDescription));
            } while (outsQuery.moveToNext());
        }
        return result;
    }*/

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}
