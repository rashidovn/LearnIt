package com.learnit.LearnIt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    final String LOG_TAG = "my_logs";
    final static String DB_NAME = "myDB";
    final String WORD_COLUMN_NAME = "word";
    final String ID_COLUMN_NAME = "id";
    final String ARTICLE_COLUMN_NAME = "article";
    final String TRANSLATION_COLUMN_NAME = "translation";

    public static final int EXIT_CODE_EMPTY_INPUT = -10;
    public static final int EXIT_CODE_WORD_ALREADY_IN_DB = -11;
    public static final int EXIT_CODE_WORD_UPDATED = 1;
    public static final int EXIT_CODE_OK = 0;
    public static final int EXIT_CODE_WRONG_ARTICLE = -12;
    public static final int EXIT_CODE_WRONG_FORMAT = -13;

    private final String[] articles = new String[]{"der", "die", "das"};

    long maxId = 0;

    private Context mContext;

    SQLiteDatabase db;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "--- onCreate database ---");
        db.execSQL("CREATE TABLE " + DB_NAME + " (" + ID_COLUMN_NAME
                + " integer primary key autoincrement," + ARTICLE_COLUMN_NAME
                + " text," + WORD_COLUMN_NAME + " text,"
                + TRANSLATION_COLUMN_NAME + " text" + ");");
    }

    public boolean deleteWord(String word)
    {
        db = this.getWritableDatabase();
        db.delete(DB_NAME,WORD_COLUMN_NAME + "='" + word + "'", null);
        return true;
    }

    public long getDBSize() {
        db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + DB_NAME, null);
        long count = cursor.getCount();
        return count;
    }

    public Cursor getRandRow()
    {
        db = this.getReadableDatabase();
        return db.rawQuery("select * from " + DB_NAME + " order by random() limit 1", null);
    }

    boolean checkArticle(String article) {
        List<String> articlesList = Arrays.asList(articles);
        if (articlesList.contains(article.toLowerCase())) {
            return true;
        }
        return false;
    }

    public int writeToDB(String word, String translation) {
        try {
            List<String> wordsList = Arrays.asList(word.split(" "));
            ContentValues cv = new ContentValues();
            switch (wordsList.size()) {
                case 2:
                    if (checkArticle(wordsList.get(0))) {
                        cv.put(WORD_COLUMN_NAME, wordsList.get(1));
                        cv.put(ARTICLE_COLUMN_NAME, wordsList.get(0));
                    } else {
                        return EXIT_CODE_WRONG_ARTICLE;
                    }
                    break;
                case 1:
                    if (word.isEmpty()) {
                        return EXIT_CODE_EMPTY_INPUT;
                    }
                    cv.put(WORD_COLUMN_NAME, word);
                    cv.put(ARTICLE_COLUMN_NAME, "");
                    break;
                case 0:
                    return EXIT_CODE_EMPTY_INPUT;
                default:
                    return EXIT_CODE_WRONG_FORMAT;
            }
            long key = -1;
            boolean updatedFlag = false;
            db = this.getWritableDatabase();
            Cursor c = db.query(DB_NAME, new String[]{ID_COLUMN_NAME,
                    ARTICLE_COLUMN_NAME, WORD_COLUMN_NAME,
                    TRANSLATION_COLUMN_NAME}, WORD_COLUMN_NAME + " like "
                    + "'" + cv.getAsString(WORD_COLUMN_NAME) + "'", null, null,
                    null, null);
            if (c.getCount() != 0) {
                if (c.moveToFirst()) {
                    int wordColIndex = c.getColumnIndex(WORD_COLUMN_NAME);
                    int translationColIndex = c
                            .getColumnIndex(TRANSLATION_COLUMN_NAME);
                    int idColIndex = c.getColumnIndex(ID_COLUMN_NAME);
                    String tempWord = (c.getString(wordColIndex));
                    String trans = (c.getString(translationColIndex));
                    List<String> transInDBList = Arrays.asList(trans
                            .split(", "));
                    List<String> inputTransList = Arrays.asList(translation
                            .split(", "));
                    String tempTranslation = trans;
                    boolean anyNewValue = false;
                    for (int i = 0; i < inputTransList.size(); ++i) {
                        if (transInDBList.contains(inputTransList.get(i))) {
                            Log.d(LOG_TAG, "translation already in table... "
                                    + inputTransList.get(i));
                        } else {
                            tempTranslation += (", " + inputTransList.get(i));
                            anyNewValue = true;
                        }
                    }
                    translation = tempTranslation;
                    if (!anyNewValue) {
                        c.close();
                        return EXIT_CODE_WORD_ALREADY_IN_DB;
                    }
                    key = c.getLong(idColIndex);
                    updatedFlag = true;
                    Log.d(LOG_TAG, "word already in table = " + tempWord
                            + " translations = " + translation);

                }
            }
            cv.put(TRANSLATION_COLUMN_NAME, translation);
            if (!updatedFlag) {
                long rowID = db.insert(DB_NAME, null, cv);
                Log.d(LOG_TAG, "row inserted, ID = " + rowID + " rows total = "
                        + maxId);
                c.close();
                return EXIT_CODE_OK;
            } else {
                long rowID = db.update(DB_NAME, cv,
                        String.format("%s = ?", ID_COLUMN_NAME),
                        new String[]{"" + key});
                Log.d(LOG_TAG, "row updated, ID = " + rowID + " rows total = "
                        + maxId);
                c.close();
                return EXIT_CODE_WORD_UPDATED;
            }
        } catch (NullPointerException e) {
            Log.d(LOG_TAG, "got exception " + e.toString());
            return EXIT_CODE_EMPTY_INPUT;
        }

    }

    private boolean idInArray(long id,  List<Long> array)
    {
        for (long a:array)
        {
            if (id==a)
            {
                return true;
            }
        }
        return false;
    }



    public ArticleWordIdStruct getRandomWord(ArrayList<Long> ids, long maxId) {
        if (ids.size()>=maxId)
        {
            return null;
        }
        String word = null;
        String article = null;
        Cursor c;
        int id = 0;
        do {
            c = getRandRow();
            if (c.moveToFirst()) {
                int wordColIndex = c.getColumnIndex(WORD_COLUMN_NAME);
                int articleColIndex = c.getColumnIndex(ARTICLE_COLUMN_NAME);
                int idColIndex = c.getColumnIndex(ID_COLUMN_NAME);
                word = (c.getString(wordColIndex));
                article = (c.getString(articleColIndex));
                id = (c.getInt(idColIndex));
                Log.d(LOG_TAG, "randWord = " + article + " " + word);
                c.close();
            } else {
                Log.d(LOG_TAG, "0 rows");
                return null;
            }
        }
        while (idInArray(id,ids));
        ArticleWordIdStruct result = new ArticleWordIdStruct(article, word, id);
        return result;
    }

    public String getRandomTranslation(String testWord) {
        maxId = getDBSize();
        String word = null;
        String trans = null;
        do {
            Cursor c = getRandRow();
            if (c.moveToFirst()) {
                int transColIndex = c.getColumnIndex(TRANSLATION_COLUMN_NAME);
                int wordColIndex = c.getColumnIndex(WORD_COLUMN_NAME);
                word = (c.getString(wordColIndex));
                trans = (c.getString(transColIndex));
                Log.d(LOG_TAG, "randTrans = " + trans);
                c.close();
            } else {
                Log.d(LOG_TAG, "0 rows in rand trans for word " + testWord);
                return null;
            }
        }
        while (testWord.equals(word));
        return trans;
    }

    public boolean exportDB()
    {
        try {
            File sd = Environment.getExternalStorageDirectory();
            sd = new File(sd, "LearnIt");
            sd.mkdirs();
            if (sd.canWrite()) {
                String backupDBPath = "DB_Backup.db";
                File currentDB = mContext.getDatabasePath(DBHelper.DB_NAME);
                Log.d(LOG_TAG, "current db path = "+currentDB.getPath());
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
                else
                {
                    Log.d(LOG_TAG,"db not exist");
                }

                Log.d(LOG_TAG,"db exported to " + backupDB.getPath());
                Toast toast = Toast.makeText(mContext, String.format(mContext.getString(R.string.db_exported),backupDB.getPath()), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
            return true;
        } catch (Exception e) {
            Log.d(LOG_TAG, "export failed - " + e.getMessage());
            return false;
        }
    }

    public boolean importDB()
    {
        try {
            File sd = Environment.getExternalStorageDirectory();
            sd = new File(sd, "LearnIt");
            if (sd.canRead()) {
                String backupDBPath = "DB_Backup.db";
                File currentDB = mContext.getDatabasePath(DBHelper.DB_NAME);
                Log.d(LOG_TAG, "current db path = "+currentDB.getPath());
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel dst = new FileInputStream(currentDB).getChannel();
                    FileChannel src = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
                else
                {
                    Log.d(LOG_TAG,"db not exist");
                }

                Log.d(LOG_TAG,"db imported from " + backupDB.getPath());
                Toast toast = Toast.makeText(mContext, String.format(mContext.getString(R.string.db_exported),backupDB.getPath()), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
            return true;
        } catch (Exception e) {
            Log.d(LOG_TAG, "import failed - " + e.getMessage());
            return false;
        }
    }




    public ArrayList<String> getTranslations(String word) {
        db = this.getReadableDatabase();
        ArrayList<String> listItems = new ArrayList<String>();
        Cursor c = db.query(DB_NAME,
                new String[]{ID_COLUMN_NAME, ARTICLE_COLUMN_NAME,
                        WORD_COLUMN_NAME, TRANSLATION_COLUMN_NAME},
                WORD_COLUMN_NAME + " like " + "'%" + word + "%'", null, null,
                null, null);
        if (c.moveToFirst()) {
            int translationColIndex = c.getColumnIndex(TRANSLATION_COLUMN_NAME);
            do {
                listItems.add(c.getString(translationColIndex));
            } while (c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();
        return listItems;
    }

    public ArrayList<String> getWords(String word) {
        db = this.getWritableDatabase();
        ArrayList<String> listItems = new ArrayList<String>();
        Cursor c = db.query(DB_NAME,
                new String[]{ID_COLUMN_NAME, ARTICLE_COLUMN_NAME,
                        WORD_COLUMN_NAME, TRANSLATION_COLUMN_NAME},
                WORD_COLUMN_NAME + " like " + "'%" + word + "%'", null, null,
                null, null);
        if (c.moveToFirst()) {
            int wordColIndex = c.getColumnIndex(WORD_COLUMN_NAME);
            do {
                listItems.add(c.getString(wordColIndex));
            } while (c.moveToNext());
        } else
            Log.d(LOG_TAG, "0 rows");
        c.close();
        return listItems;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}