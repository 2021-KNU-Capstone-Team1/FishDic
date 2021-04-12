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

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {
    private ArrayList<RecyclerViewItem> listItem = new ArrayList<>();

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#onCreateViewHolder(android.view.ViewGroup,%20int)

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_recyclerview, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        //https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#onBindViewHolder(VH,%20int)

        holder.onBind(listItem.get(position));
    }

    @Override
    public int getItemCount() { //item의 총 개수 반환
        return listItem.size();
    }

    void addItem(RecyclerViewItem Item) { // 외부에서 item 추가
        listItem.add(Item);
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
