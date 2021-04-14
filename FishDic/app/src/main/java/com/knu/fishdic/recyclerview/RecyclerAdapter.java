package com.knu.fishdic.recyclerview;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.knu.fishdic.FishDic;
import com.knu.fishdic.R;

import java.util.ArrayList;

// 이달의 금어기, 도감에 사용되는 RecyclerView를 위한 어댑터 정의
// https://developer.android.com/jetpack/androidx/releases/recyclerview

/***
 * 어댑터는 보여지는 뷰와 그 뷰에 올릴 데이터를 연결하는 일종의 다리 역할을 하는 객체
 * ---
 * RecyclerView는 특정 View Group이 반복되는 구조로서
 * RecyclerView의 각 리스트는 View Holder로 나타내어지고, View Holder를 원하는대로 추가한다.
 * View Holder는 RecyclerView에 담기는 실제 데이터 집합(View Group)으로 하나의 리스트를 구성한다.
 ***/

//implements Filterable
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {
    private ArrayList<RecyclerViewItem> itemList; //필터링 되지 않은 전체 목록
   // private ArrayList<RecyclerViewItem> filteredList; //검색 결과에 따라 필터링된 목록
    CursorAdapter cursorAdapter; //커서에서 위젯으로 데이터를 노출하는 어댑터
    
    public RecyclerAdapter(Cursor cursor) {
        this.itemList = new ArrayList<>();
        //this.filteredList = new ArrayList<>();
    }

    public void setCursorAdapter(Cursor cursor) { //DB 데이터 뷰에 바인딩을 위해 커서 어댑터 설정
        this.cursorAdapter = new CursorAdapter(FishDic.globalContext, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return null;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {

            }
        };
    }
/*
    @Override
    public Filter getFilter() { //필터링 패턴으로 검색 결과를 제한하는 데 사용할 수 있는 필터를 반환
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) { //제약 조건에 따라 데이터를 필터링하기 위해 작업자 스레드에서 호출
                String str = constraint.toString(); //사용자로부터 입력받은 문자열
                if (str.isEmpty()) { //비어있으면 초기화
                    if(!filteredList.isEmpty())
                        filteredList.clear();
                } else {
                    for (RecyclerViewItem item : itemList) { //전체 목록에 대하여
                        if (item.getTitle().toLowerCase().contains(str)) //어류 이름이 일치할 경우 필터링된 리스트에 추가
                            filteredList.add(item);
                    }
                }

                //필터링 된 결과 반환
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) { //필터링 된 결과에 따라 화면 재구성
                filteredList = (ArrayList<RecyclerViewItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }
    */

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //뷰 홀더 생성 시 호출
        //https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#onCreateViewHolder(android.view.ViewGroup,%20int)

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_recyclerview, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) { //해당 position에 데이터 표시위해 호출
        //https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#onBindViewHolder(VH,%20int)

        holder.onBind(this.itemList.get(position));
    }

    @Override
    public int getItemCount() { //item의 총 개수 반환
        return this.itemList.size();
    }

    public void addItem(RecyclerViewItem Item) { //외부에서 item 추가
        this.itemList.add(Item);
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView innerRecyclerView_imageView; //어류 이미지 뷰
        private TextView innerRecyclerView_title_textView; //제목 텍스트 뷰
        private TextView innerRecyclerView_content_textView; //내용 텍스트 뷰

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            innerRecyclerView_title_textView = itemView.findViewById(R.id.innerRecyclerView_title_textView);
            innerRecyclerView_content_textView = itemView.findViewById(R.id.innerRecyclerView_content_textView);
            innerRecyclerView_imageView = itemView.findViewById(R.id.innerRecyclerView_imageView);
        }

        public void onBind(RecyclerViewItem recyclerViewItem) {
            innerRecyclerView_title_textView.setText(recyclerViewItem.getTitle());
            innerRecyclerView_content_textView.setText(recyclerViewItem.getContent());
            innerRecyclerView_imageView.setImageResource(recyclerViewItem.getResId());
        }
    }
}
