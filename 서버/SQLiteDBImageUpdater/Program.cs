using System;
using System.Data;
using System.Data.SQLite;
using System.IO;
using System.Drawing.Imaging;
using System.Linq;
using System.Drawing;

namespace SQLiteDBImageUpdater
{
    class Program
    {
        public static string _targetDBPath = @"D:\FishDic\서버\DB\FishDicDB.db;"; //DB 경로
        public static string _totalImagePath = @"D:\FishDic\서버\DB\images\"; //전체 이미지 저장 경로
        public static string _updateImageQuery = "UPDATE 어류_테이블 SET 이미지 = (@IMAGE) WHERE 이름 = (@IMAGE_NAME);";
        public static long _imageCompressQuality = 50L; //이미지 압축 수준 (0 ~ 100)

        static void Main(string[] args)
        {
            Console.WriteLine("타겟 DB 경로 : " + _targetDBPath);
            Console.WriteLine("전체 이미지 저장 경로 : " + _totalImagePath);

            string[] imagePathList = Directory.GetFileSystemEntries(_totalImagePath); //이미지 파일 리스트
            int totalImageCount = imagePathList.Length - 1; //전체 이미지 수
            int currentImageCount = 0;

            SQLiteConnection con = new SQLiteConnection(@"Data Source = " + _targetDBPath); //DB 연결
            SQLiteCommand cmd = new SQLiteCommand(_updateImageQuery, con); //쿼리 명령어
            SQLiteParameter imageParam = new SQLiteParameter("@IMAGE", DbType.Binary); //이미지 파라미터
            SQLiteParameter imageNameParam = new SQLiteParameter("@IMAGE_NAME", DbType.String); //이미지 이름 파라미터

            try
            {
                con.Open();
            }
            catch (Exception e)
            {
                Console.WriteLine(e.Message);
            }

            foreach (string imagePath in imagePathList) //각 이미지 파일 이름 분리 후 해당되는 어류에 이미지 삽입
            {
                Console.Write("전체 : " + totalImageCount + " / 현재 : " + currentImageCount++);

                string imageFileName = Path.GetFileNameWithoutExtension(imagePath);
                FileStream fs = new FileStream(imagePath, FileMode.Open, FileAccess.Read);
                FileInfo imageFileInfo = new FileInfo(imagePath);

                byte[] buffer = new byte[imageFileInfo.Length]; //읽어들인 이미지 버퍼
                byte[] compressedImage; //압축 된 이미지 버퍼

                fs.Read(buffer, 0, buffer.Length); //이미지 크기만큼 읽는다.

                //이미지 압축 수행
                Image image;
                using (var inputStream = new MemoryStream(buffer))
                {
                    image = Image.FromStream(inputStream);
                    var jpegEncoder = ImageCodecInfo.GetImageDecoders()
                      .First(c => c.FormatID == ImageFormat.Jpeg.Guid);
                    var encoderParameters = new EncoderParameters(1);
                    encoderParameters.Param[0] = new EncoderParameter(Encoder.Quality, _imageCompressQuality);

                    using (var outputStream = new MemoryStream())
                    {
                        image.Save(outputStream, jpegEncoder, encoderParameters);
                        compressedImage = outputStream.ToArray();
                    }
                }

                imageParam.Value = compressedImage;
                imageNameParam.Value = imageFileName;

                Console.WriteLine(" (Target : " + imageFileName + ", Original Target Size : " + (buffer.Length - 1) + "bytes, Compressed Target Size : " + (compressedImage.Length - 1) + "bytes)");

                //파라미터 추가
                cmd.Parameters.Add(imageParam);
                cmd.Parameters.Add(imageNameParam);

                try
                {
                    cmd.ExecuteNonQuery();
                }
                catch (Exception e)
                {
                    Console.WriteLine(e.Message);
                }
                finally
                {
                    cmd.Parameters.Clear();
                }
            }

            using (SQLiteCommand cmd2 = con.CreateCommand()) //기존 트랜잭션은 모두 완료 된 상태에서 단편화 된 데이터베이스에 대하여 재구성 수행
            {
                cmd2.CommandText = "vacuum";
                Console.WriteLine("VACCUM Perfomred : " + cmd2.ExecuteNonQuery());
            }

            con.Close();
        }
    }
}