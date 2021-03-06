/*
 * Copyright (C) 2014  Igor Bogoslavskyi
 * This file is part of LearnIt.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.learnit.LearnIt.activities;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.learnit.LearnIt.R;
import com.learnit.LearnIt.data_types.DBHelper;
import com.learnit.LearnIt.data_types.FactoryDbHelper;
import com.learnit.LearnIt.utils.StringUtils;
import com.learnit.LearnIt.utils.Utils;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class EditWord extends FragmentActivity {
    public static final String WORD_TAG = "word";
    public final String LOG_TAG = "my_logs";
    EditText edtWord;
    EditText edtTrans;
    String oldWord;
    Utils utils;

    private ImageButton btnClearWord;
    private ImageButton btnClearTrans;

    DBHelper dbHelper;

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_menu_cancel:
                finishActivity();
                return true;
            case R.id.edit_menu_done:
                Log.d(LOG_TAG, "update word = " + edtWord.getText().toString() + " trans = " + edtTrans.getText().toString());
                if (StringUtils.isStringEmpty(edtWord.getText().toString())
                        || StringUtils.isStringEmpty(edtTrans.getText().toString())) {
                    Crouton.makeText(this, getString(R.string.crouton_empty_input), Style.ALERT).show();
                } else {
                    dbHelper.deleteWord(oldWord);
                    int exitCode = dbHelper.writeToDB(edtWord.getText().toString(), edtTrans.getText().toString());
	                if (exitCode == DBHelper.EXIT_CODE_OK) {
		                Crouton.makeText(this, getString(R.string.crouton_word_saved, edtWord.getText().toString()), Style.CONFIRM).show();
	                } else if (exitCode == DBHelper.EXIT_CODE_WORD_ALREADY_IN_DB) {
                        Crouton.makeText(this, getString(R.string.crouton_word_already_present, edtWord.getText().toString()), Style.ALERT).show();
                    }
                    // TODO: this code is shitty. Rewrite when have time.
                    new CountDownTimer(2000, 2000) {

                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            finishActivity();
                        }
                    }.start();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        removeActionBarLabelIfNeeded();
        dbHelper = FactoryDbHelper.createDbHelper(this, DBHelper.DB_WORDS);
        utils = new Utils();
        oldWord = getIntent().getStringExtra(WORD_TAG);
        String translation = dbHelper.getTranslation(oldWord);
        Log.d(LOG_TAG, "got word to edit = " + oldWord + ", trans = " + translation);

        setContentView(R.layout.edit_word);

        edtWord = (EditText) findViewById(R.id.edtWord);
        edtTrans = (EditText) findViewById(R.id.edtTrans);
        edtWord.setText(oldWord);
        edtTrans.setText(translation);

        btnClearWord = (ImageButton) findViewById(R.id.btn_add_word_clear);
        btnClearTrans = (ImageButton) findViewById(R.id.btn_add_trans_clear);
        MyBtnTouchListener myBtnTouchListener = new MyBtnTouchListener();
        btnClearTrans.setOnClickListener(myBtnTouchListener);
        btnClearWord.setOnClickListener(myBtnTouchListener);

        edtWord.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString() != null && !editable.toString().equals("")) {
                    btnClearWord.setVisibility(View.VISIBLE);
                }
                if (editable.length() == 0) {
                    btnClearWord.setVisibility(View.INVISIBLE);
                }
            }
        });
        edtTrans.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString() != null && !editable.toString().equals("")) {
                    btnClearTrans.setVisibility(View.VISIBLE);
                }
                if (editable.length() == 0) {
                    btnClearTrans.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void finishActivity() {
        this.finish();
    }

    private void removeActionBarLabelIfNeeded() {
        ActionBar actionBar = this.getActionBar();
        if (actionBar == null) { return; }
        actionBar.setTitle("");
    }

    private class MyBtnTouchListener implements View.OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_add_trans_clear:
                    edtTrans.setText("");
                    v.setVisibility(View.INVISIBLE);
                    break;
                case R.id.btn_add_word_clear:
                    edtWord.setText("");
                    v.setVisibility(View.INVISIBLE);
                    break;
//                case R.id.btnCancel:
//                    finishActivity();
//                    break;
//                case R.id.btnOk:
//
//                    Log.d(LOG_TAG, "button ok clicked");
//                    Log.d(LOG_TAG, "update word = " + edtWord.getText().toString() + " trans = " + edtTrans.getText().toString());
//                    if (dbHelper.checkEmptyString(edtWord.getText().toString()) == DBHelper.EXIT_CODE_EMPTY_INPUT
//                            || dbHelper.checkEmptyString(edtTrans.getText().toString()) == DBHelper.EXIT_CODE_EMPTY_INPUT) {
//                        showMessage(DBHelper.EXIT_CODE_EMPTY_INPUT);
//                    } else {
//                        dbHelper.deleteWord(oldStrippedWord);
//                        Log.d(LOG_TAG, "button ok clicked");
//                        dbHelper.writeToDB(edtWord.getText().toString(), edtTrans.getText().toString());
//                        finishActivity();
//                    }
//                    break;
            }
        }
    }
}
