/*
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 */

/*
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/.
 */


package com.learnit.LearnIt.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.learnit.LearnIt.R;
import com.learnit.LearnIt.activities.HomeworkActivity;
import com.learnit.LearnIt.controllers.LearnHomeworkArticlesController;
import com.learnit.LearnIt.data_types.ArticleWordId;
import com.learnit.LearnIt.interfaces.IWorkerJobInput;
import com.learnit.LearnIt.utils.Constants;
import com.learnit.LearnIt.utils.MyAnimationHelper;
import com.learnit.LearnIt.utils.StringUtils;

import java.util.ArrayList;


public class ArticlesHomeworkFragment extends LearnFragment {
	public static final String TAG = "articles_homework_frag";

	private int[] _btnIds = {
			R.id.btn_first,
			R.id.btn_second,
			R.id.btn_third };

	@Override
	protected int[] btnIds() {
		return _btnIds;
	}

	public ArticlesHomeworkFragment(IWorkerJobInput worker) {
		super();
		_listener = new LearnHomeworkArticlesController(this, worker, btnIds());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		v = inflater.inflate(R.layout.homework_articles, container, false);
		_wordToAsk = (TextView) v.findViewById(R.id.word_to_ask);
		_wordToAsk.setMovementMethod(new ScrollingMovementMethod());
		for (int id: _btnIds) {
			(v.findViewById(id)).setOnClickListener(_listener);
		}
		setAll(View.INVISIBLE);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Bundle extras = getArguments();
		if (_listener instanceof LearnHomeworkArticlesController) {
			((LearnHomeworkArticlesController) _listener).getEverythingFromExtras(extras, this.getActivity());
		}
		// this looks hacky, but the articles should be set only once
		setButtonTexts(null, 0);
	}

	@Override
	public void openButtons() {
		MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
		View[] views = {
				v.findViewById(R.id.btn_first),
				v.findViewById(R.id.btn_second),
				v.findViewById(R.id.btn_third)};
		animationHelper.invokeForAllViews(views, R.anim.float_in_right, _listener);
	}

	@Override
	public void closeButtons() {
		MyAnimationHelper animationHelper = new MyAnimationHelper(this.getActivity());
		View[] views = {
				v.findViewById(R.id.btn_first),
				v.findViewById(R.id.btn_second),
				v.findViewById(R.id.btn_third)};
		animationHelper.invokeForAllViews(views, R.anim.float_away_right, _listener);
	}

	@Override
	public void setButtonTexts(ArrayList<ArticleWordId> words, int direction) {
		String[] articles = getString(R.string.articles_de).split("\\s");
		for (int i = 0; i < _btnIds.length; ++i) {
			((TextView) v.findViewById(_btnIds[i])).setText(articles[i]);
		}

	}

	@Override
	public void setQueryWordText(ArticleWordId struct, int direction) {
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
						queryWordTextView.setText(StringUtils.capitalize(queryWord));
					} else {
						queryWordTextView.setText(queryWord);
					}
				} else {
					Log.e(LOG_TAG, "No article present in article homework fragment.");
				}
				break;
			default: Log.e(LOG_TAG, "Wrong direction in article homework fragment.");
		}
	}

	public void stopActivity() {
		this.getActivity().finish();
	}

	public void askActivityToSwitchFragments(int homeworkType) {
		if (this.getActivity() instanceof HomeworkActivity) {
			((HomeworkActivity) this.getActivity()).replaceFragment(homeworkType);
		}
	}
}