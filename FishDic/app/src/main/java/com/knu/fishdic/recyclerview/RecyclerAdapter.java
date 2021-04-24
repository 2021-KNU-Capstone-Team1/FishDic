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
    private ArrayList<RecyclerViewItem> itemList; //전체 목록 (원본)
    private ArrayList<RecyclerViewItem> refItemList; //현재 참조중인 목록
    private OnItemClickListener refItemClickListener; //아이템 클릭 리스너 참조 변수

    public RecyclerAdapter() {
        this.itemList = new ArrayList<>();
        this.refItemList = itemList; //초기 원본 리스트를 참조
        this.refItemClickListener = null;
    }

    public interface OnItemClickListener { //커스텀 리스너 인터페이스
        void onItemClick(View v, String title);
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener) { //아이템 클릭 리스너 참조 변수 할당
        this.refItemClickListener = itemClickListener;
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
        /***
         * RecyclerView는 ViewHolder를 새로 만들어야 할 때마다 이 메서드를 호출한다.
         * 이 메서드는 ViewHolder와 그에 연결된 View를 생성하고 초기화하지만 ViewHolder가 아직 특정 데이터에 바인딩된 상태가 아니기 때문에, 뷰의 콘텐츠를 채우지는 않는다.
         ***/
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
        /***
         * RecyclerView는 ViewHolder를 데이터와 연결할 때 이 메서드를 호출한다. 
         * 이 메서드는 적절한 데이터를 가져와서 그 데이터를 사용하여 뷰 홀더의 레이아웃을 채운다.
         ***/
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

    public void addItem(RecyclerViewItem Item) { //외부에서 전체 목록 (원본)에 요소 추가
        this.itemList.add(Item);
    }

    public void resetRefItemList(){ //참조 목록에 대한 초기화
        this.refItemList = this.itemList; //기존의 원본 리스트를 가라킴
        notifyDataSetChanged(); //데이터 변경에 따른 뷰의 재 바인딩 작업 수행
        System.gc(); //필터링 된 리스트에 대해 가비지 컬렉션 요청
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        /***
         * 어댑터를 통해 만들어진 각 아이템 뷰는 뷰홀더(ViewHolder) 객체에 저장되어 화면에 표시되고,
         * 필요에 따라 생성 또는 재활용(Recycle)된다.
         * ---
         * 아이템 뷰에서 클릭 이벤트를 직접 처리하고, 아이템 뷰는 뷰홀더 객체가 가지고 있으니, 아이템 클릭 이벤트는 뷰홀더에서 작성
         ***/

        private ImageView innerRecyclerView_imageView; //어류 이미지 뷰
        private TextView innerRecyclerView_title_textView; //제목 텍스트 뷰
        private TextView innerRecyclerView_content_textView; //내용 텍스트 뷰

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            setComponentsInteraction();
        }

        private void setComponentsInteraction() { //내부 구성요소 상호작용 설정
            this.innerRecyclerView_title_textView = itemView.findViewById(R.id.innerRecyclerView_title_textView);
            this.innerRecyclerView_content_textView = itemView.findViewById(R.id.innerRecyclerView_content_textView);
            this.innerRecyclerView_imageView = itemView.findViewById(R.id.innerRecyclerView_imageView);

            this.itemView.setOnClickListener(v -> { //현재 아이템에 대한 클릭 이벤트 리스너
                /***
                 * 1) 도감 혹은 이달의 금어기에서 새로운 클릭 리스너 객체 생성 하여 RecyclerAdapter 내부의 refItemClickListener가 참조
                 * 2) 각 어류 클릭 이벤트 발생 시
                 *  2-1) 현재 아이템에 대한 클릭 이벤트 먼저 발생, RecyclerAdapter 내부의 참조 된 refItemClickListener로 클릭 된 어류의 이름 전달하여 클릭 이벤트 발생
                 *  2-2) Intent 생성 및 어류 이름 전달하여 어류 상세 정보 액티비티 시작
                 ***/
                int bindingAdapterPosition = getBindingAdapterPosition();
                //int absoluteAdapterPosition = getAbsoluteAdapterPosition();
                //Log.d("bindingPos : ", Integer.toString(bindingAdapterPosition));
                //Log.d("absolutePos : ", Integer.toString(absoluteAdapterPosition));
                
                if (bindingAdapterPosition != RecyclerView.NO_POSITION && refItemClickListener != null) //클릭 된 아이템이 존재하며, 클릭 리스너가 참조되어 있으면
                    refItemClickListener.onItemClick(v, this.innerRecyclerView_title_textView.getText().toString()); //클릭 된 어류의 이름 전달
            });
        }

        public void onBind(RecyclerViewItem recyclerViewItem) { //뷰 홀더에 데이터 바인딩
            this.innerRecyclerView_title_textView.setText(recyclerViewItem.getTitle());
            this.innerRecyclerView_content_textView.setText(recyclerViewItem.getContent());

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