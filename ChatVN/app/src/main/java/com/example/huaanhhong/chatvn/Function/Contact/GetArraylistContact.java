package com.example.huaanhhong.chatvn.Function.Contact;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.example.huaanhhong.chatvn.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huaanhhong on 23/08/2017.
 */

public class GetArraylistContact {

    Context context;

    public GetArraylistContact(Context context) {
        this.context = context;
    }

    public List<Contact> readContact(){
        List<Contact> listcontact=new ArrayList<>();
        //tra ve ba?ng du lieu tong hop
        Uri uri= ContactsContract.Contacts.CONTENT_URI;
        //lay bang co khoa chinh la displayname
        Cursor contactCusor=context.getContentResolver().query(uri,null,null,null,ContactsContract.Contacts.DISPLAY_NAME
        );
        //lay chi tiet tung contact +"ASC"
        if(contactCusor.moveToFirst()){
            do{
                //lay contact id cua tung contact tu danh dach tat ca contact
                long contactId=contactCusor.getLong(contactCusor.getColumnIndex("_ID"));
                //lay bang tong hop
                Uri dataUri=ContactsContract.Data.CONTENT_URI;
                //cursor data dua vao id, tiep tuc lay bang chi tiet cua tung nguoi
                Cursor dataCusor=context.getContentResolver().query(dataUri,null,
                        ContactsContract.Data.CONTACT_ID+"="+contactId,null,null);
                //BAY GIO, CHUNG TA DA CO BANG DU LIEU CHI TIET CUA TUNG CONTACT
                //Tao bien de gan ket qua thu duoc
                String displayName="";
                String nickName="";
                String homePhone="";
                String mobilePhone="";
                String workPhone="";
                String photoPath=""+ R.mipmap.ic_launcher;
                byte[] photoByte=null;

                String homeEmail="";
                String workEmail="";
                String companyName="";
                String title="";

                String contactNumber="";
                String contactEmailAddresses="";
                String contactOtherDetails="";

                //lay tung dong du lieu chi tiet tu  bang thong tin chi tiet cua tung conatact
                if(dataCusor.moveToFirst()){
                    displayName=dataCusor.getString(dataCusor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    do {
                        String mimetype=dataCusor.getString(dataCusor.getColumnIndex("mimetype"));
                        if(mimetype.equals(ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)){
                            nickName=dataCusor.getString(dataCusor.getColumnIndex("data1"));
                            contactOtherDetails +="Nickname : "+nickName +"n";
                            //add the nick name to string
                        }
                        if (mimetype.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)){
                            switch (dataCusor.getInt(dataCusor
                                    .getColumnIndex("data2"))) {
                                case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                    homePhone = dataCusor.getString(dataCusor
                                            .getColumnIndex("data1"));
                                    contactNumber += "Home Phone : " + homePhone
                                            + "n";
                                    break;

                                case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                    workPhone = dataCusor.getString(dataCusor
                                            .getColumnIndex("data1"));
                                    contactNumber += "Work Phone : " + workPhone
                                            + "n";
                                    break;

                                case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                    mobilePhone = dataCusor.getString(dataCusor
                                            .getColumnIndex("data1"));
                                    contactNumber =mobilePhone ;
                                    break;

                            }
                        }
                        if (mimetype.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)){
                            switch (dataCusor.getInt(dataCusor
                                    .getColumnIndex("data2"))) {
                                case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                                    homeEmail = dataCusor.getString(dataCusor
                                            .getColumnIndex("data1"));
                                    contactEmailAddresses += "Home Email : "
                                            + homeEmail + "n";
                                    break;
                                case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                                    workEmail = dataCusor.getString(dataCusor
                                            .getColumnIndex("data1"));
                                    contactEmailAddresses += "Work Email : "
                                            + workEmail + "n";
                                    break;

                            }
                        }
                        if (mimetype.equals(ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)){
                            companyName = dataCusor.getString(dataCusor
                                    .getColumnIndex("data1"));// get company
                            // name
                            contactOtherDetails += "Coompany Name : "
                                    + companyName + "n";
                            title = dataCusor.getString(dataCusor
                                    .getColumnIndex("data4"));// get Company
                            // title
                            contactOtherDetails += "Title : " + title + "n";
                        }
                        if(mimetype.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)){
                            photoByte=dataCusor.getBlob(dataCusor.getColumnIndex("data15"));
//                            Log.i("CNN hinh",photoByte.toString());
//                            String str = new String(photoByte, StandardCharsets.UTF_8);
//                            Log.i("CNN",str);

//                            if(photoByte!=null){
//                                //tao bo nho cache de luu file vao
//                                Bitmap bitmap= BitmapFactory.decodeByteArray(photoByte,0,photoByte.length);
//                                File cacheDirectory=context.getCacheDir();
//                                File tmp=new File(cacheDirectory.getPath()+"anhhong"+contactId+".png");
//                                try{
//                                    FileOutputStream fileOutputStream=new FileOutputStream(tmp);
//                                    bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
//                                    fileOutputStream.flush();
//                                    fileOutputStream.close();
//
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                    Log.i("CNN","co loi trong luu cache hinh contact");
//                                }
//                                photoPath=tmp.getPath();
//                                Log.i("CNN",photoPath);
//
//                            }
                        }

                    }
                    while (dataCusor.moveToNext());
                    listcontact.add(new Contact(displayName,contactNumber,photoByte));

                }
            }
            while (contactCusor.moveToNext());

        }
        return listcontact;

    }
}
