package com.learnit.LearnIt.async_tasks;

import android.content.Context;

import com.learnit.LearnIt.data_types.DBHelper;
import com.learnit.LearnIt.fragments.WorkerFragment;
import com.learnit.LearnIt.utils.StringUtils;

import java.util.List;

public class GetWordsTask extends MySmartAsyncTask<List<String>> {
	String _word;

	public GetWordsTask(String word)
	{
		super();
		_word = word;
	}

	public void updateContextAndCallback(Context context,
	                                     WorkerFragment.OnTaskActionListener taskActionCallback)
	{
		super.updateContextAndCallback(context, taskActionCallback);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(List<String> dictName) {
		super.onPostExecute(dictName);
		if (dictName == null)
		{
			_taskActionCallback.onFail();
			return;
		}
		_taskActionCallback.onSuccess(dictName);

	}

	@Override
	protected List<String> doInBackground(Object... unused) {
		DBHelper dbHelperDict = new DBHelper(_context, DBHelper.DB_DICT_FROM);
		String newWord = StringUtils.stripFromArticle(_context, _word);
		return dbHelperDict.getHelpWords(newWord);
	}
}