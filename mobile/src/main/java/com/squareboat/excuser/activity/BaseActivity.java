package com.squareboat.excuser.activity;

import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.squareboat.excuser.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by vipul on 10/05/17.
 */

public class BaseActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @BindView(R.id.coordinatorLayout)
    protected CoordinatorLayout coordinatorLayout;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        injectViews();

        //Displaying the back button in the action bar
        if (isDisplayHomeAsUpEnabled()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

    }

    protected void injectViews() {
        ButterKnife.bind(this);
        setupToolbar();
    }

    public void setContentViewWithoutInject(int layoutResId) {
        super.setContentView(layoutResId);
    }

    protected void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    public void setActivityTitle(int title) {
        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(title);
    }

    public void setActivityTitle(String title) {
        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Menu
        switch (item.getItemId()) {
            //When home is clicked
            case android.R.id.home:
                onActionBarHomeIconClicked();
                return true;

            default:
        }
        return super.onOptionsItemSelected(item);
    }

    //If back button is displayed in action bar, return false
    protected boolean isDisplayHomeAsUpEnabled() {
        return false;
    }

    public void setDisplayHomeAsUpEnabled(boolean value) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(value);
    }

    public void setHomeAsUpIndicator(Drawable drawable) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setHomeAsUpIndicator(drawable);
    }

    //Method for when home button is clicked
    public void onActionBarHomeIconClicked() {
        if (isDisplayHomeAsUpEnabled()) {
            onBackPressed();
        } else {
            finish();
        }
    }

    public void showSnackBar(String value) {
        Snackbar snackbar = Snackbar
                .make(getCoordinatorLayout(), value, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        snackbar.show();
    }

    public void showSnackBar(int value) {
        Snackbar snackbar = Snackbar
                .make(getCoordinatorLayout(), value, Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.colorAccent));
        snackbar.show();
    }

    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }

}
