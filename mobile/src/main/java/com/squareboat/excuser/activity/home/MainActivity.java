package com.squareboat.excuser.activity.home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;
import com.squareboat.excuser.R;
import com.squareboat.excuser.activity.BaseActivity;
import com.squareboat.excuser.activity.settings.SettingsActivity;
import com.squareboat.excuser.model.Contact;
import com.squareboat.excuser.utils.LocalStoreUtils;
import com.squareboat.excuser.utils.Utils;
import com.squareboat.excuser.widget.SpaceItemDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class MainActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ContactAdapter.Callbacks {

    private static final String TAG = "MainActivity";

    @BindView(R.id.layout_no_contacts)
    View mNoContactsLayout;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    @BindView(R.id.button_add_contact)
    FloatingActionButton mAddContact;

    private GoogleApiClient mGoogleApiClient;
    private ContactAdapter mContactAdapter;
    private List<Contact> mContacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        setupGoogleClient();
    }

    private void setupGoogleClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void initView(){
        int columnCount = getResources().getInteger(R.integer.item_column);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, columnCount);

        int spacing = Utils.dpToPx(2f, this);

        mRecyclerView.addItemDecoration(new SpaceItemDecoration(spacing));
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mContactAdapter = new ContactAdapter(mContacts);
        mContactAdapter.setCallbacks(this);
        mRecyclerView.setAdapter(mContactAdapter);
        refreshData();

        mAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddContactDialog.showDialog(getSupportFragmentManager(), null, mAddContactDialogCallback);
            }
        });
    }

    private void refreshData(){
        if(LocalStoreUtils.getContacts(this)!=null) {
            if(LocalStoreUtils.getContacts(this).size()>0) {
                showNoContent(false);
                mContacts.clear();
                mContacts.addAll(LocalStoreUtils.getContacts(this));
                mContactAdapter.notifyDataSetChanged();
            } else {
                showNoContent(true);
            }
        } else {
            showNoContent(true);
        }
    }

    private void showNoContent(boolean value){
        int noContactVisibility = value ? View.VISIBLE : View.GONE;
        mNoContactsLayout.setVisibility(noContactVisibility);

        int recyclerViewVisibility = value ? View.GONE : View.VISIBLE;
        mRecyclerView.setVisibility(recyclerViewVisibility);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();

        if ((mGoogleApiClient != null) && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected()");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): connection to location client suspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed(): " + connectionResult);
    }

    AddContactDialog.DialogCallback mAddContactDialogCallback = new AddContactDialog.DialogCallback() {

        @Override
        public void onContactAdded(Contact contact) {
            Log.e("onContactAdded", "->");
            if (LocalStoreUtils.getContacts(MainActivity.this) != null) {
                int size = LocalStoreUtils.getContacts(MainActivity.this).size();
                contact.setId(size);
            } else {
                contact.setId(0);
            }

            LocalStoreUtils.addContact(contact, MainActivity.this);
            refreshData();
        }

        @Override
        public void onContactUpdated(Contact contact) {
            Log.e("onContactUpdated", "->");
            LocalStoreUtils.updateContact(contact, MainActivity.this);
            refreshData();
        }
    };

    @Override
    public void onContactClick(Contact contact) {
        AddContactDialog.showDialog(getSupportFragmentManager(), contact, mAddContactDialogCallback);
    }

    @Override
    public void onContactDelete(Contact contact) {
        LocalStoreUtils.deleteContact(contact, this);
        refreshData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings: SettingsActivity.launchActivity(this); break;
        }

        return super.onOptionsItemSelected(item);
    }
}
