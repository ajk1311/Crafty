package com.akausejr.crafty.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.akausejr.crafty.R;

/**
 *
 *
 * @author AJ Kause
 * Created on 7/19/14.
 */
public class SearchActivity extends Activity implements SearchView.OnQueryTextListener {

    private static final String TAG = SearchActivity.class.getSimpleName();

    private static final String THREAD_NAME = TAG + ".THREAD";

    private static final long USER_INPUT_DELAY = 500; // ms

    private SearchView.OnCloseListener mSearchCloseListener = new SearchView.OnCloseListener() {
        @Override
        public boolean onClose() {
            navigateUp();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.fragment_search, menu);
        final SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search_view).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnCloseListener(mSearchCloseListener);
        searchView.setOnQueryTextListener(this);
        searchView.setIconified(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateUp();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final boolean queryEmpty = TextUtils.isEmpty(query);
        if (queryEmpty) {
            // TODO hide fragments
            // TODO stop search service
        } else {
            // TODO start search service
        }
        return true;
    }

    private void navigateUp() {
        final Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(upIntent)
                .startActivities();
        } else {
            NavUtils.navigateUpTo(this, upIntent);
        }
    }
}
