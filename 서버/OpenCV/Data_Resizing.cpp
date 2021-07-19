#define _CRT_SECURE_NO_WARNINGS
#include <opencv2/opencv.hpp>
#include <iostream>
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

int main()
{
	Mat img;
	Mat resize_img;
	Mat out_img_1;
	Mat out_img_2;
	string in_dir = "C:\\Users\\wook\\Desktop\\2차\\해삼\\"; //테스트 입력 디렉터리
	string out_dir = "C:\\Users\\wook\\Desktop\\2차_출력\\해삼\\"; //테스트 출력 디렉터리
	string name;
	double angle = 0.0;
	int j = 0;
	int x = 590; //행
	int y = 445; //열
	//cout << "파일을 읽어올 디렉터리 경로를 입력하세요. ex)C:\\Users\\....\\>>";
	//cin >> in_dir;
	//cout << "파일을 출력할 디렉터리 경로를 입력하세요. ex)C:\\users\\....\\>>";
	//cin >> out_dir;
	vector<String> str; //파일 이름 저장할 벡터
	char buf[256];

	glob(in_dir, str, false); //glob(찾을 파일 경로, 찾은 파일 경로, recuisive(T or F)
		//T : 폴더 내 하위 폴더 속 까지 파일을 찾음
		//F : 폴더 내 파일을 찾음
	cout << "로드 개수 : " << str.size() << endl;

	if (str.size() == 0) //벡터에 아무런 데이터가 안들어갔을 때
	{
		cout << "이미지가 존재하지 않습니다.\n";
	}

		for (int i = 0; i < str.size(); i++)
		{
			img = imread(str[i], IMREAD_COLOR); //이미지 읽어오기
			resize(img, resize_img, Size(x, y), 0, 0, CV_INTER_LINEAR); //파일 resize (선형 보간법 사용)

			//out_img_1 = Labeling(resize_img, name);
			for (angle = 0.0; angle < 360; angle += 5) //이미지 회전
			{
				sprintf_s(buf, sizeof(buf), "C:\\Users\\wook\\Desktop\\2차_출력\\해삼\\%d.jpg", j); //파일저장위한 포멧 변경
				//out_img_2 = rotate(out_img_1, angle); //이미지 회전
				out_img_2 = rotate(resize_img, angle);
				imwrite(buf, out_img_2); //이미지 저장
				j++;
			}
			cout << i << "번째 이미지 사이즈변경 및 회전 완료\n";
		}
		return 0;
	}



//Mat Labeling(Mat src, string name)
//{
//
//	int j = 1;
//	Mat img_gray;
//	cvtColor(src, img_gray, COLOR_BGR2GRAY);
//
//	Mat img_threshold;
//	threshold(img_gray, img_threshold, 100, 255, THRESH_BINARY_INV); //이미지 이진화
//
//	Mat img_labels, stats, centroids;
//	int numOfLables = connectedComponentsWithStats(img_threshold, img_labels, stats, centroids, 8, CV_32S);
//
//	// 레이블링 결과에 사각형 그리고, 넘버 표시하기
//	for (int j = 1; j < numOfLables; j++) {
//		int area = stats.at<int>(j, CC_STAT_AREA);
//		int left = stats.at<int>(j, CC_STAT_LEFT);
//		int top = stats.at<int>(j, CC_STAT_TOP);
//		int width = stats.at<int>(j, CC_STAT_WIDTH);
//		int height = stats.at<int>(j, CC_STAT_HEIGHT);
//
//		if(width > 200 || height > 200 && left > 30 && top > 90 )
//		{
//			width = width + 30;
//			rectangle(src, Point(left, top), Point(left + width, top + height), Scalar(0, 0, 255), 1); //Point(left, top) 시작점 좌표 ,  Point(left + width, top + height) 종료점 좌표
//
//			putText(src, "Scomber japonicus", Point(left + 20, top + 20), FONT_HERSHEY_SIMPLEX, 1, Scalar(255, 0, 0), 1); //텍스트 쓰는곳
//		}
//		
//	}
//	//cout << "numOfLables : " << numOfLables - 1 << endl;	// 최종 넘버링에서 1을 빼줘야 함
//	return src;
//}
