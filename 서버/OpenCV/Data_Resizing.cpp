#define _CRT_SECURE_NO_WARNINGS
#include <opencv2/opencv.hpp>
#include <iostream>
#include <stdio.h>
#include <fstream>
#include <string>
#include <vector>
#include <Windows.h>

using namespace std;
using namespace cv;


Mat rotate(Mat src, double angle) //회전 함수
{
	Mat dst;
	Point2f pt(src.cols / 2.0, src.rows / 2.0); //회전 중심을 영상의 중앙으로함
	Mat r = getRotationMatrix2D(pt, angle, 1.0); //회전 행렬 계산
	warpAffine(src, dst, r, Size(src.cols, src.rows)); //어파인 변환 수행
	return dst;
}

Mat Image_moving(Mat src, int tx, int ty) //이미지 평행 이동
{
	Mat dst = Mat();
	Mat tmat = (Mat_<double>(2, 3) << 1, 0, tx, 0, 1, ty);; 
	//
	// 	   {1, 0, tx}
	// 	   {0, 1, ty} 행렬 생성
	warpAffine(src, dst, tmat, src.size()); // tx, ty만큼 평행이동하는 행렬
	return dst;
}

int main()
{
	Mat img;
	Mat out_img_1;
	Mat out_img_2;
	Mat resize_img;
	string in_dir = "C:\\Users\\wook\\Desktop\\new_FishPhoto\\"; //테스트 입력 디렉터리
	double angle = 0.0;
	int j = 0;
	int x = 300; //행
	int y = 300; //열
	vector<String> str; //파일 이름 저장할 벡터
	vector<string> _dir;
	char buf[256];
	int count[4] = { 0, };
	bool as = false;

	glob(in_dir, str, TRUE); //glob(찾을 파일 경로, 찾은 파일 경로, recuisive(T or F)
		//T : 폴더 내 하위 폴더 속 까지 파일을 찾음
		//F : 폴더 내 파일을 찾음
	cout << "로드 개수 : " << str.size() << endl;

	if (str.size() == 0) //벡터에 아무런 데이터가 안들어갔을 때
	{
		cout << "이미지가 존재하지 않습니다.\n";
	}

	for (int i = 0; i < str.size(); i++) //이미지 처리
	{
		img = imread(str[i], IMREAD_COLOR);
		resize(img, img, Size(x, y), 0, 0, CV_INTER_LINEAR); //이미지 리사이즈
		ofstream in("C:\\Users\\wook\\Desktop\\처리한 데이터_좌우반전.txt");
		ofstream delete_("C:\\Users\\wook\\Desktop\\삭제해야하는_데이터.txt");
		char temp[100] = { NULL, };
		strcpy(temp, str[i].c_str());
		char* tok_dir = strtok(temp, "\\");
		while (tok_dir != NULL)
		{
			tok_dir = strtok(NULL, "\\");
			if (strcmp(tok_dir, "new_FishPhoto") == 0)
			{
				tok_dir = strtok(NULL, "\\");
				break;
			}
		}
		if (!img.empty())
		{
			//이미지 반전 처리부분
			IplImage* img2 = new IplImage(img);
			sprintf_s(buf, sizeof(buf), "C:\\Users\\wook\\Desktop\\new_FishPhoto_mirror\\%s\\%d.jpg", tok_dir, i);
			cvFlip(img2, NULL, 1); //1 = 좌우반전 / -1 = 상하반전
			img = cvarrToMat(img2, true);
			imwrite(buf, img); //이미지 저장
			delete img2;
			cout << "파일이름 : " << str[i] << endl;
			in << str[i] << "\n";
			//이미지 이동 부분
			for (int tx = 10; tx < 110; tx += 10) //오른쪽으로 밀기
			{
				sprintf_s(buf, sizeof(buf), "C:\\Users\\wook\\Desktop\\Fish_right\\%s\\%d.jpg", tok_dir,count[0]);
				out_img_1 = Image_moving(img, tx, 0);
				imwrite(buf, out_img_1);
				count[0]++;
				cout << str[i] << "이미지를" << tx << "만큼 오른쪽으로 이동시켰습니다.\n";
			}
			for (int ty = 10; ty < 110; ty += 10) //아래쪽으로 밀기
			{
				sprintf_s(buf, sizeof(buf), "C:\\Users\\wook\\Desktop\\Fish_down\\%s\\%d.jpg", tok_dir, count[1]);
				out_img_1 = Image_moving(img, 0, ty);
				imwrite(buf, out_img_1);
				count[1]++;
				cout << str[i] << "이미지를" << ty << "만큼 아래쪽으로 이동시켰습니다.\n";
			}
			for (int ty = -10; ty > -110; ty -= 10) //위쪽으로 밀기
			{
				sprintf_s(buf, sizeof(buf), "C:\\Users\\wook\\Desktop\\Fish_up\\%s\\%d.jpg", tok_dir, count[2]);
				out_img_1 = Image_moving(img, 0, ty);
				imwrite(buf, out_img_1);
				count[2]++;
				cout << str[i] << "이미지를" << ty << "만큼 위쪽으로 이동시켰습니다.\n";
			}
			//이미지 회전 부분
			for (angle = 0.0; angle < 360; angle += 5) //이미지 5도씩 시계방향 회전
			{
				sprintf_s(buf, sizeof(buf), "C:\\Users\\wook\\Desktop\\new_FishPhoto\\%s\\%d.jpg",tok_dir, j); //파일저장위한 포멧 변경
				out_img_2 = rotate(resize_img, angle); //이미지 회전
				imwrite(buf, out_img_2); //이미지 저장
				j++;
			}
		}
		else
		{
			delete_ << str[i] << "\n";
		}
	}

}
