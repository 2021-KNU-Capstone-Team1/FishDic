#include <opencv2/opencv.hpp>
#include <iostream>
#include <string>
#include <vector>
#include <Windows.h>
#define _CRT_SECURE_NO_WARNINGS


using namespace std;
using namespace cv;

Mat rotate(Mat src, double angle) //ȸ�� �Լ�
{
	Mat dst;
	Point2f pt(src.cols / 2.0, src.rows / 2.0); //ȸ�� �߽��� ������ �߾�������
	Mat r = getRotationMatrix2D(pt, angle, 1.0); //ȸ�� ��� ���
	warpAffine(src, dst, r, Size(src.cols, src.rows)); //������ ��ȯ ����
	return dst;

}

int main()
{
	Mat img;
	Mat resize_img;
	Mat out_img_1;
	Mat out_img_2;
	string in_dir = "C:\\Users\\wook\\Desktop\\2��\\�ػ�\\"; //�׽�Ʈ �Է� ���͸�
	string out_dir = "C:\\Users\\wook\\Desktop\\2��_���\\�ػ�\\"; //�׽�Ʈ ��� ���͸�
	string name;
	double angle = 0.0;
	int j = 0;
	int x = 590; //��
	int y = 445; //��
	//cout << "������ �о�� ���͸� ��θ� �Է��ϼ���. ex)C:\\Users\\....\\>>";
	//cin >> in_dir;
	//cout << "������ ����� ���͸� ��θ� �Է��ϼ���. ex)C:\\users\\....\\>>";
	//cin >> out_dir;
	vector<String> str; //���� �̸� ������ ����
	char buf[256];

	glob(in_dir, str, false); //glob(ã�� ���� ���, ã�� ���� ���, recuisive(T or F)
		//T : ���� �� ���� ���� �� ���� ������ ã��
		//F : ���� �� ������ ã��
	cout << "�ε� ���� : " << str.size() << endl;

	if (str.size() == 0) //���Ϳ� �ƹ��� �����Ͱ� �ȵ��� ��
	{
		cout << "�̹����� �������� �ʽ��ϴ�.\n";
	}

		for (int i = 0; i < str.size(); i++)
		{
			img = imread(str[i], IMREAD_COLOR); //�̹��� �о����
			resize(img, resize_img, Size(x, y), 0, 0, CV_INTER_LINEAR); //���� resize (���� ������ ���)

			//out_img_1 = Labeling(resize_img, name);
			for (angle = 0.0; angle < 360; angle += 5) //�̹��� ȸ��
			{
				sprintf_s(buf, sizeof(buf), "C:\\Users\\wook\\Desktop\\2��_���\\�ػ�\\%d.jpg", j); //������������ ���� ����
				//out_img_2 = rotate(out_img_1, angle); //�̹��� ȸ��
				out_img_2 = rotate(resize_img, angle);
				imwrite(buf, out_img_2); //�̹��� ����
				j++;
			}
			cout << i << "��° �̹��� ������� �� ȸ�� �Ϸ�\n";
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
//	threshold(img_gray, img_threshold, 100, 255, THRESH_BINARY_INV); //�̹��� ����ȭ
//
//	Mat img_labels, stats, centroids;
//	int numOfLables = connectedComponentsWithStats(img_threshold, img_labels, stats, centroids, 8, CV_32S);
//
//	// ���̺� ����� �簢�� �׸���, �ѹ� ǥ���ϱ�
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
//			rectangle(src, Point(left, top), Point(left + width, top + height), Scalar(0, 0, 255), 1); //Point(left, top) ������ ��ǥ ,  Point(left + width, top + height) ������ ��ǥ
//
//			putText(src, "Scomber japonicus", Point(left + 20, top + 20), FONT_HERSHEY_SIMPLEX, 1, Scalar(255, 0, 0), 1); //�ؽ�Ʈ ���°�
//		}
//		
//	}
//	//cout << "numOfLables : " << numOfLables - 1 << endl;	// ���� �ѹ������� 1�� ����� ��
//	return src;
//}