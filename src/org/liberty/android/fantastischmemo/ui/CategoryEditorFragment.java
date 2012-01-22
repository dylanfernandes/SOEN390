package org.liberty.android.fantastischmemo.ui;

import java.sql.SQLException;

import java.util.List;

import org.liberty.android.fantastischmemo.AMActivity;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelper;
import org.liberty.android.fantastischmemo.AnyMemoDBOpenHelperManager;
import org.liberty.android.fantastischmemo.R;

import org.liberty.android.fantastischmemo.dao.CardDao;
import org.liberty.android.fantastischmemo.dao.CategoryDao;

import org.liberty.android.fantastischmemo.domain.Card;
import org.liberty.android.fantastischmemo.domain.Category;

import org.liberty.android.fantastischmemo.ui.CardEditor;

import android.app.Activity;

import android.content.Context;

import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;

public class CategoryEditorFragment extends DialogFragment implements View.OnClickListener {
    public static String EXTRA_DBPATH = "dbpath";
    public static String EXTRA_CARD_ID = "id";
    private AMActivity mActivity;
    private String dbPath;
    private CategoryDao categoryDao;
    private CardDao cardDao;
    private Card currentCard;
    private Integer currentCardId;
    private List<Category> categories;
    private static final String TAG = "CategoryEditorFragment";
    private CategoryAdapter categoryAdapter;
    private ListView categoryList;
    private Button okButton;
    private Button newButton;
    private Button deleteButton;
    private Button editButton;
    private EditText categoryEdit;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = (AMActivity)activity;
    }
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bundle args = this.getArguments();
        currentCardId = args.getInt(EXTRA_CARD_ID);
        dbPath = args.getString(EXTRA_DBPATH);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.category_dialog, container, false);
        categoryList = (ListView)v.findViewById(R.id.category_list);
        categoryList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        categoryList.setOnItemClickListener(listItemClickListener);
        InitTask initTask = new InitTask();
        initTask.execute((Void)null);
        newButton = (Button)v.findViewById(R.id.button_new);
        okButton = (Button)v.findViewById(R.id.button_ok);
        editButton = (Button)v.findViewById(R.id.button_edit);
        deleteButton = (Button)v.findViewById(R.id.button_delete);
        categoryEdit = (EditText)v.findViewById(R.id.category_dialog_edit);

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AnyMemoDBOpenHelperManager.releaseHelper(dbPath);
    }


    public void onClick(View v) {
        if (v == okButton) {
            SaveCardTask saveCardTask = new SaveCardTask();
            saveCardTask.execute((Void)null);
        }
        if (v == newButton) {
            NewCategoryTask task = new NewCategoryTask();
            task.execute((Void)null);
        }
        if (v == editButton) {
            EditCategoryTask task = new EditCategoryTask();
            task.execute((Void)null);
        }
        if (v == deleteButton) {
            DeleteCategoryTask task = new DeleteCategoryTask();
            task.execute((Void)null);
        }
    }

    /*
     * This task will mainly populate the categoryList
     */
    private class InitTask extends AsyncTask<Void, Void, Integer> {

		@Override
        public void onPreExecute() {
            mActivity.setProgressBarIndeterminateVisibility(true);
            categoryAdapter = new CategoryAdapter(mActivity, android.R.layout.simple_list_item_single_choice);
            assert categoryList != null : "Couldn't find categoryList view";
            assert categoryAdapter != null : "New adapter is null";
            categoryList.setAdapter(categoryAdapter);
        }

        @Override
        public Integer doInBackground(Void... params) {
            try {
                AnyMemoDBOpenHelper helper =
                    AnyMemoDBOpenHelperManager.getHelper(mActivity, dbPath);

                categoryDao = helper.getCategoryDao();
                cardDao = helper.getCardDao();
                currentCard = cardDao.queryForId(currentCardId);;
                categories = categoryDao.queryForAll();
            } catch (SQLException e) {
                Log.e(TAG, "Error creating daos", e);
                throw new RuntimeException("Dao creation error");
            }
            int categorySize = categories.size();
            assert categorySize > 0 : "There should be at least an empty category. Ensured in AnyMemoDBOpenHelper.";
            assert currentCard.getCategory() != null : "The card has null category, this should be avoided";
            Integer position = null;
            for (int i = 0; i < categorySize; i++) {
                if (categories.get(i).getName().equals(currentCard.getCategory().getName())) {
                    position = i;
                    break;
                }
            }
            assert position != null : "The card has no category. This shouldn't happen.";
            return position;
        }

        @Override
        public void onPostExecute(Integer pos){
            categoryAdapter.addAll(categories);
            categoryList.setItemChecked(pos, true);
            // This is needed to scroll to checked position
            categoryList.setSelection(pos);

            categoryEdit.setText(categoryAdapter.getItem(pos).getName());
            enableListeners();
            mActivity.setProgressBarIndeterminateVisibility(false);
        }
    }


    /*
     * This task will save the card and exit the dialog
     */
    private class SaveCardTask extends AsyncTask<Void, Category, Void> {
        private Category selectedCategory;

		@Override
        public void onPreExecute() {
            disableListeners();
            mActivity.setProgressBarIndeterminateVisibility(true);
            int position = categoryList.getCheckedItemPosition();
            if (position == AdapterView.INVALID_POSITION) {
                cancel(true);
                return;
            }
            selectedCategory = categoryAdapter.getItem(position);
        }

        @Override
        public Void doInBackground(Void... params) {
            assert selectedCategory != null : "Null category is selected. This shouldn't happen";
            try {
                currentCard.setCategory(selectedCategory);
                cardDao.update(currentCard);
            } catch (SQLException e) {
                Log.e(TAG, "Error updating the category of current card", e);
                throw new RuntimeException("Error updating the category of current card");
            }
            return null;
        }

        @Override
        public void onCancelled(){
            CategoryEditorFragment.this.dismiss();
        }

        @Override
        public void onPostExecute(Void result){
            mActivity.setProgressBarIndeterminateVisibility(false);
            mActivity.restartActivity();
        }
    }

    /*
     * This task will edit the category in the list
     */
    private class EditCategoryTask extends AsyncTask<Void, Category, Void> {
        private Category selectedCategory;
        private String editText;

		@Override
        public void onPreExecute() {
            disableListeners();
            mActivity.setProgressBarIndeterminateVisibility(true);
            editText = categoryEdit.getText().toString();
            int position = categoryList.getCheckedItemPosition();
            if (position == AdapterView.INVALID_POSITION || "".equals(editText)) {
                cancel(true);
                return;
            }
            selectedCategory = categoryAdapter.getItem(position);
            // Should deduplicate before editing the category
            // Point to the correct category if necessary.
            int categorySize = categories.size();
            for (int i = 0; i < categorySize; i++) {
                if (categories.get(i).getName().equals(editText)) {
                    position = i;
                    categoryList.setItemChecked(position, true);
                    categoryList.setSelection(position);
                    cancel(true);
                    return;
                }
            }
        }

        @Override
        public Void doInBackground(Void... params) {
            assert selectedCategory != null : "Null category is selected. This shouldn't happen";
            assert editText != null : "Category's EditText shouldn't get null";
            try {
                selectedCategory.setName(editText);
                currentCard.setCategory(selectedCategory);
                categoryDao.update(selectedCategory);
            } catch (SQLException e) {
                Log.e(TAG, "Error updating the category", e);
                throw new RuntimeException("Error updating the category");
            }
            return null;
        }

        @Override
        public void onCancelled(){
            enableListeners();
        }

        @Override
        public void onPostExecute(Void result){
            categoryAdapter.notifyDataSetChanged();
            mActivity.setProgressBarIndeterminateVisibility(false);
            enableListeners();
        }
    }

    private class NewCategoryTask extends AsyncTask<Void, Void, Category> {
        private String editText;

		@Override
        public void onPreExecute() {
            disableListeners();
            mActivity.setProgressBarIndeterminateVisibility(true);
            editText = categoryEdit.getText().toString();
            if ("".equals(editText)) {
                cancel(true);
                return;
            }
            int categorySize = categories.size();
            for (int i = 0; i < categorySize; i++) {
                if (categories.get(i).getName().equals(editText)) {
                    categoryList.setItemChecked(i, true);
                    categoryList.setSelection(i);
                    cancel(true);
                    return;
                }
            }
            assert editText != null : "Category's EditText shouldn't get null";
        }

        @Override
        public Category doInBackground(Void... params) {
            try {
                Category c = new Category();
                c.setName(editText);
                categoryDao.create(c);
                return c;
            } catch (SQLException e) {
                Log.e(TAG, "Error updating the category", e);
                throw new RuntimeException("Error updating the category");
            }
        }

        @Override
        public void onCancelled(){
            enableListeners();
        }

        @Override
        public void onPostExecute(Category result){
            categoryAdapter.add(result);
            categoryAdapter.notifyDataSetChanged();
            
            // Select and scroll to newly created category
            int lastPos = categoryAdapter.getCount() - 1;
            categoryList.setItemChecked(lastPos, true);
            categoryList.setSelection(lastPos);
            mActivity.setProgressBarIndeterminateVisibility(false);
            enableListeners();
        }
    }

    private class DeleteCategoryTask extends AsyncTask<Void, Void, Void> {
        private Category selectedCategory;

		@Override
        public void onPreExecute() {
            int position = categoryList.getCheckedItemPosition();
            if (position == AdapterView.INVALID_POSITION) {
                cancel(true);
                return;
            }
            selectedCategory = categoryAdapter.getItem(position);
            assert selectedCategory != null : "Null category selected!";
        }

        @Override
        public Void doInBackground(Void... params) {
            categoryDao.removeCategory(selectedCategory);
            return null;
        }

        @Override
        public void onCancelled(){
            enableListeners();
        }

        @Override
        public void onPostExecute(Void result){
            categoryAdapter.notifyDataSetChanged();
            
            // Move to first category (Uncategorized)
            categoryList.setItemChecked(0, true);
            categoryList.setSelection(0);
            categoryEdit.setText("");
            mActivity.setProgressBarIndeterminateVisibility(false);
            enableListeners();
        }
    }

    protected class CategoryAdapter extends ArrayAdapter<Category>{

        public CategoryAdapter(Context context, int textViewResourceId){
            super(context, textViewResourceId);
        }

        public void addAll(List<Category> lc) {
            for (Category c : lc) {
                add(c);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            CheckedTextView v = (CheckedTextView)convertView;
            if(v == null){
                LayoutInflater li = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                /* Reuse the filebrowser's resources */
                v = (CheckedTextView)li.inflate(android.R.layout.simple_list_item_single_choice, null);
            }
            Category item = getItem(position);
            if (item.getName().equals("")) {
                v.setText(R.string.uncategorized_text);
            } else {
                v.setText(item.getName());
            }
            return v;
        }
    }

    private OnItemClickListener listItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Category c = categoryAdapter.getItem(position);
            assert c != null : "Select a null category. This shouldn't happen";
            categoryEdit.setText(c.getName());
        }
    };

    private void enableListeners() {
        okButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        newButton.setOnClickListener(this);
        editButton.setOnClickListener(this);
    }

    private void disableListeners() {
        okButton.setOnClickListener(null);
        deleteButton.setOnClickListener(null);
        newButton.setOnClickListener(null);
        editButton.setOnClickListener(null);
    }
}
