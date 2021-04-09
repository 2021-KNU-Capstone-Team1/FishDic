package com.knu.fishdic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

// 메인 화면 액티비티 정의

public class MainActivity extends Activity {
    ImageButton main_dic_imageButton;                       //메인화면 하단부 도감 버튼
    ImageButton main_deniedFish_imageButton;                //메인화면 하단부 금어기 버튼
    ImageButton main_fishIdentification_imageButton;        //메인화면 하단부 카메라 버튼
    ImageButton main_parasite_imageButton;                  //메인화면 하단부 기생충 버튼
    ImageButton main_help_imageButton;                      //메인화면 하단부 도움 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        setContentView(R.layout.activity_main);
        setComponentsInteraction();

    }

    private void setComponentsInteraction() //내부 구성요소 상호작용 설정
    {
        main_dic_imageButton = (ImageButton) findViewById(R.id.main_dic_imageButton);
        main_deniedFish_imageButton = (ImageButton) findViewById(R.id.main_deniedFish_imageButton);
        main_fishIdentification_imageButton = (ImageButton) findViewById(R.id.main_fishIdentification_imageButton);
        main_parasite_imageButton = (ImageButton) findViewById(R.id.main_parasite_imageButton);
        main_help_imageButton = (ImageButton) findViewById(R.id.main_help_imageButton);

        main_dic_imageButton.setOnClickListener(new View.OnClickListener(){                         //도감 화면으로 넘어가는 클릭 리스너
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), DicActivity.class);
                startActivity(intent);
            }
        });

        main_deniedFish_imageButton.setOnClickListener(new View.OnClickListener(){                  //금어기 화면으로 넘어가는 클릭 리스너
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), DeniedFishActivity.class);
                startActivity(intent);
            }
        });

        main_fishIdentification_imageButton.setOnClickListener(new View.OnClickListener(){          //카메라 촬영으로 넘어가야함 아직 안했음
            public void onClick(View v){

            }
        });

        main_parasite_imageButton.setOnClickListener(new View.OnClickListener(){                    //기생충 화면으로 넘어가야 하는데 아직 없어서 금어기로 연결해두었음 수정필요
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), DeniedFishActivity.class);
                startActivity(intent);
            }
        });

        main_help_imageButton.setOnClickListener(new View.OnClickListener(){                        //도움 화면으로 넘어가야 하는데 아직 없어서 금어기로 연결해 두었음 수정필요
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), DeniedFishActivity.class);
                startActivity(intent);
            }
        });
    }
}
