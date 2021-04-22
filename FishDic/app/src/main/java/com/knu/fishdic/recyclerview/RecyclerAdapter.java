package com.knu.fishdic.recyclerview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
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


public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> implements Filterable {
    private ArrayList<RecyclerViewItem> itemList; //전체 목록
    private ArrayList<RecyclerViewItem> refItemList; //현재 참조중인 목록

    public RecyclerAdapter() {
        this.itemList = new ArrayList<>();
        this.refItemList = itemList; //초기 원본 리스트를 참조
    }

    @Override
    public Filter getFilter() { //필터링 패턴으로 검색 결과를 제한하는 데 사용할 수 있는 필터를 반환
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) { //제약 조건에 따라 데이터를 필터링하기 위해 작업자 스레드에서 호출
                String str = constraint.toString(); //사용자로부터 입력받은 문자열
                ArrayList<RecyclerViewItem> resultList = new ArrayList<>();
                FilterResults filterResults = new FilterResults(); //필터링된 결과

                if (str.isEmpty()) { //입력 받은 문자열이 없을 경우
                    refItemList = itemList; //원본 리스트 참조
                    return null;
                }

                for (RecyclerViewItem item : itemList) { //전체 목록에 대하여
                    if (item.getTitle().toLowerCase().contains(str)) //입력값이 제목을 포함하고 있을 경우 필터링된 리스트에 추가
                        resultList.add(item);
                }

                //필터링 된 결과 설정
                filterResults.values = resultList;
                filterResults.count = resultList.size();

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults filterResults) { //필터링된 결과에 따라 화면 재구성
                if (filterResults != null && filterResults.count > 0) {
                    refItemList = (ArrayList<RecyclerViewItem>) filterResults.values; //필터링된 리스트 참조
                }
                //필터링 된 결과가 없을 경우 기존의 전체 리스트를 보여주는 것에서 변동없음
                notifyDataSetChanged(); //데이터 변경에 따른 뷰의 재 바인딩 작업 수행
            }
        };
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //뷰 홀더 초기화
        //https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#onCreateViewHolder(android.view.ViewGroup,%20int)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_recyclerview, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) { //뷰 재활용을 위해 position의 뷰 타입 반환
        /***
         * 기본적으로 해당 메소드는 어댑터에 대해 단일 보기 유형을 가정하여 0을 반환한다.
         * 스크롤시에 정상적으로 각 뷰를 고유하게 식별하기 위해 해당 position을 반환
         ***/
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) { //뷰에 데이터 바인딩
        //https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#onBindViewHolder(VH,%20int)
        holder.onBind(this.refItemList.get(position)); //현재 참조중인 목록에 대하여 해당 position의 데이터를 뷰 홀더에 바인딩
    }

    @Override
    public int getItemCount() { //현재 참조 중인 목록의 총 개수 반환
        /***
         * RecyclerView에 Prefetch(미리 데이터 로드)하기 위해 해당 메소드를 호출한다.
         * 사용자가 검색 창에 입력 할 경우 참조 목록(refItemList)이 필터링 된 새로운 목록을 참조한다.
         * 올바른 데이터 바인딩을 위해 현재 참조 중인 목록의 총 개수를 반환
         ***/
        return this.refItemList.size();
    }

    public void addItem(RecyclerViewItem Item) { //외부에서 전체 목록에 요소 추가
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

        public void onBind(RecyclerViewItem recyclerViewItem) { //뷰 홀더에 데이터 바인딩
            innerRecyclerView_title_textView.setText(recyclerViewItem.getTitle());
            innerRecyclerView_content_textView.setText(recyclerViewItem.getContent());

            int imageLength = recyclerViewItem.getImageLength(); //이미지 배열 길이
            if (imageLength > 0) { //이미지가 존재 할 경우만 이미지 설정
                Bitmap bitmap = BitmapFactory.decodeByteArray(recyclerViewItem.getImage(), 0, imageLength); //DB로부터 읽어들인 이미지를 Bitmap 형식으로 변환
                innerRecyclerView_imageView.setImageBitmap(bitmap);
            } else { //사용자에 의한 검색 결과에 따른 뷰 재 바인딩 시 이미지가 존재하지 않은 어류를 위하여 리소스 설정
                innerRecyclerView_imageView.setImageResource(R.drawable.photo_coming_soon_600x600);
            }
        }
    }
}