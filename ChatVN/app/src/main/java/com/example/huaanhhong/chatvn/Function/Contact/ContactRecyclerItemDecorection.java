package com.example.huaanhhong.chatvn.Function.Contact;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.huaanhhong.chatvn.R;

/**
 * Created by huaanhhong on 23/08/2017.
 */


public class ContactRecyclerItemDecorection  extends RecyclerView.ItemDecoration{


    private final int headerOffset;
    private final boolean sticky;
    private final SectionCalback sectionCalback;

    private View headerView;
    private TextView header;

    public ContactRecyclerItemDecorection(int headerOffset, boolean sticky, SectionCalback sectionCalback) {
        this.headerOffset = headerOffset;
        this.sticky = sticky;
        this.sectionCalback = sectionCalback;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
//////////////khg hieu cai outrect nay lam./////?????
        int position=parent.getChildAdapterPosition(view);
        if(sectionCalback.isSection(position)){
            outRect.top=headerOffset;
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        //////set headerview
        if(headerView==null){
            //// TODO: 27/06/2017
            headerView=infalteHeaderview(parent);
            header=(TextView) headerView.findViewById(R.id.txt_header);
            //// TODO: 27/06/2017
            fixlayoutSize(headerView,parent);

        }
///////create headerview
        String stringHeader="";
        for(int i=0;i<parent.getChildCount();i++){
            View child=parent.getChildAt(i);
            final int position=parent.getChildAdapterPosition(child);

            String title=sectionCalback.getSectionHeader(position);
            header.setText(title);
            if(!stringHeader.equals(title)||sectionCalback.isSection(position)){
                drawble(c,child,headerView);
                stringHeader=title;
            }

        }
    }

    private void drawble(Canvas c, View child, View headerView) {

        c.save();
        if (sticky) {
            c.translate(0, Math.max(0, child.getTop() - headerView.getHeight()));
        } else {
            c.translate(0, child.getTop() - headerView.getHeight());
        }
        headerView.draw(c);
        c.restore();

    }

    private void fixlayoutSize(View headerView, ViewGroup parent) {

        int widthSPec=View.MeasureSpec.makeMeasureSpec(parent.getWidth(),View.MeasureSpec.EXACTLY);
        int heightSpec=View.MeasureSpec.makeMeasureSpec(parent.getHeight(),View.MeasureSpec.UNSPECIFIED);
        int childWidth=ViewGroup.getChildMeasureSpec(widthSPec,parent.getPaddingLeft()+parent.getPaddingRight(),
                headerView.getLayoutParams().width);
        int childHeight=ViewGroup.getChildMeasureSpec(headerView.getLayoutParams().height,parent.getPaddingBottom()
                +parent.getPaddingTop(),heightSpec);

        headerView.measure(childWidth,childHeight);
        headerView.layout(0,0,headerView.getMeasuredWidth(),headerView.getMeasuredHeight());

    }

    private View infalteHeaderview(RecyclerView parent) {
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header_layout,parent,false);
    }

    public interface SectionCalback{

        boolean isSection(int position);
        String getSectionHeader(int position);
    }
}
