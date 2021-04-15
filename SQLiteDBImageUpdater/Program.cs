using System;
using System.Data;
using System.Data.SQLite;
using System.IO;

namespace SQLiteDBImageUpdater
{
    class Program
    {
        public static string _targetDBPath = @"D:\FishDic\SQLiteDBImageUpdater\FishDicDB.db;"; //DB 경로
        public static string _totalImagePath = @"D:\FishDic\SQLiteDBImageUpdater\images\"; //전체 이미지 저장 경로
        public static string _insertImageQuery = "UPDATE 어류_테이블 SET 이미지 = (@IMAGE) WHERE 이름 = (@IMAGE_NAME);";

        //수정예정 : DB 용량 줄이기 위해 이미지 압축 수행
        static void Main(string[] args)
        {
            Console.WriteLine("타겟 DB 경로 : " + _targetDBPath);
            Console.WriteLine("전체 이미지 저장 경로 : " + _totalImagePath);

            string[] imagePathList = Directory.GetFileSystemEntries(_totalImagePath); //이미지 파일 리스트
            int totalImageCount = imagePathList.Length - 1;
            int currentImageCount = 0;

            SQLiteConnection con = new SQLiteConnection(@"Data Source = " + _targetDBPath); //DB 연결
            SQLiteCommand cmd = new SQLiteCommand(_insertImageQuery, con); //쿼리 명령어
            SQLiteParameter imageParam = new SQLiteParameter("@IMAGE", DbType.Binary); //이미지 파라미터
            SQLiteParameter imageNameParam = new SQLiteParameter("@IMAGE_NAME", DbType.String); //이미지 이름 파라미터

            foreach (string imagePath in imagePathList) //각 이미지 파일 이름 분리 후 해당되는 어류에 이미지 삽입
            {
                Console.Write("전체 : " + totalImageCount + " / 현재 : " + currentImageCount++);

                string imageFileName = Path.GetFileNameWithoutExtension(imagePath);

                FileStream fs = new FileStream(imagePath, FileMode.Open, FileAccess.Read);
                byte[] buffer = new byte[1024 * 1024]; //파일 스트림 처리를 위한 버퍼

                while (fs.Position < fs.Length)
                    fs.Read(buffer, 0, buffer.Length);

                imageParam.Value = buffer;
                imageNameParam.Value = imageFileName;
                
                Console.WriteLine(" (Target : " + imageFileName + ")");

                //파라미터 추가 후
                cmd.Parameters.Add(imageParam);
                cmd.Parameters.Add(imageNameParam);

                try
                {
                    con.Open();
                    cmd.ExecuteNonQuery();
                }
                catch (Exception e)
                {
                    Console.WriteLine(e.Message);
                }
                finally
                {
                    cmd.Parameters.Clear();
                    con.Close();
                }
            }
        }
    }
}
