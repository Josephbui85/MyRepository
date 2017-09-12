package com.example.huaanhhong.chatvn.Function.ChatFirebase;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.huaanhhong.chatvn.R;
import com.example.huaanhhong.chatvn.Function.data.SharedPreferenceHelper;
import com.example.huaanhhong.chatvn.Function.data.StaticConfig;
import com.example.huaanhhong.chatvn.Function.Model.Consersation;
import com.example.huaanhhong.chatvn.Function.Model.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLERY_CODE =1001 ;
    private static final int CAMERA_CODE =1002 ;
    private RecyclerView recyclerChat;
    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    private ListMessageAdapter adapter;
    private String roomId;
    private ArrayList<CharSequence> idFriend;
    private Consersation consersation;
    private ImageButton btnSend;
    private EditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;
    public static HashMap<String, Bitmap> bitmapAvataFriend;
    public Bitmap bitmapAvataUser;
    private ImageButton btnGalery;
    private ImageButton btnCamera;

    private Uri fileUri;

    private StorageReference sStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://chatvn-a266e.appspot.com/");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intentData = getIntent();
        idFriend = intentData.getCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID);
        roomId = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID);
        String nameFriend = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND);

        consersation = new Consersation();
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        btnGalery= (ImageButton) findViewById(R.id.btnGalery);
        btnGalery.setOnClickListener(this);
        btnCamera= (ImageButton) findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(this);

        String base64AvataUser = SharedPreferenceHelper.getInstance(this).getUserInfo().avata;
        if (!base64AvataUser.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            byte[] decodedString = Base64.decode(base64AvataUser, Base64.DEFAULT);
            bitmapAvataUser = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } else {
            bitmapAvataUser = null;
        }

        editWriteMessage = (EditText) findViewById(R.id.editWriteMessage);
        if (idFriend != null && nameFriend != null) {
            getSupportActionBar().setTitle(nameFriend);
            linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerChat = (RecyclerView) findViewById(R.id.recyclerChat);
            recyclerChat.setLayoutManager(linearLayoutManager);
            adapter = new ListMessageAdapter(this, consersation, bitmapAvataFriend, bitmapAvataUser);
            FirebaseDatabase.getInstance().getReference().child("message/" + roomId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    UpdateDatabase(dataSnapshot);

                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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
            recyclerChat.setAdapter(adapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent result = new Intent();
            result.putExtra("idFriend", idFriend.get(0));
            setResult(RESULT_OK, result);
            this.finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra("idFriend", idFriend.get(0));
        setResult(RESULT_OK, result);
        this.finish();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.btnSend:
                String content = editWriteMessage.getText().toString().trim();
                if (content.length() > 0) {
                    editWriteMessage.setText("");
                    Message newMessage = new Message();
                    newMessage.text = content;
                    newMessage.idSender = StaticConfig.UID;
                    newMessage.idReceiver = roomId;
                    newMessage.timestamp = System.currentTimeMillis();
                    FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage);
                }
                break;

            case R.id.btnGalery:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_CODE);
                break;

            case R.id.btnCamera:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, CAMERA_CODE);
                break;
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == CAMERA_CODE && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get( "data" );
                UploadPostTask uploadPostTask = new UploadPostTask();
                uploadPostTask.execute( imageBitmap );
            }

            if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && data != null) {
                //Get uri
                Uri selectedImage = data.getData();
                String[] filePathColum = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColum, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int columIndex = cursor.getColumnIndexOrThrow(filePathColum[0]);
                        String path = cursor.getString(columIndex);
                        int startPosition = path.lastIndexOf('/');
                        int length = path.length();
                        String pathCode = "";
                        //get the substring which will be the name of photo
                        for (int i = startPosition + 1; i < length; i++) {
                            pathCode += path.charAt(i);
                        }
                        final StorageReference filePath = sStorageReference.child("Photos").child(pathCode);


                        filePath.putFile(selectedImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                //Get download url and save it in database
                                Log.i("BLH","Galerry");
                                SaveImageInFirebase(taskSnapshot);


                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //Show toast if failure occurs
                                        Toast.makeText(ChatActivity.this, "Failure", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                    cursor.close();
                }
            }
        }

    @SuppressWarnings("VisibleForTests")
    private class UploadPostTask
            extends AsyncTask<Bitmap, Void, Void>
    {

        @Override
        protected Void doInBackground( Bitmap... params )
        {
            Bitmap bitmap = params[0];
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress( Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream );
            final StorageReference filePath = sStorageReference.child("Photos");
            filePath.putBytes(
                    byteArrayOutputStream.toByteArray() ).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {
                        @Override
                        public void onSuccess( UploadTask.TaskSnapshot taskSnapshot )
                        {
                            if ( taskSnapshot.getDownloadUrl() != null )
                            {
                                final Message newMessage = new Message();
                                newMessage.text = taskSnapshot.getDownloadUrl().toString();
                                newMessage.idSender = StaticConfig.UID;
                                newMessage.idReceiver = roomId;
                                newMessage.timestamp = System.currentTimeMillis();
                                runOnUiThread( new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage);
                                    }
                                } );
                            }
                        }
                    } );

            return null;
        }
    }


//    public class UpdateDatabase extends AsyncTask<Void,Void,Consersation>{
//
//
//        @Override
//        protected Consersation doInBackground(Void... voids) {
//            if(consersation!=null) {
//                return consersation;
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Consersation consersation) {
//            super.onPostExecute(consersation);
//        }
//    }
    private void UpdateDatabase(final DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() != null) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                    Message newMessage = new Message();
                    newMessage.idSender = (String) mapMessage.get("idSender");
                    newMessage.idReceiver = (String) mapMessage.get("idReceiver");
                    newMessage.text = (String) mapMessage.get("text");
                    newMessage.timestamp = (long) mapMessage.get("timestamp");
                    consersation.getListMessageData().add(newMessage);
                    adapter.notifyDataSetChanged();
                    linearLayoutManager.scrollToPosition(consersation.getListMessageData().size() - 1);
                }
            });

        }
    }

    private void SaveImageInFirebase(final UploadTask.TaskSnapshot taskSnapshot) {

        if (taskSnapshot.getDownloadUrl() != null) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Message newMessage = new Message();
                    newMessage.text = taskSnapshot.getDownloadUrl().toString();
                    newMessage.idSender = StaticConfig.UID;
                    newMessage.idReceiver = roomId;
                    newMessage.timestamp = System.currentTimeMillis();
                    FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage);
                }
            });

        }

    }
}

class ListMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private Consersation consersation;
        private HashMap<String, Bitmap> bitmapAvata;
        private HashMap<String, DatabaseReference> bitmapAvataDB;
        private Bitmap bitmapAvataUser;

        public ListMessageAdapter(Context context, Consersation consersation, HashMap<String, Bitmap> bitmapAvata, Bitmap bitmapAvataUser) {
            this.context = context;
            this.consersation = consersation;
            this.bitmapAvata = bitmapAvata;
            this.bitmapAvataUser = bitmapAvataUser;
            bitmapAvataDB = new HashMap<>();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == ChatActivity.VIEW_TYPE_FRIEND_MESSAGE) {
                View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_friend, parent, false);
                return new ItemMessageFriendHolder(view);
            } else if (viewType == ChatActivity.VIEW_TYPE_USER_MESSAGE) {
                View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_user, parent, false);
                return new ItemMessageUserHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ItemMessageFriendHolder) {


//            Sent image

                if (consersation.getListMessageData().get(position).text.contains("firebasestorage.googleapis.com")) {

                    ((ItemMessageFriendHolder) holder).imageView_Server.setVisibility(View.VISIBLE);
                    ((ItemMessageFriendHolder) holder).txtContent.setVisibility(View.GONE);
                    ((ItemMessageFriendHolder) holder).mProgressBar.setVisibility(View.VISIBLE);


                    Glide.with(context)
                            .load(consersation.getListMessageData().get(position).text)
                            .listener(new RequestListener<String, GlideDrawable>() {

                                @Override

                                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                                    ((ItemMessageFriendHolder) holder).mProgressBar.setVisibility(View.GONE);

                                    return false;

                                }

                                @Override
                                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                    ((ItemMessageFriendHolder) holder).mProgressBar.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .into(((ItemMessageFriendHolder) holder).imageView_Server);

                }else {

                    ((ItemMessageFriendHolder) holder).imageView_Server.setVisibility(View.GONE);
                    ((ItemMessageFriendHolder) holder).txtContent.setVisibility(View.VISIBLE);
                    ((ItemMessageFriendHolder) holder).txtContent.setText(consersation.getListMessageData().get(position).text);
                }

                Bitmap currentAvata = bitmapAvata.get(consersation.getListMessageData().get(position).idSender);
                if (currentAvata != null) {
                    ((ItemMessageFriendHolder) holder).avata.setImageBitmap(currentAvata);
                } else {
                    final String id = consersation.getListMessageData().get(position).idSender;
                    if (bitmapAvataDB.get(id) == null) {
                        bitmapAvataDB.put(id, FirebaseDatabase.getInstance().getReference().child("user/" + id + "/avata"));
                        bitmapAvataDB.get(id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    String avataStr = (String) dataSnapshot.getValue();
                                    if (!avataStr.equals(StaticConfig.STR_DEFAULT_BASE64)) {
                                        byte[] decodedString = Base64.decode(avataStr, Base64.DEFAULT);
                                        ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                                    } else {
                                        ChatActivity.bitmapAvataFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avata));
                                    }
                                    notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }
            } else if (holder instanceof ItemMessageUserHolder) {

                if (consersation.getListMessageData().get(position).text.contains("firebasestorage.googleapis.com")) {
                    Log.i("BLH","gửi image");
                    ((ItemMessageUserHolder) holder).imageView_Server.setVisibility(View.VISIBLE);
                    ((ItemMessageUserHolder) holder).txtContent.setVisibility(View.GONE);
                    ((ItemMessageUserHolder) holder).mProgressBar.setVisibility(View.VISIBLE);


                    Glide.with(context)
                         .load(consersation.getListMessageData().get(position).text)

                         .listener(new RequestListener<String, GlideDrawable>() {

                             @Override

                             public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                                 ((ItemMessageUserHolder) holder).mProgressBar.setVisibility(View.GONE);

                                 return false;

                             }

                             @Override
                             public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                 ((ItemMessageUserHolder) holder).mProgressBar.setVisibility(View.GONE);
                                 return false;
                             }
                         })
                         .into(((ItemMessageUserHolder) holder).imageView_Server);



                }else {
                    Log.i("BLH","gửi text");
                    ((ItemMessageUserHolder) holder).imageView_Server.setVisibility(View.GONE);
                    ((ItemMessageUserHolder) holder).txtContent.setVisibility(View.VISIBLE);
                    ((ItemMessageUserHolder) holder).txtContent.setText(consersation.getListMessageData().get(position).text);
                }



                if (bitmapAvataUser != null) {
                    ((ItemMessageUserHolder) holder).avata.setImageBitmap(bitmapAvataUser);
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            return consersation.getListMessageData().get(position).idSender.equals(StaticConfig.UID) ? ChatActivity.VIEW_TYPE_USER_MESSAGE : ChatActivity.VIEW_TYPE_FRIEND_MESSAGE;
        }

        @Override
        public int getItemCount() {
            return consersation.getListMessageData().size();
        }
    }

class ItemMessageUserHolder extends RecyclerView.ViewHolder {
        public TextView txtContent;
        public CircleImageView avata;
        public ImageView imageView_Server;
        ProgressBar mProgressBar;


        public ItemMessageUserHolder(View itemView) {
            super(itemView);
            txtContent = (TextView) itemView.findViewById(R.id.textContentUser);
            avata = (CircleImageView) itemView.findViewById(R.id.imageView2);
            imageView_Server = (ImageView) itemView.findViewById(R.id.image_server);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }

    class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
        public TextView txtContent;
        public CircleImageView avata;
        public ImageView imageView_Server;
        ProgressBar mProgressBar;

        public ItemMessageFriendHolder(View itemView) {
            super(itemView);
            txtContent = (TextView) itemView.findViewById(R.id.textContentFriend);
            avata = (CircleImageView) itemView.findViewById(R.id.imageView3);
            imageView_Server = (ImageView) itemView.findViewById(R.id.image_server);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }