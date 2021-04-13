package com.knu.fishdic.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {
    private ArrayList<RecyclerViewItem> itemList = new ArrayList<>();

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

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView innerRecyclerView_imageView; //어류 이미지 뷰
        private TextView innerRecyclerView_title_textView; //제목 텍스트 뷰
        private TextView innerRecyclerView_content_textView; //내용 텍스트 뷰

        ItemViewHolder(View itemView) {
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
