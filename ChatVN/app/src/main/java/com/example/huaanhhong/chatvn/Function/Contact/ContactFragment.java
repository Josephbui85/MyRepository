package com.example.huaanhhong.chatvn.Function.Contact;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.huaanhhong.chatvn.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ContactFragment extends Fragment {


    RecyclerView viewMain;
    private ContactRecycleAdapter contactRecycleAdapter;
    List<Contact> listcontact=new ArrayList<>();
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_contact, container, false);

        progressBar=(ProgressBar) view.findViewById(R.id.progress_loadconact);
        viewMain= (RecyclerView) view.findViewById(R.id.recycler_contact);
        LinearLayoutManager layout= new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        viewMain.setLayoutManager(layout);
        DividerItemDecoration divi= new DividerItemDecoration(getActivity(),layout.getOrientation());
        viewMain.addItemDecoration(divi);



        new loadContact().execute();

        return view;
    }



    public class loadContact extends AsyncTask<ArrayList<Contact>,ArrayList<Contact>,ArrayList<Contact>>{

        @Override
        protected ArrayList<Contact> doInBackground(ArrayList<Contact>... arrayLists) {
            GetArraylistContact getArraylistContact=new GetArraylistContact(getActivity());
            ArrayList<Contact> listcontact=new ArrayList();
            listcontact= (ArrayList<Contact>) getArraylistContact.readContact();
            return listcontact;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(ArrayList<Contact>... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(ArrayList<Contact> contacts) {
            super.onPostExecute(contacts);
             progressBar.setVisibility(View.GONE);
             viewMain.setVisibility(View.VISIBLE);
            Collections.sort(contacts);
            contactRecycleAdapter=new ContactRecycleAdapter(contacts,getActivity());
            contactRecycleAdapter.notifyDataSetChanged();
            viewMain.setAdapter(contactRecycleAdapter);
            ContactRecyclerItemDecorection contactRecyclerItemDecorection=new ContactRecyclerItemDecorection(40,true,getSelectionCallback(contacts));
            viewMain.addItemDecoration(contactRecyclerItemDecorection);
        }
    }



    private ContactRecyclerItemDecorection.SectionCalback getSelectionCallback(final List<Contact> listcontact) {
        return new ContactRecyclerItemDecorection.SectionCalback(){

            @Override
            public boolean isSection(int position) {
                return position==0||
                        listcontact.get(position).getName().charAt(0)
                                !=listcontact.get(position-1).getName().charAt(0);
            }

            @Override
            public String getSectionHeader(int position) {
                return (String) listcontact.get(position).getName().subSequence(0,1);
            }
        };
    }

}
