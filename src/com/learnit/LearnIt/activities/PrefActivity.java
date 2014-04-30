
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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.learnit.LearnIt.R;
import com.learnit.LearnIt.services.NotificationService;
import com.learnit.LearnIt.utils.Utils;
import com.learnit.LearnIt.views.TimePreference;

public class PrefActivity extends PreferenceActivity {
    protected static boolean m_languages_changed = false;
    String selectedLanguageFrom;
    String selectedLanguageTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    this.setFinishOnTouchOutside(false);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new PrefsFragment1())
                .commit();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        selectedLanguageFrom = sp.getString(getString(R.string.key_language_from), "NONE");
        selectedLanguageTo = sp.getString(getString(R.string.key_language_to), "NONE");
    }

    public static class PrefsFragment1 extends PreferenceFragment {
        private final String LOG_TAG = "my_logs";
        ListPreference lstNotifFreq;
        ListPreference lstWayToLearn;
        ListPreference lstDirectionOfTrans;
        ListPreference lstNumOfWords;
        ListPreference lstLanguageToLearn;
        ListPreference lstLanguageYouKnow;
        CheckBoxPreference checkBoxPreference;
        TimePreference timePreference;
        boolean changed = false;

        boolean updated = false;
        boolean showArticlesOption = true;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref);
            lstNotifFreq = (ListPreference) findPreference(getString(R.string.key_notification_frequency));
            lstNotifFreq.setOnPreferenceChangeListener(listener);

            lstLanguageToLearn = (ListPreference) findPreference(getString(R.string.key_language_from));
            lstLanguageToLearn.setOnPreferenceChangeListener(listener);

            try {
                if (!lstLanguageToLearn.getValue().equals("de")) {
                    showArticlesOption = false;
                }
            } catch (Exception ex) {
                Log.d(LOG_TAG, "cought exception on pref start");
            }

            lstDirectionOfTrans = (ListPreference) findPreference(getString(R.string.key_direction_of_trans));
            lstDirectionOfTrans.setOnPreferenceChangeListener(listener);

            lstLanguageYouKnow = (ListPreference) findPreference(getString(R.string.key_language_to));
            lstLanguageYouKnow.setOnPreferenceChangeListener(listener);

            lstWayToLearn = (ListPreference) findPreference(getString(R.string.key_way_to_learn));
            if (showArticlesOption) {
                lstWayToLearn.setOnPreferenceChangeListener(listener);
            } else {
                PreferenceCategory mCategory = (PreferenceCategory) findPreference("prefs_main");    //TODO: change to R
                mCategory.removePreference(lstWayToLearn);
            }

            lstNumOfWords = (ListPreference) findPreference(getString(R.string.key_num_of_words));
            lstNumOfWords.setOnPreferenceChangeListener(listener);

            checkBoxPreference = (CheckBoxPreference) findPreference(getString(R.string.key_pref_notif_active));
            checkBoxPreference.setOnPreferenceChangeListener(listener);

            timePreference = (TimePreference) findPreference(getString(R.string.key_time_to_start));
	        timePreference.setOnPreferenceChangeListener(listener);


            updateAllSummaries();
        }

        void updateAllSummaries() {
	        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            if (checkBoxPreference.isChecked()) {
	            sp.edit().putBoolean(getString(R.string.key_pref_notif_active), true);
                checkBoxPreference.setSummary(R.string.pref_notifications_enabled);
            } else {
	            sp.edit().putBoolean(getString(R.string.key_pref_notif_active), false);
                checkBoxPreference.setSummary(R.string.pref_notifications_disabled);
            }
            if (lstNotifFreq.getEntry() != null)
                lstNotifFreq.setSummary(lstNotifFreq.getEntry().toString());
            if (lstLanguageToLearn.getEntry() != null)
                lstLanguageToLearn.setSummary(lstLanguageToLearn.getEntry().toString());
            if (lstLanguageYouKnow.getEntry() != null)
                lstLanguageYouKnow.setSummary(lstLanguageYouKnow.getEntry().toString());
            if (lstWayToLearn.getEntry() != null)
                lstWayToLearn.setSummary(lstWayToLearn.getEntry().toString());
            if (lstNumOfWords.getEntry() != null)
                lstNumOfWords.setSummary(lstNumOfWords.getEntry().toString());
            if (lstDirectionOfTrans.getEntry() != null)
                lstDirectionOfTrans.setSummary(lstDirectionOfTrans.getEntry().toString());
            if (lstWayToLearn.getValue().equals("2")) {
                lstDirectionOfTrans.setValue("2");
                lstDirectionOfTrans.setSummary(lstDirectionOfTrans.getEntries()[1]);
            } else
            {
	            lstDirectionOfTrans.setValue("3");
	            lstDirectionOfTrans.setSummary(lstDirectionOfTrans.getEntries()[2]);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            updateEnabled();
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d(LOG_TAG, "number of Words in Prefs = " + lstNumOfWords.getValue());
            updateNotificationTimer();
        }

        void updateEnabled() {
            boolean enabled = checkBoxPreference.isChecked();
            lstNotifFreq.setEnabled(enabled);
            lstNumOfWords.setEnabled(enabled);
            timePreference.setEnabled(enabled);
        }

        void updateNotificationTimer() {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
            if (changed) {
                boolean notif_enabled = sp.getBoolean(getString(R.string.key_pref_notif_active), false);
                if (notif_enabled) {
                    Utils.startRepeatingTimer(this.getActivity());
                } else {
                    cancelRepeatingTimer();
                }
            }
        }

        public void cancelRepeatingTimer() {
            Context context = this.getActivity();
            Intent intent = new Intent(context, NotificationService.class);
            PendingIntent sender = PendingIntent.getService(context, 0, intent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(sender);
            Toast.makeText(context, context.getString(R.string.toast_notif_stop_text), Toast.LENGTH_LONG).show();
        }


        OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference pref, Object newValue) {
                if (pref instanceof CheckBoxPreference) {
                    if (pref.getKey().equals(getString(R.string.key_pref_notif_active))) {
                        lstNotifFreq.setEnabled((Boolean) newValue);
                        timePreference.setEnabled(((Boolean) newValue));
                        lstNumOfWords.setEnabled((Boolean) newValue);
                        if ((Boolean) newValue) {
                            pref.setSummary(R.string.pref_notifications_enabled);
                        } else {
                            pref.setSummary(R.string.pref_notifications_disabled);
                        }
                        changed = true;
                        return true;
                    }
                } else if (pref instanceof ListPreference) {
                    if (pref.getKey().equals(getString(R.string.key_notification_frequency))) {
                        lstNotifFreq = (ListPreference) pref;
                        pref.setSummary(lstNotifFreq.getEntries()[lstNotifFreq.findIndexOfValue(newValue.toString())]);
                        changed = true;
                        updated = true;
                        return true;
                    } else if (pref.getKey().equals(getString(R.string.key_num_of_words))) {
                        lstNumOfWords = (ListPreference) pref;
                        pref.setSummary(lstNumOfWords.getEntries()[lstNumOfWords.findIndexOfValue(newValue.toString())]);
                        changed = true;
                        updated = true;
                        return true;
                    } else if (pref.getKey().equals(getString(R.string.key_language_from))) {
                        lstLanguageToLearn = (ListPreference) pref;
                        if (!newValue.toString().equals(lstLanguageToLearn.getValue())) {
                            pref.setSummary(lstLanguageToLearn.getEntries()[lstLanguageToLearn.findIndexOfValue(newValue.toString())]);
                            if (!newValue.toString().equals("de")) {
                                PreferenceCategory mCategory = (PreferenceCategory) findPreference("prefs_main");
                                lstWayToLearn.setValue("1");
                                mCategory.removePreference(lstWayToLearn);
                            } else {
                                PreferenceCategory mCategory = (PreferenceCategory) findPreference("prefs_main");
                                mCategory.addPreference(lstWayToLearn);
                            }
                            m_languages_changed = true;
                        }
                        return true;
                    } else if (pref.getKey().equals(getString(R.string.key_language_to))) {
                        lstLanguageYouKnow = (ListPreference) pref;
                        if (!newValue.toString().equals(lstLanguageYouKnow.getValue())) {
                            pref.setSummary(lstLanguageYouKnow.getEntries()[lstLanguageYouKnow.findIndexOfValue(newValue.toString())]);
                            m_languages_changed = true;
                        }
                        return true;
                    } else if (pref.getKey().equals(getString(R.string.key_way_to_learn))) {
                        lstWayToLearn = (ListPreference) pref;
                        pref.setSummary(lstWayToLearn.getEntries()[lstWayToLearn.findIndexOfValue(newValue.toString())]);
                        if (newValue.toString().equals("2")) {
                            lstDirectionOfTrans.setValue("2");
                            lstDirectionOfTrans.setSummary(lstDirectionOfTrans.getEntries()[1]);
                            lstDirectionOfTrans.setEnabled(false);
                        } else {
                            lstDirectionOfTrans.setEnabled(true);
                        }
                        changed = true;
                        updated = true;
                        return true;
                    } else if (pref.getKey().equals(getString(R.string.key_direction_of_trans))) {
                        lstDirectionOfTrans = (ListPreference) pref;
                        pref.setSummary(lstDirectionOfTrans.getEntries()[lstDirectionOfTrans.findIndexOfValue(newValue.toString())]);
                        Log.d(LOG_TAG, "changed!!!!");
                        return true;
                    }
                } else if (pref instanceof TimePreference) {
	                changed = true;
	                updated = true;
	                return true;
                }
                return false;
            }
        };
    }


}