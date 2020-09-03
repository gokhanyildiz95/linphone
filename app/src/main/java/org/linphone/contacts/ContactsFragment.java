/*
 * Copyright (c) 2010-2019 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.contacts;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.CrmInterface;
import org.linphone.DatabaseHelper;
import org.linphone.LinphoneManager;
import org.linphone.R;
import org.linphone.UnsafeOkHttpClient;
import org.linphone.call.views.LinphoneLinearLayoutManager;
import org.linphone.utils.SelectableHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ContactsFragment extends Fragment
        implements OnItemClickListener,
                ContactsUpdatedListener,
                ContactViewHolder.ClickListener,
                SelectableHelper.DeleteListener {
    private RecyclerView mContactsList;
    private TextView mNoSipContact, mNoContact;
    private ImageView mAllContacts;
    private ImageView mLinphoneContacts;
    private boolean mOnlyDisplayLinphoneContacts;
    private View mAllContactsSelected, mLinphoneContactsSelected;
    private int mLastKnownPosition;
    private SearchView mSearchView;
    private ProgressBar mContactsFetchInProgress;
    private LinearLayoutManager mLayoutManager;
    private Context mContext;
    private SelectableHelper mSelectionHelper;
    private CrmAdapter mContactAdapter;
    private SwipeRefreshLayout mContactsRefresher;
    private DatabaseHelper databaseHelper;
    public android.util.Log sysLog;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.contacts_list, container, false);
        mContext = getActivity().getApplicationContext();
        databaseHelper = new DatabaseHelper(mContext);
        mSelectionHelper = new SelectableHelper(view, this);
        mSelectionHelper.setDialogMessage(R.string.delete_contacts_text);

        mNoSipContact = view.findViewById(R.id.noSipContact);
        mNoContact = view.findViewById(R.id.noContact);
        mContactsList = view.findViewById(R.id.contactsList);

        mAllContacts = view.findViewById(R.id.all_contacts);
        mLinphoneContacts = view.findViewById(R.id.linphone_contacts);
        mAllContactsSelected = view.findViewById(R.id.all_contacts_select);
        mLinphoneContactsSelected = view.findViewById(R.id.linphone_contacts_select);
        mContactsFetchInProgress = view.findViewById(R.id.contactsFetchInProgress);
        ImageView newContact = view.findViewById(R.id.newContact);
        mContactsRefresher = view.findViewById(R.id.contactsListRefresher);

        mContactsRefresher.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        ContactsManager.getInstance().fetchContactsAsync();
                    }
                });

        mAllContacts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnlyDisplayLinphoneContacts = false;
                        mAllContactsSelected.setVisibility(View.VISIBLE);
                        mAllContacts.setEnabled(false);
                        mLinphoneContacts.setEnabled(true);
                        mLinphoneContactsSelected.setVisibility(View.INVISIBLE);
                        changeContactsAdapter();
                    }
                });

        mLinphoneContacts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mAllContactsSelected.setVisibility(View.INVISIBLE);
                        mLinphoneContactsSelected.setVisibility(View.VISIBLE);
                        mLinphoneContacts.setEnabled(false);
                        mAllContacts.setEnabled(true);
                        mOnlyDisplayLinphoneContacts = true;
                        changeContactsAdapter();
                    }
                });

        newContact.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ContactsActivity) getActivity()).showContactEdit(null);
                    }
                });

        if (getResources().getBoolean(R.bool.hide_non_linphone_contacts)) {
            mAllContacts.setEnabled(false);
            mLinphoneContacts.setEnabled(false);
            mOnlyDisplayLinphoneContacts = true;
            mAllContacts.setOnClickListener(null);
            mLinphoneContacts.setOnClickListener(null);
            mLinphoneContacts.setVisibility(View.INVISIBLE);
            mLinphoneContactsSelected.setVisibility(View.INVISIBLE);
        } else {
            mAllContacts.setEnabled(mOnlyDisplayLinphoneContacts);
            mLinphoneContacts.setEnabled(!mAllContacts.isEnabled());
        }
        newContact.setEnabled(LinphoneManager.getCore().getCallsNb() == 0);

        if (!ContactsManager.getInstance().contactsFetchedOnce()) {
            if (ContactsManager.getInstance().hasReadContactsAccess()) {
                mContactsFetchInProgress.setVisibility(View.VISIBLE);
            }
        } else {
            if (!mOnlyDisplayLinphoneContacts
                    && ContactsManager.getInstance().getContacts().isEmpty()) {
                mNoContact.setVisibility(View.VISIBLE);
            } else if (mOnlyDisplayLinphoneContacts
                    && ContactsManager.getInstance().getSIPContacts().isEmpty()) {
                mNoSipContact.setVisibility(View.VISIBLE);
            }
        }

        mSearchView = view.findViewById(R.id.searchField);
        mSearchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        searchContacts(newText);
                        return true;
                    }
                });

        mLayoutManager = new LinphoneLinearLayoutManager(mContext);
        mContactsList.setLayoutManager(mLayoutManager);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(
                        mContactsList.getContext(), mLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(
                getActivity().getResources().getDrawable(R.drawable.divider));
        mContactsList.addItemDecoration(dividerItemDecoration);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
        /** Add call phone function here * */
        /* CrmAdapter contact = (CrmAdapter) adapter.getItemAtPosition(position);
        mLastKnownPosition = mLayoutManager.findFirstVisibleItemPosition();
        ((ContactsActivity) getActivity()).showContactDetails(contact); */
    }

    @Override
    public void onItemClicked(int position) {
        /*  CrmAdapter contact = (CrmAdapter) mContactAdapter.getItem(position);

        if (mContactAdapter.isEditionEnabled()) {
            mContactAdapter.toggleSelection(position);
        } else {
            mLastKnownPosition = mLayoutManager.findFirstVisibleItemPosition();
           ((ContactsActivity) getActivity()).showContactDetails(contact);
        } */
    }

    @Override
    public boolean onItemLongClicked(int position) {
        /*
        if (!mContactAdapter.isEditionEnabled()) {
            mSelectionHelper.enterEditionMode();
        }
        mContactAdapter.toggleSelection(position); */
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        ContactsManager.getInstance().addContactsListener(this);

        mOnlyDisplayLinphoneContacts =
                ContactsManager.getInstance().isLinphoneContactsPrefered()
                        || getResources().getBoolean(R.bool.hide_non_linphone_contacts);

        changeContactsToggle();
        invalidate();

        ((ContactsActivity) (getActivity())).showTabBar();
    }

    @Override
    public void onPause() {
        ContactsManager.getInstance().removeContactsListener(this);
        super.onPause();
    }

    @Override
    public void onContactsUpdated() {
        /*if (mContactAdapter != null) {
            mContactAdapter.updateDataSet(
                    mOnlyDisplayLinphoneContacts
                            ? ContactsManager.getInstance().getSIPContacts()
                            : ContactsManager.getInstance().getContacts());
            mContactAdapter.notifyDataSetChanged();

            if (mContactAdapter.getItemCount() > 0) {
                mNoContact.setVisibility(View.GONE);
                mNoSipContact.setVisibility(View.GONE);
            }
        }
        mContactsFetchInProgress.setVisibility(View.GONE);
        mContactsRefresher.setRefreshing(false); */
    }

    @Override
    public void onDeleteSelection(Object[] objectsToDelete) {
        /** Update delete function with your own endpoint * */
        /*ArrayList<String> ids = new ArrayList<>();
        int size = mContactAdapter.getSelectedItemCount();
        for (int i = size - 1; i >= 0; i--) {
            LinphoneContact contact = (LinphoneContact) objectsToDelete[i];
            if (contact.isAndroidContact()) {
                contact.deleteFriend();
                ids.add(contact.getAndroidId());
            } else {
                contact.delete();
            }
        }
        ContactsManager.getInstance().deleteMultipleContactsAtOnce(ids); */
    }

    private void searchContacts(String search) {
        boolean isEditionEnabled = false;
        if (search == null || search.isEmpty()) {
            changeContactsAdapter();
            return;
        }
        changeContactsToggle();

        ArrayList<CrmContacts> listContact = new ArrayList<CrmContacts>();
        // listContact.add(new CrmContacts("Test","Test","Test","Test","Test","Test","Test"));

        /*  if (mOnlyDisplayLinphoneContacts) {
            listContact = ContactsManager.getInstance().getSIPContacts(search);
        } else {
            listContact = ContactsManager.getInstance().getContacts(search);
        } */
        if (mContactAdapter != null && mContactAdapter.isEditionEnabled()) {
            isEditionEnabled = true;
        }

        /** Fetch data from server and put listContact as a your data * */
        mContactAdapter = new CrmAdapter(mContext, listContact, this, mSelectionHelper);
        mContactAdapter.setIsSearchMode(true);

        mSelectionHelper.setAdapter(mContactAdapter);
        if (isEditionEnabled) {
            mSelectionHelper.enterEditionMode();
        }
        mContactsList.setAdapter(mContactAdapter);

        HashMap<String, String> userData = databaseHelper.getUser();
        String domainId = userData.get("domain_id");
        String jwt = userData.get("jwt");
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl("https://" + domainId + ".mobikob.com/")
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                        .build();

        CrmInterface api = retrofit.create(CrmInterface.class);
        Call<String> call = api.getCustomerList(null, null, null, search, jwt);
        call.enqueue(
                new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response.body());
                                    JSONArray dataArray = jsonObject.getJSONArray("data");
                                    for (int i = 0; i < dataArray.length(); i++) {
                                        if (!dataArray
                                                .getJSONObject(i)
                                                .getString("name")
                                                .equals("")) {
                                            listContact.add(
                                                    new CrmContacts(
                                                            null,
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("name"),
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("phone"),
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("email"),
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("property"),
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("create_date"),
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("tenant")));
                                            sysLog.d(
                                                    "MOBILOG",
                                                    String.valueOf(
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("name")));
                                        }
                                    }
                                    mContactAdapter.notifyDataSetChanged();
                                    if (mContactAdapter.getItemCount() == 0) {
                                        mNoContact.setVisibility(View.VISIBLE);
                                    } else if (mContactAdapter.getItemCount() == 0) {
                                        mNoSipContact.setVisibility(View.VISIBLE);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                String jsonresponse = response.body().toString();
                                sysLog.d("MOBILOG", "get crm list json resp " + jsonresponse);
                            } else {
                                sysLog.d(
                                        "MOBILOG",
                                        "get crm list onResponse() : Returned empty response");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        sysLog.d("MOBILOG", "crm list on fail ");
                    }
                });
    }

    private void changeContactsAdapter() {
        changeContactsToggle();
        ArrayList<CrmContacts> listContact = new ArrayList<CrmContacts>();
        mNoSipContact.setVisibility(View.GONE);
        mNoContact.setVisibility(View.GONE);
        mContactsList.setVisibility(View.VISIBLE);
        boolean isEditionEnabled = false;
        String query = mSearchView.getQuery().toString();
        /* if (query.equals("")) {
            if (mOnlyDisplayLinphoneContacts) {
                listContact = ContactsManager.getInstance().getSIPContacts();
            } else {
                listContact = ContactsManager.getInstance().getContacts();
            }
        } else {
            if (mOnlyDisplayLinphoneContacts) {
                listContact = ContactsManager.getInstance().getSIPContacts(query);
            } else {
                listContact = ContactsManager.getInstance().getContacts(query);
            }
        } */

        if (mContactAdapter != null && mContactAdapter.isEditionEnabled()) {
            isEditionEnabled = true;
        }

        mContactAdapter = new CrmAdapter(mContext, listContact, this, mSelectionHelper);

        mSelectionHelper.setAdapter(mContactAdapter);

        if (isEditionEnabled) {
            mSelectionHelper.enterEditionMode();
        }
        mContactsList.setAdapter(mContactAdapter);

        HashMap<String, String> userData = databaseHelper.getUser();
        String domainId = userData.get("domain_id");
        String jwt = userData.get("jwt");
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl("https://" + domainId + ".mobikob.com/")
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                        .build();

        CrmInterface api = retrofit.create(CrmInterface.class);
        Call<String> call = api.getCustomerList(null, null, null, query, jwt);
        call.enqueue(
                new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(response.body());
                                    JSONArray dataArray = jsonObject.getJSONArray("data");
                                    for (int i = 0; i < dataArray.length(); i++) {
                                        if (!dataArray
                                                .getJSONObject(i)
                                                .getString("name")
                                                .equals("")) {
                                            listContact.add(
                                                    new CrmContacts(
                                                            null,
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("name"),
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("phone"),
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("email"),
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("property"),
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("create_date"),
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("tenant")));
                                            sysLog.d(
                                                    "MOBILOG",
                                                    String.valueOf(
                                                            dataArray
                                                                    .getJSONObject(i)
                                                                    .getString("name")));
                                        }
                                    }
                                    mContactAdapter.notifyDataSetChanged();
                                    if (mContactAdapter.getItemCount() == 0) {
                                        mNoContact.setVisibility(View.VISIBLE);
                                    } else if (mContactAdapter.getItemCount() == 0) {
                                        mNoSipContact.setVisibility(View.VISIBLE);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                String jsonresponse = response.body().toString();
                                sysLog.d("MOBILOG", "get crm list json resp " + jsonresponse);
                            } else {
                                sysLog.d(
                                        "MOBILOG",
                                        "get crm list onResponse() : Returned empty response");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        sysLog.d("MOBILOG", "crm list on fail ");
                    }
                });

        /*   if (!mOnlyDisplayLinphoneContacts && mContactAdapter.getItemCount() == 0) {
            mNoContact.setVisibility(View.VISIBLE);
        } else if (mOnlyDisplayLinphoneContacts && mContactAdapter.getItemCount() == 0) {
            mNoSipContact.setVisibility(View.VISIBLE);
        } */
    }

    private void changeContactsToggle() {
        if (mOnlyDisplayLinphoneContacts
                && !getResources().getBoolean(R.bool.hide_non_linphone_contacts)) {
            mAllContacts.setEnabled(true);
            mAllContactsSelected.setVisibility(View.INVISIBLE);
            mLinphoneContacts.setEnabled(false);
            mLinphoneContactsSelected.setVisibility(View.VISIBLE);
        } else {
            mAllContacts.setEnabled(false);
            mAllContactsSelected.setVisibility(View.VISIBLE);
            mLinphoneContacts.setEnabled(true);
            mLinphoneContactsSelected.setVisibility(View.INVISIBLE);
        }
    }

    private void invalidate() {
        if (mSearchView != null && mSearchView.getQuery().toString().length() > 0) {
            searchContacts(mSearchView.getQuery().toString());
        } else {
            changeContactsAdapter();
        }
        mContactsList.scrollToPosition(mLastKnownPosition);
    }
}
