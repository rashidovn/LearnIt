
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

package com.learnit.LearnIt.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.learnit.LearnIt.R;
import com.learnit.LearnIt.activities.HomeworkActivity;
import com.learnit.LearnIt.controllers.LearnHomeworkTranslationController;
import com.learnit.LearnIt.data_types.ArticleWordId;
import com.learnit.LearnIt.interfaces.IWorkerJobInput;
import com.learnit.LearnIt.utils.Constants;
import com.learnit.LearnIt.utils.MyAnimationHelper;
import com.learnit.LearnIt.utils.StringUtils;
import com.learnit.LearnIt.utils.Utils;
import com.learnit.LearnIt.views.WordButton;

import java.util.ArrayList;


public class LearnHomeworkTranslationFragment extends LearnFragment {
	public static final String TAG = "homework_frag";

	private int[] _btnIds = Constants.btnIdsTranslations;

	@Override
	protected int[] btnIds() {
		return _btnIds;
	}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        IWorkerJobInput worker = Utils.getCurrentTaskScheduler(activity);
        _listener = new LearnHomeworkTranslationController(this, worker, btnIds());
        Bundle extras = getArguments();
        if (_listener instanceof LearnHomeworkTranslationController) {
            ((LearnHomeworkTranslationController) _listener).getEverythingFromExtras(extras, this.getActivity());
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.homework, container, false);
		updateDirectionOfTranslation();
		_wordToAsk = (TextView) v.findViewById(R.id.word_to_ask);
		_wordToAsk.setMovementMethod(new ScrollingMovementMethod());
        if (savedInstanceState == null || _listener == null) {
            setAll(View.INVISIBLE);
            if (_listener != null) {
                _listener.showNext();
                TextView btn;
                for (int id: _btnIds) {
                    btn = (TextView) v.findViewById(id);
                    btn.setOnClickListener(_listener);
                }
            }
        } else {
            String word = savedInstanceState.getString(WORD_TAG, "");
            if (!word.isEmpty()) {
                _wordToAsk.setText(word);
            }
            int idx = savedInstanceState.getInt(CORRECT_INDEX_TAG, -1);
            if (idx != -1) {
                _listener.setCorrectWordIdFromPrefs(idx);
            }
            TextView btn;
            int btnCounter = 0;
            for (int id: _btnIds) {
                btn = (TextView) v.findViewById(id);
                btn.setOnClickListener(_listener);
                String btnText = savedInstanceState.getString(BUTTON_PREFIX_TAG + btnCounter++, "");
                if (btnText.isEmpty()) {
                    btn.setEnabled(false);
                } else {
                    btn.setText(btnText);
                }
            }
            setAll(View.VISIBLE);
        }
		return v;
	}

	@Override
	public void openButtons() {
        if (!this.isAdded()) { return; }
		MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
		View[] viewsBottom = {
				v.findViewById(R.id.second_button_layout),
				v.findViewById(R.id.right_bottom_button),
				v.findViewById(R.id.left_bottom_button)};
		View[] viewsTop = {
				v.findViewById(R.id.first_button_layout),
				v.findViewById(R.id.left_top_button),
				v.findViewById(R.id.right_top_button)};
		animationHelper.invokeForAllViews(viewsBottom, R.anim.float_in_up_first_row, _listener);
		animationHelper.invokeForAllViews(viewsTop, R.anim.float_in_up_second_row, _listener);
	}

	@Override
	public void closeButtons() {
        if (!this.isAdded()) { return; }
		MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
		View[] viewsBottom = {
				v.findViewById(R.id.second_button_layout),
				v.findViewById(R.id.right_bottom_button),
				v.findViewById(R.id.left_bottom_button)};
		View[] viewsTop = {
				v.findViewById(R.id.first_button_layout),
				v.findViewById(R.id.left_top_button),
				v.findViewById(R.id.right_top_button)};
		animationHelper.invokeForAllViews(viewsBottom, R.anim.float_away_down_first_row, _listener);
		animationHelper.invokeForAllViews(viewsTop, R.anim.float_away_down_second_row, _listener);
	}

	@Override
	public void setButtonTexts(ArrayList<ArticleWordId> words, int direction) {
		if (direction > 0) { _direction = direction; }
		int showOnButton;
		switch (_direction) {
			case Constants.FROM_FOREIGN_TO_MY:
				showOnButton = WordButton.SHOW_TRANSLATION;
				break;
			case Constants.FROM_MY_TO_FOREIGN:
				showOnButton = WordButton.SHOW_WORD;
				break;
			default:
				showOnButton = 0; //won't show anything on button
		}

		// actually set the texts to the buttons
		for (int i = 0; i < btnIds().length; ++i) {
			WordButton tempButton = (WordButton) v.findViewById(btnIds()[i]);
			if (words != null && i < words.size() && showOnButton > 0) {
				tempButton.setEnabled(true);
				tempButton.setText(words.get(i), showOnButton);
			} else {
				tempButton.setEnabled(false);
				tempButton.setText("");
			}
		}
	}

	@Override
	public void setQueryWordText(ArticleWordId struct, int direction) {
        if (!this.isAdded()) { return; }
		TextView queryWordTextView = (TextView) v.findViewById(R.id.word_to_ask);
		queryWord = struct.word;
		if (direction > 0) { _direction = direction; }
		switch (_direction) {
			case Constants.FROM_FOREIGN_TO_MY:
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
				String learnLang = sp.getString(getString(R.string.key_language_from), "null");
				if (null != struct.article) {
					// a small hack for German and capitalization of all nouns
					if ("de".equals(learnLang)) {
						queryWordTextView.setText(struct.article + "\n" + StringUtils.capitalize(queryWord));
					} else {
						queryWordTextView.setText(struct.article + "\n" + queryWord);
					}
				} else if (null != struct.prefix) {
					queryWordTextView.setText(struct.prefix + " " + queryWord);
				} else {
					queryWordTextView.setText(queryWord);
				}
				break;
			case Constants.FROM_MY_TO_FOREIGN:
				queryWordTextView.setText(StringUtils.splitOnRegex(struct.translation, ",|\\s"));
				break;
		}
	}

	public void stopActivity() {
        if (!this.isAdded()) { return; }
        this.getActivity().finish();
	}

	public void askActivityToSwitchFragments(int homeworkType) {
		if (this.getActivity() instanceof HomeworkActivity) {
			((HomeworkActivity) this.getActivity()).replaceFragment(homeworkType);
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
			Utils.removeOldSavedValues(sp, Constants.btnIdsTranslations);
		}
	}
}