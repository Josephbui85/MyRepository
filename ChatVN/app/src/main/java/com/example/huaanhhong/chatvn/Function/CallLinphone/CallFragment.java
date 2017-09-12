package com.example.huaanhhong.chatvn.Function.CallLinphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.huaanhhong.chatvn.Function.Model.Friend;
import com.example.huaanhhong.chatvn.Function.Model.ListFriend;
import com.example.huaanhhong.chatvn.Function.Service.ServiceUtils;
import com.example.huaanhhong.chatvn.Function.data.FriendDB;
import com.example.huaanhhong.chatvn.Function.data.StaticConfig;
import com.example.huaanhhong.chatvn.Linphone.LinphoneManager;
import com.example.huaanhhong.chatvn.Linphone.LinphonePreferences;
import com.example.huaanhhong.chatvn.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import org.linphone.core.CallDirection;
import org.linphone.core.LinphoneCallLog;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneProxyConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.huaanhhong.chatvn.R.id.swipeRefreshLayout;

/**
 * Created by Asus on 8/15/2017.
 */

public class CallFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener  {

    private RecyclerView recyclerListFrends;
    private ListCallAdapter adapter;
    private ListFriend dataListFriend=null ;
    private ArrayList<String> listFriendID=null ;
    private LovelyProgressDialog dialogFindAllFriend;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CountDownTimer detectFriendOnline;


    public static final String ACTION_DELETE_FRIEND = "com.android.rivchat.DELETE_FRIEND";

    private BroadcastReceiver deleteFriendReceiver;

    ////////////////////////////////LINPHONE/////////////////////////////////////
    private static CallFragment instance;
    private static boolean isCallTransferOngoing = false;
    private Button mBtnCall;
    private EditText mEdtAddress;
    private View.OnClickListener transferListener;
    String mMobile=null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        detectFriendOnline = new CountDownTimer(System.currentTimeMillis(), StaticConfig.TIME_TO_REFRESH) {
            @Override
            public void onTick(long l) {
                ServiceUtils.updateFriendStatus(getContext(), dataListFriend);
                ServiceUtils.updateUserStatus(getContext());
            }

            @Override
            public void onFinish() {

            }
        };
        if (dataListFriend == null) {
            Log.i("BLH","Load list frend data list frend null");
            dataListFriend = FriendDB.getInstance(getContext()).getListFriend();
            if (dataListFriend.getListFriend().size() > 0) {
                listFriendID = new ArrayList<>();
                Log.i("BLH","Load list frend");
                for (Friend friend : dataListFriend.getListFriend()) {

                    listFriendID.add(friend.id);
                }
                detectFriendOnline.start();
            }
        }
        View layout = inflater.inflate(R.layout.fragment_chat, container, false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerListFrends = (RecyclerView) layout.findViewById(R.id.recycleListFriend);
        recyclerListFrends.setLayoutManager(linearLayoutManager);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        adapter = new ListCallAdapter(getContext(), dataListFriend, this);
        recyclerListFrends.setAdapter(adapter);
        dialogFindAllFriend = new LovelyProgressDialog(getContext());
        if (listFriendID == null) {

            Log.i("BLH","Load list frendID data list frend null");

            listFriendID = new ArrayList<>();
            dialogFindAllFriend.setCancelable(false)
                    .setIcon(R.drawable.ic_add_friend)
                    .setTitle("Get all friend....")
                    .setTopColorRes(R.color.colorPrimary)
                    .show();
            getListFriendUId();
        }

        deleteFriendReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String idDeleted = intent.getExtras().getString("idFriend");
                for (Friend friend : dataListFriend.getListFriend()) {
                    if(idDeleted.equals(friend.id)){
                        ArrayList<Friend> friends = dataListFriend.getListFriend();
                        friends.remove(friend);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        };

        IntentFilter intentFilter = new IntentFilter(ACTION_DELETE_FRIEND);
        getContext().registerReceiver(deleteFriendReceiver, intentFilter);

        instance=this;


        return layout;
    }

    public static CallFragment instance() {
        android.util.Log.i("CNN", "DialerFragment_instance");
        return instance;
    }
    @Override
    public void onDestroyView (){
        super.onDestroyView();

        getContext().unregisterReceiver(deleteFriendReceiver);
    }
    public void newOutgoingCall(String numberOrSipAddress) {
        android.util.Log.i("CNN", "DialerFragment_newoutgoingcall_string number");
        LinphoneManager.getInstance().newOutgoingCall(mMobile);
    }



        @Override
    public void onRefresh() {

        listFriendID.clear();
        dataListFriend.getListFriend().clear();
        adapter.notifyDataSetChanged();
        FriendDB.getInstance(getContext()).dropDB();
        detectFriendOnline.cancel();
        getListFriendUId();

    }

    private void getListFriendUId() {
        FirebaseDatabase.getInstance().getReference().child("friend/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap mapRecord = (HashMap) dataSnapshot.getValue();
                    Iterator listKey = mapRecord.keySet().iterator();
                    while (listKey.hasNext()) {
                        String key = listKey.next().toString();
                        listFriendID.add(mapRecord.get(key).toString());
                    }
                    getAllFriendInfo(0);
                } else {
                    dialogFindAllFriend.dismiss();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Truy cap bang user lay thong tin id nguoi dung
     */
    private void getAllFriendInfo(final int index) {
        if (index == listFriendID.size()) {
            //save list friend
            adapter.notifyDataSetChanged();
            dialogFindAllFriend.dismiss();
            mSwipeRefreshLayout.setRefreshing(false);
            detectFriendOnline.start();
        } else {
            final String id = listFriendID.get(index);
            FirebaseDatabase.getInstance().getReference().child("ListUser/" + id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null) {
                        Friend user = new Friend();
                        HashMap mapUserInfo = (HashMap) dataSnapshot.getValue();
                        user.name = (String) mapUserInfo.get("name");
                        user.email = (String) mapUserInfo.get("email");
                        user.avata = (String) mapUserInfo.get("avata");
                        user.id = id;
                        user.idRoom = id.compareTo(StaticConfig.UID) > 0 ? (StaticConfig.UID + id).hashCode() + "" : "" + (id + StaticConfig.UID).hashCode();
                        dataListFriend.getListFriend().add(user);
                        FriendDB.getInstance(getContext()).addFriend(user);
                    }
                    getAllFriendInfo(index + 1);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void truyen(String mMobile) {
        mMobile=mMobile;
    }
}

class ListCallAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ListFriend listFriend;
    private Context context;
    public static Map<String, Query> mapQuery;
    public static Map<String, DatabaseReference> mapQueryOnline;
    public static Map<String, ChildEventListener> mapQueryMobil;
    public static Map<String, ChildEventListener> mapChildListener;
    public static Map<String, ChildEventListener> mapChildListenerOnline;
    public static Map<String, Boolean> mapMark;
    private CallFragment fragment;
    LovelyProgressDialog dialogWaitDeleting;

    public ListCallAdapter(Context context, ListFriend listFriend, CallFragment fragment) {
        this.listFriend = listFriend;
        this.context = context;
        mapQuery = new HashMap<>();
        mapChildListener = new HashMap<>();
        mapMark = new HashMap<>();
        mapChildListenerOnline = new HashMap<>();
        mapQueryOnline = new HashMap<>();
        mapQueryMobil = new HashMap<>();
        this.fragment = fragment;
        dialogWaitDeleting = new LovelyProgressDialog(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_call, parent, false);
        return new ItemCallViewHolder(context, view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final String name = listFriend.getListFriend().get(position).name;
        final String id = listFriend.getListFriend().get(position).id;
        final String mobile=listFriend.getListFriend().get(position).mobil;
        final String idRoom = listFriend.getListFriend().get(position).idRoom;
        final String avata = listFriend.getListFriend().get(position).avata;
        ((ItemCallViewHolder) holder).txtName.setText(name);
        final String mMobile= ((ItemCallViewHolder) holder).txtMobil.getText().toString();
        FirebaseDatabase.getInstance().getReference().child("ListUser/" + id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap mobilMap= (HashMap) dataSnapshot.getValue();
                String mMobile = (String) mobilMap.get("mobil");
                ((ItemCallViewHolder) holder).txtMobil.setText(mMobile);
                ((ItemCallViewHolder) holder).txtMobil.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        ((ItemCallViewHolder) holder).btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call(mMobile,false);
                CallFragment.instance().truyen(mMobile);
            }
        });
        ((ItemCallViewHolder) holder).btnVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call(mMobile,true);
                CallFragment.instance().truyen(mMobile);
            }
        });


        if (listFriend.getListFriend().get(position).avata.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            ((ItemCallViewHolder) holder).avata.setImageResource(R.drawable.default_avata);
        } else {
            byte[] decodedString = Base64.decode(listFriend.getListFriend().get(position).avata, Base64.DEFAULT);
            Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ((ItemCallViewHolder) holder).avata.setImageBitmap(src);
        }


        if (mapQueryOnline.get(id) == null && mapChildListenerOnline.get(id) == null) {
            mapQueryOnline.put(id, FirebaseDatabase.getInstance().getReference().child("ListUser/" + id+"/status"));
            mapChildListenerOnline.put(id, new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                    if(dataSnapshot.getValue() != null && dataSnapshot.getKey().equals("isOnline")) {
                        Log.i("BLH","FriendsFragment add " + id);
                        Log.d("FriendsFragment add " + id,  (boolean)dataSnapshot.getValue() +"");
                        listFriend.getListFriend().get(position).status.isOnline = (boolean)dataSnapshot.getValue();
                        notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    if(dataSnapshot.getValue() != null&& dataSnapshot.getKey().equals("isOnline")) {
                        Log.d("FriendsFragment change " + id,  (boolean)dataSnapshot.getValue() +"");
                        listFriend.getListFriend().get(position).status.isOnline = (boolean)dataSnapshot.getValue();
                        notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mapQueryOnline.get(id).addChildEventListener(mapChildListenerOnline.get(id));
        }

        if (listFriend.getListFriend().get(position).status.isOnline) {
            ((ItemCallViewHolder) holder).img_online.setVisibility(View.VISIBLE);
        } else {
            ((ItemCallViewHolder) holder).img_online.setVisibility(View.GONE);
        }
    }

    private void call(String mMobile, boolean b) {
        LinphonePreferences.instance().setInitiateVideoCall(b);

        try {
            if (!LinphoneManager.getInstance().acceptCallIfIncomingPending()) {
                if (mMobile.length() > 0) {
                    LinphoneManager.getInstance().newOutgoingCall(mMobile);
                } else {
                    if (context.getResources().getBoolean(R.bool.call_last_log_if_adress_is_empty)) {
                        LinphoneCallLog[] logs = LinphoneManager.getLc().getCallLogs();
                        LinphoneCallLog log = null;
                        for (LinphoneCallLog l : logs) {
                            if (l.getDirection() == CallDirection.Outgoing) {
                                log = l;
                                break;
                            }
                        }
                        if (log == null) {
                            return;
                        }

                        LinphoneProxyConfig lpc = LinphoneManager.getLc().getDefaultProxyConfig();
                        if (lpc != null && log.getTo().getDomain().equals(lpc.getDomain())) {
                            mMobile=(log.getTo().getUserName());
                        } else {
                           mMobile=(log.getTo().asStringUriOnly());
                        }
                        mMobile=(log.getTo().getDisplayName());
                    }
                }
            }
        } catch (LinphoneCoreException e) {
            LinphoneManager.getInstance().terminateCall();
//                    onWrongDestinationAddress();
        }
    }


    @Override
    public int getItemCount() {
        return listFriend.getListFriend() != null ? listFriend.getListFriend().size() : 0;
    }

    /**
     * Delete friend
     *
     * @param idFriend
     */
    private void deleteFriend(final String idFriend) {
        if (idFriend != null) {
            FirebaseDatabase.getInstance().getReference().child("friend").child(StaticConfig.UID)
                    .orderByValue().equalTo(idFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue() == null) {
                        //email not found
                        dialogWaitDeleting.dismiss();
                        new LovelyInfoDialog(context)
                                .setTopColorRes(R.color.colorAccent)
                                .setTitle("Error")
                                .setMessage("Error occurred during deleting friend")
                                .show();
                    } else {
                        String idRemoval = ((HashMap) dataSnapshot.getValue()).keySet().iterator().next().toString();
                        FirebaseDatabase.getInstance().getReference().child("friend")
                                .child(StaticConfig.UID).child(idRemoval).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        dialogWaitDeleting.dismiss();

                                        new LovelyInfoDialog(context)
                                                .setTopColorRes(R.color.colorAccent)
                                                .setTitle("Success")
                                                .setMessage("Friend deleting successfully")
                                                .show();

                                        Intent intentDeleted = new Intent(CallFragment.ACTION_DELETE_FRIEND);
                                        intentDeleted.putExtra("idFriend", idFriend);
                                        context.sendBroadcast(intentDeleted);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        dialogWaitDeleting.dismiss();
                                        new LovelyInfoDialog(context)
                                                .setTopColorRes(R.color.colorAccent)
                                                .setTitle("Error")
                                                .setMessage("Error occurred during deleting friend")
                                                .show();
                                    }
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            dialogWaitDeleting.dismiss();
            new LovelyInfoDialog(context)
                    .setTopColorRes(R.color.colorPrimary)
                    .setTitle("Error")
                    .setMessage("Error occurred during deleting friend")
                    .show();
        }
    }
}

class ItemCallViewHolder extends RecyclerView.ViewHolder{
    public CircleImageView avata;
    public TextView txtName,  txtMobil;
    public ImageButton btnCall, btnVideoCall;
    public ImageView img_online;
    private Context context;

    ItemCallViewHolder(Context context, View itemView) {
        super(itemView);
        avata = (CircleImageView) itemView.findViewById(R.id.icon_avata);
        txtName = (TextView) itemView.findViewById(R.id.txtName);
        txtMobil = (TextView) itemView.findViewById(R.id.txtMobil);
        btnCall=(ImageButton) itemView.findViewById(R.id.btnCall);
        btnVideoCall=(ImageButton) itemView.findViewById(R.id.btnCallVideo);
        img_online=(ImageView) itemView.findViewById(R.id.img_online);
        this.context = context;
    }
}
